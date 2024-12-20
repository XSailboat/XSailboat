package team.sailboat.ms.ac.component;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dtool.DBHelper;
import team.sailboat.commons.fan.dtool.DBType;
import team.sailboat.commons.fan.dtool.IDBTool;
import team.sailboat.commons.fan.dtool.TableSchema;
import team.sailboat.commons.fan.dtool.UpdateOrInsertKit;
import team.sailboat.commons.fan.dtool.mysql.MySQLConst;
import team.sailboat.commons.fan.dtool.mysql.MySQLFeatures;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.exec.CommonExecutor;
import team.sailboat.commons.fan.exec.DefaultAutoCleaner;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.struct.XInt;
import team.sailboat.commons.fan.time.XTime;
import team.sailboat.commons.fan.time.XTimeUnit;
import team.sailboat.ms.ac.data.VisitTimes;

/**
 * 
 * 访问统计
 *
 * @author yyl
 * @since 2024年11月26日
 */
@Component
public class AccessStatistics
{
	static final String sTableName = "visit_times" ;
	
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	@Qualifier("sysDB")
	@Autowired
    DataSource mSysDB ;
	
	/**
	 * 最近30天的访问次数统计缓存			<br />
	 * 超过30天的缓存数据，会自动清理
	 */
	final TreeMap<String, VisitTimes> mVisitTimesMap = XC.treeMap() ;
	
	/**
	 * 30天某个人的访问次数				<br />
	 * 访问的各个应用次数之和
	 * 键是用户id
	 */
	final Map<String, XInt> mUserTimes30d = XC.concurrentHashMap() ;
	
	final Set<String> mChangedSet = XC.hashSet() ;
	
	final long mOneDay = XTimeUnit.DAY.toMillis(1) ;
	
	long mTodayStartMS = XTime.today().getTime() ;
	String mTodayStr = XTime.format$yyyyMMdd(XTime.today()) ;
	
	DefaultAutoCleaner mStsCommitter ;
	
	public AccessStatistics()
	{
	}
	
	@PostConstruct
	void _init()
	{
		// 检查表是否存在，不存在的话创建
		try(Connection conn = mSysDB.getConnection())
		{
			IDBTool dbTool = DBHelper.getDBTool(conn) ;
			if(!dbTool.isTableExists(conn, sTableName, null))
			{
				TableSchema tblSchema = dbTool.builder_tableSchema()
						.name(sTableName)
						.comment("访问次数统计")
						.column("user_id")
							.dataType_vchar(32)
							.comment("用户id")
						.and().column("app_id")
							.dataType_vchar(32)
							.comment("应用Id")
						.and().column("sts_date")
							.dataType_datetime()
							.comment("统计日期")
						.and().column("times")
							.dataType_int(11)
							.comment("访问次数")
						.and().withPrimaryKey("sts_date" , "app_id" , "user_id")
						.featureFor(MySQLFeatures.TABLE__ENGINE, "InnoDB", DBType.MySQL)
						.featureFor(MySQLFeatures.TABLE__CHARACTER_SET, "utf8", DBType.MySQL)
						.featureFor(MySQLFeatures.TABLE__COLLATION 
								, MySQLConst.sTBL_FEATURE__COLLATION__VAL__utf8_general_ci 
								, DBType.MySQL)
						.build() ;
				dbTool.createTables(conn , tblSchema) ;
			}
			// 读取最近30天的访问量统计数据
			Date startDate = XTime.plusDays(XTime.today() , -29) ;
			DBHelper.executeQuery(conn, "SELECT * FROM "+sTableName+" WHERE sts_date > ? ORDER BY sts_date ASC", (rs)->{
				String dateStr = XTime.format$yyyyMMdd(rs.getTimestamp("sts_date")) ;
				record(dateStr, rs.getString("app_id") , rs.getString("user_id") , rs.getInt("times")) ;
			} , 1000 , startDate);
			
			CommonExecutor.exec(()->{
				for(VisitTimes vt : mVisitTimesMap.values())
				{
					vt.forEachUserTimes((userId , times)->{
						XC.getOrPut(mUserTimes30d , userId , XInt::new).plus(times.get()) ;
					}) ;
				}
			}) ;
			
			mStsCommitter = new DefaultAutoCleaner(2, ()->{
				if(XTime.pass(mTodayStartMS, mOneDay))
				{
					Date today = XTime.today() ;
					mTodayStr = XTime.format$yyyyMMdd(today) ;
					mTodayStartMS = today.getTime() ;
					// 把三十天之前的移除一下
					
					var subMap = mVisitTimesMap.subMap("1970-01-01" , XTime.format$yyyyMMdd(XTime.plusDays(today, -29))) ;
					subMap.values().forEach(vt->vt.forEachUserTimes((userId , times)->{
						mUserTimes30d.get(userId).substract(times.get()) ;
					})) ;
					subMap.clear(); 
				}
				if(!mChangedSet.isEmpty())
				{
					String[] changeds = null ;
					synchronized (mChangedSet)
					{
						changeds = mChangedSet.toArray(JCommon.sEmptyStringArray) ;
						mChangedSet.clear(); 
					}
					UpdateOrInsertKit kit = dbTool.createUpdateOrInsertKit(sTableName, new String[] {"sts_date" , "app_id" , "user_id" , "times"} 
						,  new String[] {XClassUtil.sCSN_DateTime , XClassUtil.sCSN_String , XClassUtil.sCSN_String , XClassUtil.sCSN_Integer} 
						,  0 , 1, 2) ;
					try(Connection conn1 = mSysDB.getConnection())
					{
						kit.prepare(conn1);
						for(String changed : changeds)
						{
							JSONArray ja = new JSONArray(changed) ;
							String dateStr = ja.optString(0) ;
							String appId = ja.optString(1) ;
							String userId = ja.optString(2) ;
							VisitTimes vt = mVisitTimesMap.get(dateStr) ;
							XInt times = vt.mAppUserTimes.get(appId , userId) ;
							if(times != null)
							{
								kit.add(XTime.parse$yyyyMMdd(dateStr) , appId , userId , times.get()) ;
							}
						}
						kit.finish() ;
					}
					catch (SQLException | ParseException e)
					{
						mLogger.error(ExceptionAssist.getClearMessage(getClass(), e)) ;
					}
				}
			}) ;
		}
		catch (SQLException e)
		{
			mLogger.error(ExceptionAssist.getClearMessage(getClass(), e)) ;
		}
	}
	
	/**
	 * 
	 * 记录某天、某个人访问某个应用多少次		<br />
	 * 如果原来有数据，将在原来的基础上累加
	 * 
	 * @param aDateStr
	 * @param aAppId
	 * @param aUserId
	 * @param aTimes
	 */
	void record(String aDateStr , String aAppId , String aUserId , int aTimes)
	{
		VisitTimes vt = mVisitTimesMap.get(aDateStr) ;
		if(vt == null)
		{
			vt = new VisitTimes(aDateStr) ;
			mVisitTimesMap.put(aDateStr, vt) ;
		}
		vt.record(aAppId, aUserId, aTimes) ;
	}
	
	/**
	 * 
	 * 记录一次当前的应用访问
	 * 
	 * @param aAppId
	 * @param aUserId
	 */
	public void recordOneVisit(String aAppId , String aUserId)
	{
		String dateStr = mTodayStr ;
		record(dateStr, aAppId, aUserId, 1) ;
		synchronized (mChangedSet)
		{
			XC.getOrPut(mUserTimes30d , aUserId , XInt::new).getAndIncrement() ;
			mChangedSet.add(new JSONArray().put(dateStr)
					.put(aAppId)
					.put(aUserId)
					.toJSONString()) ;
		}
	}
	
	/**
	 * 
	 * 取得近30天的访问次数数据
	 * 
	 * @return
	 */
	public List<VisitTimes> getLatest30d()
	{
		return new ArrayList<>(mVisitTimesMap.values()) ;
	}
	
	/**
	 * 
	 * 取得某天的总访问次数数据
	 * 
	 * @param aDateStr
	 * @param aDefaultTimes
	 * @return
	 */
	public int getTotalTimes(String aDateStr , int aDefaultTimes)
	{
		VisitTimes vt = mVisitTimesMap.get(aDateStr) ;
		return vt == null?aDefaultTimes:vt.getTotalTimes() ;
	}
	
	/**
	 * 
	 * 取得某天、某个应用的访问次数数据
	 * 
	 * @param aDateStr
	 * @param aAppId
	 * @param aDefaultTimes
	 * @return
	 */
	public int getAppTimes(String aDateStr , String aAppId , int aDefaultTimes)
	{
		VisitTimes vt = mVisitTimesMap.get(aDateStr) ;
		return vt == null?aDefaultTimes:vt.getAppTimes(aAppId, aDefaultTimes) ;
	}
	
	/**
	 * 
	 * 取得某天某个用户的访问次数数据
	 * 
	 * @param aDateStr
	 * @param aUserId
	 * @param aDefaultTimes
	 * @return
	 */
	public int getUserTimes(String aDateStr , String aUserId , int aDefaultTimes)
	{
		VisitTimes vt = mVisitTimesMap.get(aDateStr) ;
		return vt == null?aDefaultTimes:vt.getUserTimes(aUserId, aDefaultTimes) ;
	}
	
	/**
	 * 
	 * 取得某天、某人访问某个应用的次数
	 * 
	 * @param aDateStr
	 * @param aAppId
	 * @param aUserId
	 * @param aDefaultTimes
	 * @return
	 */
	public int getAppUserTimes(String aDateStr , String aAppId , String aUserId , int aDefaultTimes)
	{
		VisitTimes vt = mVisitTimesMap.get(aDateStr) ;
		return vt == null?aDefaultTimes:vt.getAppUserTimes(aAppId , aUserId, aDefaultTimes) ;
	}
	
	/**
	 * 
	 * 取得用户最近30天的访问次数
	 * 
	 * @return
	 */
	public Map<String , XInt> getUserVisitTimes30d()
	{
		return mUserTimes30d ;
	}
	
	/**
	 * 
	 * 取得各个用户最近的登录日期		<br />
	 * 没有精确到时分秒
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Map<String , String> getUsersLatestLoginTime() throws SQLException
	{
		try(Connection conn = mSysDB.getConnection())
		{
			Map<String , String> map = XC.hashMap() ;
			DBHelper.executeQuery(conn , "SELECT user_id , MAX(sts_date) AS latest_sts_date FROM "+sTableName + " GROUP BY user_id" 
					, rs->{
						map.put(rs.getString(1) , XTime.format$yyyyMMdd(rs.getTimestamp(2))) ;
					}) ;
			return map ;
		}
	}
}
