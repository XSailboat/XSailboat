package team.sailboat.aviator.common;

import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorNil;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

public class Func_stype extends AbstractFunction
{

	private static final long serialVersionUID = 1L;

	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aArg1)
	{
		Object val = aArg1.getValue(aEnv) ;
		return val == null?AviatorNil.NIL:new AviatorString(val.getClass().getSimpleName()) ;
	}

	@Override
	public String getName()
	{
		return "stype" ;
	}
}