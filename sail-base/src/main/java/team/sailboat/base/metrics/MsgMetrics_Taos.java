package team.sailboat.base.metrics;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dtool.DBHelper;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.exec.CommonExecutor;
import team.sailboat.commons.fan.jquery.JSqlBuilder;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.fan.time.XTime;

/**
 * 
 *
 * @author yyl
 * @since 2024年1月5日
 */
public class MsgMetrics_Taos implements IMetricsRW<Metrics_Msg , TimeString> , IMetricsConst
{
	
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	String mDBName ;
	String mTableName ;
	
	DataSource mTdDB ;
	
	boolean mDisposed = false ;
	
	final List<Metrics_Msg> mValueList = XC.arrayList() ;
	
	public MsgMetrics_Taos(DataSource aTdDB , String aDBName
			, String aTableName)
	{
		mTdDB = aTdDB ;
		mDBName = aDBName ;
		mTableName = aTableName ;
		_init();
	}
	
	void _init()
	{
		// 连接TDengine
		try(Connection conn = mTdDB.getConnection())
		{
			try(Statement stm = conn.createStatement())
			{
				// 创建数据库
				// 最近7天的高密度数据
				stm.addBatch("CREATE DATABASE IF NOT EXISTS "+ mDBName +" KEEP 366d DURATION 7d") ;	// 3.x
				stm.addBatch(XString.msgFmt("CREATE STABLE IF NOT EXISTS {}.{} (ts TIMESTAMP , val NCHAR(8192))"
							+" TAGS(name NCHAR(128) , category NCHAR(32) , source NCHAR(32))"
						, mDBName , mTableName)) ;
				stm.executeBatch() ;
				conn.commit();
				mLogger.info("已经确保表{}.{}存在！" , mDBName , mTableName) ;
			}
		}
		catch (SQLException e)
		{
			mLogger.error(ExceptionAssist.getClearMessage(getClass(), e)) ;
		}
		CommonExecutor.execInSelfThread(()->{
			
			while(!mDisposed)
			{
				long startTime = System.currentTimeMillis() ;
				final int size = mValueList.size() ;
				if(size > 0)
				{
					Metrics_Msg[] metrics = null ;
					synchronized (mValueList)
					{
						List<Metrics_Msg> subList = mValueList.subList(0, size) ;
						metrics = subList.toArray(new Metrics_Msg[0]) ;
						subList.clear();
					}
					try(Connection conn = mTdDB.getConnection() ; Statement stm = conn.createStatement())
					{
						for(Metrics_Msg result : metrics)
						{
							String value = result.getValue() ;
							stm.addBatch(XString.msgFmt("INSERT INTO {}.`{}` USING {}.{} TAGS('{}' , '{}' , '{}') VALUES('{}' , {})"
								, mDBName , result.getItem()
								, mDBName , mTableName
								, result.getName()
								, result.getCategory()
								, result.getSource()
								, XTime.format$yyyyMMddHHmmssSSS(result.getTs() , "1970-01-01 00:00:00.000")
								, value==null?"null":("'"+value.replace("'", "\\'")+"'"))) ;
						}
						stm.executeBatch() ;
					}
					catch(Exception e)
					{
						mLogger.error(ExceptionAssist.getClearMessage(MsgMetrics_Taos.class , e)) ;
					}
				}
				long diff = System.currentTimeMillis() - startTime ;
				if(diff < 500)
					JCommon.sleep((int)(1000-diff)) ;
			}
			
		}, "状态度量数据提交");
	}
	
	@Override
	public List<TimeString> getLatest(String aItem, int aAmountLimit) throws Exception
	{
		List<TimeString> resultList = XC.arrayList() ;
		if(aAmountLimit <= 1)
		{
			StringBuilder sqlBld = new StringBuilder("SELECT LAST_ROW(ts , val) FROM ")
					.append(mDBName).append(".`").append(mTableName)
					.append("` WHERE tbname = ?") ;
			
			// 查询
			DBHelper.executeQuery(mTdDB , sqlBld.toString() , rs->{
				resultList.add(new TimeString(rs.getTimestamp(1).getTime()
						, rs.getString(2))) ;
			}, 1000 , aItem) ;
		}
		else
		{
			StringBuilder sqlBld = new StringBuilder("SELECT ts , val FROM ")
					.append(mDBName).append(".`").append(mTableName)
					.append("` WHERE tbname = ? ORDER BY ts DESC LIMIT " + aAmountLimit) ;
			
			// 查询
			DBHelper.executeQuery(mTdDB , sqlBld.toString() , rs->{
				resultList.add(new TimeString(rs.getTimestamp(1).getTime()
						, rs.getString(2))) ;
			}, 1000 , aItem) ;
		}
		return resultList ;
	}
	
	@Override
	public List<TimeString> getValues(String aItem, Date aStartTime, Date aEndTime) throws Exception
	{
		StringBuilder sqlBld = new StringBuilder("SELECT tbname , ts , val FROM ")
				.append(mDBName).append(".`").append(mTableName)
				.append("` WHERE tbname = ? AND ts >= ? AND ts < ?") ;
		List<TimeString> resultList = XC.arrayList() ;
		// 查询
		DBHelper.executeQuery(mTdDB , sqlBld.toString() , rs->{
			resultList.add(new TimeString(rs.getTimestamp(2).getTime()
					, rs.getString(3))) ;
		}, 1000 , aItem , aStartTime , aEndTime) ;
		return resultList ;
	}
	
	@Override
	public List<TimeInt> getValues(String aItem, Date aStartTime, Date aEndTime
			, String aInterval, AggOperator aOperator) throws Exception
	{
		switch(aOperator)
		{
		case avg:
		case sum:
		case max:
			throw new IllegalStateException("Msg度量不支持的操作："+aOperator) ;
		default:
			break ;
		}
		StringBuilder sqlBld = new StringBuilder("SELECT tbname , ts , ")
				.append(aOperator.name()).append("(val) FROM ")
				.append(mDBName).append(".`").append(mTableName)
				.append("` WHERE tbname = ? AND ts >= ? AND ts < ?")
				.append(" PARTITION BY tbname , INTERVAL(").append(aInterval).append(")") ;
		List<TimeInt> resultList = XC.arrayList() ;
		// 查询
		DBHelper.executeQuery(mTdDB , sqlBld.toString() , rs->{
			resultList.add(new TimeInt(rs.getTimestamp(2).getTime()
					, rs.getInt(3))) ;
		}, 1000 , aItem , aStartTime , aEndTime) ;
		return resultList ;
	}
	
	@Override
	public Map<String, List<TimeString>> getValues(String[] aItems, Date aStartTime, Date aEndTime) throws Exception
	{
		JSqlBuilder sqlBld = JSqlBuilder.one("SELECT tbname , ts , val FROM ")
				.append(mDBName).append(".`").append(mTableName)
				.append("` WHERE ")
				.appendIn(true, "tbname IN ({})", (Object[])aItems)
				.append(true , "AND ts >= ? AND ts < ?" , aStartTime , aEndTime) ;
		Map<String, List<TimeString>> map = XC.hashMap() ;
		// 查询
		DBHelper.executeQuery(mTdDB , sqlBld.toString() , rs->{
			String tbName = rs.getString(1) ;
			List<TimeString> list = map.get(tbName) ;
			if(list == null)
			{
				list = XC.arrayList() ;
				map.put(tbName , list) ;
			}
			list.add(new TimeString(rs.getTimestamp(2).getTime() , rs.getString(3))) ;
		}, 1000 , sqlBld.getArgs()) ;
		return map ;
	}
	
	@Override
	public Map<String, List<TimeObject>> getValues(String[] aItems, Date aStartTime, Date aEndTime, String aInterval, AggOperator aOperator)
					throws Exception
	{
		switch(aOperator)
		{
		case count:
			break ;
		default:
			throw new IllegalStateException("Msg度量不支持的操作："+aOperator) ;
		}
		JSqlBuilder sqlBld = JSqlBuilder.one("SELECT tbname , ts , ")
				.append(aOperator.name()).append("(val) FROM ")
				.append(mDBName).append(".`").append(mTableName)
				.append("` WHERE ")
				.appendIn(true, "tbname IN ({})", (Object[])aItems)
				.append(true , "AND ts >= ? AND ts < ?" , aStartTime , aEndTime)
				.append(" PARTITION BY tbname , INTERVAL(").append(aInterval).append(")") ;
		Map<String, List<TimeObject>> map = XC.hashMap() ;
		// 查询
		DBHelper.executeQuery(mTdDB , sqlBld.toString() , rs->{
			String tbName = rs.getString(1) ;
			List<TimeObject> list = map.get(tbName) ;
			if(list == null)
			{
				list = XC.arrayList() ;
				map.put(tbName , list) ;
			}
			list.add(new TimeInt(rs.getTimestamp(2).getTime() , rs.getInt(3))) ;
		}, 1000 , sqlBld.getArgs()) ;
		return map ;
	}
	
	@Override
	public Map<String , TimeString> getLatest(Collection<String> aItems) throws SQLException
	{
		StringBuilder sqlBld = new StringBuilder("SELECT tbname , LAST_ROW(ts , val) FROM ")
				.append(mDBName).append(".`").append(mTableName).append("` WHERE tbname IN(")  ;
		int i= 0 ;
		String sqlHead = sqlBld.toString() ;
		Map<String ,TimeString> result = XC.hashMap() ;
		for(String item : aItems)
		{
			if(i > 0)
				sqlBld.append(" , ") ;
			sqlBld.append("'").append(item).append('\'') ;
			if(++i >= 100)
			{
				sqlBld.append(")") ;
				// 查询
				DBHelper.executeQuery(mTdDB , sqlBld.toString() , rs->{
					result.put(rs.getString(1), new TimeString(rs.getTimestamp(2).getTime()
							, rs.getString(3))) ;
				}) ;
				i = 0 ;
				sqlBld = new StringBuilder(sqlHead) ;
			}
		}
		if(i > 0)
		{
			sqlBld.append(") GROUP BY tbname") ;
			// 查询
			DBHelper.executeQuery(mTdDB , sqlBld.toString() , rs->{
				result.put(rs.getString(1), new TimeString(rs.getTimestamp(2).getTime()
						, rs.getString(3))) ;
			}) ;
		}
		return result ;
	}

	@Override
	public void store(Metrics_Msg... aMetrics)
	{
		if(XC.isNotEmpty(aMetrics))
		{
			synchronized (mValueList)
			{
				XC.addAll(mValueList , aMetrics) ;
			}
		}
	}
}
