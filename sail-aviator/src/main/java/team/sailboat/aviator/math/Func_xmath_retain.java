package team.sailboat.aviator.math;

import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorDouble;
import com.googlecode.aviator.runtime.type.AviatorNil;
import com.googlecode.aviator.runtime.type.AviatorObject;

import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.math.XMath;

/**
 * 
 *
 * @author yyl
 * @since 2024年9月30日
 */
public class Func_xmath_retain extends AbstractFunction
{

	private static final long serialVersionUID = 1L;

	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject arg1 , AviatorObject arg2)
	{
		Double val = XClassUtil.toDouble(arg1.getValue(env)) ;
		if(val == null)
			return AviatorNil.NIL ;
		
		Integer precision = XClassUtil.toInteger(arg2.getValue(env)) ;
		double v = val.doubleValue() ;
		if(precision != null)
		{
			v = XMath.retainEffectDigits(v, precision) ;
		}
		return new AviatorDouble(v) ;
	}

	@Override
	public String getName()
	{
		return "xmath.retain";
	}

}