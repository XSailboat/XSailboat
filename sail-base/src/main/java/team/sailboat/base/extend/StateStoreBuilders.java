package team.sailboat.base.extend;

import java.sql.Driver;

import com.alibaba.druid.pool.DruidDataSource;

import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.dtool.DBHelper;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.statestore.IStateStore;
import team.sailboat.commons.fan.statestore.IStateStoreBuilder;
import team.sailboat.commons.fan.statestore.IStateStoreBuilder.IRDBStateStoreBuilder;
import team.sailboat.commons.fan.statestore.StateStore_RDB;

public class StateStoreBuilders
{
	public static IRDBStateStoreBuilder ofRdb()
	{
		return new RDBStateStoreBuilder() ;
	}
	
	public static IStateStoreBuilder newStateStore(PropertiesEx aPropEx)
	{
		String className = aPropEx.getString("state-store.className") ;
		if(StateStore_RDB.class.getName().equals(className))
		{
			return StateStoreBuilders.ofRdb().connUrl(aPropEx.getString("state-store.connUrl"))
					.username(aPropEx.getString("state-store.username"))
					.password(aPropEx.getString("state-store.password"))
					.tableName(aPropEx.getString("state-store.tableName"));
		}
		throw new IllegalStateException("未支持的StateStore类型："+className) ;
	}

	public static class RDBStateStoreBuilder implements IRDBStateStoreBuilder
	{

		String mConnUrl ;
		
		String mUsername ;
		
		String mPassword ;
		
		String mTableName ;
		

		@Override
		public IRDBStateStoreBuilder connUrl(String aConnUrl)
		{
			mConnUrl = aConnUrl ;
			return this ;
		}

		@Override
		public IRDBStateStoreBuilder username(String aUsername)
		{
			mUsername = aUsername ;
			return this ;
		}

		@Override
		public IRDBStateStoreBuilder password(String aPassword)
		{
			mPassword = aPassword ;
			return this ;
		}

		@Override
		public IRDBStateStoreBuilder tableName(String aTableName)
		{
			Assert.notEmpty(aTableName , "状态存储器的数据库表名不能为空!");
			mTableName = aTableName ;
			return this ;
		}
		
		@SuppressWarnings("resource")
		@Override
		public IStateStore build(String aDomainName) throws Exception
		{
			DruidDataSource ds = new DruidDataSource() ;
	    	ds.setUrl(mConnUrl) ;
	    	ds.setUsername(mUsername) ;
	    	ds.setPassword(mPassword) ;
	    	ds.setDefaultAutoCommit(false) ;
	    	try
			{
				ds.setDriver((Driver) DBHelper.loadJDBC(DBHelper.getDBType(mConnUrl))
						.getConstructor().newInstance()) ;
			}
			catch (InstantiationException | IllegalAccessException | ClassNotFoundException e)
			{
				throw new IllegalStateException(e) ;
			}
			return new StateStore_RDB(ds , mTableName , aDomainName , ds.getUrl()) ;
		}
		
	}
}
