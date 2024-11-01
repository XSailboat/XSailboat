package team.sailboat.commons.fan.es.query;

import java.util.Iterator;
import java.util.Map;

import team.sailboat.commons.fan.collection.SizeIter;
import team.sailboat.commons.fan.infc.EFunction;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.struct.Tuples;

public class ScrollSearchResult extends SearchResult
{
	String mScrollId ;
	EFunction<String , JSONObject , Throwable> mQueryPerformer ;
	
	public ScrollSearchResult(JSONObject aResult , EFunction<String , JSONObject , Throwable> aQueryPerformer)
	{
		super(aResult) ;
		mQueryPerformer = aQueryPerformer ;
	}
	
	@Override
	protected void construct(JSONObject aResult)
	{
		super.construct(aResult);
		mScrollId = aResult.optString("_scroll_id") ;
	}
	
	boolean hasMore()
	{
		return mScrollId != null ;
	}
	
	void loadNext() throws Throwable
	{
		if(mScrollId != null)
			construct(mQueryPerformer.apply(mScrollId)) ;
	}
	
	@Override
	public SizeIter<JSONObject> sourceObjects()
	{
		return new SizeIterable<>(new SourceObjectsIter() , getTotal()) ;
	}
	
	@Override
	public SizeIter<Map.Entry<String , JSONObject>> sourceObjectEntries()
	{
		return new SizeIterable<>(new SourceObjectEntriesIter() , mSize) ;
	}
	
	class SourceObjectsIter implements Iterator<JSONObject>
	{
		int i = 0 ;
		
		public SourceObjectsIter()
		{
		}

		@Override
		public boolean hasNext()
		{
			if(i < mSize)
				return true ;
			else if(hasMore())
			{
				try
				{
					loadNext();
				}
				catch(RuntimeException e)
				{
					throw e ;
				}
				catch (Throwable e)
				{
					throw new RuntimeException(e) ;
				}
				i = 0 ;
				return mSize>0 ;
			}
			else
				return false ;
		}

		@Override
		public JSONObject next()
		{
			return mResultItems.optJSONObject(i++).optJSONObject("_source") ;
		}
	}
	
	class SourceObjectEntriesIter implements Iterator<Map.Entry<String , JSONObject>>
	{
		int i = 0 ;
		
		public SourceObjectEntriesIter()
		{
		}

		@Override
		public boolean hasNext()
		{
			if(i < mSize)
				return true ;
			else if(hasMore())
			{
				try
				{
					loadNext();
				}
				catch(RuntimeException e)
				{
					throw e ;
				}
				catch (Throwable e)
				{
					throw new RuntimeException(e) ;
				}
				i = 0 ;
				return mSize>0 ;
			}
			else
				return false ;
		}

		@Override
		public Map.Entry<String ,JSONObject> next()
		{
			JSONObject jobj = mResultItems.optJSONObject(i++) ;
			return Tuples.of(jobj.optString("_id") , jobj.optJSONObject("_source")) ;
		}
	}
}
