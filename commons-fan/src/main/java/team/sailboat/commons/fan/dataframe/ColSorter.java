package team.sailboat.commons.fan.dataframe;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.ToIntFunction;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.Assert;

public class ColSorter
{
	int mColIndex ;
	Comparator<Object> mComp ;
	
	public ColSorter(int aColIndex , Comparator<Object> aComp)
	{
		mColIndex = aColIndex ;
		mComp = aComp ;
	}
	
	public static List<ColSorter> build(Collection<? extends Entry<String, ? extends Comparator<Object>>> aColSortMethods
			, ToIntFunction<String> aColIndexPvd)
	{
		if(XC.isEmpty(aColSortMethods))
			return Collections.emptyList() ;
		List<ColSorter> sorterList = XC.arrayList() ;
		for(Entry<String, ? extends Comparator<Object>> entry : aColSortMethods)
		{
			int i = aColIndexPvd.applyAsInt(entry.getKey()) ;
			Assert.isTrue(i != -1 , "指定的排序列[%s]不是分组的列！" , entry.getKey()) ;
			sorterList.add(new ColSorter(i, entry.getValue())) ;
		}
		return sorterList ;
	}
}