package team.sailboat.commons.fan.time;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import team.sailboat.commons.fan.collection.AutoSortedList;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.struct.CountElement;
import team.sailboat.commons.fan.text.XString;

/**
 * 
 * <strong>功能：</strong>
 * <p style="text-indent:2em">
 * DateTime是非线程安全的，所以用ThreadLocal存储
 *
 * @author yyl
 * @since 2017年4月5日
 */
public class XTime
{
	static final long sUTCOffset = TimeZone.getDefault().getRawOffset() ;
	
	static final Object sMutext = new Object() ;
	
	/**
	 * 日期格式：yy-MM-dd HH:mm:ss
	 */
	static ThreadLocal<DateFormat> sSDF_yyMMddHHmmss ;
	/**
	 * 日期格式：yyyy-MM-dd HH:mm:ss
	 */
	static ThreadLocal<DateFormat> sSDF_yyyyMMddHHmmss ;
	
	/**
	 * 日期格式：MM-dd HH:mm
	 */
	static ThreadLocal<DateFormat> sSDF_MMddHHmm ;
	
	/**
	 * 日期格式：yyyy-MM-dd HH:mm:ss.SSS
	 */
	static ThreadLocal<DateFormat> sSDF_yyyyMMddHHmmssSSS ;
	
	/**
	 * 日期格式：yyyyMMdd_HHmmss.SSS
	 */
	static ThreadLocal<DateFormat> sSDF_Plain_yyyyMMddHHmmssSSS ;
	
	/**
	 * 日期格式：yyyyMM
	 */
	static ThreadLocal<DateFormat> sSDF_Plain_yyyyMM ; 
	
	/**
	 * 日期格式：yyyy-MM
	 */
	static ThreadLocal<DateFormat> sSDF_yyyyMM ;
	
	/**
	 * 日期格式：yyyyMMdd
	 */
	static ThreadLocal<DateFormat> sSDF_Plain_yyyyMMdd ;
	
	/**
	 * 日期格式：yyyy-MM-dd
	 */
	static ThreadLocal<DateFormat> sSDF_yyyyMMdd ;
	
	/**
	 * 日期格式：yyMMdd_HHmmss
	 */
	static ThreadLocal<DateFormat> sSDF_Plain_yyMMddHHmmss ;
	
	/**
	 * 日期格式：yyyyMMdd_HHmmss
	 */
	static ThreadLocal<DateFormat> sSDF_Plain_yyyyMMddHHmmss ;
	
	/**
	 * 日期格式：yyMMdd_HHmmss.SSS
	 */
	static ThreadLocal<DateFormat> sSDF_Plain_yyMMddHHmmssSSS ;
	/**
	 * 日期格式：HH:mm:ss.SSS
	 */
	static ThreadLocal<DateFormat> sSDF_HHmmss_SSS ;
	
	static ThreadLocal<DateFormat> sSDF_GMT ;
	
	static ThreadLocal<DateFormat> sSDF_ISO_8601_yyyyMMddHHmmssSSS ;
	
	static ThreadLocal<DateFormat> sSDF_ISO_8601_yyyyMMddHHmmss ;
	
	static final ThreadLocal<AutoSortedList<CountElement<DateFormat>>> mFmtListTL = new ThreadLocal<>() ;
	
	static Pattern sDurationPtn = Pattern.compile("(\\d+)([yMdHms]?)") ;
	
	/**
	 * 
	 * @param aYear				
	 * @param aMonth			1-12
	 * @param aDayOfMonth		1-31
	 * @return
	 */
	public static Date of(int aYear , int aMonth , int aDayOfMonth)
	{
		return of(aYear, aMonth, aDayOfMonth, false) ;
	}
	
	/**
	 * 
	 * @param aYear
	 * @param aMonth			1-12
	 * @param aDayOfMonth		1-31
	 * @param aEnd
	 * @return
	 */
	public static Date of(int aYear , int aMonth , int aDayOfMonth , boolean aEnd)
	{
		Calendar cal = Calendar.getInstance() ;
		if(aEnd)
		{
			cal.set(aYear, aMonth-1 , aDayOfMonth , 23 , 59 , 59) ;
			cal.set(Calendar.MILLISECOND, 999) ;
		}
		else
		{
			cal.set(aYear, aMonth-1 , aDayOfMonth , 0 , 0 , 0) ;
			cal.set(Calendar.MILLISECOND, 0) ;
		}
		return cal.getTime() ;
	}
	
	/**
	 * 日期格式：yy-MM-dd HH:mm:ss
	 * @return
	 */
	static DateFormat getFmt$yyMMddHHmmss()
	{
		if(sSDF_yyMMddHHmmss == null)
		{
			synchronized(sMutext)
			{
				if(sSDF_yyMMddHHmmss == null)
					sSDF_yyMMddHHmmss = new ThreadLocal<>() ;
			}
		}
		DateFormat fmt = sSDF_yyMMddHHmmss.get() ;
		if(fmt == null)
		{
			fmt = new SimpleDateFormat("yy-MM-dd HH:mm:ss") ;
			sSDF_yyMMddHHmmss.set(fmt);
		}
		return fmt ;
	}
	
	/**
	 * 日期格式：yy-MM-dd HH:mm:ss
	 * @param aDate
	 * @return
	 */
	public static String format$yyMMddHHmmss(Date aDate)
	{
		return getFmt$yyMMddHHmmss().format(aDate) ;
	}
	
	/**
	 * 日期格式：yy-MM-dd HH:mm:ss
	 * @param aText
	 * @return
	 * @throws ParseException 
	 */
	public static Date parse$yyMMddHHmmss(String aText) throws ParseException
	{
		return getFmt$yyMMddHHmmss().parse(aText) ;
	}
	
	/**
	 * 日期格式：yy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String current$yyMMddHHmmss()
	{
		return format$yyMMddHHmmss(new Date()) ;
	}
	
	/**
	 * 日期格式：yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	static DateFormat getFmt$yyyyMMddHHmmss()
	{
		if(sSDF_yyyyMMddHHmmss == null)
		{
			synchronized(sMutext)
			{
				if(sSDF_yyyyMMddHHmmss == null)
					sSDF_yyyyMMddHHmmss = new ThreadLocal<>() ;
			}
		}
		DateFormat fmt = sSDF_yyyyMMddHHmmss.get() ;
		if(fmt == null)
		{
			fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
			sSDF_yyyyMMddHHmmss.set(fmt);
		}
		return fmt ;
	}
	
	/**
	 * 格式：MM-dd HH:mm
	 * @return
	 */
	static DateFormat getFmt$MMddHHmm()
	{
		if(sSDF_MMddHHmm == null)
		{
			synchronized(sMutext)
			{
				if(sSDF_MMddHHmm == null)
					sSDF_MMddHHmm = new ThreadLocal<>() ;
			}
		}
		DateFormat fmt = sSDF_MMddHHmm.get() ;
		if(fmt == null)
		{
			fmt = new SimpleDateFormat("MM-dd HH:mm") ;
			sSDF_MMddHHmm.set(fmt);
		}
		return fmt ;
	}
	
	/**
	 * 日期格式：yyyy-MM-dd HH:mm:ss.SSS
	 * @return
	 */
	static DateFormat getFmt$yyyyMMddHHmmssSSS()
	{
		if(sSDF_yyyyMMddHHmmssSSS == null)
		{
			synchronized(sMutext)
			{
				if(sSDF_yyyyMMddHHmmssSSS == null)
					sSDF_yyyyMMddHHmmssSSS = new ThreadLocal<>() ;
			}
		}
		DateFormat fmt = sSDF_yyyyMMddHHmmssSSS.get() ;
		if(fmt == null)
		{
			fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS") ;
			sSDF_yyyyMMddHHmmssSSS.set(fmt);
		}
		return fmt ;
	}
	
	public static String format$yyyyMMddHHmmss(long aTimeInMills)
	{
		return getFmt$yyyyMMddHHmmss().format(new Date(aTimeInMills)) ;			
	}
	
	/**
	 * 日期格式：yyyy-MM-dd HH:mm:ss
	 * @param aDate
	 * @return
	 */
	public static String format$yyyyMMddHHmmss(Date aDate)
	{
		return aDate==null?null:getFmt$yyyyMMddHHmmss().format(aDate) ;			
	}
	
	public static String format$MMddHHmm(Date aDate)
	{
		return aDate==null?null:getFmt$MMddHHmm().format(aDate) ;			
	}
	
	public static String friendly$MMddHHmm(Date aDate)
	{
		if(aDate == null)
			return null ;
		int year = getYear(aDate) ;
		int thisYear = getYear(new Date()) ;
		if(year == thisYear)
			return getFmt$MMddHHmm().format(aDate) ;
		else if(year == thisYear - 1)
			return "去年"+getFmt$MMddHHmm().format(aDate) ;
		else
			return year+"-"+getFmt$MMddHHmm().format(aDate) ;
	}
	
	public static String format$yyyyMMddHHmmss(Date aDate , String aDefaultVal)
	{
		if(aDate == null)
			return aDefaultVal ;
		try
		{
			return getFmt$yyyyMMddHHmmss().format(aDate) ;
		}
		catch(Throwable e)
		{
			return aDefaultVal ;
		}
	}
	
	public static String format$yyyyMMddHHmmssSSS(Date aDate , String aDefaultVal)
	{
		if(aDate == null)
			return aDefaultVal ;
		try
		{
			return getFmt$yyyyMMddHHmmssSSS().format(aDate) ;
		}
		catch(Throwable e)
		{
			return aDefaultVal ;
		}
	}
	
	public static String format$yyyyMMddHHmmssSSS(Date aDate)
	{
		return aDate==null?null:getFmt$yyyyMMddHHmmssSSS().format(aDate) ;
	}
	
	/**
	 * 日期格式：yyyy-MM-dd HH:mm:ss
	 * @param aText
	 * @return
	 * @throws ParseException
	 */
	public static Date parse$yyyyMMddHHmmss(String aText) throws ParseException
	{
		if(XString.isEmpty(aText))
			return null ;
		return getFmt$yyyyMMddHHmmss().parse(aText) ;
	}
	
	public static Date parse$yyyyMMddHHmmss_0(String aText)
	{
		if(XString.isEmpty(aText))
			return null ;
		try
		{
			return getFmt$yyyyMMddHHmmss().parse(aText) ;
		}
		catch (ParseException e)
		{
			return null ;
		}
	}
	
	/**
	 * 日期格式：yyyy-MM-dd HH:mm:ss.SSS
	 * @param aText
	 * @return
	 * @throws ParseException
	 */
	public static Date parse$yyyyMMddHHmmssSSS(String aText) throws ParseException
	{
		return getFmt$yyyyMMddHHmmssSSS().parse(aText) ;
	}
	
	public static Date parse$yyyyMMddHHmmssSSS_0(String aText , Date aDefaultValue)
	{
		if(XString.isEmpty(aText))
			return aDefaultValue ;
		try
		{
			return getFmt$yyyyMMddHHmmssSSS().parse(aText) ;
		}
		catch (ParseException e)
		{
			return aDefaultValue ;
		}
	}
	
	/**
	 * 日期格式：yyyyMM
	 * @return
	 */
	static DateFormat getFmtPlain_yyyyMM()
	{
		if(sSDF_Plain_yyyyMM == null)
		{
			synchronized(sMutext)
			{
				if(sSDF_Plain_yyyyMM == null)
					sSDF_Plain_yyyyMM = new ThreadLocal<>() ;
			}
		}
		DateFormat fmt = sSDF_Plain_yyyyMM.get() ;
		if(fmt == null)
		{
			fmt = new SimpleDateFormat("yyyyMM") ;
			sSDF_Plain_yyyyMM.set(fmt);
		}
		return fmt ;
	}
	
	/**
	 * 日期格式：yyyyMM
	 * @param aDate
	 * @return
	 */
	public static String formatPlain_yyyyMM(Date aDate)
	{
		return getFmtPlain_yyyyMM().format(aDate) ;
	}
	
	/**
	 * 日期格式：yyyyMM
	 * @param aText
	 * @return
	 * @throws ParseException
	 */
	public static Date parse$yyyyMM(String aText) throws ParseException
	{
		return getFmt$yyyyMM().parse(aText) ;
	}
	
	public static Date parse$yyyyMM_0(String aText)
	{
		try
		{
			return getFmt$yyyyMM().parse(aText) ;
		}
		catch (ParseException e)
		{
			return null ;
		}
	}
	
	static DateFormat getFmt$yyyyMM()
	{
		if(sSDF_yyyyMM == null)
		{
			synchronized(sMutext)
			{
				if(sSDF_yyyyMM == null)
					sSDF_yyyyMM = new ThreadLocal<>() ;
			}
		}
		DateFormat fmt = sSDF_yyyyMM.get() ;
		if(fmt == null)
		{
			fmt = new SimpleDateFormat("yyyy-MM") ;
			sSDF_yyyyMM.set(fmt);
		}
		return fmt ;
	}
	
	/**
	 * 日期格式：yyyy-MM-dd
	 * @return
	 */
	static DateFormat getFmt$yyyyMMdd()
	{
		if(sSDF_yyyyMMdd == null)
		{
			synchronized (sMutext)
			{
				if(sSDF_yyyyMMdd == null)
					sSDF_yyyyMMdd = new ThreadLocal<>() ;
			}
		}
		DateFormat fmt = sSDF_yyyyMMdd.get() ;
		if(fmt == null)
		{
			fmt = new SimpleDateFormat("yyyy-MM-dd") ;
			sSDF_yyyyMMdd.set(fmt);
		}
		return fmt ;
	}
	
	/**
	 * 日期格式：yyyy-MM-dd
	 * @param aDate
	 * @return
	 */
	public static String format$yyyyMMdd(Date aDate)
	{
		return aDate == null ? null : getFmt$yyyyMMdd().format(aDate) ;
	}
	
	public static String format$yyyyMMdd(Date aDate , String aDefaultValue)
	{
		return aDate == null? aDefaultValue : getFmt$yyyyMMdd().format(aDate) ;
	}
	
	/**
	 * 
	 * @param aDate
	 * @return
	 */
	public static String format$yyyyMM(Date aDate)
	{
		return getFmt$yyyyMM().format(aDate) ;
	}
	
	/**
	 * 日期格式：yyyy-MM-dd
	 * @param aText
	 * @return
	 * @throws ParseException
	 */
	public static Date parse$yyyyMMdd(String aText) throws ParseException
	{
		if(XString.isEmpty(aText))
			return null ;
		return getFmt$yyyyMMdd().parse(aText) ;
	}
	
	public static Date parse$yyyyMMdd_0(String aText)
	{
		try
		{
			return parse$yyyyMMdd(aText) ;
		}
		catch (ParseException e)
		{
			WrapException.wrapThrow(e) ;
			return null ;		// dead code
		}
	}
	
	/**
	 * 日期格式：yyMMdd_HHmmss
	 * @return
	 */
	static DateFormat getFmtPlain_yyMMddHHmmss()
	{
		if(sSDF_Plain_yyMMddHHmmss == null)
		{
			synchronized(sMutext)
			{
				if(sSDF_Plain_yyMMddHHmmss == null)
					sSDF_Plain_yyMMddHHmmss = new ThreadLocal<>() ;
			}
		}
		DateFormat fmt = sSDF_Plain_yyMMddHHmmss.get() ;
		if(fmt == null)
		{
			fmt = new SimpleDateFormat("yyMMdd_HHmmss") ;
			sSDF_Plain_yyMMddHHmmss.set(fmt);
		}
		return fmt ;
	}
	
	/**
	 * 日期格式：yyyyMMdd_HHmmss
	 * @return
	 */
	static DateFormat getFmtPlain_yyyyMMddHHmmss()
	{
		if(sSDF_Plain_yyyyMMddHHmmss == null)
		{
			synchronized(sMutext)
			{
				if(sSDF_Plain_yyyyMMddHHmmss == null)
					sSDF_Plain_yyyyMMddHHmmss = new ThreadLocal<>() ;
			}
		}
		DateFormat fmt = sSDF_Plain_yyyyMMddHHmmss.get() ;
		if(fmt == null)
		{
			fmt = new SimpleDateFormat("yyyyMMdd_HHmmss") ;
			sSDF_Plain_yyyyMMddHHmmss.set(fmt);
		}
		return fmt ;
	}
	
	/**
	 * 日期格式：yyMMdd_HHmmss
	 * @param aDate
	 * @return
	 */
	public static String formatPlain_yyMMddHHmmss(Date aDate)
	{
		return getFmtPlain_yyMMddHHmmss().format(aDate) ;
	}
	
	/**
	 * 日期格式：yyMMdd_HHmmss
	 * @param aText
	 * @return
	 * @throws ParseException
	 */
	public static Date parsePlain_yyMMddHHmmss(String aText) throws ParseException
	{
		return getFmtPlain_yyMMddHHmmss().parse(aText) ;
	}
	
	/**
	 * 日期格式：yyMMdd_HHmmss.SSS
	 * @return
	 */
	static DateFormat getFmtPlain_yyMMddHHmmssSSS()
	{
		if(sSDF_Plain_yyMMddHHmmssSSS == null)
		{
			synchronized(sMutext)
			{
				if(sSDF_Plain_yyMMddHHmmssSSS == null)
					sSDF_Plain_yyMMddHHmmssSSS = new ThreadLocal<>() ;
			}
		}
		DateFormat fmt = sSDF_Plain_yyMMddHHmmssSSS.get() ;
		if(fmt == null)
		{
			fmt = new SimpleDateFormat("yyMMdd_HHmmss.SSS") ;
			sSDF_Plain_yyMMddHHmmssSSS.set(fmt);
		}
		return fmt ;
	}
	
	/**
	 * 日期格式：yyMMdd_HHmmss.SSS
	 * @param aDate
	 * @return
	 */
	public static String formatPlain_yyMMddHHmmssSSS(Date aDate)
	{
		return getFmtPlain_yyMMddHHmmssSSS().format(aDate) ;
	}
	
	/**
	 * 日期格式：yyMMdd_HHmmss.SSS
	 * @param aDateStr
	 * @return
	 * @throws ParseException
	 */
	public static Date parsePlain_yyMMddHHmmssSSS(String aDateStr) throws ParseException
	{
		return getFmtPlain_yyMMddHHmmssSSS().parse(aDateStr) ;
	}
	
	/**
	 * 日期格式：yyyyMMdd_HHmmss.SSS
	 * @param aText
	 * @return
	 * @throws ParseException
	 */
	public static Date parsePlain_yyyyMMddHHmmssSSS(String aText) throws ParseException
	{
		return getFmtPlain_yyyyMMddHHmmssSSS().parse(aText) ;
	}
	
	/**
	 * 日期格式：yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String current$yyyyMMddHHmmss()
	{ 
		return getFmt$yyyyMMddHHmmss().format(new Date()) ;
	}
	
	/**
	 * 日期格式：yyyy-MM-dd HH:mm:ss.SSS
	 * @return
	 */
	public static String current$yyyyMMddHHmmssSSS()
	{ 
		return getFmt$yyyyMMddHHmmssSSS().format(new Date()) ;
	}
	
	/**
	 * 日期格式：yyyyMM
	 * @return
	 */
	public static String currentPlain_yyyyMM()
	{
		return getFmtPlain_yyyyMM().format(new Date()) ;
	}
	
	/**
	 * 日期格式：yyyyMMdd
	 * @return
	 */
	public static String currentPlain_yyyyMMdd()
	{
		return getFmtPlain_yyyyMMdd().format(new Date()) ;
	}
	
	/**
	 * 日期格式：yyyyMMdd_HHmmss.SSS
	 * @return
	 */
	static DateFormat getFmtPlain_yyyyMMddHHmmssSSS()
	{
		if(sSDF_Plain_yyyyMMddHHmmssSSS == null)
		{
			synchronized(sMutext)
			{
				if(sSDF_Plain_yyyyMMddHHmmssSSS == null)
					sSDF_Plain_yyyyMMddHHmmssSSS = new ThreadLocal<>() ;
			}
		}
		DateFormat fmt = sSDF_Plain_yyyyMMddHHmmssSSS.get() ;
		if(fmt == null)
		{
			fmt = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS") ;
			sSDF_Plain_yyyyMMddHHmmssSSS.set(fmt);
		}
		return fmt ;
	}
	
	public static String formatPlain_yyyyMMddHHmmssSSS(Date aDate)
	{
		return getFmtPlain_yyyyMMddHHmmssSSS().format(aDate) ;
	}
	
	/**
	 * 日期格式：yyyyMMdd_HHmmss.SSS
	 * @return
	 */
	public static String currentPlain_yyyyMMddHHmmssSSS()
	{
		return getFmtPlain_yyyyMMddHHmmssSSS().format(new Date()) ;
	}
	
	/**
	 * 日期格式：yyMMdd_HHmmss
	 * @return
	 */
	public static String currentPlain_yyMMddHHmmss()
	{
		return getFmtPlain_yyMMddHHmmss().format(new Date()) ;
	}
	
	/**
	 * 日期格式：yyyyMMdd_HHmmss
	 * @return
	 */
	public static String currentPlain_yyyyMMddHHmmss()
	{
		return getFmtPlain_yyyyMMddHHmmss().format(new Date()) ;
	}
	
	public static Date getSourceTime(XTimeUnit aUnit)
	{
		Calendar calendar = GregorianCalendar.getInstance() ;
		switch(aUnit)
		{
		case HOUR:
			calendar.set(Calendar.HOUR_OF_DAY, 0) ;
		case MINUTE:
			calendar.set(Calendar.MINUTE, 0) ;
		case SECOND:
			calendar.set(Calendar.SECOND, 0) ;
		default:
		}
		return calendar.getTime() ;
	}
	
	/**
	 * aTime1-aTime0
	 * @param aTime0
	 * @param aTime1
	 * @param aTimeUnit
	 * @return
	 */
	public static int getDiff(Date aTime0 , Date aTime1 , XTimeUnit aTimeUnit)
	{
		long diff = aTime1.getTime()-aTime0.getTime() ;
		switch (aTimeUnit)
		{
		case SECOND:
			return (int) (diff/1000) ;
		case MINUTE:
			return (int) (diff/60000) ;
		case HOUR:
			return (int) (diff/3600000) ;
		default:
			throw new RuntimeException("未实现") ;
		}
		
	}
	
	public static String getDisplayName(XTimeUnit aTimeUnit , int aBack)
	{
		if(aTimeUnit == XTimeUnit.DAY)
		{
			if(aBack == 0)
				return "今天" ;
			else if(aBack == 1)
				return "昨天" ;
			else if(aBack == 2)
				return "前天" ;
			else if(aBack>0)
				return aBack+"天前" ;
			else if(aBack == -1)
				return "明天" ;
			else if(aBack == -2)
				return "后天" ;
			else if(aBack>0)
				return aBack+"天后" ;
		}
		return "" ;
	}
	
	public static char getTimeUnitMark(XTimeUnit aTimeUnit)
	{
		switch(aTimeUnit)
		{
		case SECOND:
			return 's' ;
		case MINUTE:
			return 'm' ;
		case HOUR:
			return 'H' ;
		case DAY:
			return 'd' ;
		case WEEK:
			return 'w' ;
		case MONTH:
			return 'M' ;
		case YEAR:
			return 'y' ;
		default:
			throw new IllegalStateException("未知时间单位:"+aTimeUnit.name()) ;
		}
	}
	
	public static XTimeUnit getTimeUnitByMark(char aCh)
	{
		switch(aCh)
		{
		case 's':
			return XTimeUnit.SECOND ;
		case 'm':
			return XTimeUnit.MINUTE ;
		case 'H':
		case 'h':
			return XTimeUnit.HOUR ;
		case 'd':
			return XTimeUnit.DAY ;
		case 'w':
			return XTimeUnit.WEEK ;
		case 'M':
			return XTimeUnit.MONTH ;
		case 'y':
			return XTimeUnit.YEAR ;
		default:
			throw new IllegalStateException("未知时间单位："+aCh) ;
		}
	}
	
	
	static DateFormat getFmt$HHmmssSSS()
	{
		if(sSDF_HHmmss_SSS == null)
		{
			synchronized(sMutext)
			{
				if(sSDF_HHmmss_SSS == null)
					sSDF_HHmmss_SSS = new ThreadLocal<>() ;
			}
		}
		DateFormat fmt = sSDF_HHmmss_SSS.get() ;
		if(fmt == null)
		{
			fmt = new SimpleDateFormat("HH:mm:ss.SSS") ;
			sSDF_HHmmss_SSS.set(fmt);
		}
		return fmt ;
	}
	
	/**
	 * 日期格式：HH:mm:ss.SSS
	 * @param aDate
	 * @return
	 */
	public static String format$HHmmssSSS(Date aDate)
	{
		return getFmt$HHmmssSSS().format(aDate) ;
	}
	
	static DateFormat getFmtPlain_yyyyMMdd()
	{
		if(sSDF_Plain_yyyyMMdd == null)
		{
			synchronized(sMutext)
			{
				if(sSDF_Plain_yyyyMMdd == null)
					sSDF_Plain_yyyyMMdd = new ThreadLocal<>() ;
			}
		}
		DateFormat fmt = sSDF_Plain_yyyyMMdd.get() ;
		if(fmt == null)
		{
			fmt = new SimpleDateFormat("yyyyMMdd") ;
			sSDF_Plain_yyyyMMdd.set(fmt);
		}
		return fmt ;
	}
	
	static DateFormat getFmtISO8601_yyyyMMddHHmmssSSS()
	{
		if(sSDF_ISO_8601_yyyyMMddHHmmssSSS == null)
		{
			synchronized(sMutext)
			{
				if(sSDF_ISO_8601_yyyyMMddHHmmssSSS == null)
					sSDF_ISO_8601_yyyyMMddHHmmssSSS = new ThreadLocal<>() ;
			}
		}
		DateFormat fmt = sSDF_ISO_8601_yyyyMMddHHmmssSSS.get() ;
		if(fmt == null)
		{
			fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") ;
			sSDF_ISO_8601_yyyyMMddHHmmssSSS.set(fmt);
		}
		return fmt ;
	}
	
	static DateFormat getFmtISO8601_yyyyMMddHHmmss()
	{
		if(sSDF_ISO_8601_yyyyMMddHHmmss == null)
		{
			synchronized(sMutext)
			{
				if(sSDF_ISO_8601_yyyyMMddHHmmss == null)
					sSDF_ISO_8601_yyyyMMddHHmmss = new ThreadLocal<>() ;
			}
		}
		DateFormat fmt = sSDF_ISO_8601_yyyyMMddHHmmss.get() ;
		if(fmt == null)
		{
			fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'") ;
			sSDF_ISO_8601_yyyyMMddHHmmss.set(fmt);
		}
		return fmt ;
	}
	
	public static String formatISO8601_yyyyMMddHHmmssSSS(Date aDate)
	{
		return aDate==null?null:getFmtISO8601_yyyyMMddHHmmssSSS().format(aDate) ;
	}
	
	public static String formatISO8601_yyyyMMddHHmmss(Date aDate)
	{
		return aDate==null?null:getFmtISO8601_yyyyMMddHHmmss().format(aDate) ;
	}
	
	static DateFormat getFmtGMT()
	{
		if(sSDF_GMT == null)
		{
			synchronized(sMutext)
			{
				if(sSDF_GMT == null)
					sSDF_GMT = new ThreadLocal<>() ;
			}
		}
		DateFormat fmt = sSDF_GMT.get() ;
		if(fmt == null)
		{
			fmt = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);  
//			fmt.setTimeZone(TimeZone.getTimeZone("GMT+8")); // 设置时区为GMT  +8为北京时间东八区
			fmt.setTimeZone(TimeZone.getTimeZone("GMT")); // 设置时区为GMT
			sSDF_GMT.set(fmt);
		}
		return fmt ;
	}
	
	/**
	 * 日期格式：yyyyMMdd
	 * @param aDate
	 * @return
	 */
	public static String formatPlain_yyyyMMdd(Date aDate)
	{
		return getFmtPlain_yyyyMMdd().format(aDate) ;
	}
	
	/**
	 * 日期格式：yyyyMMdd
	 * @param aText
	 * @return
	 * @throws ParseException 
	 */
	public static Date parsePlain_yyyyMMdd(String aText) throws ParseException
	{
		return getFmtPlain_yyyyMMdd().parse(aText) ;
	}
	
	public static Date parsePlain_yyyyMMdd_0(String aText)
	{
		try
		{
			return getFmtPlain_yyyyMMdd().parse(aText) ;
		}
		catch (ParseException e)
		{
			return null ;
		}
	}
	
	public static Date parsePlain_yyyyMM(String aText) throws ParseException
	{
		return getFmtPlain_yyyyMM().parse(aText) ;
	}
	
	/**
	 * 当前距离aLastTime是否已经过去指定的时间
	 * @param aLastTime				单位毫秒
	 * @param aTime					单位aTimeUnit
	 * @param aTimeUnit
	 * @return
	 */
	public static boolean pass(long aLastTime , int aTime , XTimeUnit aTimeUnit)
	{
		return pass(aLastTime , aTimeUnit.toMillis(aTime)) ;
	}
	
	public static boolean pass(Instant aLastTime , int aTime , XTimeUnit aTimeUnit)
	{
		return pass(aLastTime.toEpochMilli() , aTimeUnit.toMillis(aTime)) ;
	}
	
	public static boolean pass(long aLastTime , long aMilliSeconds)
	{
		return System.currentTimeMillis()-aLastTime >= aMilliSeconds ;
	}
	
	public static Date plusHours(Date aTime ,  int aHours)
	{
		return aHours == 0?aTime:new Date(aTime.getTime() + TimeUnit.HOURS.toMillis(aHours)) ;
	}
	
	public static Date plusDays(Date aTime , int aDays)
	{
		return aDays == 0 ?aTime:new Date(aTime.getTime() + TimeUnit.DAYS.toMillis(aDays)) ;
	}
	
	public static Date plus(Date aTime , XTimeUnit aTimeUnit , int aDuration)
	{
		if(aTime == null)
			return null ;
		if(aDuration == 0)
			return aTime ;
		Calendar cal = Calendar.getInstance() ;
		cal.setTime(aTime) ;
		cal.add(aTimeUnit.toCalendar() , aDuration);
		return cal.getTime() ;
	}
	
	/**
	 * 自适应解析时间
	 * @param aText
	 * @return
	 * @throws ParseException 
	 */
	public static Date adaptiveParse(String aText) throws ParseException
	{
		AutoSortedList<CountElement<DateFormat>> fmtList = mFmtListTL.get() ;
		if(fmtList == null)
		{
			fmtList = AutoSortedList.createCountSortList() ;
			fillDateFmt(fmtList) ;
			mFmtListTL.set(fmtList) ;
		}
		final int size = fmtList.size() ;
		for(int i=0 ; i<size ; i++)
		{
			try
			{
				CountElement<DateFormat> count = fmtList.get(i) ;
				Date date = count.getValue().parse(aText) ;
				count.plus();
				return date ;
			}
			catch (ParseException e)
			{}
		}
		throw new ParseException(String.format("无法解析日期：%1$s，字符数量：%2$d，日期格式解析器数量：%3$d" 
				, aText , aText!=null?aText.length():0  , size) , 0) ;
	}
	
	private static void fillDateFmt(AutoSortedList<CountElement<DateFormat>> aFmtList)
	{
		aFmtList.add(new CountElement<>(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))) ;
		aFmtList.add(new CountElement<>(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"))) ;
		aFmtList.add(new CountElement<>(new SimpleDateFormat("yyyy-MM-dd"))) ;
		aFmtList.add(new CountElement<>(new SimpleDateFormat("yyyyMMdd_HHmmss"))) ;
		aFmtList.add(new CountElement<>(new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss"))) ;
		aFmtList.add(new CountElement<>(new SimpleDateFormat("dd-MMM-yyyy"))) ;
	}
	
	/**
	 * 现在相对于过去的某个时刻，已经超过指定时间
	 * @param aTime
	 * @param aDuration
	 * @param aTimeUnit
	 * @return
	 */
	public static boolean longThan(long aPassedTime , int aDuration , XTimeUnit aTimeUnit)
	{
		return System.currentTimeMillis() - aPassedTime > aTimeUnit.toMillis(aDuration) ;
	}
	
	public static Date today()
	{
		return Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()) ;
	}
	
	public static Date tomorrow()
	{
		return Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).plusDays(1).toInstant()) ;
	}
	
	public static Date yesterday()
	{
		return Date.from(LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()) ;
	}
	
	public static Date currentMonth()
	{
		return Date.from(LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant()) ;
	}
	
	public static Date currentHour()
	{
		return Date.from(Instant.now().truncatedTo(ChronoUnit.HOURS)) ;
	}
	
	public static Date currentYear()
	{
		return Date.from(LocalDate.now().withDayOfYear(1).atStartOfDay(ZoneId.systemDefault()).toInstant()) ;
	}
	
	public static String getDisplayText(int aSeconds)
	{
		StringBuilder strBld = new StringBuilder() ;
		if(aSeconds>=60)
		{
			int minutes = aSeconds/60 ;
			aSeconds %= 60 ;
			if(minutes>=60)
			{
				int hours = minutes/60 ;
				minutes %= 60 ;
				if(hours>=24)
				{
					int day = hours/24 ;
					hours %= 24 ;
					strBld.append(day).append("天") ;
				}
				if(hours != 0)
					strBld.append(hours).append("小时") ;
			}
			if(minutes != 0)
				strBld.append(minutes).append("分") ;
		}
		if(aSeconds != 0 || strBld.length() == 0)
			strBld.append(aSeconds).append("秒") ;
		return strBld.toString() ;
	}
	
	public static String getDisplayTextContainsMs(long aMillSeconds)
	{
		StringBuilder strBld = new StringBuilder() ;
		
		if(aMillSeconds>=1000)
		{
			long seconds = aMillSeconds/1000 ;
			aMillSeconds %= 1000 ;
			if(seconds>=60)
			{
				int minutes = (int)(seconds/60) ;
				seconds %= 60 ;
				if(minutes>=60)
				{
					int hours = minutes/60 ;
					minutes %= 60 ;
					if(hours>=24)
					{
						int day = hours/24 ;
						hours %= 24 ;
						strBld.append(day).append("天") ;
					}
					if(hours != 0)
						strBld.append(hours).append("小时") ;
				}
				if(minutes != 0)
					strBld.append(minutes).append("分") ;
			}
			if(seconds != 0)
				strBld.append(seconds).append("秒") ;
		}
		if(aMillSeconds != 0 || strBld.length() == 0)
			strBld.append(aMillSeconds).append("毫秒") ;
		return strBld.toString() ;
	}
	
	/**
	 * 从天数上来看相差多少
	 * @param aDate1
	 * @param aDate2
	 * @return
	 */
	public static int diffDays(Date aDate1 , Date aDate2)
	{
		return diffDays(aDate1.getTime() , aDate2.getTime()) ;
	}
	
	public static int diffDays(long aTimeInMillSecs1 , long aTimeInMillSecs2)
	{
		final long dayMillSecs = 24*3600000L ;
		return (int)(aTimeInMillSecs1/dayMillSecs-aTimeInMillSecs2/dayMillSecs) ;
	}
	
	public static Date currentUTC()
	{
		return new Date(System.currentTimeMillis() - sUTCOffset) ;
	}
	
	public static long currentTimeInSeconds()
	{
		return System.currentTimeMillis()/1000 ;
	}
	
	public static Date toUTC(Date aDate)
	{
		return new Date(aDate.getTime() - sUTCOffset) ;
	}
	
	public static String currentInGMT()
	{
		return getFmtGMT().format(new Date()) ;
	}
	
	public static long getDurationInMillSecs(String aDurationText)
	{
		Matcher matcher = sDurationPtn.matcher(aDurationText) ;
		Assert.isTrue(matcher.matches() , "指定的时间长度%s格式不符合要求，要求是“整数”+[y,M,d,H,m,s]其中之一");
		String d = matcher.group(1) ;
		String u = matcher.group(2) ;
		if(XString.isEmpty(u))
			return Long.parseLong(d) ;
		else
			return XTime.getTimeUnitByMark(u.charAt(0)).toMillis(Integer.parseInt(d)) ;
	}
	
	public static Date getStartOfYear(int aYear)
	{
		return Date.from(LocalDate.of(aYear, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()) ;
	}
	
	public static Date getEndOfDay(Date aDate)
	{
		return Date.from(aDate.toInstant().atZone(ZoneId.systemDefault()).withHour(23).withMinute(59).withSecond(59).withNano(999_999_999).toInstant()) ;
	}
	
	/**
	 * 
	 * @param aDate
	 * @return 1-12，如果aDate为null，将返回-1
	 */
	public static int getMonth(Date aDate)
	{
		return aDate==null?-1:aDate.toInstant().atZone(ZoneId.systemDefault()).getMonthValue() ;
	}
	
	public static int getYear(Date aDate)
	{
		return aDate==null?-1:aDate.toInstant().atZone(ZoneId.systemDefault()).getYear() ;
	}
	
	public static boolean isToday(Date aDate)
	{
		long testTime = aDate.getTime() ;
		long beginTime = today().getTime() ;
		return testTime>=beginTime && testTime<beginTime+XTimeUnit.DAY.toMillis(1) ;
	}
	
	/**
	 * 指定时间的下一天，时分秒都是0
	 * @param aDate
	 * @return
	 */
	public static Date nextDay(Date aDate)
	{
		return Date.from(aDate.toInstant().atZone(ZoneId.systemDefault()).plus(1, ChronoUnit.DAYS)
				.truncatedTo(ChronoUnit.DAYS)
				.toInstant()) ;
	}
	
	public static Date min(Date... aDates)
	{
		if(XC.isEmpty(aDates))
			return null ;
		Date min = null ;
		for(Date date : aDates)
		{
			if(min == null)
				min = date ;
			else if(date != null && date.before(min))
			{
				min = date ;
			}
		}
		return min ;
	}
}
