package team.sailboat.aviator.string ;

import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorNil;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.text.XString;

/**
 * 
 *
 * @author yyl
 * @since 2024年9月30日
 */
public class Func_str_join extends AbstractFunction
{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aEles , AviatorObject aJoinStr )
	{
		Object value = aEles.getValue(aEnv) ;
		if(value == null)
			return AviatorNil.NIL ;
		String result = null ;
		String joinStr = XClassUtil.toString(aJoinStr.getValue(aEnv)) ;
		if(value.getClass().isArray())
		{
			result = XString.toString(joinStr , (Object[])value) ;
		}
		else if(value instanceof Iterable<?>)
		{
			result = XString.toString(joinStr , (Iterable<?>)value) ;
		}
		else
			result = JCommon.toString(value) ;
		return AviatorRuntimeJavaType.valueOf(result) ;
	}

	@Override
	public String getName()
	{
		return "str.join" ;
	}

}
