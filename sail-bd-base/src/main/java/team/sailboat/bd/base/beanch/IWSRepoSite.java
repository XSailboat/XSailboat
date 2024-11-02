package team.sailboat.bd.base.beanch;

import java.sql.SQLException;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hbase.client.Connection;

import team.sailboat.bd.base.AppConfigBase;
import team.sailboat.bd.base.ZBDException;
import team.sailboat.bd.base.model.Workspace;
import team.sailboat.commons.fan.lang.Assert;

/**
 * 
 *
 * @author yyl
 * @since 2021年3月20日
 */
public interface IWSRepoSite
{
	/**
	 * 取得所有工作空间的id
	 * @return
	 */
	String[] getAllWorkspaceIds() ;
	
	/**
	 * 获取工作空间的信息
	 * @param aId
	 * @return
	 */
	Workspace getWorkspaceById(String aId) ;
	
	/**
	 * 
	 * @return
	 */
	FileSystem getSysHdfs() ;
	
	/**
	 * 用完要释放
	 * @return
	 * @throws SQLException 
	 */
	java.sql.Connection getSysDBConnection() throws SQLException ;
	
	/**
	 * 用完不要close
	 * @return
	 */
	Connection getSysHBaseConnection() ;
	
	/**
	 * 用完要close
	 * @return
	 * @throws SQLException
	 */
	java.sql.Connection getSysHiveConnection() throws SQLException ;
	
	/**
	 * 
	 * @param aId
	 * @return		返回结果必然不为null，如果不存在将抛出异常
	 */
	default IWSRepo checkWorkspaceRepo(String aId) throws ZBDException
	{
		IWSRepo wsRepo = getOrLoadWorkspaceRepo(aId) ;
		Assert.notNull(wsRepo, "不存在id为 %s 的工作空间" , aId) ;
		return wsRepo ;
	}
	
	/**
	 * 如果当前缓存中没有构造出IWSRepo，将返回null
	 * @param aId
	 * @return
	 * @throws ZBDException
	 */
	IWSRepo getWorkspaceRepo(String aId) ;
	
	/**
	 * 如果当前缓存中没有构造出IWSRepo，将去加载，如果无法找到并加载，将返回null
	 * @param aId
	 * @return
	 * @throws ZBDException
	 */
	IWSRepo getOrLoadWorkspaceRepo(String aId)  throws ZBDException ;
	
	/**
	 * 如果当前缓存中没有构造出IWSRepo，将返回null
	 * @param aName
	 * @return
	 * @throws ZBDException
	 */
	IWSRepo getWorkspaceRepoByName(String aName) throws ZBDException ;
	
	/**
	 * 如果当前缓存中没有构造出IWSRepo，将去加载，如果无法找到并加载，将返回null
	 * @param aName
	 * @return
	 * @throws ZBDException
	 */
	IWSRepo getOrLoadWorkspaceRepoByName(String aName) throws ZBDException ;
	
	
	/**
	 * 如果不存在指定名称的工作空间，将会抛出异常
	 * @param aName
	 * @return
	 * @throws ZBDException
	 */
	default IWSRepo checkWorkspaceRepoByName(String aName) throws ZBDException
	{
		IWSRepo wsRepo = getOrLoadWorkspaceRepoByName(aName) ;
		Assert.notNull(wsRepo, "不存在名为 %s 的工作空间" , aName) ;
		return wsRepo ;
	}
	
	String getWorkspaceIdByName(String aName) ;
	
	String getWorkspaceNameById(String aWsId) ;
	
	void removeWorkspace(String aWsId) ;
	
	default String checkWorkspaceIdByName(String aName)
	{
		String wsId = getWorkspaceIdByName(aName) ;
		Assert.notNull(wsId , "不存在名为 %s 的工作空间" , aName) ;
		return wsId ;
	}
	
	default String checkWorkspaceNameById(String aWsId) 
	{
		String wsName = getWorkspaceNameById(aWsId) ;
		Assert.notNull(wsName , "不存在id为 %s 的工作空间" , aWsId) ;
		return wsName ;
	}
	
	IFlowDiscovery getFlowDiscovery() ;
	
	AppConfigBase getAppConfig() ;
	
	String getCluserName() ;
	
}
