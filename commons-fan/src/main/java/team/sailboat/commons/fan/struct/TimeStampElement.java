package team.sailboat.commons.fan.struct;

import java.util.Comparator;

/**
 * 记录最近取用时间戳的包
 *
 * @author yyl
 * @since 2017年10月25日
 */
public class TimeStampElement<T> extends SortedElement<T>
{
	long mTimeStamp ;
	
	public TimeStampElement(T aValue)
	{
		super(aValue) ;
		mTimeStamp = System.currentTimeMillis() ;
	}
	
	public TimeStampElement(T aValue , long aTimeStamp)
	{
		super(aValue) ;
		mTimeStamp = aTimeStamp ;
	}
	
	public long getTimeStamp()
	{
		return mTimeStamp ;
	}
	
	public void update()
	{
		long oldTimeStamp = mTimeStamp ;
		mTimeStamp = System.currentTimeMillis() ;
		notifySortedPropertyChange("timeStamp" , oldTimeStamp,  mTimeStamp);
	}
	
	/**
	 * 创建一个CountElement的比较器
	 * @return
	 */
	public static <T> Comparator<TimeStampElement<T>> createComparator()
	{
		return (e1 , e2)->(int)(e1.getTimeStamp()-e2.getTimeStamp()) ;
	}
}
