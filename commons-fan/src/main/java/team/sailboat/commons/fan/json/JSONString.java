package team.sailboat.commons.fan.json;

import java.util.Date;
import java.util.Map;

import team.sailboat.commons.fan.time.XTime;

/**
 * 
 *
 * @author yyl
 * @since 2024年11月16日
 */
public interface JSONString
{
	/**
	 * 
	 * 将此对象转成JSON字符串
	 * 
	 * @return
	 */
	String toJSONString();
	
	default String toString(int indentFactor , int indent) throws JSONException
	{
		throw new IllegalStateException("未实现！") ;
	}
	
	/**
	 * 将一个对象转换为JSON元素（JSONObject或JSONArray）。
	 * 
	 * 此方法根据输入对象的类型，将其转换为对应的JSON对象。
	 * 支持的类型包括：null、JSONArray、实现了ToJSONObject接口的对象、Map、数组、Iterable集合、枚举和Date。
	 * 对于不支持的类型，将直接返回该对象本身（这可能不是一个有效的JSON元素，需要调用者注意）。
	 * 
	 * @param aValue 要转换的对象。
	 * @return 转换后的JSON元素（JSONObject或JSONArray），或者对于不支持的类型直接返回对象本身。
	 */
	default Object toJSONElement(Object aValue)
	{
		if(aValue == null)
			return null ;
		else if(aValue.getClass().equals(JSONArray.class))
			return aValue ;
		else if(aValue instanceof ToJSONObject)
			return ((ToJSONObject)aValue).toJSONObject() ;
		else if(aValue instanceof Map)
			return JSONObject.of((Map<String, Object>) aValue) ;
		else if(aValue.getClass().isArray())
			return JSONArray.of((Object[])aValue) ;
		else if(aValue instanceof Iterable<?>)
			return JSONArray.of((Iterable<?>)aValue) ;
		else if(aValue instanceof Enum<?>)
			return ((Enum<?>)aValue).name() ;
		else if(aValue instanceof Date)
			return XTime.format$yyyyMMddHHmmssSSS((Date)aValue) ;
		else
			return aValue ;
	}
}
