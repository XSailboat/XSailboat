package team.sailboat.commons.fan.es.query;

import java.util.Iterator;
import java.util.Map;

import team.sailboat.commons.fan.collection.SizeIter;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;

public abstract class SearchResult
{
	protected JSONObject mResult ;
	protected JSONArray mResultItems ;
	protected int mSize ;
	
	public SearchResult()
	{
	}
	
	public SearchResult(JSONObject aResult)
	{
		construct(aResult);
	}
	
	protected void construct(JSONObject aResult)
	{
		mResult = aResult ;
		mResultItems = mResult.getJSONObject("hits")
				.getJSONArray("hits") ;
		mSize = mResultItems.size() ;
	}
	
	public int size()
	{
		return mSize ;
	}
	
	public int getTotal()
	{
		return mResult.pathInt(0, "hits" , "total" , "value") ;
	}
	
	public abstract SizeIter<JSONObject> sourceObjects() ;
	
	public abstract SizeIter<Map.Entry<String , JSONObject>> sourceObjectEntries() ;
	
	@Override
	public String toString()
	{
		return mResult.toJSONString() ;
	}
	
	protected static class  SizeIterable<T> implements SizeIter<T>
	{
		Iterator<T> mIt ;
		int mSize_own ;
		
		public SizeIterable(Iterator<T> aIt , int aSize)
		{
			mIt = aIt ;
			mSize_own = aSize ;
		}
		
		@Override
		public Iterator<T> iterator()
		{
			return mIt ;
		}

		@Override
		public int size()
		{
			return mSize_own ;
		}

		@Override
		public boolean isEmpty()
		{
			return mSize_own == 0 ;
		}
	}
}
