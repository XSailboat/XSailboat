package team.sailboat.commons.fan.jquery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.sql.DataSource;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dtool.IDBTool;
import team.sailboat.commons.fan.dtool.RS2JSONObject;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.fan.struct.Wrapper;

class JQuery_JSONObjectRow extends JQueryBase implements JQueryJo
{
	protected Map<String, String> mCvtColumnNameMap ;
	
	Consumer<JSONObject> mRecordHandler ;
	
	boolean mColumnNameHumpFormat = false ;
	
	
	public JQuery_JSONObjectRow(DataSource aDataSource , String aBaseSql , Object...aArgs)
	{
		super(aDataSource) ;
		mSqlBld.append(aBaseSql) ;
		XC.addAll(mArgList , aArgs) ;
	}
	
	@Override
	public JQueryJo append(boolean aWhen, String aSqlSeg, Object... aArgs)
	{
		return (JQueryJo) super.append(aWhen, aSqlSeg, aArgs);
	}
	
	@Override
	public JQueryJo appendIn(boolean aWhen, String aSqlSeg, Object... aVals)
	{
		return (JQueryJo) super.appendIn(aWhen, aSqlSeg, aVals);
	}
	
	public JQueryJo append(String aSqlSeg)
	{
		return (JQueryJo)super.append(aSqlSeg) ;
	}
	
	@Override
	public JQueryJo appendOrderBy(boolean aWhen, Object... aArgs)
	{
		return (JQueryJo)super.appendOrderBy(aWhen, aArgs);
	}
	
	@Override
	public JQueryJo columnNameMap(String aOldName, String aNewName)
	{
		if(mCvtColumnNameMap == null)
			mCvtColumnNameMap = new HashMap<>() ;
		mCvtColumnNameMap.put(aOldName, aNewName) ;
		return this ;
	}
	
	@Override
	public JQueryJo columnNameMaps(Map<String, String> aColumnNameMap)
	{
		if(XC.isNotEmpty(aColumnNameMap))
		{
			if(mCvtColumnNameMap == null)
				mCvtColumnNameMap = aColumnNameMap ;
			else
				mCvtColumnNameMap.putAll(aColumnNameMap);
		}
		return this ;
	}
	
	@Override
	public JQueryJo columnNameHumpFormat(boolean aHumpFormat)
	{
		mColumnNameHumpFormat = aHumpFormat ;
		return this ;
	}
	
	@Override
	public JQueryJo recordHandler(Consumer<JSONObject> aConsumer)
	{
		mRecordHandler = aConsumer ;
		return this ;
	}
	
	@Override
	public void query(Predicate<JSONObject> aConsumerJo) throws SQLException
	{
		String sql = mSqlBld.toString() ;
		try(Connection conn = mDataSource.getConnection())
		{
			IDBTool dbTool = getDBTool(conn) ;
			
			dbTool.query(conn, sql, (rs)->{
				if(mRsmdConsumer != null)
					mRsmdConsumer.accept(rs.getMetaData()) ;
				RS2JSONObject cvt = new RS2JSONObject(rs.getMetaData() , mColumnNameHumpFormat , mCvtColumnNameMap) ;
				while(rs.next())
				{
					if(mEPredList != null)
					{
						try
						{
							if(doFilter(rs))
							{
								JSONObject jobj = cvt.apply(rs) ;
								if(mRecordHandler != null)
									mRecordHandler.accept(jobj) ;
								if(!aConsumerJo.test(jobj))
								{
									return ;
								}
							}
						}
						catch(InterruptedException e)
						{
							break ;
						}
						catch (Throwable e)
						{
							WrapException.wrapThrow(e) ;
						}
					}
					else
					{
						JSONObject jobj = cvt.apply(rs) ;
						if(mRecordHandler != null)
							mRecordHandler.accept(jobj) ;
						if(!aConsumerJo.test(jobj))
						{
							return ;
						}
					}
				}
			}, 1000 , mArgList.toArray()) ;
		}
	}
	
	@Override
	public JSONArray query(int aAmountLimit) throws SQLException
	{
		String sql = mSqlBld.toString() ;
		try(Connection conn = mDataSource.getConnection())
		{
			IDBTool dbTool = getDBTool(conn) ;
			final JSONArray result = new JSONArray() ;
			
			dbTool.query(conn, sql, (rs)->{
				if(mRsmdConsumer != null)
					mRsmdConsumer.accept(rs.getMetaData()) ;
				RS2JSONObject cvt = new RS2JSONObject(rs.getMetaData() , mColumnNameHumpFormat , mCvtColumnNameMap) ;
				while(rs.next())
				{
					if(mEPredList != null)
					{
						try
						{
							if(doFilter(rs))
							{
								JSONObject jobj = cvt.apply(rs) ;
								if(mRecordHandler != null)
									mRecordHandler.accept(jobj) ;
								result.put(jobj) ;
							}
						}
						catch(InterruptedException e)
						{
							break ;
						}
						catch (Throwable e)
						{
							WrapException.wrapThrow(e) ;
						}
					}
					else
					{
						JSONObject jobj = cvt.apply(rs) ;
						if(mRecordHandler != null)
							mRecordHandler.accept(jobj) ;
						result.put(jobj) ;
					}
					if(result.size() >= aAmountLimit)
						break ;
				}
			}, 1000 , mArgList.toArray());
			
			if(result.isNotEmpty() && mComparator != null)
				result.sort(mComparator) ;
			return result ;
		}
	}

	@Override
	public JSONArray query() throws SQLException
	{
		return query(Integer.MAX_VALUE) ;
	}
	
	@Override
	public JSONArray query(int aPageSize , int aPage) throws SQLException
	{
		String sql = mSqlBld.toString() ;
		try(Connection conn = mDataSource.getConnection())
		{
			IDBTool dbTool = getDBTool(conn) ;
			final JSONArray result = new JSONArray() ;
			
			final Wrapper<JSONObject> metaWrapper = mPageQueryMetaConsumer == null?null:new Wrapper<>() ;
			Wrapper<RS2JSONObject> cvtWrapper = new Wrapper<>() ;
			dbTool.queryPage(conn , sql, aPageSize, aPage 
				, (rsmd)->{
					if(mRsmdConsumer != null)
						mRsmdConsumer.accept(rsmd) ;
					cvtWrapper.set(new RS2JSONObject(rsmd , mColumnNameHumpFormat , mCvtColumnNameMap)) ;
				} , (rs)->{
					if(mEPredList != null)
					{
						try
						{
							if(doFilter(rs))
							{
								JSONObject jobj = cvtWrapper.get().apply(rs) ;
								if(mRecordHandler != null)
									mRecordHandler.accept(jobj) ;
								result.put(jobj) ;
							}
						}
						catch(InterruptedException e)
						{
						}
						catch (Throwable e)
						{
							WrapException.wrapThrow(e) ;
						}
					}
					else
					{
						JSONObject jobj = cvtWrapper.get().apply(rs) ;
						if(mRecordHandler != null)
							mRecordHandler.accept(jobj) ;
						result.put(jobj) ;
					}
				} , metaWrapper ,  mArgList.toArray()); 
			
			if(mPageQueryMetaConsumer != null)
				mPageQueryMetaConsumer.accept(metaWrapper.get()) ;
			
			if(result.isNotEmpty() && mComparator != null)
				result.sort(mComparator) ;
			return result ;
		}
	}

	@Override
	public JSONObject scrollQuery(int aSize, int aLifeCycleInSeconds) throws SQLException
	{
		String sql = mSqlBld.toString() ;
		if(aSize<=0)
			aSize = 2000 ;
		Connection conn = mDataSource.getConnection() ;
		getDBTool(conn) ;
		PreparedStatement pstm = null ;
		ResultSet rs = null ;
		try
		{
			pstm = conn.prepareStatement(sql , ResultSet.TYPE_FORWARD_ONLY , ResultSet.CONCUR_READ_ONLY) ;
			if(XC.isNotEmpty(mArgList))
			{
				if(mArgList.size() == 1 && mArgList.get(0) == null && !sql.contains("?"))
				{
					//do nothing
				}
				else
				{
					final int argSize = mArgList.size() ;
					for(int i=1 ; i<=argSize ; i++)
					{
						pstm.setObject(i, mArgList.get(i-1)) ;
					}
				}
			}
			pstm.setFetchSize(1000);
			rs =  pstm.executeQuery() ;
			if(mRsmdConsumer != null)
				mRsmdConsumer.accept(rs.getMetaData()) ;
			
			final RS2JSONObject cvt = new RS2JSONObject(rs.getMetaData() , mColumnNameHumpFormat , mCvtColumnNameMap) ;
			final Wrapper<Throwable> excepWrapper = new Wrapper<Throwable>() ;
			@SuppressWarnings("resource")
			JSONObject resultJo = new JScroll(conn, pstm, rs, aSize, (resultset , ja)->{
				try
				{
					if(mEPredList != null)
					{
						if(doFilter(resultset))
						{
							JSONObject jobj = cvt.apply(resultset) ;
							if(mRecordHandler != null)
								mRecordHandler.accept(jobj) ;
							ja.put(jobj) ;
						}
						else
							return false ;
					}
					else
					{
						JSONObject jobj = cvt.apply(resultset) ;
						if(mRecordHandler != null)
							mRecordHandler.accept(jobj) ;
						ja.put(jobj) ;
					}
				}
				catch(Throwable e)
				{
					excepWrapper.set(e) ;
					return false ;
				}
				return true ;
			} , mFac , mComparator).scrollNext(aSize) ;
			
			if(excepWrapper.get() != null)
			{
				if(excepWrapper.get() instanceof SQLException)
					throw (SQLException)excepWrapper.get() ;
				else
					WrapException.wrapThrow(excepWrapper.get()) ;
			}
			
			return resultJo ;
		}
		catch(Throwable e)
		{
			StreamAssist.closeAll(rs , pstm , conn) ;
			if(e instanceof SQLException)
				throw (SQLException)e ;
			else
				WrapException.wrapThrow(e) ;
			//dead code
			return null ;
		}
	}
	
}
