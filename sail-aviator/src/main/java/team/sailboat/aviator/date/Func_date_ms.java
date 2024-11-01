package team.sailboat.aviator.date ;

import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorNil;
import com.googlecode.aviator.runtime.type.AviatorObject;

import team.sailboat.commons.fan.lang.XClassUtil;

public class Func_date_ms extends AbstractFunction
{

	private static final long serialVersionUID = 1L;

	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject arg1)
	{
		Object val = arg1.getValue(env) ;
		return val == null?AviatorNil.NIL : AviatorLong.valueOf(XClassUtil.toDate(val).getTime());
	}

	@Override
	public String getName()
	{
		return "date.ms";
	}

}
