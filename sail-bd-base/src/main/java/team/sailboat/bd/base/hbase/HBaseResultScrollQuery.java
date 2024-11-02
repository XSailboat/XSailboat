package team.sailboat.bd.base.hbase;

import java.util.Iterator;
import java.util.UUID;
import java.util.function.BiPredicate;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Table;

import team.sailboat.commons.fan.gadget.IScrollQuery;
import team.sailboat.commons.fan.gadget.ScrollQuerySite;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.serial.StreamAssist;

public class HBaseResultScrollQuery implements IScrollQuery<Result>
{
	
	Table mTable ;
	ResultScanner mRs ;
	Iterator<Result> mIt ;
	
	String mHandle ;
	int mMaxSize ;
	
	BiPredicate<Result, JSONArray> mPred ;
	
	public HBaseResultScrollQuery(Table aTable , ResultScanner aRs , int aMaxSize
			, BiPredicate<Result, JSONArray> aPred)
	{
		mTable = aTable ;
		mRs = aRs ;
		mMaxSize = aMaxSize<=0?500:aMaxSize ;
		mPred = aPred ;
	}

	@Override
	public void close()
	{
		StreamAssist.closeAll(mRs , mTable) ; 
		mRs = null ;
		mTable = null ;
	}

	@Override
	public JSONObject scrollNext(int aMaxSize)
	{
		if(mIt == null)
			mIt = mRs.iterator() ;
		
		int count = 0 ;
		String handle = null ;
		if(aMaxSize<=0)
			aMaxSize = mMaxSize ;
		JSONArray ja = new JSONArray() ;
		while(mIt.hasNext())
		{
			if(count++>=aMaxSize)
			{
				handle = UUID.randomUUID().toString() ;
				break ;
			}
			if(!mPred.test(mIt.next() , ja))
			{
				mHandle = null ;
				close();
				return null ;
			}
		}
		if(handle != null)
		{
			mHandle = handle ;
			ScrollQuerySite.getInstance().cacheScrollQuery(this) ;
		}
		else
		{
			mHandle = null ;
			close();
		}
		return new JSONObject().put("data", ja)
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
