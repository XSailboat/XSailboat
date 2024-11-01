package team.sailboat.commons.fan.adapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import team.sailboat.commons.fan.excep.WrapException;

public class TA_Date implements ITypeAdapter<Date>
{

	static final DateFormat sSDF_yyyy = new SimpleDateFormat("yyyy") ;
	static final DateFormat sSDF_yyyyMM = new SimpleDateFormat("yyyy-MM") ;
	static final DateFormat sSDF_yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd") ;
	static final DateFormat sSDF_yyyyMMddHHmmss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
	
	public static String format(Date aDate , String aRefer)
	{
		int strLen = aRefer.length() ;
		try
		{
			if(strLen == 4)
			{
				return sSDF_yyyy.format(aDate) ;
			}
			else if(strLen <= 7)
			{
				return sSDF_yyyyMM.format(aDate) ;
			}
			else if(strLen <= 10)
			{
				return sSDF_yyyyMMdd.format(aDate) ;
			}
			else
				return sSDF_yyyyMMddHHmmss.format(aDate) ;
		}
		catch(Exception e)
		{
			WrapException.wrapThrow(e) ;
			return null ;			// daad code
		}
	}

	@Override
	public Date apply(Object aT)
	{
		if(aT == null)
			return null ;
		if(aT instanceof Date)
			return (Date)aT ;
		if(aT instanceof Long)
			return new Date((Long)aT) ;
		if(aT instanceof String)
		{
			String dateStr = (String)aT ;
			int strLen = dateStr.length() ; 
			try
			{
				if(strLen == 4)
				{
					return sSDF_yyyy.parse(dateStr) ;
				}
				else if(strLen <= 7)
				{
					return sSDF_yyyyMM.parse(dateStr) ;
				}
				else if(strLen <= 10)
				{
					return sSDF_yyyyMMdd.parse(dateStr) ;
				}
				else
					return sSDF_yyyyMMddHHmmss.parse(dateStr) ;
			}
			catch(ParseException e)
			{
				WrapException.wrapThrow(e) ;
			}
		}
		if(aT instanceof LocalDateTime ldt)
		{
			return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()) ;
		}
		throw new IllegalArgumentException("不支持转成Date的类型："+aT.getClass()) ;
	}

	@Override
	public Class<Date> getType()
	{
		return Date.class ;
	}

}
