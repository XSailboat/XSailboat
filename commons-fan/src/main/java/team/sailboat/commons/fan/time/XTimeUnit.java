package team.sailboat.commons.fan.time;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.Date;

public enum XTimeUnit implements Serializable
{
	SECOND(ChronoUnit.SECONDS) ,
	MINUTE(ChronoUnit.MINUTES) ,
	HOUR(ChronoUnit.HOURS) ,
	DAY(ChronoUnit.DAYS) ,
	WEEK(ChronoUnit.WEEKS) ,
	MONTH(ChronoUnit.MONTHS) ,
	YEAR(ChronoUnit.YEARS)
	;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final TemporalUnit mTemporalUnit ;
	
	private XTimeUnit(TemporalUnit aTemporalUnit)
	{
		mTemporalUnit = aTemporalUnit ;
	}
	
	public TemporalUnit toTemporalUnit()
	{
		return mTemporalUnit ;
	}
	
	public int toCalendar()
	{
		switch (this)
		{
		case SECOND:
			return Calendar.SECOND ;
		case MINUTE:
			return Calendar.MINUTE ;
		case HOUR:
			return Calendar.HOUR_OF_DAY ;
		case DAY:
			return Calendar.DAY_OF_MONTH ;
		case WEEK:
			return Calendar.WEEK_OF_MONTH ;
		case MONTH:
			return Calendar.MONTH ;
		case YEAR:
			return Calendar.YEAR ;
		default:
			throw new IllegalStateException() ;
		}
	}
	
	/**
	 * 毫秒数
	 * @param aDuration
	 * @return
	 */
	public long toMillis(int aDuration)
	{
		return getMilliSeconds(aDuration, this) ;
	}
	
	public long toSeconds(int aDuration)
	{
		return getSeconds(aDuration, this) ;
	}
	
	/**
	 * 时间长度的单位描述<br>
	 * toString()是时间长度描述，它的HOUR返回的是“小时”，DAY返回的是“天”；<br>
	 * toChineseDateTime是时间点的描述，它的HOUR返回的是“时”，DAY返回的是“日”
	 */
	@Override
	public String toString()
	{
		switch (this)
		{
		case SECOND:
			return "秒";
		case MINUTE:
			return "分" ;
		case HOUR:
			return "小时" ;
		case DAY:
			return "天" ;
		case WEEK:
			return "周" ;
		case MONTH:
			return "月" ;
		case YEAR:
			return "年" ;
		default:
			throw new IllegalStateException() ;
		}
	}
	
	/**
	 * 中文时间点描述<br>
	 * toString()是时间长度描述，它的HOUR返回的是“小时”，DAY返回的是“天”；<br>
	 * toChineseDateTime是时间点的描述，它的HOUR返回的是“时”，DAY返回的是“日”
	 * @return
	 */
	public String toChineseDateTime()
	{
		switch (this)
		{
		case SECOND:
			return "秒";
		case MINUTE:
			return "分" ;
		case HOUR:
			return "时" ;
		case DAY:
			return "日" ;
		case WEEK:
			return "周" ;
		case MONTH:
			return "月" ;
		case YEAR:
			return "年" ;
		default:
			throw new IllegalStateException() ;
		}
	}
	
	/**
	 * 支持返回 从秒到周的毫秒数
	 * @param aSpace
	 * @param aTimeUnit
	 * @return
	 */
	public static long getMilliSeconds(int aSpace , XTimeUnit aTimeUnit)
	{
		switch (aTimeUnit)
		{
		case SECOND:
			return aSpace*1000L ;
		case MINUTE:
			return aSpace*60000L ;
		case HOUR:
			return aSpace*3600000L ;
		case DAY:
			return aSpace*24*3600000L ;
		case WEEK:
			return aSpace*7*24*3600000L ;
		default:
			throw new IllegalArgumentException("不支持转成毫秒的时间单位："+aTimeUnit.toString()) ;
		}
	}
	
	public static long getSeconds(int aSpace , XTimeUnit aTimeUnit)
	{
		return aSpace * getSeconds(aTimeUnit) ;
	}
	
	/**
	 * 取出aTimeUnit以下的时间秒数。支持 分 到 月
	 * @param aTimeUnit
	 * @param aTime
	 * @return
	 */
	public static long getSeconds(XTimeUnit aTimeUnit , Date aTime)
	{
		Calendar cal = Calendar.getInstance() ;
		cal.setTime(aTime) ;
		switch (aTimeUnit)
		{
		case MINUTE:
			return cal.get(Calendar.SECOND) ;
		case HOUR:
		{
			int m = cal.get(Calendar.MINUTE) ;
			int s = cal.get(Calendar.SECOND) ;
			return m*60+s ;
		}
		case DAY:
		{
			int h = cal.get(Calendar.HOUR_OF_DAY) ;
			int m = cal.get(Calendar.MINUTE) ;
			int s = cal.get(Calendar.SECOND) ;
			return h*3600+m*60+s ;
		}
		case WEEK:
		{
			//第一天是星期一。将星期一置为0
			int d = (cal.get(Calendar.DAY_OF_WEEK)+5)%7 ;
			int h = cal.get(Calendar.HOUR_OF_DAY) ;
			int m = cal.get(Calendar.MINUTE) ;
			int s = cal.get(Calendar.SECOND) ;
			return d*24*3600+h*3600+m*60+s ;
		}
		case MONTH:
		{
			//一个月总定位31天
			int d = cal.get(Calendar.DAY_OF_MONTH)-1 ; 
			int h = cal.get(Calendar.HOUR_OF_DAY) ;
			int m = cal.get(Calendar.MINUTE) ;
			int s = cal.get(Calendar.SECOND) ;
			return d*24*3600+h*3600+m*60+s ;
		}
		default:
			throw new IllegalArgumentException() ;
		}
	}
	
	public static Date getDate(XTimeUnit aTimeUnit, long aSeconds)
	{
		Calendar calendar = Calendar.getInstance() ;
		switch(aTimeUnit)
		{		
			case MONTH:
				calendar.set(Calendar.DAY_OF_MONTH, 1);
			case WEEK:
				if (aTimeUnit != MONTH)
				{
					calendar.setFirstDayOfWeek(Calendar.MONDAY);
					calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				}
			case DAY:
				calendar.set(Calendar.HOUR_OF_DAY, 0);
			case HOUR:
				calendar.set(Calendar.MINUTE, 0);
			case MINUTE:
				calendar.set(Calendar.SECOND, 0);
			default:
		}
		calendar.add(Calendar.SECOND, (int)aSeconds) ;
		return calendar.getTime() ;
	}
	
	public static Date getDate(XTimeUnit aTimeUnit , Date aTime , long aSeconds)
	{
		Calendar calendar = Calendar.getInstance() ;
		calendar.setTime(aTime) ;
		switch(aTimeUnit)
		{		
			case MONTH:
				calendar.set(Calendar.DAY_OF_MONTH, 1);
			case WEEK:
				if (aTimeUnit != MONTH)
				{
					calendar.setFirstDayOfWeek(Calendar.MONDAY);
					calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				}
			case DAY:
				calendar.set(Calendar.HOUR_OF_DAY, 0);
			case HOUR:
				calendar.set(Calendar.MINUTE, 0);
			case MINUTE:
				calendar.set(Calendar.SECOND, 0);
			default:
		}
		calendar.add(Calendar.SECOND, (int)aSeconds) ;
		return calendar.getTime() ;
	}
	
	/**
	 * 支持分到月
	 * @param aTimeUnit
	 * @return
	 */
	public static long getSeconds(XTimeUnit aTimeUnit)
	{
		switch (aTimeUnit)
		{
		case MINUTE:
			return 60 ;
		case HOUR:
			return 3600 ;
		case DAY:
			return 24*3600 ;
		case WEEK:
			return 7*24*3600 ;
		case MONTH:
			return 31*7*24*3600 ;
		default:
			throw new IllegalArgumentException() ;
		}
	}
}
