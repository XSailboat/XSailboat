package team.sailboat.aviator.json;

import java.util.List;
import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.fan.text.XStringReader;

public class Func_json_put extends AbstractFunction
{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aJa , AviatorObject aValue)
	{
		Object obj = aJa.getValue(aEnv) ;
		if(obj instanceof JSONArray)
		{
			((JSONArray)obj).put(aValue.getValue(aEnv)) ;
			return AviatorRuntimeJavaType.valueOf(obj) ;
		}
		else
		{
			List<Object> list = Func_json_array.of(aEnv, aJa) ;
			list.add(aValue.getValue(aEnv)) ;
			return AviatorRuntimeJavaType.valueOf(list) ;
		}
	}
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aArg1, AviatorObject aArg2, AviatorObject aArg3)
	{
		Object obj = aArg1.getValue(aEnv) ;
		Object val = aArg2.getValue(aEnv) ;
		if(isJSONObject(obj))
		{
			String key = XClassUtil.toString(val) ;
			val = aArg3.getValue(aEnv) ;
			Map<String, Object> map = toMap(obj) ;
			map.put(key, val) ;
			return AviatorRuntimeJavaType.valueOf(map) ;
		}
		else if(isJSONArray(obj))
		{
			List list = toList(obj) ;
			Object val2 = aArg3.getValue(aEnv) ;
			if(val2 == null)
				list.add(val) ;
			else if(val2 instanceof Number)
			{
				int index = ((Number)val2).intValue() ;
				if(index < 0 || index >=list.size())
					list.add(val) ;
				else
					list.set(index , val) ;
			}
			else if(!XClassUtil.toBoolean(val2 , false)		// ignoreNull，忽略空值
						|| (val != null && (val instanceof String && XString.isNotEmpty((String)val))))
			{
				list.add(val) ;
			}
			return AviatorRuntimeJavaType.valueOf(list) ;
		}
		else if(obj instanceof JSONObject)
		{
			((JSONObject)obj).put(XClassUtil.toString(val) , aArg3.getValue(aEnv)) ;
			return AviatorRuntimeJavaType.valueOf(obj) ;
		}
		else if(obj instanceof JSONArray)
		{
			Object val2 = aArg3.getValue(aEnv) ;
			if(val2 == null)
				((JSONArray)obj).put(val) ;
			else if(val2 instanceof Number)
			{
				int index = ((Number)val2).intValue() ;
				if(index < 0 || index >=((JSONArray)obj).size())
					((JSONArray)obj).put(val) ;
				else
					((JSONArray)obj).put(index , val) ;
			}
			else if(!XClassUtil.toBoolean(val2 , false)		// ignoreNull，忽略空值
						|| (val != null && (val instanceof String && XString.isNotEmpty((String)val))))
			{
				((JSONArray)obj).put(val) ;
			}
			return AviatorRuntimeJavaType.valueOf(obj) ;
		}
		throw new IllegalArgumentException("指定的第1个参数无法解析成JSONObject或者JSONArray！") ;
	}
	
	boolean isJSONObject(Object aObj)
	{
		if(aObj == null)
			return false ;
		else if(aObj instanceof Map)
			return true ;
		else if(aObj instanceof String)
		{
			return new XStringReader((String)aObj).readNextUnblankChar() == '{' ;
		}
		return false ;
	}
	
	static Map toMap(Object aObj)
	{
		if(aObj instanceof Map)
		{
			return (Map)aObj ;
		}
		else
			return new JSONObject(aObj.toString()).toMap() ;
	}
	
	static List toList(Object aObj)
	{
		if(aObj instanceof List)
		{
			return (List)aObj ;
		}
		else
			return new JSONArray(aObj.toString()).toList() ;
	}
	
	boolean isJSONArray(Object aObj)
	{
		if(aObj == null)
			return false ;
		else if(aObj instanceof List)
			return true ;
		else if(aObj instanceof String)
		{
			return new XStringReader((String)aObj).readNextUnblankChar() == '[' ;
		}
		return aObj.getClass().isArray() ;
	}

	@Override
	public String getName()
	{
		return "json.put" ;
	}

}
