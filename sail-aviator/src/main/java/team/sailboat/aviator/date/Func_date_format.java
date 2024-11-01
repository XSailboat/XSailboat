package team.sailboat.aviator.date ;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.time.XTime;

public class Func_date_format extends AbstractFunction
{

	private static final long serialVersionUID = 1L;
	
	static Map<String , Function<Date, String>> sFmtMap = XC.hashMap() ;
	static
	{
		sFmtMap.put("yyyy-MM-dd HH:mm:ss.SSS" , XTime::format$yyyyMMddHHmmssSSS) ;
		sFmtMap.put("yyyy-MM-dd HH:mm:ss", XTime::format$yyyyMMddHHmmss) ;
		sFmtMap.put("yyyy-MM-dd", XTime::format$yyyyMMdd) ;
		sFmtMap.put("yyyy-MM", XTime::format$yyyyMM) ;
		sFmtMap.put("yyyy", date->Integer.valueOf(date.toInstant().atZone(ZoneId.systemDefault()).getYear()).toString()) ;
	}

	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject arg1 , AviatorObject arg2)
	{
		String fmt = XClassUtil.toString(arg1.getValue(env)) ;
		Date date = XClassUtil.toDate(arg2.getValue(env)) ;
		if(date == null)
			return null ;
		Function<Date , String> func = sFmtMap.get(fmt) ;
		String dateStr = null ;
		if(func != null)
			dateStr = func.apply(date) ;
		else
			dateStr = new SimpleDateFormat(fmt).format(date) ;
		return AviatorRuntimeJavaType.valueOf(dateStr) ;
	}

	@Override
	public String getName()
	{
		return "date.format";
	}

}
