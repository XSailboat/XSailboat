package team.sailboat.commons.fan.es.query;

import java.util.Iterator;
import java.util.Map;

import team.sailboat.commons.fan.collection.SizeIter;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.struct.Tuples;

public class PageSearchResult extends SearchResult
{

	public PageSearchResult(JSONObject aResult)
	{
		super(aResult);
	}

	@Override
	public SizeIter<JSONObject> sourceObjects()
	{
		return new SizeIterable<>(new SourceObjectsIter() , mSize) ;
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
			return i < mSize ;
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
			return i < mSize ;
		}

		@Override
		public Map.Entry<String ,JSONObject> next()
		{
			JSONObject jobj = mResultItems.optJSONObject(i++) ;
			return Tuples.of(jobj.optString("_id") , jobj.optJSONObject("_source")) ;
		}
	}
}
