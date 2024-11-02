package team.sailboat.bd.base.beanch;

import java.io.Closeable;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Wrapper;

import javax.sql.DataSource;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hive.jdbc.HiveConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;

import team.sailboat.bd.base.AppConfigBase;
import team.sailboat.bd.base.ZBDException;
import team.sailboat.bd.base.hbase.HBaseUtils;
import team.sailboat.bd.base.infc.IFunctionResProvider;
import team.sailboat.bd.base.infc.IWorkspace;
import team.sailboat.bd.base.model.FlowValve;
import team.sailboat.bd.base.model.IWSNodeSite;
import team.sailboat.bd.base.proxy.INodeResourceProxy;
import team.sailboat.bd.base.proxy.NodeResourceProxy_HDFS;
import team.sailboat.commons.fan.dpa.DRepository;
import team.sailboat.commons.fan.dtool.DBHelper;
import team.sailboat.commons.fan.dtool.IDBTool;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.ms.db.RunData;

public abstract class WSRepoBase implements IWSRepo , Closeable
{
	protected final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	protected IWSRepoSite mSite ;
	
	/**
	 * 相关的工作空间id
	 */
	protected IWorkspace mWorkspace ;
	/**
	 * 资源的名称，开发环境下，资源名称格式是：工作空间的名称Dev
	 */
	protected String mWsResourceName ;

	/**
	 * 此工作空间在hdfs上的工作目录
	 */
	protected String mHdfsWorkDir ;
	
	/**
	 * 这个工作空间的MySQL数据源
	 */
	protected DataSource mWsDS ;
	
	/**
	 * 专属于这个工作空间的Hive数据库
	 */
	protected DataSource mHiveDS ;
	
	/**
	 * 基于这个工作空间专属的MySQL数据库（即，mWsDS）的对象仓库
	 */
	protected DRepository mRepo ;
	
	protected IWSNodeSite mNodeSite ;
	
	protected INodeResourceProxy mNodeResourceProxy ;
	
	protected String mRootNodeId ;
	
	protected RunData mRunData ;
	
	protected WSRepoBase(IWSRepoSite aSite , IWorkspace aWs , String aWsResourceName , String aHomeDirOnHdfs)
	{
		mSite = aSite ;
		mWorkspace = aWs ;
		mWsResourceName = aWsResourceName ;
		mHdfsWorkDir = aHomeDirOnHdfs+"/"+mWsResourceName ;
		mRootNodeId = aWs.getId()+"#root" ;
	}
	
	@Override
	public IWSRepoSite getParent()
	{
		return mSite ;
	}
	
	@Override
	public String getWorkspaceId()
	{
		return mWorkspace.getId() ;
	}
	
	@Override
	public IWorkspace getWorkspace()
	{
		return mWorkspace ;
	}
	
	@Override
	public String getRootNodeId()
	{
		return mRootNodeId ;
	}
	
	@Override
	public String getHdfsWorkDir()
	{
		return mHdfsWorkDir;
	}
	
	@Override
	public FileSystem getWsHdfs()
	{
		return mSite.getSysHdfs() ;
	}
	
	/**
	 * 使用完不要close
	 * @return
	 */
	@Override
	public Connection getWsHBaseConnection()
	{
		return mSite.getSysHBaseConnection() ;
	}
	
	/**
	 * 使用完要close
	 * @return
	 * @throws SQLException 
	 */
	@Override
	public java.sql.Connection getWsDBConnection() throws SQLException
	{
		return mWsDS.getConnection() ;
	}
	
	/**
	 * 使用完要close
	 * @return
	 * @throws SQLException 
	 */
	@Override
	public java.sql.Connection getWsHiveConnection() throws SQLException
	{
		IFunctionResProvider funcResPvd = getFuncResProvider() ;
		for(;;)
		{
			java.sql.Connection conn = mHiveDS.getConnection() ;
			long version = funcResPvd.getJarResourcesVersion() ;
			long funcVersion = funcResPvd.getFunctionsVersion() ;
			if(version == 0)
			{
				// 说明没有jar包，更不会有函数
				return conn ;
			}
			long oldVersion = 0 ;
			String v = conn.getClientInfo(sCIK_jarResourcesVersion) ;
			if(XString.isNotEmpty(v))
				oldVersion = Long.parseLong(v) ;
			if(version == oldVersion)
			{
				// 函数版本可能有变化
				oldVersion = 0 ;
				v = conn.getClientInfo(sCIK_functionsVersion) ;
				if(XString.isNotEmpty(v))
					oldVersion = Long.parseLong(v) ;
				if(funcVersion != oldVersion)
				{
					try(Statement stm = conn.createStatement())
					{
						stm.execute("RELOAD FUNCTIONS ;") ;
					}
					conn.setClientInfo(sCIK_functionsVersion , Long.toString(funcVersion)) ;
				}
				return conn ;
			}
			if(oldVersion > 0)
			{
				// 释放掉连接，重新构建，添加jar
				((DruidDataSource)mHiveDS).discardConnection(((DruidPooledConnection)conn).getConnectionHolder()) ;
				continue ;
			}
			else
			{
				String sqlPtn = "ADD JAR {}" ; 
				try(Statement stm = conn.createStatement())
				{
					for(String path : funcResPvd.getJarResourcesUrlPaths())
					{
						stm.execute(XString.msgFmt(sqlPtn , path)) ;
					}
				}
				catch(ZBDException e)
				{
					WrapException.wrapThrow(e) ;
				}
				conn.setClientInfo(sCIK_jarResourcesVersion , Long.toString(version)) ;
				conn.setClientInfo(sCIK_functionsVersion , Long.toString(funcVersion)) ;
			}
			return conn ;
		}
	}
	
	@Override
	public DRepository getWsRepository()
	{
		return mRepo ;
	}
	
	@Override
	public IWSNodeSite getWsNodeSite()
	{
		return mNodeSite;
	}
	
	@Override
	public void close() throws IOException
	{
		StreamAssist.closeAll(mWsDS , mHiveDS , mRepo ) ;
	}
	
	@Override
	public String getWsResourceName()
	{
		return mWsResourceName ;
	}
	
	@Override
	public void checkAndPrepare() throws ZBDException
	{
		//检查相关资源是否已经齐备，如果不存在，创建它们
		FileSystem fs = mSite.getSysHdfs() ;
		try
		{
			fs.mkdirs(new Path(mHdfsWorkDir)) ;
			mLogger.info("工作空间资源名{}在hdfs上的路径{}已经确保其存在。" , mWsResourceName , mHdfsWorkDir) ;
		}
		catch (IllegalArgumentException | IOException e)
		{
			throw new ZBDException(e , "创建工作空间资源名[{}]在hdfs上的工作目录出现异常!"  , mWsResourceName) ;
		}
		
		//MySql的数据库
		try(java.sql.Connection conn = mSite.getSysDBConnection())
		{
			AppConfigBase appCfg = mSite.getAppConfig() ;
			mWsDS =  _prepareDB(conn, appCfg.getSysRdbConnKey() , appCfg.getSysRdbConnSecret() , mWsResourceName , "关系数据库") ;
	    	
	    	mRepo = DRepository.of(mWsResourceName , mWsDS) ;
	    	loadDBeans(mRepo) ;
	    	mLogger.info("工作空间资源名{}的关系数据库{}已经确保其存在。" , mWsResourceName , mWsResourceName) ;
		}
		catch (SQLException e)
		{
			throw new ZBDException(e,  "创建工作空间资源名[{}]在关系数据库上的数据库出现异常!", mWsResourceName) ;
		}
		
		Connection sysHBaseConn = mSite.getSysHBaseConnection() ;
		String wsName = mWorkspace.getName() ;
		try(Admin admin = sysHBaseConn.getAdmin())
		{
			//判断名称空间是否已经存在，这里使用工作空间名即可，不需要区分开发环境还是生产环境
			if(!HBaseUtils.existsNamespace(admin , wsName))
			{
				//创建名称空间
				admin.createNamespace(NamespaceDescriptor.create(wsName).build());
			}
			mLogger.info("工作空间{}的HBase命名空间{}已经确保其存在。" , wsName , wsName) ;
		}
		catch (IOException e)
		{
			throw new ZBDException(e, "创建工作空间[{}]在HBase上的名称空间出现异常!", wsName) ;
		}
		/**
		 * 专属于这个工作空间的Hive数据库
		 */
		try(java.sql.Connection conn = mSite.getSysHiveConnection())
		{
			AppConfigBase appCfg = mSite.getAppConfig() ;
			mHiveDS =  _prepareDB(conn, appCfg.getHiveDBConnKey() , appCfg.getHiveDBConnSecret() , mWsResourceName , "hive") ;
			mLogger.info("工作空间{}的Hive数据库{}已经确保其存在。" , mWsResourceName , mWsResourceName) ;
		}
		catch (SQLException e)
		{
			throw new ZBDException(e,  "创建工作空间资源名[{}]在hive上的数据库出现异常!", mWsResourceName) ;
		}
		
		//
		try
		{
			Path path = new Path(mHdfsWorkDir+"/nodeFiles") ;
	    	if(!getWsHdfs().exists(path))
	    		getWsHdfs().mkdirs(path) ;
	    	mNodeResourceProxy = new NodeResourceProxy_HDFS(getWsHdfs() , path.toString()) ;
			mLogger.info("工作空间资源名{}在hdfs上的节点数据文件路径{}已经确保其存在。" , mWsResourceName , path) ;
		}
		catch (IOException e)
		{
			throw new ZBDException(e, "创建工作空间[{}]生产环境下基于hdfs的NodeResourceProxy出现异常!" , mWsResourceName) ;
		}
		//
		try
		{
			mRunData = new RunData(this::getWsDBConnection , "run_data") ;
		}
		catch (SQLException e)
		{
			throw new ZBDException(e, "构建RunData出现异常。") ;
		}
	}
	
	protected void loadDBeans(DRepository aRepo) throws SQLException
	{
		aRepo.load(FlowValve.class) ;
	}
	
	protected DataSource _prepareDB(java.sql.Connection aConn , String aUsername
			, String aPassword , String aDBName , String aDbDesc) throws SQLException
	{
		IDBTool dbTool = DBHelper.getDBTool(aConn) ;
		dbTool.createDatabase(aConn , aDBName);
		java.sql.Connection originalConn = aConn ;
		if(aConn instanceof Wrapper)
			originalConn = ((Wrapper)aConn).unwrap(java.sql.Connection.class) ;
		DatabaseMetaData md = originalConn.getMetaData() ;
		/**
		 * 权限：
		 * GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, RELOAD, REFERENCES, INDEX, ALTER
		 * , CREATE TEMPORARY TABLES, LOCK TABLES, EXECUTE, CREATE VIEW, SHOW VIEW, CREATE ROUTINE
		 * , ALTER ROUTINE, CREATE USER, TRIGGER, CREATE TABLESPACE ON *.* TO `zbd`@`%` WITH GRANT OPTION
		 */
//		String user = md.getUserName() ;
		//不用授权，服务所使用的数据库用户，有访问全部数据库和表的权限
//		dbTool.grantSchemaPrivileges(aConn, mName, user);
		String url = null ;
		boolean isHive = false ;
		if("Apache Hive".equals(md.getDatabaseProductName()))
		{
			url = ((HiveConnection)originalConn).getConnectedUrl() ;
//			url += "?serverTimezone=UTC+8" ;
			isHive = true ;
		}
		else
			url = md.getURL() ;
		int b = XString.indexOf_i(url, 0, '/', 2) ;
		int end = XString.indexOf_i(url, b, '?' , 0) ;
		if(end == -1)
			end = url.length() ;
		url = new StringBuilder().append(url, 0, b+1)
				.append(aDBName)
				.append(url, end , url.length())
				.toString() ;
		DruidDataSource ds = new DruidDataSource() ;
    	ds.setUrl(url) ;
    	ds.setUsername(aUsername) ;
    	ds.setPassword(aPassword) ;
    	ds.setDefaultAutoCommit(isHive) ;
    	if(isHive)
    		ds.setConnectionProperties("serverTimezone=UTC");
    	mLogger.info("工作空间资源名{}的{}URL:{}" , mWsResourceName , aDbDesc , url);
    	mLogger.info("工作空间资源名{}的{}用户:{}" , mWsResourceName , aDbDesc , aUsername);
    	try
		{
			ds.setDriver((Driver) DBHelper.loadJDBC(DBHelper.getDBType(url)).newInstance()) ;
		}
		catch (InstantiationException | IllegalAccessException | ClassNotFoundException e)
		{
			throw new IllegalStateException(e) ;
		}
    	return ds ;
	}
	
	@Override
	public RunData getRunData()
	{
		return mRunData ;
	}
}
