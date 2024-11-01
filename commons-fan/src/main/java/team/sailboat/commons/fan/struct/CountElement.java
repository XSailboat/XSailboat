package team.sailboat.commons.fan.struct;

import java.util.Comparator;

public class CountElement<T> extends SortedElement<T>
{
	long mCount ;
	
	public CountElement(T aValue)
	{
		super(aValue) ;
	}
	
	public long getCount()
	{
		return mCount;
	}
	
	public void plus()
	{
		mCount++ ;
		notifySortedPropertyChange("count" , mCount-1, mCount);
	}
	
	/**
	 * 创建一个CountElement的比较器
	 * @return
	 */
	public static <T> Comparator<CountElement<T>> createComparator()
	{
		return (e1 , e2)->Long.compare(e2.getCount() , e1.getCount()) ;
	}
}
