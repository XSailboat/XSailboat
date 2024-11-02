package team.sailboat.bd.base.beanch;

import java.sql.SQLException;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hbase.client.Connection;

import team.sailboat.base.def.WorkEnv;
import team.sailboat.base.util.IWSDBeanIdHelper;
import team.sailboat.bd.base.ZBDException;
import team.sailboat.bd.base.infc.IFunctionResProvider;
import team.sailboat.bd.base.infc.IWorkspace;
import team.sailboat.bd.base.model.IWSNodeSite;
import team.sailboat.commons.fan.dpa.DRepository;
import team.sailboat.commons.ms.db.RunData;

/**
 * 此对象不要长期持有
 *
 * @author yyl
 * @since 2021年6月16日
 */
public interface IWSRepo extends IWSDBeanIdHelper
{
	public static final String sCIK_jarResourcesVersion = "jarResourcesVersion" ;
	public static final String sCIK_functionsVersion = "functionsVersion" ;
	
	IWSRepoSite getParent() ;
	
	String getWorkspaceId() ;
	
	/**
	 * 工作空间在当前环境下的资源名，如果是开发环境，将是“工作空间名称Dev”
	 * @return
	 */
	String getWsResourceName() ;
	
	IWorkspace getWorkspace() ;
	
	String getRootNodeId() ;
	
	String getHdfsWorkDir() ;
	
	FileSystem getWsHdfs() ;
	
	/**
	 * 使用完不要close
	 * @return
	 */
	Connection getWsHBaseConnection() ;
	
	/**
	 * 使用完要close
	 * @return
	 * @throws SQLException 
	 */
	java.sql.Connection getWsDBConnection() throws SQLException ;
	
	/**
	 * 使用完要close
	 * @return
	 * @throws SQLException 
	 */
	java.sql.Connection getWsHiveConnection() throws SQLException ;
	
	DRepository getWsRepository() ;
	
	IWSNodeSite getWsNodeSite() ;
	
	void checkAndPrepare() throws ZBDException ;
	
	RunData getRunData() ;
	
	IFunctionResProvider getFuncResProvider() ;
	
	/**
	 * 获取工作空间的配置
	 * @return
	 */
	WSConf getConf() ;
	
	public static String getWsResourceName(String aWsName , boolean aDevEnv)
	{
		return aDevEnv?getDevWsResourceName(aWsName):aWsName ;
	}
	
	public static String getWsResourceName(String aWsName , WorkEnv aWorkEnv)
	{
		return aWorkEnv == WorkEnv.dev ?getDevWsResourceName(aWsName):aWsName ;
	}
	
	public static boolean isDevEnv(String aWsResourceName)
	{
		return aWsResourceName.endsWith("Dev") ;
	}
	
	/**
	 * 是否是工作空间根节点id		<br />
	 * 工作空间根节点的id格式：工作空间id#root
	 * @param aId
	 * @return
	 */
	public static boolean isWsRootNodeId(String aId)
	{
		return aId.endsWith("#root") ;
	}
	
	public static String getDevWsResourceName(String aWsName)
	{
		return aWsName+"Dev" ;
	}
}
