package team.sailboat.commons.fan.dataframe;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

public interface GroupBy
{
	GroupBy sort(String[] columns, boolean[] ascending) ;
	
	GroupBy sort(Collection<? extends Entry<String , ? extends Comparator<Object>>> aColSortMethods) ;

//    JDataFrame agg(Exp<?>... aggregators) ;
    
    JDataFrame agg(List<AggExp<?>> aAggregators) ;
}
