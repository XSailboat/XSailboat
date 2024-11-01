package team.sailboat.aviator.json;

import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import team.sailboat.commons.fan.json.JSONArray;

/**
 * 
 *
 * @author yyl
 * @since 2024年9月30日
 */
public class Func_j_array extends AbstractFunction
{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv)
	{
		return AviatorRuntimeJavaType.valueOf(new JSONArray()) ;
	}
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aJa)
	{
		return AviatorRuntimeJavaType.valueOf(of(aEnv, aJa)) ;
	}

	@Override
	public String getName()
	{
		return "j.array" ;
	}
	
	public static JSONArray of(Map<String, Object> aEnv, AviatorObject aJa)
	{
		Object ja = aJa.getValue(aEnv) ;
		if(ja == null)
			return null ;
		
		if(ja instanceof JSONArray)
			return (JSONArray)ja ;
		
		return new JSONArray(ja) ;
	}

}
