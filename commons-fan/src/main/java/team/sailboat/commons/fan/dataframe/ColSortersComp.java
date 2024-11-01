package team.sailboat.commons.fan.dataframe;

import java.util.Comparator;
import java.util.List;

import team.sailboat.commons.fan.json.JSONArray;

public class ColSortersComp implements Comparator<Object>
{
	List<ColSorter> mColSorters ;
	
	public ColSortersComp(List<ColSorter> aSorters)
	{
		mColSorters = aSorters ;
	}

	@Override
	public int compare(Object aO1, Object aO2)
	{
		for(ColSorter sorter : mColSorters)
		{
			int result = sorter.mComp.compare(((JSONArray)aO1).opt__source(sorter.mColIndex)
					, ((JSONArray)aO2).opt__source(sorter.mColIndex)) ;
			if(result != 0)
				return result ;
		}
		return 0 ;
	}
}
