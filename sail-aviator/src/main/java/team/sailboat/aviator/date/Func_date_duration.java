package team.sailboat.aviator.date;

import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import team.sailboat.aviator.bean.DateDuration;
import team.sailboat.commons.fan.lang.XClassUtil;

public class Func_date_duration extends AbstractFunction
{

	private static final long serialVersionUID = 1L;
	
	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject arg1 , AviatorObject arg2)
	{
		return AviatorRuntimeJavaType.valueOf(new DateDuration(XClassUtil.toInteger(arg1.getValue(env))
				, XClassUtil.toString(arg2.getValue(env)).charAt(0))) ;
	}

	@Override
	public String getName()
	{
		return "date.duration";
	}

}

