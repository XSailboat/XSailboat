package team.sailboat.aviator.common;

import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import team.sailboat.commons.fan.lang.XClassUtil;

public class Func_toBool extends AbstractFunction
{

	private static final long serialVersionUID = 1L;

	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aArg1 , AviatorObject aArg2)
	{
		Object val = aArg1.getValue(aEnv) ;
		return AviatorRuntimeJavaType.valueOf(val == null?aArg2.getValue(aEnv):XClassUtil.assetBoolean(val)) ;
	}

	@Override
	public String getName()
	{
		return "toBool";
	}
}