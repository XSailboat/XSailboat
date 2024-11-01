package team.sailboat.aviator.json;

import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import team.sailboat.commons.fan.json.JSONObject;

public class Func_j_object extends AbstractFunction
{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv)
	{
		return AviatorRuntimeJavaType.valueOf(new JSONObject()) ;
	}
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aObj)
	{
		return AviatorRuntimeJavaType.valueOf(of(aEnv, aObj)) ;
	}

	@Override
	public String getName()
	{
		return "j.object" ;
	}
	
	public static JSONObject of(Map<String, Object> aEnv, AviatorObject aObj)
	{
		Object obj = aObj.getValue(aEnv) ;
		if(obj == null)
			return new JSONObject() ;
		if(obj instanceof JSONObject)
			return (JSONObject)obj ;
		
		return new JSONObject(obj) ;
	}

}
