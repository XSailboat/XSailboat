package team.sailboat.aviator.json;

import java.util.Collection;
import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorNil;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.XClassUtil;

public class Func_json_to_string extends AbstractFunction
{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aObj)
	{
		String val = toJSONString(aObj.getValue(aEnv)) ;
		if(val == null)
			return AviatorNil.NIL ;
		return new AviatorString(XClassUtil.toString(val)) ;
	}

	@Override
	public String getName()
	{
		return "json.to_string" ;
	}
	
	public static Map<String , Object> of(Map<String, Object> aEnv, AviatorObject aObj)
	{
		Object obj = aObj.getValue(aEnv) ;
		Map<String, Object> map = XC.linkedHashMap() ;
		if(obj != null)
		{
			new JSONObject(obj.toString()).toMap(map) ;
		}
		return map ;
	}
	
	public static String toJSONString(Object aVal)
	{
		if(aVal == null)
			return null ;
		if(aVal instanceof Map)
			return JSONObject.of((Map<String, Object>)aVal).toJSONString() ;
		else if(aVal instanceof Collection)
			return new JSONArray((Collection)aVal).toJSONString() ;
		else if(aVal.getClass().isArray())
			return new JSONArray(aVal).toJSONString() ;
		else 
			return XClassUtil.toString(aVal) ;
	}

}
