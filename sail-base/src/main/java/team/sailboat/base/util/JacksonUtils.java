package team.sailboat.base.util;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import team.sailboat.commons.fan.dpa.anno.BForwardMethod;
import team.sailboat.commons.fan.dpa.anno.BReverseMethod;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONException;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;
import team.sailboat.commons.fan.text.XString;

public class JacksonUtils
{
	static ObjectMapper sObjectMapper ;
	
	public static ObjectMapper getObjectMapper()
	{
		if(sObjectMapper == null)
		{
			// 不依赖Spring，以更多的地方被使用
//			ConfigurableApplicationContext ctx = (ConfigurableApplicationContext)AppContext.get(ACKeys.sSpringAppContext) ;
//			if(ctx != null)
//			{
//				sObjectMapper = ctx.getBean(ObjectMapper.class) ;
//			}
			if(sObjectMapper == null)
			{
				sObjectMapper = new ObjectMapper() ;
				// 遇到不认识的字段不要失败
				sObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES , false) ;
			}
		}
		return sObjectMapper ;
	}
	
	@BReverseMethod
	public static <T> T asBean(String aJsonStr , Class<T> aClass)
	{
		if(XString.isEmpty(aJsonStr))
			return null ;
		try
		{
			return getObjectMapper().readValue(aJsonStr, aClass) ;
		}
		catch (IOException e)
		{
			throw new JSONException(e) ;
		}
	}
	
	public static <T> List<T> asList(String aJsonStr , Class<T> aClass)
	{
		if(XString.isEmpty(aJsonStr))
			return null ;
		try
		{
			return getObjectMapper().readerForListOf(aClass).readValue(aJsonStr) ;
		}
		catch (IOException e)
		{
			throw new JSONException(e) ;
		}
	}
	
	public static JSONObject toJSONObject(Object aBean)
	{
		if(aBean == null)
			return null ;
		if(aBean instanceof ToJSONObject)
			return ((ToJSONObject)aBean).toJSONObject() ;
		JObjGenerator gen = new JObjGenerator() ;
		try
		{
			getObjectMapper().writeValue(gen , aBean);
		}
		catch (IOException e)
		{
			throw new JSONException(e) ;
		}
		return gen.getJSONObject() ;
	}
	
	public static JSONArray toJSONArray(Object aBean)
	{
		if(aBean == null)
			return null ;
		JObjGenerator gen = new JObjGenerator() ;
		try
		{
			getObjectMapper().writeValue(gen , aBean);
		}
		catch (IOException e)
		{
			throw new JSONException(e) ;
		}
		return gen.getJSONArray() ;
	}
	
	@BForwardMethod
	public static String toString(Object aBean)
	{
		if(aBean == null)
			return null ;
		if(aBean instanceof ToJSONObject)
			return ((ToJSONObject)aBean).toJSONString() ;
		try
		{
			return getObjectMapper().writeValueAsString(aBean) ;
		}
		catch (JsonProcessingException e)
		{
			throw new JSONException(e) ;
		}
	}
	

}
