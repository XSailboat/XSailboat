package team.sailboat.base.jpa;

import java.util.LinkedHashSet;
import java.util.List;

import jakarta.persistence.AttributeConverter;
import team.sailboat.base.util.JacksonUtils;
import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.text.XString;

/**
 * 通用的数据转换器。用在JPA持久化中
 *
 * @author yyl
 * @since 2024年12月19日
 */
public class CommonConverters
{
	public static class LinkedHashSet_String implements AttributeConverter<LinkedHashSet<String>, String>
	{

		@Override
		public String convertToDatabaseColumn(LinkedHashSet<String> aAttribute)
		{
			return aAttribute == null?null:JSONArray.of(aAttribute).toJSONString() ;
		}

		@Override
		public LinkedHashSet<String> convertToEntityAttribute(String aDbData)
		{
			if(aDbData == null)
				return null ;
			LinkedHashSet<String> result = XC.linkedHashSet() ;
			JSONArray ja = new JSONArray(aDbData) ;
			ja.forEach(ele->result.add(XClassUtil.toString(ele))) ;
			return result ;
		}

	}
	
	public class JSONArrayConverter implements AttributeConverter<JSONArray, String>
	{

		@Override
		public String convertToDatabaseColumn(JSONArray aAttribute)
		{
			return JCommon.toString(aAttribute) ;
		}

		@Override
		public JSONArray convertToEntityAttribute(String aDbData)
		{
			return new JSONArray(aDbData) ;
		}

	}
	
	public static class JSONObjectConverter implements AttributeConverter<JSONObject, String>
	{

		@Override
		public String convertToDatabaseColumn(JSONObject aAttribute)
		{
			return JCommon.toString(aAttribute) ;
		}

		@Override
		public JSONObject convertToEntityAttribute(String aDbData)
		{
			return JSONObject.of(aDbData) ;
		}

	}
	
	public static class ListStringConverter implements AttributeConverter<List<String>, String>
	{

		@Override
		public String convertToDatabaseColumn(List<String> aAttribute)
		{
			
			return JCommon.toString(JacksonUtils.toString(aAttribute)) ;
		}

		@Override
		public List<String> convertToEntityAttribute(String aDbData)
		{
			return JacksonUtils.asList(aDbData, String.class) ;
		}
	}
	
	public static class StringArrayConverter implements AttributeConverter<String[], String>
	{

		@Override
		public String convertToDatabaseColumn(String[] aAttribute)
		{
			return XString.toString("," , aAttribute) ;
		}

		@Override
		public String[] convertToEntityAttribute(String aDbData)
		{
			return PropertiesEx.split(aDbData) ;
		}

	}
}
