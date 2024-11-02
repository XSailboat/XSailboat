package team.sailboat.bd.base.beanch;

import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hbase.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import team.sailboat.base.ZKSysProxy;
import team.sailboat.bd.base.AppConfigBase;
import team.sailboat.commons.fan.collection.AutoCleanHashMap;
import team.sailboat.commons.fan.dpa.DRepository;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.serial.StreamAssist;

public abstract class WSRepoSiteBase implements IWSRepoSite
{
	protected final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	@Resource(name="hdfs")
	protected FileSystem mHdfs ;
	
	@Resource(name="hbaseConn_sys")
	protected Connection mHBaseConn ;
	
	@Resource(name="sysDB")
	protected DataSource mSysDS ;
	
	@Resource(name="hiveDB")
	protected DataSource mHiveDS ;
	
	@Resource(name = "sysRepo")
    protected DRepository mRepo ;
	
	@Autowired
	protected AppConfigBase mAppCfg ;
	
	protected AutoCleanHashMap<String, IWSRepo> mRepoMap ;
	
	protected String mHomeDirOnHdfs ;
	
	IFlowDiscovery mFlowDiscovery ;
	
	String mClusterName ;
	
	public WSRepoSiteBase()
	{
		
	}
	
	@PostConstruct
	protected void _init()
	{
		mRepoMap = AutoCleanHashMap.withExpired_Idle(30 , true) ;
		mHomeDirOnHdfs = mAppCfg.getHomeDirOnHdfs() ;
		mFlowDiscovery = new FlowDiscovery(this)  ;
		try
		{
			mClusterName = ZKSysProxy.getSysDefault().getHadoopClusterName() ;
		}
		catch (Exception e)
		{
			WrapException.wrapThrow(e) ;
		}
	}
	
	@Override
	public FileSystem getSysHdfs()
	{
		return mHdfs ;
	}
	
	/**
	 * 用完要释放
	 * @return
	 * @throws SQLException 
	 */
	@Override
	public java.sql.Connection getSysDBConnection() throws SQLException
	{
		return mSysDS.getConnection() ;
	}
	
	@Override
	public AppConfigBase getAppConfig()
	{
		return mAppCfg ;
	}
	
	@Override
	public Connection getSysHBaseConnection()
	{
		return mHBaseConn ;
	}
	
	/**
	 * 用完要close
	 * @return
	 * @throws SQLException
	 */
	@Override
	public java.sql.Connection getSysHiveConnection() throws SQLException
	{
		return mHiveDS.getConnection() ;
	}
	
	@Override
	public IFlowDiscovery getFlowDiscovery()
	{
		return mFlowDiscovery ;
	}
	
	@Override
	public String getCluserName()
	{
		return mClusterName ;
	}
	
	/**
	 * 从缓存里面移除，并释放相关资源
	 */
	@Override
	public void removeWorkspace(String aWsId)
	{
		StreamAssist.close(mRepoMap.remove(aWsId)) ;
	}
}
