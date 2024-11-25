package team.sailboat.base;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.Watcher.WatcherType;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.event.IXListener;
import team.sailboat.commons.fan.event.XEvent;
import team.sailboat.commons.fan.event.XListenerAssist;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.exec.CommonExecutor;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.struct.Tuples;
import team.sailboat.commons.fan.struct.Wrapper;
import team.sailboat.commons.fan.text.ChineseComparator;
import team.sailboat.commons.fan.text.XString;

public class ZKProxy implements IZKProxy
{
	static final Logger sLogger = LoggerFactory.getLogger(ZKProxy.class) ;
	static Map<String, IZKProxy> sProxyMap = XC.hashMap() ;
	
	public static IZKProxy get(String aZkQuorum) throws IOException, KeeperException, InterruptedException
	{
		IZKProxy proxy = sProxyMap.get(aZkQuorum) ;
		if(proxy == null)
		{
			proxy = new ZKProxy(aZkQuorum) ;
			sProxyMap.put(aZkQuorum , proxy) ;
		}
		return proxy ;
	}
	
	String mZKQuorum = null ;
	protected ZooKeeper mZK ;
	protected final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	final Map<String, WatcherRecord> mWatcherRcdMap = XC.concurrentHashMap() ;
	
	final AtomicBoolean mReconnecting = new AtomicBoolean(false) ;
	
	final XListenerAssist mLsnAssist_reconn = new XListenerAssist() ;
	
	protected ZKProxy(String aZkQuorum) throws IOException, KeeperException, InterruptedException
	{
		mZKQuorum = aZkQuorum ;
		_connect();
	}
	
	/**
	 * 
	 * @return		如果是根目录，则返回“”
	 */
	public String getHomeDir()
	{
		int i =mZKQuorum.indexOf('/') ;
		return i != -1?mZKQuorum.substring(i):"" ;
	}
	
	void _connect() throws IOException, KeeperException, InterruptedException
	{
		mZK = new ZooKeeper(mZKQuorum , 2000 , (event)->{
			if(event.getType() == EventType.None
					&& event.getState() == KeeperState.Disconnected)
			{
				switch (event.getState())
				{
				case Disconnected:
				{
					mLogger.error("Zookeeper客户端连接断开！");
					if(mReconnecting.compareAndSet(false, true))
					{
						CommonExecutor.execInSelfThread(()->{
							try
							{
								_reconnect(true);
							}
							catch(Exception e)
							{}
							finally
							{
								mReconnecting.set(false) ;
							}
						}, "重连Zookeeper");
					}
				}
					break;
				case ConnectedReadOnly:
				case SyncConnected:
					mLogger.info("Zookeeper重连成功！");
					mLsnAssist_reconn.asyncNotifyLsns(new XEvent(this, 0)) ;
					break ;
				default:
					break;
				}
			}
			
		}) ;
		while(mZK.getState() == States.CONNECTING)
		{
			JCommon.sleep(100) ;
		}
		if(mZK.exists("/", false) == null)
		{
			int i = mZKQuorum.indexOf('/') ;
			if(i != -1)
			{
				ZooKeeper zk = null;
				try
				{
					String rootQuorum = mZKQuorum.substring(0, i) ;
					zk = new ZooKeeper(rootQuorum , 2000, (event_1)->{});
					String path = mZKQuorum.substring(i);
					mLogger.info("连接ZK："+rootQuorum + " , 试图确保路径存在："+path) ;
					IZKProxy.ensureExists(zk , path) ;
				}
				finally
				{
					closeQuietly(zk) ;
				}
			}
		}
	}
	
	
	@Override
	public void addReconnectedListener(IXListener aLsn)
	{
		mLsnAssist_reconn.addLastListener(aLsn) ;
	}
	
	@Override
	public void removeReconnectedListener(IXListener aLsn)
	{
		mLsnAssist_reconn.removeLsn(aLsn) ;
	}
	
	
	static void closeQuietly(ZooKeeper aZK)
	{
		if(aZK != null)
		{
			try
			{
				aZK.close();
			}
			catch (InterruptedException e)
			{
				if(sLogger.isDebugEnabled())
					sLogger.debug(ExceptionAssist.getStackTrace(e)) ;
			}
		}
	}
	
	void _reconnect() throws Exception
	{
		_reconnect(false) ;
	}
	
	synchronized void _reconnect( boolean aInOwnThread) throws Exception
	{
		States states = mZK.getState() ;
		switch(states)
		{
		case CONNECTED:
		case CONNECTING:
		case CONNECTEDREADONLY:
			mLogger.info("放弃reconnect，当前处于"+states.name()+"状态");
			return ;
		default:
			break ;
		}
		closeQuietly(mZK);
		mZK = null ;
		int retryTimes = 0 ;
		while(true)
		{
			try
			{
				_connect();
				//所有watcher重新注册
				mWatcherRcdMap.forEach((id , watcherRcd)->{
					try
					{
						mZK.exists(watcherRcd.getPath() , watcherRcd.getWatcher());
					}
					catch (KeeperException | InterruptedException e)
					{
						mLogger.error(ExceptionAssist.getStackTrace(e)) ;
					}
				});
				mLogger.info("zookeeper重新连接成功。");
				break ;
			}
			catch (IOException | KeeperException | InterruptedException e)
			{
				if(!aInOwnThread && retryTimes++ >= 3)
					throw e ;
				else
					JCommon.sleepInSeconds(1) ;
			}
		}
	}
	
	public void setNodeData(String aPath , String aData) throws Exception
	{
		byte[] data = XString.isNotEmpty(aData)?aData.getBytes(AppContext.sUTF8):new byte[0] ;
		setNodeData(aPath, data);
	}
	
	@Override
	public String getNodeData_Str(String aPath) throws Exception
	{
		byte[] data = getNodeData(aPath) ;
		return data != null?new String(data, AppContext.sUTF8):null ;
	}
	
	@Override
	public String getNodeData_Str(String aPath, Stat aStat) throws Exception
	{
		byte[] data = getNodeData(aPath , aStat) ;
		return data != null?new String(data, AppContext.sUTF8):null ;
	}
	
	@Override
	public Tuples.T2<String, Stat> getNodeData_StrWithStat(String aPath) throws Exception
	{
		Tuples.T2<byte[], Stat> tuple = getNodeDataWithStat(aPath) ;
		return Tuples.of(new String(tuple.getEle_1() , AppContext.sUTF8) , tuple.getEle_2()) ;
	}
	
	@Override
	public Integer getNodeData_int(String aPath) throws Exception
	{
		String valStr = getNodeData_Str(aPath) ;
		return XString.isEmpty(valStr)?null:Integer.valueOf(valStr) ;
	}
	
	byte[] _getNodeData(String aPath , Stat aStat , Wrapper<Stat> aReturnStat) throws KeeperException, InterruptedException
	{
		Stat curStat = mZK.exists(aPath, false) ;
		if(aReturnStat != null)
			aReturnStat.set(curStat) ;
		if(curStat != null)
			return mZK.getData(aPath, false, aStat) ;
		else
			return JCommon.sEmptyByteArray ;
	}
	
	@Override
	public byte[] getNodeData(String aPath) throws Exception
	{
		return getNodeData(aPath, null) ;
	}
	
	@Override
	public byte[] getNodeData(String aPath, Stat aStat) throws Exception
	{
		try
		{
			return _getNodeData(aPath , aStat , null) ;
		}
		catch (KeeperException e)
		{
			if(e.code() == Code.SESSIONEXPIRED)
			{
				//重联
				_reconnect();
				return _getNodeData(aPath , aStat , null) ;
			}
			else
				throw e ;
		}
	}
	
	@Override
	public Tuples.T2<byte[], Stat> getNodeDataWithStat(String aPath) throws Exception
	{
		Wrapper<Stat> returnStat = new Wrapper<>() ;
		try
		{
			byte[] data = _getNodeData(aPath , null , returnStat) ;
			return Tuples.of(data, returnStat.get()) ;
		}
		catch (KeeperException e)
		{
			if(e.code() == Code.SESSIONEXPIRED)
			{
				//重联
				_reconnect();
				byte[] data = _getNodeData(aPath , null , returnStat) ;
				return Tuples.of(data, returnStat.get()) ;
			}
			else
				throw e ;
		}
	}
	
	boolean _ensureExists(String aPath , boolean aLeafAsTemp) throws Exception
	{
		try
		{
			return IZKProxy.ensureExists(mZK, aPath , aLeafAsTemp) ;
		}
		catch (KeeperException e)
		{
			if(e.code() == Code.SESSIONEXPIRED)
			{
				//重联
				_reconnect();
				return IZKProxy.ensureExists(mZK, aPath , aLeafAsTemp) ;
			}
			else
				throw e ;
		}
		
	}
	
	@Override
	public boolean exists(String aPath) throws Exception
	{
		return mZK.exists(aPath, false) != null ;
	}
	
	@Override
	public boolean ensureExists(String aPath) throws Exception
	{
		return _ensureExists(aPath, false) ;
	}
	
	@Override
	public boolean ensureExists_Temp(String aPath) throws Exception
	{
		return _ensureExists(aPath, true) ;
	}
	
	void _setNodeData(String aPath , byte[] aData , Stat aStat , boolean aLeafAsTemp) throws KeeperException, InterruptedException
	{
		Stat stat = aStat == null?mZK.exists(aPath, false):aStat ;
		if(stat != null)
			mZK.setData(aPath , aData, stat.getVersion()) ;
		else
		{
			//可能是父节点不存在，试着创建父节点
			String path = FileUtils.getParent(aPath) ;
			IZKProxy.ensureExists(mZK, path , false) ;
			mZK.create(aPath , aData , Ids.OPEN_ACL_UNSAFE 
					, aLeafAsTemp?CreateMode.EPHEMERAL:CreateMode.PERSISTENT) ;
		}
	}
	
	@Override
	public void setTempNodeData(String aPath, String aData) throws Exception
	{
		byte[] data = XString.isNotEmpty(aData)?aData.getBytes(AppContext.sUTF8):new byte[0] ;
		setNodeData(aPath, data, null, true);
	}
	
	@Override
	public void setNodeData(String aPath, byte[] aData) throws Exception
	{
		setNodeData(aPath, aData, null) ;
	}
	
	@Override
	public void setNodeData(String aPath , byte[] aData , Stat aStat) throws Exception
	{
		setNodeData(aPath, aData, aStat, false) ;
	}
	
	@Override
	public void setNodeData(String aPath , byte[] aData , Stat aStat , boolean aLeafAsTemp) throws Exception
	{
		try 
		{
			_setNodeData(aPath, aData , aStat , aLeafAsTemp) ;
		}
		catch (KeeperException e)
		{
			if(e.code() == Code.SESSIONEXPIRED)
			{
				//重联
				_reconnect();
				_setNodeData(aPath, aData , aStat , aLeafAsTemp) ;
			}
			else
				throw e ;
		}
	}
	
	boolean _delete(String aPath, boolean aWhenIsLeaf) throws Exception
	{
		Assert.isNotTrue("/".equals(aPath), "不能删除根节点");
		Stat stat_0 = mZK.exists(aPath, false) ;
		if(stat_0 == null)
			return false ;			//指定节点不存在
		
		return _deepthFirstDelete(aPath, aWhenIsLeaf) ;
	}
	
	boolean _deepthFirstDelete(String aPath, boolean aWhenIsLeaf) throws Exception
	{
		List<String> children = mZK.getChildren(aPath, false) ;
		if(aWhenIsLeaf)
		{
			if(XC.isEmpty(children))
			{
				//可以删除
				Stat stat = mZK.exists(aPath, false) ;
				if(stat != null)
				{
					mZK.delete(aPath, stat.getVersion());
					return true ;
				}
			}
			return false ;
		}
		else
		{
			if(XC.isNotEmpty(children))
			{
				//级联删除
				for(String child : children)
				{
					_deepthFirstDelete(aPath+"/"+child, aWhenIsLeaf) ;
				}
			}
			//可以删除
			Stat stat = mZK.exists(aPath, false) ;
			if(stat != null)
				mZK.delete(aPath, stat.getVersion());
			return true ;
		}
	}
	
	@Override
	public boolean delete(String aPath, boolean aWhenIsLeaf) throws Exception
	{
		try
		{
			return _delete(aPath, aWhenIsLeaf) ;
		}
		catch (KeeperException e)
		{
			if(e.code() == Code.SESSIONEXPIRED)
			{
				//重联
				_reconnect();
				return _delete(aPath, aWhenIsLeaf) ;
			}
			else
				throw e ;
		}
	}
	
	void _duplicate(String aPath, String aNewPath, boolean aDeepth) throws Exception
	{
		Assert.isNotTrue("/".equals(aPath), "不能对根进行复刻");
		Stat stat_0 = mZK.exists(aPath, false) ;
		Assert.notNull(stat_0, "不能复刻，源路径不存在：%s", aPath) ;
		Stat stat_1 = mZK.exists(aNewPath, false) ;
		Assert.isNull(stat_1, "不能复刻，已经存在路径：%s", aNewPath) ;
		
		IZKProxy.ensureExists(mZK, FileUtils.getParent(aNewPath) , false) ;
		_deepthFirstDouplicate(aPath, aNewPath, aDeepth);
	}
	
	void _deepthFirstDouplicate(String aPath, String aNewPath, boolean aDeepth) throws Exception
	{
		byte[] data = mZK.getData(aPath, false, null) ;
		mZK.create(aNewPath, data, Ids.OPEN_ACL_UNSAFE , CreateMode.PERSISTENT) ;
		if(aDeepth)
		{
			List<String> children = mZK.getChildren(aPath, aDeepth) ;
			if(XC.isNotEmpty(children))
			{
				for(String child : children)
				{
					_deepthFirstDouplicate(aPath+"/"+child, aNewPath+"/"+child, aDeepth) ;
				}
			}
		}
	}
	
	@Override
	public void duplicate(String aPath, String aNewPath, boolean aDeepth) throws Exception
	{
		try
		{
			_duplicate(aPath, aNewPath, aDeepth);
		}
		catch (KeeperException e)
		{
			if(e.code() == Code.SESSIONEXPIRED)
			{
				//重联
				_reconnect();
				_duplicate(aPath, aNewPath, aDeepth);
			}
			else
				throw e ;
		}
	}
	
	String _rename(String aPath, String aNewName) throws Exception
	{
		//先复制，然后再删除
		Assert.isNotTrue("/".equals(aPath), "不能对根进行重命名");
		Assert.notEmpty(aNewName , "名称不能为空") ;
		Assert.isNotTrue(XString.containsAny(aNewName, '/', '\\') , "节点名称不能包含“\\”或“/”");
		String newPath = FileUtils.getPath(FileUtils.getParent(aPath) , aNewName) ;
		_duplicate(aPath, newPath, true) ;
		_delete(aPath, false) ;
		return newPath ;
	}
	
	@Override
	public String rename(String aPath, String aNewName) throws Exception
	{
		try 
		{
			return _rename(aPath, aNewName) ;
		}
		catch (KeeperException e)
		{
			if(e.code() == Code.SESSIONEXPIRED)
			{
				//重联
				_reconnect();
				return _rename(aPath, aNewName) ;
			}
			else
				throw e ;
		}
	}
	
	@Override
	public String watchNodeOnce(String aPath , Watcher aWatch) throws Exception
	{
		mZK.exists(aPath, aWatch) ;
		WatcherRecord rcd = new WatcherRecord(aPath , aWatch , WatcherType.Data , false) ;
		mWatcherRcdMap.put(rcd.getId() , rcd) ;
		return rcd.getId() ;
	}
	
	@Override
	public String watchChildrenOnce(String aPath, Watcher aWatch) throws Exception
	{
		mZK.getChildren(aPath, aWatch) ;
		WatcherRecord rcd = new WatcherRecord(aPath , aWatch , WatcherType.Children , false) ;
		mWatcherRcdMap.put(aPath, rcd) ;
		return rcd.getId() ;
	}
	
	@Override
	public String watchChildren(String aPath, Watcher aWatcher) throws Exception
	{
		ContinueWatcher watcher = new ContinueWatcher(aPath, aWatcher, WatcherType.Children , false) ;
		mZK.getChildren(aPath, watcher) ;
		WatcherRecord rcd = new WatcherRecord(aPath , watcher , WatcherType.Children , false) ;
		mWatcherRcdMap.put(rcd.getId() , rcd) ;
		return rcd.getId() ;
	}
	
	@Override
	public String watchNode(String aPath, Watcher aWatch , boolean aFocusCreateEvent) throws Exception
	{
		ContinueWatcher watcher = new ContinueWatcher(aPath, aWatch, WatcherType.Data , aFocusCreateEvent) ;
		if(mZK.exists(aPath, watcher) != null && aFocusCreateEvent)
		{
			mZK.getChildren(watcher.mParentPath , watcher) ;
		}
		WatcherRecord rcd = new WatcherRecord(aPath , watcher , WatcherType.Data , aFocusCreateEvent) ;
		mWatcherRcdMap.put(rcd.getId() , rcd) ;
		return rcd.getId() ;
	}
	
	@Override
	public void cancelWatch(String aHandle) throws Exception
	{
		WatcherRecord rcd = mWatcherRcdMap.remove(aHandle) ;
		if(rcd != null)
		{
			mZK.removeWatches(rcd.getPath(), rcd.getWatcher() , rcd.getWatcherType() , true);
			if(rcd.isFocusCreateEvent())
			{
				if(!FileUtils.isRoot(rcd.getPath()))
				{
					String parentPath = FileUtils.getParent(rcd.getPath()) ;
					mZK.removeWatches(parentPath , rcd.getWatcher() , WatcherType.Children , true);
				}
			}
		}
	}
	
	@Override
	public String getAnyOneChildPath(String aPath) throws Exception
	{
		List<String> childNameList = mZK.getChildren(aPath, false) ;
		return XC.isEmpty(childNameList)?null:aPath+"/"+childNameList.get(0) ;
	}
	
	/**
	 * 如果不存在指定的路径，将返回null	<br />
	 * 如果存在指定路径，但下面没有节点，将返回emptyList()
	 * @param aPath
	 * @param aWatch
	 * @return
	 * @throws Exception
	 */
	List<String> _getChildren(String aPath, boolean aWatch) throws Exception
	{
		if(mZK.exists(aPath, aWatch) != null)
		{
			List<String> children = mZK.getChildren(aPath, aWatch) ;
			if(XC.isNotEmpty(children))
				children.sort(ChineseComparator.getInstance());
			return children ;
		}
		return null ;
	}
	
	@Override
	public List<String> getChildren(String aPath, boolean aWatch) throws Exception
	{
		try
		{
			return _getChildren(aPath, aWatch) ;
		}
		catch (KeeperException e)
		{
			if(e.code() == Code.SESSIONEXPIRED)
			{
				//重联
				_reconnect();
				return _getChildren(aPath, aWatch) ;
			}
			else
				throw e ;
		}
	}
	
	boolean _deleteNode(String aPath) throws Exception
	{
		Stat stat = mZK.exists(aPath, false) ;
		if(stat != null)
		{
			mZK.delete(aPath, stat.getVersion());
			return true ;
		}
		else
			return false ;
	}
	
	@Override
	public boolean deleteNode(String aPath) throws Exception
	{
		try
		{
			return _deleteNode(aPath) ;
		}
		catch (KeeperException e)
		{
			if(e.code() == Code.SESSIONEXPIRED)
			{
				//重联
				_reconnect();
				return _deleteNode(aPath) ;
			}
			else
				throw e ;
		}
	}
	
	@Override
	public String getKafkaBootstrapServers() throws Exception
	{
		List<String> ids = getChildren(SysConst.sZK_Path_KafkaBrokerIds, false);
		StringBuilder brokersBld = new StringBuilder();
		for (String id : ids)
		{
			String confStr = getNodeData_Str(SysConst.sZK_Path_KafkaBrokerIds + "/" + id);
			JSONObject confJo = new JSONObject(confStr);
			if (brokersBld.length() > 0)
				brokersBld.append(",");
			brokersBld.append(confJo.optString("host") + ":" + confJo.optString("port"));
		}
		return brokersBld.toString();
	}
	
	static void listChildren(ZooKeeper aZK , String aPath) throws KeeperException, InterruptedException
	{
		List<String> childrenPath = aZK.getChildren(aPath, false) ;
		if(XC.isNotEmpty(childrenPath))
		{
			for(String path : childrenPath)
			{
				path = aPath+"/"+path ;
				System.out.println(path);
				listChildren(aZK, path) ;
			}
		}
	}
	
	class ContinueWatcher implements Watcher
	{
		String mPath ;
		Watcher mWatcher ;
		WatcherType mWatcherType ;
		boolean mFocusCreateEvent ;
		
		String mParentPath ;
		
		
		public ContinueWatcher(String aPath , Watcher aWatcher , WatcherType aWatcherType , boolean aFocusCreateEvent)
		{
			mPath = aPath ;
			mWatcher = aWatcher ;
			mWatcherType = aWatcherType ;
			mFocusCreateEvent = aFocusCreateEvent ;
			if(mFocusCreateEvent && !FileUtils.isRoot(mPath))
			{
				mParentPath = FileUtils.getParent(mPath) ;
			}
		}

		@Override
		public void process(WatchedEvent aEvent)
		{
			String eventPath = aEvent.getPath() ;
			try
			{
				mWatcher.process(aEvent);
			}
			finally
			{
				if(eventPath != null && eventPath.equals(mPath))
				{
					try
					{
						switch(mWatcherType)
						{
						case Children:
							mZK.getChildren(mPath, this) ;
							break ;
						default:
							mZK.exists(mPath , this) ;
							break ;
						}
					}
					catch (KeeperException | InterruptedException e)
					{
						mLogger.error(ExceptionAssist.getClearMessage(getClass(), e)) ;
					}
				}
				else if(eventPath !=null && eventPath.equals(mParentPath))
				{
					try
					{
						mZK.getChildren(mParentPath , this) ;
					}
					catch (KeeperException | InterruptedException e)
					{
						mLogger.error(ExceptionAssist.getClearMessage(getClass(), e)) ;
					}
				}
				else
				{
					mLogger.error("ZK收到事件的路径为 {} ，不等于path[{}]和parentPath，事件类型是{}，无法继续注册监听器！" 
							, eventPath
							, mPath
							, aEvent.getType()==null?null:aEvent.getType().name()) ;
				}
			}
		}
		
	}
	
	static class WatcherRecord
	{
		final String mId ;
		final Watcher mWatcher ;
		final String mPath ;
		final WatcherType mWatcherType ;
		final boolean mFocusCreateEvent ;
		
		public WatcherRecord(String aPath , Watcher aWatcher, WatcherType aWatcherType
				, boolean aFocusCreateEvent)
		{
			mId = UUID.randomUUID().toString() ;
			mWatcher = aWatcher;
			mPath = aPath;
			mWatcherType = aWatcherType ;
			mFocusCreateEvent = aFocusCreateEvent ;
		}

		public String getId()
		{
			return mId;
		}
		
		public String getPath()
		{
			return mPath;
		}
		
		public Watcher getWatcher()
		{
			return mWatcher;
		}
		
		public WatcherType getWatcherType()
		{
			return mWatcherType;
		}
		
		public boolean isFocusCreateEvent()
		{
			return mFocusCreateEvent;
		}
	}
}


	