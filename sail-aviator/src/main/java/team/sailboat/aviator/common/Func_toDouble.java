package team.sailboat.aviator.common;

import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorNil;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import team.sailboat.commons.fan.lang.XClassUtil;

public class Func_toDouble extends AbstractFunction
{

	private static final long serialVersionUID = 1L;
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aArg1)
	{
		Object val = aArg1.getValue(aEnv) ;
		Double v = XClassUtil.toDouble(val) ;
		if(v == null)
			return AviatorNil.NIL ;
		return AviatorRuntimeJavaType.valueOf(v) ;
	}


	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aArg1 , AviatorObject aArg2)
	{
		Object val = aArg1.getValue(aEnv) ;
		Double v = XClassUtil.toDouble(val) ;
		if(v == null)
			v = XClassUtil.assetDouble(aArg2.getValue(aEnv)) ;
		return AviatorRuntimeJavaType.valueOf(v) ;
	}

	@Override
	public String getName()
	{
		return "toDouble";
	}
}