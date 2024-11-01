package team.sailboat.aviator.date;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorNil;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.fan.time.XTime;

public class Func_date_add extends AbstractFunction
{

	private static final long serialVersionUID = 1L;
	
	static Map<String , Function<Date, String>> sFmtMap = XC.hashMap() ;
	static
	{
		sFmtMap.put("yyyy-MM-dd HH:mm:ss.SSS" , XTime::format$yyyyMMddHHmmssSSS) ;
		sFmtMap.put("yyyy-MM-dd HH:mm:ss", XTime::format$yyyyMMddHHmmss) ;
		sFmtMap.put("yyyy-MM-dd", XTime::format$yyyyMMdd) ;
		sFmtMap.put("yyyy-MM", XTime::format$yyyyMM) ;
	}

	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject arg1 , AviatorObject arg2
			 , AviatorObject arg3)
	{
		Date date = XClassUtil.toDate(arg1.getValue(env)) ;
		if(date == null)
			return AviatorNil.NIL ;
		
		
		Long delta = XClassUtil.toLong(arg2.getValue(env)) ;
		if(delta != null)
		{
			String unit = XClassUtil.toString(arg3.getValue(env)) ;
			if(XString.isEmpty(unit))
				unit = "S" ;
			switch(unit)
			{
			case "S":
				date = new Date(date.getTime() + delta) ;
				break ;
			default:
				date = XTime.plus(date, XTime.getTimeUnitByMark(unit.charAt(0)) , delta.intValue()) ;
				break ;
			}
		}
		return AviatorRuntimeJavaType.valueOf(date) ;
	}

	@Override
	public String getName()
	{
		return "date.add";
	}

}