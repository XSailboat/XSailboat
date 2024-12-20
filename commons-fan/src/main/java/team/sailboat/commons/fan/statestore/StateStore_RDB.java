package team.sailboat.commons.fan.statestore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dtool.DBHelper;
import team.sailboat.commons.fan.dtool.DBType;
import team.sailboat.commons.fan.dtool.IDBTool;
import team.sailboat.commons.fan.dtool.TableSchema;
import team.sailboat.commons.fan.dtool.UpdateOrInsertKit;
import team.sailboat.commons.fan.dtool.mysql.MySQLConst;
import team.sailboat.commons.fan.dtool.mysql.MySQLFeatures;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.jquery.RDB_JQuery;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.struct.LongObject;
import team.sailboat.commons.fan.struct.Wrapper;
import team.sailboat.commons.fan.text.XString;

public class StateStore_RDB implements IStateStore
{
	DataSource mDs ;
	
	String mName ;
	
	String mTableName ;
	
	String mDomain ;
	
	IDBTool mDBTool ;
	
	String mQuerySql_1 ;
	
	String mQuerySql_n ;
	
	String mQuerySql_1_NoDomain ;
	
	String mInsertSql ;
	
	String mUpdateSql ;
	
	String mQuerySql_all ;
	
	String mDeleteAllSql ;
	
	boolean mCacheAllAndAsSingletonInstanceMode ;
	
	Map<String, LongObject<String>> mCacheMap ;
	
	public StateStore_RDB(DataSource aDs 
			, String aTableName
			, String aDomain
			, String aName) throws SQLException
	{
		mDs = aDs ;
		mName = aName ;
		mTableName = aTableName ;
		mDomain = aDomain ;
		mQuerySql_1 = "SELECT mkey , mvalue , domain , version , create_time , update_time"
				+ " FROM "+mTableName+" WHERE mkey=? AND domain = '"+mDomain+"'" ;
		mQuerySql_n = "SELECT mkey , mvalue , domain , version , create_time , update_time"
				+ " FROM "+mTableName+" WHERE mkey IN ({}) AND domain = '"+mDomain+"'" ;
		mQuerySql_1_NoDomain = "SELECT mkey , mvalue , domain , version , create_time , update_time"
				+ " FROM "+mTableName+" WHERE mkey=?" ;
		mInsertSql = "INSERT INTO "+mTableName+" (mkey , mvalue , domain , version) VALUES (?,?,?,?)" ;
		mUpdateSql = "UPDATE "+mTableName+" SET mvalue=? , version=? WHERE mkey=? AND domain=? AND version=?" ;
		mQuerySql_all = "SELECT mkey , mvalue , domain , version , create_time , update_time"
				+ " FROM "+mTableName+" WHERE domain = '"+mDomain+"'" ;
		mDeleteAllSql = "DELETE FROM "+mTableName+" WHERE domain = '"+mDomain+"'" ;
		_init() ;
	}
	
	void _init() throws SQLException
	{
		try(Connection conn = mDs.getConnection())
		{
			mDBTool = DBHelper.getDBTool(conn) ;
			
			if(!mDBTool.isTableExists(conn, mTableName, null))
			{
				TableSchema tblSchema = mDBTool.builder_tableSchema()
						.name(mTableName)
						.comment("状态存储器")
						.column("mkey")
							.dataType_vchar(128)
							.comment("主键")
						.and().column("mvalue")
							.dataType_vchar(2048)
							.comment("值")
						.and().column("domain")
							.dataType_vchar(256)
							.comment("域")
						.and().column("version")
							.dataType_long()
							.comment("版本")
						.and().column("create_time")
							.dataType_datetime_autocreate()
							.comment("创建时间")
						.and().column("update_time")
							.dataType_datetime_autoupdate()
							.comment("更新时间")								
						.and().index(mTableName+"pk_idx")
							.on("mkey" , "domain")
						.and()
						.featureFor(MySQLFeatures.TABLE__ENGINE, "InnoDB", DBType.MySQL)
						.featureFor(MySQLFeatures.TABLE__CHARACTER_SET, "utf8", DBType.MySQL)
						.featureFor(MySQLFeatures.TABLE__COLLATION 
								, MySQLConst.sTBL_FEATURE__COLLATION__VAL__utf8_general_ci 
								, DBType.MySQL)
						.build() ;
				mDBTool.createTables(conn , tblSchema) ;
			}
		}
	}
	
	
	UpdateOrInsertKit createInsertKit(Connection aConn) throws SQLException
	{
		UpdateOrInsertKit kit = mDBTool.createInsertKit(mTableName, new String[] {"mkey" , "mvalue" , "domain" , "version"}
			, new int[] {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR , Types.BIGINT} ) ;
		kit.prepare(aConn) ;
		return kit ;
	}
	
	UpdateOrInsertKit createUpdateKit(Connection aConn) throws SQLException
	{
		UpdateOrInsertKit kit = mDBTool.createUpdateKit(mTableName, new String[] {"mkey" , "mvalue" , "domain" , "version"}
			, new int[] {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR , Types.BIGINT} , 0 , 2 , 3) ;
		kit.prepare(aConn) ;
		return kit ;
	}
	
	@Override
	public LongObject<String> get(String aKey) throws SQLException
	{
		if(mCacheAllAndAsSingletonInstanceMode)
		{
			return mCacheMap.get(aKey) ;
		}
		else
		{
			try(Connection conn = mDs.getConnection())
			{
				Wrapper<LongObject<String>> wrapper = new Wrapper<LongObject<String>>() ;
				mDBTool.query(conn , mQuerySql_1 , (rs)->{
					if(rs.next())
						wrapper.set(new LongObject<String>(rs.getLong(4), rs.getString(2))) ;
				}, 1 , aKey);
				return wrapper.get() ;
			}
		}
	}
	
	@Override
	public Map<String, LongObject<String>> getAll(String... aKeys) throws Exception
	{
		if(XC.isEmpty(aKeys))
			return Collections.emptyMap() ;
		Map<String, LongObject<String>> map = XC.hashMap(Math.max((int)XC.count(aKeys) , 8)) ; 
		if(mCacheAllAndAsSingletonInstanceMode)
		{
			for(String key : aKeys)
			{
				LongObject<String> val = mCacheMap.get(key) ;
				if(val != null)
					map.put(key, val) ;
			}
			return map ;
		}
		else
		{
			try(Connection conn = mDs.getConnection())
			{
				mDBTool.query(conn , XString.msgFmt(mQuerySql_n , XString.repeat(",", '?', aKeys.length)) , (rs)->{
					while(rs.next())
						map.put(rs.getString(1), new LongObject<String>(rs.getLong(4), rs.getString(2))) ;
				}, 1000 , (Object[])aKeys);
				return map ;
			}
		}
	}
	
	@Override
	public void removeStates(Map<String, Long> aKeyVerMap) throws SQLException
	{
		if(XC.isEmpty(aKeyVerMap))
			return ;
		try(Connection conn = mDs.getConnection()
				; PreparedStatement pstm = conn.prepareStatement(mDeleteAllSql + " AND mkey=? AND version=?"))
		{
			for(Entry<String, Long> entry : aKeyVerMap.entrySet())
			{
				pstm.setString(1 , entry.getKey()) ;
				pstm.setLong(2 , entry.getValue()) ;
				pstm.addBatch(); 
			}
			pstm.executeBatch() ;
			conn.commit(); 
		}
	}
	
	@Override
	public JSONObject getRecords(String aPattern , int aMaxAmount) throws Exception
	{
		JSONObject columnsJo = new JSONObject() ;
		Wrapper<JSONObject> resultWrapper = new Wrapper<>() ;
		String sql = mQuerySql_all ;
		aPattern = XString.trim(aPattern) ;
		if(XString.isNotEmpty(aPattern))
		{
			sql += " AND mkey LIKE '%"+ aPattern.replaceAll(" +", "%")+"%'" ;
		}
		return (JSONObject)new RDB_JQuery(mDs).oneJa(sql)
				.careResultSetMetadata((rsmd)->{
					int len = rsmd.getColumnCount() ;
					for(int i=1 ; i<=len ; i++)
					{
						String name = rsmd.getColumnLabel(i) ;
						int j = name.indexOf('.') ; 
						name = j == -1?name:name.substring(j+1) ;
						String dataType = null ;
						try
						{
							dataType = IDBTool.convertTypeToCSN(rsmd.getColumnType(i)) ;
						}
						catch(SQLException e)
						{
							dataType = rsmd.getColumnTypeName(i) ;
						}
						String name_0 = null ;
						switch(name)
						{
						case "mkey":
							name_0 = "key" ;
							break ;
						case "mvalue":
							name_0 = "value" ;
							break ;
						default:
							name_0 = name ;
							break ;
						}
						columnsJo.put(name_0 , new JSONObject().put("dataType", dataType)
								.put("index", i-1)) ;
					}
				})
				.carePageQueryMeta((jobj)->{
					resultWrapper.set(jobj) ;
				})
				.resultFactory((ja)->{
					if(resultWrapper.isNull())
						resultWrapper.set(new JSONObject()) ;
					return resultWrapper.get().put("data", ja)
							.put("columns" , columnsJo) ;
				})
				.queryPageCustom(aMaxAmount , 0) ;
	}
	
	@Override
	public Map<String, LongObject<String>> getAllCrossDomain(String aKey) throws Exception
	{
		try(Connection conn = mDs.getConnection())
		{
			Map<String , LongObject<String>> map = XC.hashMap() ;
			mDBTool.query(conn , mQuerySql_1_NoDomain , (rs)->{
				while(rs.next())
					map.put(rs.getString(3), new LongObject<String>(rs.getLong(4), rs.getString(2))) ;
			}, 10 , aKey);
			return map ;
		}
	}
	
	@Override
	public Collection<String> putAll_expect(Map<String, LongObject<String>> aMap) throws Exception
	{
		if(XC.isNotEmpty(aMap))
		{
			try(Connection conn = mDs.getConnection())
			{
				List<String> insertList = XC.arrayList() ;
				List<String> updateList = XC.arrayList() ;
				for(Entry<String, LongObject<String>> entry : aMap.entrySet())
				{
					Long expectVersion = entry.getValue().getP() ;
					if(expectVersion == null || expectVersion.longValue()<=0)
					{
						insertList.add(entry.getKey()) ;
					}
					else
					{
						updateList.add(entry.getKey()) ;
					}
				}
				if(!insertList.isEmpty())
				{
					// INSERT
					try(PreparedStatement pstm = conn.prepareStatement(mInsertSql))
					{
						for(String key : insertList)
						{
							LongObject<String> v = aMap.get(key) ;
							pstm.setString(1, key) ;
							pstm.setString(2, v.getObject()) ;
							pstm.setString(3, mDomain) ;
							pstm.setLong(4, 1L) ;
							pstm.addBatch() ;
						}
						pstm.executeBatch() ;
						conn.commit();
						if(mCacheAllAndAsSingletonInstanceMode)
						{
							for(String key : insertList)
								mCacheMap.put(key, new LongObject<String>(1L, aMap.get(key).getObject())) ;
						}
					}
				}
				if(!updateList.isEmpty())
				{
					try(PreparedStatement pstm = conn.prepareStatement(mUpdateSql))
					{
						for(String key : updateList)
						{
							LongObject<String> v = aMap.get(key) ;
							pstm.setString(1, v.getObject()) ;
							pstm.setLong(2, v.getP()+1) ;
							pstm.setString(3, key) ;
							pstm.setString(4, mDomain) ;
							pstm.setLong(5, v.getP()) ;
							pstm.addBatch(); 
						}
						List<String> unupdateKeys = XC.arrayList() ;
						int[] results = pstm.executeBatch() ;
						conn.commit();
						for(int i=0 ; i<results.length ; i++)
						{
							if(results[i]<1)
							{
								String key = updateList.get(i) ;
								unupdateKeys.add(key) ;
								if(mCacheAllAndAsSingletonInstanceMode)
								{
									LongObject<String> v1 = aMap.get(key) ;
									mCacheMap.put(key
											, new LongObject<String>(v1.getP()+1 , v1.getObject())) ;
								}
							}
						}
						return unupdateKeys ;
					}
				}
			}
		}
		return Collections.emptyList() ;
	}

	@Override
	public boolean put(String aKey, String aValue, Long aExpectVersion) throws SQLException
	{	
		try(Connection conn = mDs.getConnection())
		{
			if(aExpectVersion == null || aExpectVersion.longValue()<=0)
			{
				// INSERT
				try(PreparedStatement pstm = conn.prepareStatement(mInsertSql))
				{
					pstm.setString(1, aKey) ;
					pstm.setString(2, aValue) ;
					pstm.setString(3, mDomain) ;
					pstm.setLong(4, 1L) ;
					pstm.execute() ;
					conn.commit();
					if(mCacheAllAndAsSingletonInstanceMode)
						mCacheMap.put(aKey, new LongObject<String>(1L, aValue)) ;
					return true ;
				}
			}
			else
			{
				// UPDATE
				try(PreparedStatement pstm = conn.prepareStatement(mUpdateSql))
				{
					pstm.setString(1, aValue) ;
					pstm.setLong(2, aExpectVersion+1) ;
					pstm.setString(3, aKey) ;
					pstm.setString(4, mDomain) ;
					pstm.setLong(5, aExpectVersion) ;
					boolean rc = pstm.executeUpdate() >0 ;
					conn.commit();
					if(rc && mCacheAllAndAsSingletonInstanceMode)
					{
						mCacheMap.put(aKey, new LongObject<String>(aExpectVersion+1, aValue)) ;
					}
					return rc ;
				}
			}
		}
	}
	
	@Override
	public void clear() throws Exception
	{
		try(Connection conn = mDs.getConnection()
				; Statement stm = conn.createStatement())
		{
			stm.execute(mDeleteAllSql) ;
			conn.commit();
			if(mCacheMap != null)
				mCacheMap.clear(); 
		}
	}
	
	@Override
	public synchronized boolean cacheAllAndAsSingletonInstanceMode()
	{
		if(!mCacheAllAndAsSingletonInstanceMode)
		{
			Map<String, LongObject<String>> cacheMap = new ConcurrentHashMap<String, LongObject<String>>() ;
			//
			try
			{
				DBHelper.executeQuery(mDs.getConnection() , mQuerySql_all, (rs)->{
					cacheMap.put(rs.getString(1), new LongObject<String>(rs.getLong(4), rs.getString(2))) ;
				}) ;
			}
			catch(Exception e)
			{
				WrapException.wrapThrow(e) ;
			}
			mCacheMap = cacheMap ;
			// 缓存好了，再开启
			mCacheAllAndAsSingletonInstanceMode = true ;
			return true ;
		}
		return false ;
	}
	
	@Override
	public boolean isCacheAllAndAsSingletonInstanceMode()
	{
		return mCacheAllAndAsSingletonInstanceMode ;
	}

	@Override
	public String toString()
	{
		return getClass().getName() +"["+mDomain+" - "+mName+"]" ;
	}
}
