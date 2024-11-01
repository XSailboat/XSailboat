package team.sailboat.commons.fan.dataframe;

import java.util.List;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONArray;

public class AggregatorCacheItem
{
	final JSONArray mRowJa = new JSONArray() ;
	
	final List<AggregatorBuffer<?>> mBufList = XC.arrayList() ;
	
	public AggregatorCacheItem()
	{
	}
	
	public void putGroupCell(Object aCell)
	{
		mRowJa.put(aCell) ;
	}
	
	public void putAggBuffer(AggregatorBuffer<?> aBuf)
	{
		mBufList.add(aBuf) ;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AggregatorBuffer<?>> T  getAggBuffer(int aIndex)
	{
		return (T) mBufList.get(aIndex) ;
	}
	
	public void terminate()
	{
		for(AggregatorBuffer<?> buf : mBufList)
		{
			mRowJa.put(buf.terminate()) ;
		}
	}
	
	public JSONArray getRow()
	{
		return mRowJa ;
	}
}
