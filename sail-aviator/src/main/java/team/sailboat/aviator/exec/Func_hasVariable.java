package team.sailboat.aviator.exec;

import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import team.sailboat.commons.fan.lang.XClassUtil;

public class Func_hasVariable extends AbstractFunction
{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aArg1 , AviatorObject aDefaultValue)
	{
		String arg = XClassUtil.toString(aArg1.getValue(aEnv)) ;
		return AviatorRuntimeJavaType.valueOf(aEnv.containsKey(arg)) ;
	}

	@Override
	public String getName()
	{
		return "hasVariable" ;
	}

}
