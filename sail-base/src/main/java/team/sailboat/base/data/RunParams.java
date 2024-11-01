package team.sailboat.base.data;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.time.XTimeUnit;

public class RunParams
{
	static long sLastDay = 0 ;
	static Map<String, Object> sParamMap ;
	static long sDays2 = XTimeUnit.DAY.toMillis(2) ;
	static
	{
		sLastDay = injectParams() ;
	}
	
	static synchronized long injectParams()
	{
		ZonedDateTime zd = LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()) ;
		Map<String, Object> map = XC.hashMap() ;
		map.put("year", zd.getYear()) ;
		map.put("month", zd.getMonth()) ;
		map.put("day", zd.getDayOfMonth()) ;
		sParamMap = map ;
		return zd.toInstant().toEpochMilli() ;
	}
	
	public static Map<String, Object> getParamMap()
	{
		if(System.currentTimeMillis()-sLastDay>sDays2)
		{
			sLastDay = injectParams() ;
		}
		return sParamMap ;
	}
}
