package team.sailboat.base;

import java.util.List;
import java.util.Stack;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.event.IXListener;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.struct.Tuples;

public interface IZKProxy
{	
	
	String getHomeDir() ;
	
	void addReconnectedListener(IXListener aLsn) ;
	
	void removeReconnectedListener(IXListener aLsn) ;
	
	void setNodeData(String aPath , String aData) throws Exception ;
	
	default void setNodeData(String aPath , String aData , Stat aStat) throws Exception
	{
		setNodeData(aPath, aData==null?JCommon.sEmptyByteArray:aData.getBytes(AppContext.sUTF8) , aStat);
	}
	
	void setTempNodeData(String aPath , String aData) throws Exception ;
	
	boolean deleteNode(String aPath) throws Exception ;
	
	String getNodeData_Str(String aPath) throws Exception ;
	
	String getNodeData_Str(String aPath , Stat aStat) throws Exception ;
	
	Tuples.T2<String , Stat> getNodeData_StrWithStat(String aPath) throws Exception ;
	
	Integer getNodeData_int(String aPath) throws Exception ;
	
	byte[] getNodeData(String aPath) throws Exception ;
	
	byte[] getNodeData(String aPath , Stat aStat) throws Exception ;
	
	Tuples.T2<byte[] , Stat> getNodeDataWithStat(String aPath) throws Exception ;
	
	boolean exists(String aPath) throws Exception ;
	
	/**
	 * 
	 * @param aPath
	 * @return			如果原先节点已经存在，将返回false；如果新建了节点，将返回true
	 * @throws Exception
	 */
	boolean ensureExists(String aPath) throws Exception ;
	
	/**
	 * 如果路径不存在，将会创建。只有最后一层是TempNode。		<br>
	 * 如果路径已经存在，则不会造成任何影响
	 * @param aPath
	 * @return
	 * @throws Exception
	 */
	boolean ensureExists_Temp(String aPath) throws Exception ;
	
	default boolean ensureExists(String aPath , byte[] aData) throws Exception
	{
		if(ensureExists(aPath) && aData != null)
		{
			setNodeData(aPath, aData) ;
			return true ;
		}
		return false ;
	}
	
	default boolean ensureExists(String aPath , String aDefaultValue) throws Exception
	{
		return ensureExists(aPath , aDefaultValue != null?aDefaultValue.getBytes(AppContext.sUTF8): null) ;
	}
	
	void setNodeData(String aPath , byte[] aData) throws Exception ;
	
	void setNodeData(String aPath , byte[] aData , Stat aStat) throws Exception ;
	
	void setNodeData(String aPath , byte[] aData , Stat aStat , boolean aLeafAsTemp) throws Exception ;
	
	/**
	 * 监听节点的create、delete及data变化			<br>
	 * 一次性的，如果想持续监听，需要在Watcher里面重复注册
	 * @param aPath
	 * @param aWatch
	 * @return 代表此次注册的标识，如果需要取消监听，则应该保存返回字符串
	 * @throws Exception
	 */
	String watchNodeOnce(String aPath , Watcher aWatch) throws Exception ;
	
	/**
	 * 监听节点的create、delete及data变化			<br>
	 * 持续监听，在Watcher内<b>不要再自己注册监听</b>
	 * 
	 * @param aPath
	 * @param aWatch
	 * @return	代表此次注册的标识，如果需要取消监听，则应该保存返回字符串
	 * @throws Exception
	 */
	default String watchNode(String aPath , Watcher aWatch) throws Exception
	{
		return watchNode(aPath, aWatch, false) ;
	}
	
	/**
	 * 监听节点的create、delete及data变化			<br>
	 * 持续监听，在Watcher内<b>不要再自己注册监听</b>
	 * 
	 * @param aPath
	 * @param aWatch
	 * @param aFocusCreateEvent    是否关心创建事件
	 * @return	代表此次注册的标识，如果需要取消监听，则应该保存返回字符串
	 * @throws Exception
	 */
	String watchNode(String aPath , Watcher aWatch , boolean aFocusCreateEvent) throws Exception ;
	
	/**
	 * 取消监听器
	 * @param aPath
	 * @param aWatch
	 * @throws Exception
	 */
	void cancelWatch(String aHandle) throws Exception ;
	
	/**
	 * 监听子节点的创建和删除事件
	 * @param aPath
	 * @param aWatcher
	 * @throws Exception
	 */
	String watchChildrenOnce(String aPath , Watcher aWatcher) throws Exception ;
	/**
	 * 持续监听子节点
	 * @param aPath
	 * @param aWatcher 在Watcher内部不要主动注册监听
	 * @return
	 * @throws Exception
	 */
	String watchChildren(String aPath , Watcher aWatcher) throws Exception ;
	
	String getAnyOneChildPath(String aPath) throws Exception ;
	
	/**
	 * 指定路径下的文件名
	 * @param aPath
	 * @param aWatch
	 * @return
	 * @throws Exception
	 */
	List<String> getChildren(String aPath, boolean aWatch) throws Exception ;
	
	/**
	 * 
	 * @param aPath
	 * @param aNewName
	 * @return			返回新的节点路径
	 * @throws Exception
	 */
	String rename(String aPath , String aNewName) throws Exception ;
	
	void duplicate(String aPath , String aNewPath , boolean aDeepth) throws Exception ;
	
	boolean delete(String aPath , boolean aWhenIsLeaf) throws Exception ;
	
	String getKafkaBootstrapServers() throws Exception ;
	
	/**
	 * 
	 * @param aZK
	 * @param aPath
	 * @return		如果原先节点已经存在，将返回false；如果新建了节点，将返回true
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	public static boolean ensureExists(ZooKeeper aZK , String aPath) throws KeeperException, InterruptedException
	{
		return ensureExists(aZK, aPath , false) ;
	}
	
	public static boolean ensureExists(ZooKeeper aZK , String aPath
			, boolean aLeafAsTemp) throws KeeperException, InterruptedException
	{
		String path = aPath ;
		Stack<String> pathStack = new Stack<>() ;
		while(!path.isEmpty() && !"/".equals(path) && aZK.exists(path, false) == null)
		{
			pathStack.push(path) ;
			path = FileUtils.getParent(path) ;
		}
		boolean needCreate = !pathStack.isEmpty() ;
		while(!pathStack.isEmpty())
		{
			aZK.create(pathStack.pop(), JCommon.sEmptyByteArray, Ids.OPEN_ACL_UNSAFE 
					, aLeafAsTemp && pathStack.isEmpty()?CreateMode.EPHEMERAL:CreateMode.PERSISTENT) ;
		}
		return needCreate ;
	}
}
