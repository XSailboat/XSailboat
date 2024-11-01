package team.sailboat.commons.fan.jquery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.UUID;
import java.util.function.BiPredicate;

import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.gadget.IScrollQuery;
import team.sailboat.commons.fan.gadget.ScrollQuerySite;
import team.sailboat.commons.fan.infc.EFunction2;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.serial.StreamAssist;

public class JScroll implements IScrollQuery<ResultSet>
{
	

	Connection mConn ;
	PreparedStatement mPStm ;
	ResultSet mRs ;
	
	EFunction2<JSONArray , QueryContext, Object, SQLException> mFac ;
	Comparator<Object> mComparator ;

	String mHandle ;
	int mMaxSize ;
	
	BiPredicate<ResultSet , JSONArray> mPred ;
	
	boolean mLookAhead = false ;

	
	public JScroll(Connection aConn , PreparedStatement aPstm , ResultSet aRs , int aMaxSize
			, BiPredicate<ResultSet , JSONArray> aPred
			, EFunction2<JSONArray, QueryContext , Object, SQLException> aFac
			, Comparator<Object> aComparator)
	{
		mConn = aConn ;
		mPStm = aPstm ;
		mRs = aRs ;
		mMaxSize = aMaxSize<=0?500:aMaxSize ;
		mPred = aPred ;
		mFac = aFac ;
		mComparator = aComparator ;
	}

	@Override
	public void close()
	{
		StreamAssist.closeAll(mRs , mPStm , mConn) ; 
	}

	@Override
	public JSONObject scrollNext(int aMaxSize)
	{
		int count = 0 ;
		String handle = null ;
		if(aMaxSize<=0)
			aMaxSize = mMaxSize ;
		JSONArray ja = new JSONArray() ;
		try
		{
			while(mLookAhead || mRs.next())
			{
				if(count++>=aMaxSize)
				{
					handle = UUID.randomUUID().toString() ;
					mLookAhead = true ; 
					break ;
				}
				mLookAhead = false ;
				if(!mPred.test(mRs , ja))
				{
					mHandle = null ;
					return null ;
				}
			}
		}
		catch (Exception e)
		{
			WrapException.wrapThrow(e) ;
		}
		
		if(mComparator != null && ja.isNotEmpty())
			ja.sort(mComparator) ;
		
		Object data = ja ;
		JSONObject resultJo = null ;
		if(mFac != null)
		{
			try
			{
				Object genObj = mFac.apply(ja , null) ;
				if(!(genObj instanceof JSONObject))
				{
					data = genObj ;
				}
				else
				{
					resultJo = (JSONObject)genObj ;
				}
			}
			catch (Exception e)
			{
				WrapException.wrapThrow(e) ;
			}
		}
		
		mHandle = handle ;
		if(handle != null)
			ScrollQuerySite.getInstance().cacheScrollQuery(this) ;
		else
			close();
		
		if(resultJo == null)
			resultJo = new JSONObject() ;
		
		return resultJo.put("data", data)
				.put("returnAmount", ja.size())
				.put("handle", mHandle)
				.put("hasMore", mHandle != null) ;
	}

	@Override
	public String getHandle()
	{
		return mHandle ;
	}

}
