package team.sailboat.bd.base.hbase;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiPredicate;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;

import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.gadget.IScrollQuery;
import team.sailboat.commons.fan.gadget.ScrollQuerySite;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.res.ResourceStore;
import team.sailboat.commons.fan.serial.StreamAssist;

public class HBaseCachedResultScrollQuery implements IScrollQuery<Result>
{
	
	ResourceStore<HBaseTableBundle> mTblStore ;
	Map.Entry<String, HBaseTableBundle> mTblBundleEntry ;
	boolean mClosed = false ;
	Scan mScan ;
	ResultScanner mRs ;
	Iterator<Result> mIt ;
	
	String mHandle ;
	int mMaxSize ;
	
	BiPredicate<Result, JSONArray> mPred ;
	
	public HBaseCachedResultScrollQuery(ResourceStore<HBaseTableBundle> aTblStore , Scan aScan , int aMaxSize
			, BiPredicate<Result, JSONArray> aPred)
	{
		mTblStore = aTblStore ;
		mScan = aScan ;
		mMaxSize = aMaxSize<=0?500:aMaxSize ;
		mPred = aPred ;
	}

	@Override
	public void close()
	{
		if(mRs != null)
		{
			StreamAssist.closeAll(mRs) ;
			mRs = null ;
		}
		if(mTblBundleEntry != null)
		{
			mTblStore.release(mTblBundleEntry.getKey()) ;
			mTblBundleEntry = null ;
		}
		mClosed = true ;
	}

	@Override
	public JSONObject scrollNext(int aMaxSize)
	{
		if(mIt == null)
		{
			Assert.isNotTrue(mClosed , "已经关闭，不能再scrollQuery");
			try
			{
				mTblBundleEntry = mTblStore.get(null) ;
				Table tbl =  mTblBundleEntry.getValue().get() ;
				mRs = tbl.getScanner(mScan) ;
				mIt = mRs.iterator() ;
			}
			catch (Exception e)
			{
				WrapException.wrapThrow(e) ;
				close();
				return null ;		// dead code
			}
		}
		int count = 0 ;
		
		String handle = null ;
		if(aMaxSize<=0)
			aMaxSize = mMaxSize ;
		JSONArray ja = new JSONArray() ;
		try
		{
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
					//没有数据了，可以关闭相关资源了
					close();
					return null ;
				}
			}
		}
		catch(Exception e)
		{
			close();
		}
		if(handle != null)
		{
			mHandle = handle ;
			ScrollQuerySite.getInstance().cacheScrollQuery(this) ;
		}
		else
		{
			mHandle = null ;
			//没有数据了，可以关闭相关资源了
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
