package team.sailboat.base;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

import team.sailboat.commons.fan.app.App;
import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.app.App.Status;
import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONException;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.struct.Tuples;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.fan.time.XTime;

/**
 * 平台的Zookeeper操作代理
 *
 * @author yyl
 * @since 2024年12月19日
 */
public class ZKSysProxy extends ZKProxy implements IZKSysProxy
{
	
	static IZKSysProxy sDefault ;
	static String sDefaultQuorum ;
	
	static final String sCommonPathPrefix = SysConst.sZK_Path_SysCommon+"/" ;
	
	public static IZKSysProxy setSysDefault(String aZkQuorum) throws IOException, KeeperException, InterruptedException
	{
		if(JCommon.unequals(sDefaultQuorum, aZkQuorum))
		{
			String sysEnv = System.getProperty(SysConst.sPK_SysEnv , SysConst.sPKV_SysEnv_prod) ;
			sLogger.info("依据System.property的{}设定，认为当前属于环境为：{}" , SysConst.sPK_SysEnv , sysEnv);
			sDefault = new ZKSysProxy(aZkQuorum+XString.msgFmt(SysConst.sZK_PathPtn_SysRoot , sysEnv)) ;
			sDefaultQuorum = aZkQuorum ;
		}
		return sDefault ;
	}
	
	public static String getDefaultQuorum()
	{
		Assert.notNull(sDefault , "没有设置缺省的ZkQuorum") ;
		return sDefaultQuorum ;
	}
	
	public static IZKSysProxy getSysDefault()
	{
		Assert.notNull(sDefault , "没有设置缺省的ZkQuorum") ;
		return sDefault ;
	}
	
	protected JSONObject mHostsJo ;
	protected final JSONArray mModulesJa = new JSONArray() ;
	protected final JSONObject mModulesJo = new JSONObject() ;
	protected long mLastRefreshTime = 0 ;
	final Map<String, ServiceRegistery> mServiceRegisteryMap = XC.hashMap() ;
	
	protected final Watcher mWebModuleDataWatcher = new Watcher()
			{
				@Override
				public void process(WatchedEvent aEvent)
				{
					if(aEvent.getType() == EventType.NodeDataChanged)
					{
						try
						{
							// 模块的节点内容发生改变
							String moduleName = FileUtils.getFileName(aEvent.getPath()) ;
							JSONObject jo = JSONObject.of(getNodeData_Str(aEvent.getPath()))
									.put("moduleName" , moduleName) ;
							
							JSONObject jo_hold = mModulesJo.optJSONObject(moduleName) ;
							if(jo_hold != null)
							{
								double oldOrder = jo_hold.optDouble("order", 0d) ;
								jo_hold.duplicate(jo) ;
								if(jo_hold.optDouble("order", 0d) != oldOrder)
									_sortModules();
							}
						}
						catch (Exception e)
						{
							mLogger.error(ExceptionAssist.getClearMessage(getClass(), e)) ;
						}
						finally
						{
							// 重新注册
							try
							{
								mZK.exists(aEvent.getPath(), this) ;
							}
							catch (KeeperException | InterruptedException e)
							{
								mLogger.error(ExceptionAssist.getClearMessage(getClass(), e
										, XString.msgFmt("在路径[{}]上注册Watcher出现异常！" , aEvent.getPath()))) ;
							}
						}
					}
					
				}
		
			} ;
	
	protected ZKSysProxy(String aZkQuorum) throws IOException, KeeperException, InterruptedException
	{
		super(aZkQuorum) ;
	}
	
	/**
	 * 
	 * @param aModuleName
	 * @param aDisplayName
	 * @param aServiceAddr		http://ip:port格式
	 * @param aPagePath			"/"开头，不用包含主机和地址
	 * @param aIconPath			"/"开头，不用包含主机和地址
	 * @throws Exception 
	 */
	@Override
	public void registerSailboatWebModule(String aModuleName , String aDisplayName , String aServiceAddr , String aPagePath , String aIconPath , double aOrder) throws Exception
	{
		setNodeData(XString.msgFmt(SysConst.sZK_SysPathPtn_SailboatWebModuleOne , aModuleName) 
				, new JSONObject().put("moduleName" , aModuleName)
					.put("displayName" , aDisplayName)
					.put("serviceAddr", aServiceAddr)
					.put("pagePath", aPagePath)
					.put("iconPath", aIconPath)
					.put("order" , aOrder)
					.toString()) ;
	}
	
	@Override
	public JSONArray getRegisteredSailboatWebModules() throws JSONException, Exception
	{
		if(mModulesJa == null)
		{
			mZK.exists(SysConst.sZK_SysPath_SailboatWebModules, (event)->{
				switch(event.getType())
				{
				case NodeChildrenChanged:
					//刷新模块数据
					try
					{
						_refreshXWebModules() ;
						mLogger.info("刷新了一次XWeb的模块信息。");
					}
					catch (Exception e1)
					{
						mLogger.error("从ZK中读取XWeb的模块出现异常。异常消息："
								+ExceptionAssist.getClearMessage(ZKSysProxy.class, e1));
					}
		            break ;
		         default:
		        	 break ;
				}
			}) ;
			_refreshXWebModules() ;
			mLogger.info("刷新了一次XWeb的模块信息。");
		}
		return mModulesJa ;
	}
	
	@Override
	public JSONObject getRegisteredSailboatWebModule(String aName) throws JSONException, Exception
	{
		if(XTime.pass(mLastRefreshTime , 3600_000))
		{
			_refreshXWebModules();
			mLastRefreshTime = System.currentTimeMillis() ;
		}
		return mModulesJo.optJSONObject(aName) ;
	}
	
	synchronized void _refreshXWebModules() throws JSONException, Exception
	{
		List<String> children = mZK.getChildren(SysConst.sZK_SysPath_SailboatWebModules , false) ;
		boolean changed = false ;
		for(String child : children)
		{
			JSONObject moduleJo = mModulesJo.optJSONObject(child) ;
			if(moduleJo == null)
			{
				String path = SysConst.sZK_SysPath_SailboatWebModules+"/"+child ;
				mZK.exists(path, mWebModuleDataWatcher) ;
				moduleJo = JSONObject.of(getNodeData_Str(path))
						.put("moduleName", child) ;
				mModulesJo.put(child, moduleJo) ;
				mModulesJa.put(moduleJo) ;
				changed = true ;
			}
		}
		if(changed || children.size() != mModulesJo.size())
		{
			for(String moduleName : mModulesJo.keyArray())
			{
				if(!children.contains(moduleName))
					mModulesJa.remove(mModulesJo.remove(moduleName));
			}
			changed = true ;
		}
		if(changed)
		{
			_sortModules() ;
		}
		mLastRefreshTime = System.currentTimeMillis() ;
	}
	
	void _sortModules()
	{
		mModulesJa.sort((e1 , e2)->{
			double diff = ((JSONObject)e1).optDouble("order", 0d) - ((JSONObject)e2).optDouble("order", 0d) ;
			return diff == 0?0:(diff>0?1:-1) ;
		}) ;
	}
	
	void notifyServiceAddressChanged(String aZKPath , String aMyAddr)
	{
		try
		{
			String addr = JCommon.defaultIfNull(getNodeData_Str(aZKPath) , "") ;
			if(addr.contains(aMyAddr))
				App.instance().active() ;
			else
				App.instance().standby() ;
		}
		catch (Exception e)
		{
			mLogger.error(ExceptionAssist.getClearMessage(getClass(), e)) ;
		}
	}
	
	@Override
	public void _registerApiService(String aServiceName, Properties aInfo, ClusterMode aMode
			, String aProtocol , String aAddrs) throws Exception
	{	
		String protocolPath = XString.msgFmt(SysConst.sZK_SysPathPtn_ApiService_special
				, aServiceName , aProtocol) ;
		ServiceRegistery reg = mServiceRegisteryMap.get(protocolPath) ;
		if(reg == null)
		{
			reg = new ServiceRegistery(aProtocol , aServiceName) ;
			reg.setAddrs(aAddrs) ;
			reg.setInfo(aInfo) ;
			reg.setMode(aMode) ;
			mServiceRegisteryMap.put(protocolPath, reg) ;
		}
		else 
		{
			reg.setInfo(aInfo) ;
			reg.setMode(aMode) ;
			reg.setAddrs(aAddrs) ;
		}
		byte[] addrsData = aAddrs.getBytes(AppContext.sUTF8) ;
		// 创建协议容器节点
		if(mZK.exists(protocolPath , false) == null)
		{
			String servicePath = XString.msgFmt(SysConst.sZK_SysPathPtn_ApiService
					, aServiceName) ;
			if(mZK.exists(servicePath, false) == null)
			{
				ensureExists(servicePath) ;
				setNodeData(servicePath, PropertiesEx.toString(aInfo, true, false));
			}
			ensureExists(protocolPath) ;
		}
		if(XString.isEmpty(reg.getWatchNodeDataId()))
		{
			String watchNodeDataId = watchNode(protocolPath, new Watcher()
			{
				@Override
				public void process(WatchedEvent aEvent)
				{
					if(aEvent.getType() == EventType.NodeDataChanged)
					{
						notifyServiceAddressChanged(aEvent.getPath() , aAddrs) ;
					}
				}
			}) ;
			reg.setWatchNodeDataId(watchNodeDataId) ;
		}
		
		if(XString.isEmpty(reg.getWatchChildrenId()))
		{
			String watchChildrenId = watchChildren(protocolPath , new Watcher()
			{	
				@Override
				public void process(WatchedEvent aEvent)
				{
					// API网关同时注册了http和https，会被触发两次，如果第一次avtive成功，第二次调用是没有影响的
					if(aEvent.getType() == EventType.NodeChildrenChanged)
					{
						try
						{
							Stat stat = new Stat() ;
							byte[] currentAddrData = getNodeData(protocolPath , stat) ;
							switch(aMode)
							{
							case MasterSlave:
							{
								// 覆盖
								List<String> children = getChildren(aEvent.getPath(), false) ;
								byte[] lastNode_addrData = JCommon.sEmptyByteArray ;
								if(!children.isEmpty())
								{
									Collections.sort(children) ;
									// 选取最新的一个临时节点
									String lastNode = XC.getLast(children) ;
									lastNode_addrData = getNodeData(protocolPath+"/"+lastNode) ;
								}
								if(!JCommon.equals(currentAddrData, lastNode_addrData))
								{
									setNodeData(protocolPath, lastNode_addrData , stat) ;
								}
								else if(App.instance().getStatus() != Status.Active)
								{
									// 如果这地址是自己的地址，则触发一下
									notifyServiceAddressChanged(protocolPath , aAddrs) ;
								}
							}
							break ;
							case Federation:
							{
								// 追加
								List<String> children = getChildren(aEvent.getPath(), false) ;
								byte[] new_addrData = JCommon.sEmptyByteArray ;
								if(!children.isEmpty())
								{
									Collections.sort(children) ;
									StringBuilder strBld = new StringBuilder() ;
									for(String child : children)
									{
										if(strBld.length()>0)
											strBld.append(',') ;
										strBld.append(getNodeData_Str(protocolPath+"/"+child)) ;
									}
									new_addrData = strBld.toString().getBytes(AppContext.sUTF8) ;
								}
								if(!JCommon.equals(currentAddrData, new_addrData))
								{
									setNodeData(protocolPath, new_addrData , stat) ;
								}
							}
							break ;
							}
						}
						catch (Exception e)
						{
							mLogger.error(ExceptionAssist.getClearMessage(getClass(), e)) ;
						}
					}
				}
			}) ;
			reg.setWatchChildrenId(watchChildrenId) ;
		}
		// 在协议节点下面创建实例临时节点
		String instPath = protocolPath+"/"+App.instance().getStartTime().getTime() ;
		// 认定这个节点肯定已经不存在了
		instPath = mZK.create(instPath , addrsData, Ids.OPEN_ACL_UNSAFE , CreateMode.EPHEMERAL) ;
	}
	
	@Override
	public String getRegisteredHttpService(String aAppDirName) throws Exception
	{
		return getNodeData_Str(XString.msgFmt(SysConst.sZK_SysPathPtn_http , aAppDirName)) ;
	}
	
	@Override
	public JSONArray getRegisteredServiceBriefs(String aProduce) throws Exception
	{
		getChildren(aProduce, false) ;
		return null;
	}
	
	@Override
	synchronized void _reconnect(boolean aInOwnThread) throws Exception
	{
		super._reconnect(aInOwnThread);
		if(!mServiceRegisteryMap.isEmpty())
		{
			for(ServiceRegistery reg : mServiceRegisteryMap.values())
			{
				_registerApiService(reg.getServiceName() 
						, reg.getInfo()
						, reg.getMode()
						, reg.getProtocol()
						, reg.getAddrs());
			}
			mLogger.info("Zookeeper重新连接之后，已经重新注册服务！") ;
		}
	}
	
	@Override
	public JSONObject getHosts() throws Exception
	{
		if(mHostsJo == null)
		{
			watchNode(SysConst.sZK_CommonPath_hosts, (event)->{
				switch(event.getType())
				{
				case NodeDataChanged:
					try
					{
						mHostsJo = new JSONObject(PropertiesEx.loadFromReader(new StringReader(getNodeData_Str(SysConst.sZK_CommonPath_hosts)))) ;
						mLogger.info("刷新了一次hosts信息。");
					}
					catch (Exception e1)
					{
						mLogger.error("从ZK中读取hosts信息出现异常。异常消息："
								+ExceptionAssist.getClearMessage(ZKProxy.class, e1));
					}
		            break ;
		         default:
		        	 break ;
				}
			}) ;
			mHostsJo = new JSONObject(PropertiesEx.loadFromReader(new StringReader(getNodeData_Str(SysConst.sZK_CommonPath_hosts)))) ;
			mLogger.info("刷新了一次hosts信息。");
		}
		return mHostsJo ;
	}
	
	@Override
	public void setNodeData(String aPath, byte[] aData) throws Exception
	{
		if(aPath.startsWith(sCommonPathPrefix))
		{
			IZKProxy proxy = ZKProxy.get(sDefaultQuorum) ;
			proxy.setNodeData(aPath, aData) ;
		}
		else
			super.setNodeData(aPath, aData);
	}
	
	@Override
	public byte[] getNodeData(String aPath) throws Exception
	{
		if(aPath.startsWith(sCommonPathPrefix))
		{
			IZKProxy proxy = ZKProxy.get(sDefaultQuorum) ;
			return proxy.getNodeData(aPath) ;
		}
		else
			return super.getNodeData(aPath);
	}
	
	@Override
	public Tuples.T2<byte[], Stat> getNodeDataWithStat(String aPath) throws Exception
	{
		if(aPath.startsWith(sCommonPathPrefix))
		{
			IZKProxy proxy = ZKProxy.get(sDefaultQuorum) ;
			return proxy.getNodeDataWithStat(aPath) ;
		}
		else
			return super.getNodeDataWithStat(aPath);
	}
	
	@Override
	public boolean delete(String aPath, boolean aWhenIsLeaf) throws Exception
	{
		if(aPath.startsWith(sCommonPathPrefix))
		{
			IZKProxy proxy = ZKProxy.get(sDefaultQuorum) ;
			return proxy.delete(aPath, aWhenIsLeaf) ;
		}
		else
			return super.delete(aPath, aWhenIsLeaf);
	}
	
	@Override
	public boolean deleteNode(String aPath) throws Exception
	{
		if(aPath.startsWith(sCommonPathPrefix))
		{
			IZKProxy proxy = ZKProxy.get(sDefaultQuorum) ;
			return proxy.deleteNode(aPath) ;
		}
		else
			return super.deleteNode(aPath);
	}
	
	@Override
	public void duplicate(String aPath, String aNewPath, boolean aDeepth) throws Exception
	{
		if(aPath.startsWith(sCommonPathPrefix))
		{
			IZKProxy proxy = ZKProxy.get(sDefaultQuorum) ;
			proxy.duplicate(aPath, aNewPath, aDeepth) ;
		}
		else
			super.duplicate(aPath, aNewPath, aDeepth);
	}
	
	@Override
	public boolean exists(String aPath) throws Exception
	{
		if(aPath.startsWith(sCommonPathPrefix))
		{
			IZKProxy proxy = ZKProxy.get(sDefaultQuorum) ;
			return proxy.exists(aPath) ;
		}
		else
			return super.exists(aPath) ;
	}
	
	@Override
	public boolean ensureExists(String aPath) throws Exception
	{
		if(aPath.startsWith(sCommonPathPrefix))
		{
			IZKProxy proxy = ZKProxy.get(sDefaultQuorum) ;
			return proxy.ensureExists(aPath);
		}
		else
			return super.ensureExists(aPath);
	}
	
	@Override
	public String getAnyOneChildPath(String aPath) throws Exception
	{
		if(aPath.startsWith(sCommonPathPrefix))
		{
			IZKProxy proxy = ZKProxy.get(sDefaultQuorum) ;
			return proxy.getAnyOneChildPath(aPath) ;
		}
		else
			return super.getAnyOneChildPath(aPath);
	}
	
	@Override
	public List<String> getChildren(String aPath, boolean aWatch) throws Exception
	{
		if(aPath.startsWith(sCommonPathPrefix))
		{
			IZKProxy proxy = ZKProxy.get(sDefaultQuorum) ;
			return proxy.getChildren(aPath, aWatch) ;
		}
		else
			return super.getChildren(aPath, aWatch);
	}
	
	@Override
	public String watchNodeOnce(String aPath, Watcher aWatch) throws Exception
	{
		if(aPath.startsWith(sCommonPathPrefix))
		{
			IZKProxy proxy = ZKProxy.get(sDefaultQuorum) ;
			return proxy.watchNodeOnce(aPath, aWatch) ;
		}
		else
			return super.watchNodeOnce(aPath, aWatch);
	}
	
	@Override
	public String watchChildrenOnce(String aPath, Watcher aWatch) throws Exception
	{
		if(aPath.startsWith(sCommonPathPrefix))
		{
			IZKProxy proxy = ZKProxy.get(sDefaultQuorum) ;
			return proxy.watchChildrenOnce(aPath, aWatch) ;
		}
		else
			return super.watchChildrenOnce(aPath, aWatch);
	}
	
	@Override
	public String watchChildren(String aPath, Watcher aWatcher) throws Exception
	{
		if(aPath.startsWith(sCommonPathPrefix))
		{
			IZKProxy proxy = ZKProxy.get(sDefaultQuorum) ;
			return proxy.watchChildren(aPath, aWatcher) ;
		}
		else
			return super.watchChildren(aPath, aWatcher);
	}
	
	@Override
	public String watchNode(String aPath, Watcher aWatcher , boolean aFocusCreateEvent) throws Exception
	{
		if(aPath.startsWith(sCommonPathPrefix))
		{
			IZKProxy proxy = ZKProxy.get(sDefaultQuorum) ;
			return proxy.watchNode(aPath, aWatcher , aFocusCreateEvent) ;
		}
		else
			return super.watchNode(aPath, aWatcher , aFocusCreateEvent);
	}
	
	@Override
	public String getHadoopClusterName() throws Exception
	{
		String props = getNodeData_Str(SysConst.sZK_CommonPath_hadoop) ;
		PropertiesEx propEx = new PropertiesEx() ;
		propEx.load(new StringReader(props));
		return propEx.getProperty("cluster") ;
	}
	
	@Override
	public boolean ensureExists_Temp(String aPath) throws Exception
	{
		if(aPath.startsWith(sCommonPathPrefix))
		{
			IZKProxy proxy = ZKProxy.get(sDefaultQuorum) ;
			return proxy.ensureExists_Temp(aPath) ;
		}
		else
			return super.ensureExists_Temp(aPath);
	}
	
	@Override
	public void setNodeData(String aPath, byte[] aData, Stat aStat, boolean aLeafAsTemp) throws Exception
	{
		if(aPath.startsWith(sCommonPathPrefix))
		{
			IZKProxy proxy = ZKProxy.get(sDefaultQuorum) ;
			proxy.setNodeData(aPath, aData, aStat, aLeafAsTemp);
		}
		else
			super.setNodeData(aPath, aData, aStat, aLeafAsTemp);
	}
	
	@Override
	public String getKafkaBootstrapServers() throws Exception
	{
		IZKProxy proxy = ZKProxy.get(sDefaultQuorum) ;
		return proxy.getKafkaBootstrapServers() ;
	}
	
	@Override
	public void registerWebApp(String aAppName, PropertiesEx aProp
			, ClusterMode aMode, String aServiceUri) throws Exception
	{	
		String webAppPath = XString.msgFmt(SysConst.sZK_SysPathPtn_WebApp
				, aAppName) ;
		String activeWebAppPath = XString.msgFmt(SysConst.sZK_SysPathPtn_ActiveWebApp
				, aAppName) ;
		ServiceRegistery reg = mServiceRegisteryMap.get(activeWebAppPath) ;
		if(reg == null)
		{
			reg = new ServiceRegistery(null , aAppName) ;
			reg.setAddrs(aServiceUri) ;
			reg.setInfo(aProp) ;
			reg.setMode(aMode) ;
			mServiceRegisteryMap.put(activeWebAppPath , reg) ;
		}
		else 
		{
			reg.setAddrs(aServiceUri) ;
			reg.setInfo(aProp) ;
			reg.setMode(aMode) ;
		}
		byte[] addrsData = aServiceUri.getBytes(AppContext.sUTF8) ;
		// 创建协议容器节点
		boolean updatedWebAppNodeData = false ;
		String appInfoStr_new = PropertiesEx.toString(reg.getInfo() , true, false) ;
		if(mZK.exists(activeWebAppPath , false) == null)
		{
			if(mZK.exists(webAppPath , false) == null)
			{
				ensureExists(webAppPath) ;
				setNodeData(webAppPath , appInfoStr_new);
				updatedWebAppNodeData = true ;
			}
			ensureExists(activeWebAppPath) ;
		}
		if(!updatedWebAppNodeData)
		{
			String appInfoStr = getNodeData_Str(webAppPath) ;
			if(!appInfoStr_new.equals(appInfoStr))
			{
				setNodeData(webAppPath , appInfoStr_new);
				updatedWebAppNodeData = true ;
			}
		}
		if(XString.isEmpty(reg.getWatchNodeDataId()))
		{
			String watchNodeDataId = watchNode(activeWebAppPath , new Watcher()
			{
				@Override
				public void process(WatchedEvent aEvent)
				{
					if(aEvent.getType() == EventType.NodeDataChanged)
					{
						notifyServiceAddressChanged(aEvent.getPath() , aServiceUri) ;
					}
				}
			}) ;
			reg.setWatchNodeDataId(watchNodeDataId) ;
		}
		
		if(XString.isEmpty(reg.getWatchChildrenId()))
		{
			String watchChildrenId = watchChildren(activeWebAppPath , new Watcher()
			{	
				@Override
				public void process(WatchedEvent aEvent)
				{
					if(aEvent.getType() == EventType.NodeChildrenChanged)
					{
						try
						{
							Stat stat = new Stat() ;
							byte[] currentAddrData = getNodeData(activeWebAppPath , stat) ;
							switch(aMode)
							{
							case MasterSlave:
							{
								// 覆盖
								List<String> children = getChildren(aEvent.getPath(), false) ;
								byte[] lastNode_addrData = JCommon.sEmptyByteArray ;
								if(!children.isEmpty())
								{
									Collections.sort(children) ;
									// 选取最新的一个临时节点
									String lastNode = XC.getLast(children) ;
									lastNode_addrData = getNodeData(activeWebAppPath+"/"+lastNode) ;
								}
								if(!JCommon.equals(currentAddrData, lastNode_addrData))
								{
									setNodeData(activeWebAppPath , lastNode_addrData , stat) ;
								}
								else if(App.instance().getStatus() != Status.Active)
								{
									// 如果这地址是自己的地址，则触发一下
									notifyServiceAddressChanged(activeWebAppPath , aServiceUri) ;
								}
							}
							break ;
							case Federation:
							{
								// 追加
								List<String> children = getChildren(aEvent.getPath(), false) ;
								byte[] new_addrData = JCommon.sEmptyByteArray ;
								if(!children.isEmpty())
								{
									Collections.sort(children) ;
									StringBuilder strBld = new StringBuilder() ;
									for(String child : children)
									{
										if(strBld.length()>0)
											strBld.append(',') ;
										strBld.append(getNodeData_Str(activeWebAppPath+"/"+child)) ;
									}
									new_addrData = strBld.toString().getBytes(AppContext.sUTF8) ;
								}
								if(!JCommon.equals(currentAddrData, new_addrData))
								{
									setNodeData(activeWebAppPath , new_addrData , stat) ;
								}
							}
							break ;
							}
						}
						catch (Exception e)
						{
							mLogger.error(ExceptionAssist.getClearMessage(getClass(), e)) ;
						}
					}
				}
			}) ;
			reg.setWatchChildrenId(watchChildrenId) ;
		}
		// 在协议节点下面创建实例临时节点
		String instPath = activeWebAppPath+"/"+App.instance().getStartTime().getTime() ;
		// 认定这个节点肯定已经不存在了
		instPath = mZK.create(instPath , addrsData, Ids.OPEN_ACL_UNSAFE , CreateMode.EPHEMERAL) ;
	}
	
	@Override
	public JSONObject getRegisteredWebApp(String aName) throws Exception
	{
		String webAppPath = XString.msgFmt(SysConst.sZK_SysPathPtn_WebApp , aName) ;
		String str = getNodeData_Str(webAppPath) ;
		if(str != null)
		{
			JSONObject appInfoJo = JSONObject.of(PropertiesEx.loadFromReader(new StringReader(str))
					.toStringMap()) ;
			// 取下面临时节点中的服务地址信息
			String activeNodePath = XString.msgFmt(SysConst.sZK_SysPathPtn_ActiveWebApp , aName) ;
			appInfoJo.put("serviceUri" , getNodeData_Str(activeNodePath))
					.put("active" , XC.isNotEmpty(getChildren(activeNodePath , false)));
			return appInfoJo ;
		}
		return null ;
	}
	
	static class ServiceRegistery
	{
		String mProtocol ;
		String mServiceName ;
		String mAddrs ;
		Properties mInfo ;
		ClusterMode mMode ;
		
		String mWatchChildrenId ;
		String mWatchNodeDataId ;
		
		public ServiceRegistery(String aProtocol , String aServiceName)
		{
			mProtocol = aProtocol ;
			mServiceName = aServiceName ;
		}
		
		public String getProtocol()
		{
			return mProtocol;
		}
		
		public String getServiceName()
		{
			return mServiceName;
		}
		
		public boolean setAddrs(String aAddrs)
		{
			if(JCommon.unequals(aAddrs, mAddrs))
			{
				mAddrs = aAddrs;
				return true ;
			}
			return false ;
		}
		public String getAddrs()
		{
			return mAddrs;
		}
		
		public boolean setInfo(Properties aInfo)
		{
			if(JCommon.unequals(mInfo, aInfo))
			{
				mInfo = aInfo;
				return true ;
			}
			return false ;
		}
		public Properties getInfo()
		{
			return mInfo;
		}
		
		public boolean setMode(ClusterMode aMode)
		{
			if(mMode != aMode)
			{
				mMode = aMode;
				return true ;
			}
			return false ;
		}
		public ClusterMode getMode()
		{
			return mMode;
		}
		
		public void setWatchChildrenId(String aWatchChildrenId)
		{
			mWatchChildrenId = aWatchChildrenId;
		}
		public String getWatchChildrenId()
		{
			return mWatchChildrenId;
		}
		
		public void setWatchNodeDataId(String aWatchNodeDataId)
		{
			mWatchNodeDataId = aWatchNodeDataId;
		}
		public String getWatchNodeDataId()
		{
			return mWatchNodeDataId;
		}
	}
}
