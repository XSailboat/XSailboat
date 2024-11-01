package team.sailboat.base.ds;

import java.sql.Connection;
import java.sql.Driver;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;

import team.sailboat.base.def.DataSourceType;
import team.sailboat.base.def.WorkEnv;
import team.sailboat.commons.fan.collection.IMultiMap;
import team.sailboat.commons.fan.dtool.DBHelper;
import team.sailboat.commons.fan.dtool.DBType;
import team.sailboat.commons.fan.text.XString;

public class DSHelper_JDBC
{
	static final Logger sLogger = LoggerFactory.getLogger(DSHelper_JDBC.class) ;
	
	public static javax.sql.DataSource getDataSource(ConnInfo_RDB aConnInfo , DataSourceType aDSType) throws Exception
	{
		return getDataSource(aConnInfo, aDSType, false) ;
	}
	
	public static javax.sql.DataSource getDataSource(ConnInfo_RDB aConnInfo , DataSourceType aDSType , boolean aUnmanage) throws Exception
	{
		javax.sql.DataSource dbSource = (javax.sql.DataSource) aConnInfo.getResource() ;
		if(dbSource == null)
		{
			synchronized (aConnInfo)
			{
				dbSource = (javax.sql.DataSource) aConnInfo.getResource() ;
				if(dbSource == null )
				{
					DBType dbType = DataSourceType.toDBType(aDSType) ;
					boolean autoCommit = false ;
					if(aDSType == DataSourceType.Hive)
					{
						autoCommit = true ;
					}
					
					Properties prop = null ;
					if(XString.isNotEmpty(aConnInfo.getParams()))
					{
						prop = IMultiMap.parseFromUrlParams(aConnInfo.getParams()).toPropertiesEx() ;
					}
					else
						prop = new Properties() ;
					if(!prop.containsKey("connectTimeout"))
						prop.setProperty("connectTimeout", "1000") ;
					
					String connStr = DBHelper.getConnStr(dbType , aConnInfo.getHost() , aConnInfo.getPort() 
							, aConnInfo.getDbName()
							, prop.getProperty("currentSchema")) ;

					DruidDataSource ds = new DruidDataSource() ;
					ds.setUrl(connStr) ;
			    	ds.setUsername(aConnInfo.getUsername()) ;
			    	ds.setPassword(aConnInfo.getPassword()) ;
			    	ds.setDriver((Driver) DBHelper.loadJDBC(dbType).getConstructor().newInstance()) ;
			    	ds.setDefaultAutoCommit(autoCommit) ;
			    	ds.setInitialSize(1);
			    	ds.setMinIdle(1) ;
			    	ds.setConnectProperties(prop) ;
			    	dbSource = ds ;
			    	if(!aUnmanage && aConnInfo.isManaged())
			    		aConnInfo.setResource(ds) ;
			    	sLogger.info("新建了一个连接{}的数据源。" , connStr) ;
				}
			} 
		}
		return dbSource ;
	}
	
	public static javax.sql.DataSource getDataSource(DataSource aDs , WorkEnv aEnv) throws Exception
	{
		ConnInfo_RDB connInfo = (ConnInfo_RDB) aDs.getConnInfo(aEnv) ;
		try
		{
			return getDataSource(connInfo, aDs.getType()) ;
		}
		finally
		{
			// DataSource的密码用过以后就丢弃，不一直拿着
			if(connInfo.isManaged())
				connInfo.setPassword(null);
		}
	}
	
	public static Connection getRDBConnection(ConnInfo_RDB aConnInfo , DataSourceType aDSType) throws Exception
	{
		return getDataSource(aConnInfo , aDSType).getConnection() ;
	}
	
	public static Connection getRDBConnection(DataSource aDs , WorkEnv aEnv) throws Exception
	{
		ConnInfo_RDB connInfo = (ConnInfo_RDB) aDs.getConnInfo(aEnv) ;
		try
		{
			return getRDBConnection(connInfo, aDs.getType()) ;
		}
		finally
		{
			if(connInfo.isManaged())
				connInfo.setPassword(null) ;
		}
	}
}
