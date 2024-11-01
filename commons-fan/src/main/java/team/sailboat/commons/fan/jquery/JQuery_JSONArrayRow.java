package team.sailboat.commons.fan.jquery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.sql.DataSource;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dtool.IDBTool;
import team.sailboat.commons.fan.dtool.RS2JSONArray;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.fan.struct.Wrapper;

class JQuery_JSONArrayRow extends JQueryBase implements JQueryJa
{
	
	Consumer<JSONArray> mRecordHandler ;
	
	
	public JQuery_JSONArrayRow(DataSource aDataSource , String aBaseSql , Object...aArgs)
	{
		super(aDataSource) ;
		mSqlBld.append(aBaseSql) ;
		XC.addAll(mArgList , aArgs) ;
	}
	
	@Override
	public JQueryJa appendIn(boolean aWhen, String aSqlSeg, Object... aVals)
	{
		return (JQueryJa)super.appendIn(aWhen, aSqlSeg, aVals);
	}
	
	@Override
	public JQueryJa recordHandler(Consumer<JSONArray> aConsumer)
	{
		mRecordHandler = aConsumer ;
		return this ;
	}
	
	@Override
	public JSONArray query(int aAmountLimit) throws SQLException
	{
		String sql = mSqlBld.toString() ;
		try(Connection conn = mDataSource.getConnection())
		{
			IDBTool dbTool = getDBTool(conn) ;
			final JSONArray result = new JSONArray() ;
			
			QueryContext queryCtx = new QueryContext() ;
			mQueryContextTL.set(queryCtx) ;
			
			dbTool.queryLimit(conn, sql, (rs)->{
				if(mRsmdConsumer != null)
					mRsmdConsumer.accept(rs.getMetaData()) ;
				RS2JSONArray cvt = new RS2JSONArray(rs.getMetaData()) ;
				while(rs.next())
				{
					if(mEPredList != null)
					{
						try
						{
							if(doFilter(rs))
							{
								JSONArray ja = cvt.apply(rs) ;
								if(mRecordHandler != null)
									mRecordHandler.accept(ja) ;
								result.put(ja) ;
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
						JSONArray ja = cvt.apply(rs) ;
						if(mRecordHandler != null)
							mRecordHandler.accept(ja) ;
						result.put(ja) ;
					}
					if(result.size() >= aAmountLimit)
						break ;
				}
				queryCtx.setHasMore(rs.next());
			}, 1000 , 0 , aAmountLimit , mArgList.toArray());
			
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
			Wrapper<RS2JSONArray> cvtWrapper = new Wrapper<>() ;
			dbTool.queryPage(conn , sql, aPageSize, aPage 
				, (rsmd)->{
					if(mRsmdConsumer != null)
						mRsmdConsumer.accept(rsmd) ;
					cvtWrapper.set(new RS2JSONArray(rsmd)) ;
				} , (rs)->{
					if(mEPredList != null)
					{
						try
						{
							if(doFilter(rs))
							{
								JSONArray ja = cvtWrapper.get().apply(rs) ;
								if(mRecordHandler != null)
									mRecordHandler.accept(ja) ;
								result.put(ja) ;
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
						JSONArray ja = cvtWrapper.get().apply(rs) ;
						if(mRecordHandler != null)
							mRecordHandler.accept(ja) ;
						result.put(ja) ;
					}
				} , metaWrapper , mCareTotalAmount ,  mArgList.toArray()); 
			
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
			
			final RS2JSONArray cvt = new RS2JSONArray(rs.getMetaData()) ;
			final Wrapper<Throwable> excepWrapper = new Wrapper<Throwable>() ;
			@SuppressWarnings("resource")
			JSONObject resultJo = new JScroll(conn, pstm, rs, aSize, (resultset , ja)->{
				try
				{
					if(mEPredList != null)
					{
						if(doFilter(resultset))
						{
							JSONArray ja_0 = cvt.apply(resultset) ;
							if(mRecordHandler != null)
								mRecordHandler.accept(ja_0) ;
							ja.put(ja_0) ;
						}
						else
							return false ;
					}
					else
					{
						JSONArray ja_0 = cvt.apply(resultset) ;
						if(mRecordHandler != null)
							mRecordHandler.accept(ja_0) ;
						ja.put(ja_0) ;
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
	
	@Override
	public JQueryJa appendMsgFmt(boolean aWhen, String aSqlSeg, Object... aArgs)
	{
		return (JQueryJa) super.appendMsgFmt(aWhen, aSqlSeg, aArgs);
	}
	
	@Override
	public JQueryJa append(boolean aWhen, String aSqlSeg, Object... aArgs)
	{
		return (JQueryJa) super.append(aWhen, aSqlSeg, aArgs);
	}
	
	@Override
	public JQueryJa append(String aSqlSeg)
	{
		return (JQueryJa) super.append(aSqlSeg);
	}
	
	@Override
	public JQueryJa append(boolean aWhen, Supplier<String> aSqlSupplier, Object... aArgs)
	{
		return (JQueryJa) super.append(aWhen, aSqlSupplier, aArgs);
	}
	
	public JQueryJa appendOrderBy(boolean aWhen , Object... aArgs)
	{
		return (JQueryJa) super.appendOrderBy(aWhen, aArgs);
	}
}
