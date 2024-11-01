package team.sailboat.aviator.string ;

import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorNil;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.text.XString;

/**
 * 
 *
 * @author yyl
 * @since 2024年9月30日
 */
public class Func_str_var_fmt extends AbstractFunction
{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aEles , AviatorObject aMap )
	{
		String msgFmt = XClassUtil.toString(aEles.getValue(aEnv)) ;
		if(msgFmt == null)
			return AviatorNil.NIL ;
		if(msgFmt.isEmpty())
			return AviatorRuntimeJavaType.valueOf(msgFmt) ;
		else
			return AviatorRuntimeJavaType.valueOf(XString.format(msgFmt , (Map) aMap.getValue(aEnv))) ;
	}

	@Override
	public String getName()
	{
		return "str.var_fmt" ;
	}

}
