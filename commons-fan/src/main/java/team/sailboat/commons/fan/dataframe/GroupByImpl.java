package team.sailboat.commons.fan.dataframe;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;

class GroupByImpl implements GroupBy
{
	JDataFrame mDataFrame ;
	List<ColSorter> mSorterList ;
	ScalarExp[] mGroupCols ;
	
	public GroupByImpl(JDataFrame aDataFrame , ScalarExp... aGroupCols)
	{
		Assert.notEmpty(aGroupCols, "未指定分组列！") ;
		mDataFrame = aDataFrame ;
		mGroupCols = aGroupCols ;
	}
	
	public GroupByImpl(JDataFrame aDataFrame , Collection<ScalarExp> aGroupCols)
	{
		Assert.notEmpty(aGroupCols, "未指定分组列！") ;
		mDataFrame = aDataFrame ;
		mGroupCols = aGroupCols.toArray(new ScalarExp[0]) ;
	}

	@Override
	public GroupBy sort(String[] aColumns, boolean[] aAscending)
	{
		return this ;
	}
	
	protected int getCollumnIndex(String aColName)
	{
		if(XC.isEmpty(mGroupCols))
			return -1 ;
		for(int i=0 ; i<mGroupCols.length ; i++)
		{
			ScalarExp exp = mGroupCols[i] ;
			if(aColName.equals(exp.getName()))
				return i ;
		}
		return -1 ;
	}

	@Override
	public GroupBy sort(Collection<? extends Entry<String, ? extends Comparator<Object>>> aColSortMethods)
	{
		if(XC.isNotEmpty(aColSortMethods))
		{
			List<ColSorter> sorterList = ColSorter.build(aColSortMethods, this::getCollumnIndex) ;
			if(mSorterList == null)
				mSorterList = sorterList ;
			else
				mSorterList.addAll(sorterList) ;
		}
		return this ;
	}

	@Override
	public JDataFrame agg(List<AggExp<?>> aAggregators)
	{
		// 分组
		Map<String, AggregatorCacheItem> newDataMap = XC.hashMap() ;
		JSONObject colsJo = new JSONObject() ;
		int j=0 ;
		for(; j<mGroupCols.length ; j++)
		{
			colsJo.put(mGroupCols[j].getName() , new JSONObject().put("dataType" , mGroupCols[j].getDataType())
					.put("index", j)) ;
		}
		for(AggExp<?> exp : aAggregators)
		{
			colsJo.put(exp.getName() , new JSONObject().put("dataType" , exp.getDataType())
					.put("index", j++)) ;
		}
		mDataFrame.forEachRow((rowJa)->{
			StringBuilder groupKeyBld = new StringBuilder() ;
			
			for(int i=0 ; i<mGroupCols.length ; i++)
			{
				Object cell = mGroupCols[i].eval(rowJa) ;
				if(cell == null)
					groupKeyBld.append(-1) ;
				else
				{
					String str = cell.toString() ;
					groupKeyBld.append(str.length())
						.append(str) ;
				}
			}
			String groupKey = groupKeyBld.toString() ;
			AggregatorCacheItem item =  newDataMap.get(groupKey) ;
			if(item == null)
			{
				item = new AggregatorCacheItem() ;
				for(int i=0 ; i<mGroupCols.length ; i++)
				{
					item.putGroupCell(mGroupCols[i].eval(rowJa)) ;
				}
				for(AggExp<?> agg : aAggregators)
				{
					item.putAggBuffer(agg.newBuffer()) ;
				}
				newDataMap.put(groupKey, item) ;
				
			}
			int i= 0 ;
			for(AggExp<?> agg : aAggregators)
			{
				agg.setCurrentBuffer(item.getAggBuffer(i++)) ;
				agg.eval(rowJa) ;
			}
		}); 
		// 结束分组
		JSONArray ja = new JSONArray() ;
		
		for(AggregatorCacheItem item : newDataMap.values())
		{
			item.terminate();
			ja.put(item.getRow()) ;
		}
		// 开始排序
		if(mSorterList!= null)
		{
			ja.sort(new ColSortersComp(mSorterList)) ;
		}
		
		return JDataFrame.newFrame(colsJo)
				.appendRows(ja) ;
	}
}
