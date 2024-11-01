package team.sailboat.aviator.date;

import java.util.Date;
import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorNil;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.time.XTime;

public class Func_date_truncate extends AbstractFunction
{

	private static final long serialVersionUID = 1L;

	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject arg1 , AviatorObject arg2)
	{
		Date date = XClassUtil.toDate(arg1.getValue(env)) ;
		if(date == null)
			return AviatorNil.NIL ;
		
		String timeUnitMark = XClassUtil.toString(arg2.getValue(env)) ;
		Assert.notEmpty(timeUnitMark , "没有指定时间单位！") ;
		timeUnitMark = timeUnitMark.trim() ;
		Assert.isTrue(timeUnitMark.length() == 1 , "指定的时间单位[%s]不合法！" , timeUnitMark) ;
		date = Date.from(date.toInstant().truncatedTo(XTime.getTimeUnitByMark(timeUnitMark.charAt(0)).toTemporalUnit())) ;
		return AviatorRuntimeJavaType.valueOf(date) ;
	}

	@Override
	public String getName()
	{
		return "date.truncate";
	}

}