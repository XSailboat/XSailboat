package team.sailboat.aviator.string ;

import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.text.IDGen;

/**
 * 
 *
 * @author yyl
 * @since 2024年9月30日
 */
public class Func_gen_id extends AbstractFunction
{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aStr)
	{
		Object value = aStr.getValue(aEnv) ;
		return AviatorRuntimeJavaType.valueOf(IDGen.newID(XClassUtil.toString(value) , 5)) ;
	}

	@Override
	public String getName()
	{
		return "gen_id" ;
	}

}
