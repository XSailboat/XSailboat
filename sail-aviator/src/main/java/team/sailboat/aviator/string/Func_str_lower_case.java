package team.sailboat.aviator.string ;

import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorNil;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import team.sailboat.commons.fan.lang.XClassUtil;

/**
 * 
 *
 * @author yyl
 * @since 2024年9月30日
 */
public class Func_str_lower_case extends AbstractFunction
{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aStr)
	{
		Object value = aStr.getValue(aEnv) ;
		if(value == null)
			return AviatorNil.NIL ;
		return AviatorRuntimeJavaType.valueOf(XClassUtil.toString(value).toLowerCase()) ;
	}

	@Override
	public String getName()
	{
		return "str.lower_case" ;
	}

}
