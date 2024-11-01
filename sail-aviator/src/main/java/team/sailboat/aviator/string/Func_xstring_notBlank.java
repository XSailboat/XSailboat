package team.sailboat.aviator.string ;

import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;

import team.sailboat.commons.fan.lang.XClassUtil;

/**
 * 
 *
 * @author yyl
 * @since 2024年9月30日
 */
public class Func_xstring_notBlank extends AbstractFunction
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
			return AviatorBoolean.FALSE ;
		String v = XClassUtil.toString(value) ;
		if(v.isEmpty() || v.trim().isEmpty())
			return AviatorBoolean.FALSE ;
		return AviatorBoolean.TRUE ;
	}

	@Override
	public String getName()
	{
		return "xstring.notBlank" ;
	}

}
