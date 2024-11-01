package team.sailboat.ms.base.jpa;

import java.util.LinkedHashSet;

import jakarta.persistence.AttributeConverter;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.lang.XClassUtil;

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
	
}
