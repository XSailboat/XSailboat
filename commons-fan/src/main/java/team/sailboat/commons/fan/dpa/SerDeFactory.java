package team.sailboat.commons.fan.dpa;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import team.sailboat.commons.fan.collection.IMultiMap;
import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dpa.anno.BForwardMethod;
import team.sailboat.commons.fan.dpa.anno.BReverseMethod;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.http.URLBuilder;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.JCommon;

public class SerDeFactory
{
	
	public static class List_String
	{
		@BForwardMethod
		public static String forward(List aVal)
		{
			return new JSONArray(aVal).toString() ;
		}
		
		@BReverseMethod
		public static List reverse(Object aVal)
		{
			List list = XC.arrayList() ;
			if(aVal == null)
				return list ;
			String text = aVal.toString() ;
			if(text.isEmpty())
				return list ;
			new JSONArray(text)
				.forEach(list::add) ;
			return list ;
		}
	}
	
	public static class Set_String
	{
		@BForwardMethod
		public static String forward(Set aVal)
		{
			return new JSONArray(aVal).toString() ;
		}
		
		@BReverseMethod
		public static Set reverse(Object aVal)
		{
			Set set = XC.hashSet() ;
			if(aVal == null)
				return set ;
			String text = aVal.toString() ;
			if(text.isEmpty())
				return set ;
			new JSONArray(text)
				.forEach(set::add) ;
			return set ;
		}
	}
	
	public static class LinkedHashSet_String
	{
		
		@BReverseMethod
		public static LinkedHashSet reverse(Object aVal)
		{
			LinkedHashSet set = XC.linkedHashSet() ;
			if(aVal == null)
				return set ;
			String text = aVal.toString() ;
			if(text.isEmpty())
				return set ;
			new JSONArray(text)
				.forEach(set::add) ;
			return set ;
		}
	}
	
	public static class Map_String
	{
		@BForwardMethod
		public static String forward(Map aVal)
		{
			return new JSONObject(aVal).toString() ;
		}
		
		@BReverseMethod
		public static Map reverse(Object aVal)
		{
			Map map = XC.linkedHashMap() ;
			if(aVal == null)
				return map ;
			JSONObject.of(aVal.toString()).forEach(map::put);
			return map ;
		}
	}
	
	public static class Map_StringList
	{
		@BForwardMethod
		public static String forward(Map aVal)
		{
			return new JSONObject(aVal).toString() ;
		}
		
		@BReverseMethod
		public static Map reverse(Object aVal)
		{
			Map<String , List<String>> map = XC.linkedHashMap() ;
			if(aVal == null)
				return map ;
			JSONObject.of(aVal.toString()).forEach((key, value)->{
				List<String> list = XC.arrayList() ;
				((JSONArray)value).forEach((ele)->list.add(JCommon.toString(ele))) ;
				map.put(key, list) ;
			});
			return map ;
		}
	}
	
	public static class Map_StringBool
	{
		@BForwardMethod
		public static String forward(Map aVal)
		{
			return new JSONObject(aVal).toString() ;
		}
		
		@BReverseMethod
		public static Map reverse(Object aVal)
		{
			Map<String , Boolean> map = XC.linkedHashMap() ;
			if(aVal == null)
				return map ;
			JSONObject jo = JSONObject.of(aVal.toString()) ; 
			for(String key : jo.keySet())
			{
				map.put(key, jo.optBoolean(key)) ;
			}
			return map ;
		}
	}
	
	public static class MultiMap_String
	{
		@BForwardMethod
		public static String forward(IMultiMap aVal)
		{
			return JCommon.toString(aVal) ;
		}
		
		@BReverseMethod
		public static IMultiMap reverse(Object aVal)
		{
			IMultiMap map = XC.multiMap() ;
			if(aVal != null)
				URLBuilder.parseQueryStr(aVal.toString() , map);
			return map ;
		}
	}
	
	public static class Enum 
	{
		@BForwardMethod
		public static Object forward(java.lang.Enum<?> aVal)
		{
			return aVal == null?null:aVal.name() ;
		}
		
		@BReverseMethod
		@SuppressWarnings("unchecked")
		public static Object reverse(Object aVal , Class aFieldType)
		{
			return aVal == null?null:java.lang.Enum.valueOf(aFieldType , aVal.toString()) ;
		}
	}
	
	public static class StringSecret
	{
		@BForwardMethod
		public static Object forward(Object aVal)
		{
			return aVal == null?null:PropertiesEx.asSecret(aVal.toString()) ;
		}
		
		@BReverseMethod
		public static Object reverse(Object aVal)
		{
			return aVal == null?null:PropertiesEx.deSecret(aVal.toString()) ;
		}
	}
	
	public static class PropertiesExSerDe
	{
		@BForwardMethod
		public static String forward(PropertiesEx aProp)
		{
			if(aProp == null)
				return null ;
			StringWriter strW = new StringWriter(2048) ;
			try
			{
				aProp.store(strW, null) ;
			}
			catch (IOException e)
			{
				WrapException.wrapThrow(e) ;
			}
			return strW.toString() ;
		}
		
		@BReverseMethod
		public static PropertiesEx reverse(Object aVal)
		{
			if(aVal == null)
				return null ;
			String text = aVal.toString() ;
			if(text.isEmpty())
				return null ;
			try
			{
				return PropertiesEx.loadFromReader(new StringReader(text)) ;
			}
			catch (IOException e)
			{
				WrapException.wrapThrow(e);
				return null ;			// dead code
			}
		}
	}
	
	public static class JSONObjectSerDe
	{
		@BForwardMethod
		public static String forward(JSONObject aJo)
		{
			if(aJo == null)
				return null ;
			return aJo.toJSONString() ;
		}
		
		@BReverseMethod
		public static JSONObject reverse(Object aVal)
		{
			if(aVal == null)
				return null ;
			return JSONObject.of(aVal.toString()) ;
		}
	}
	
	public static class JSONArraySerDe
	{
		@BForwardMethod
		public static String forward(JSONArray aJa)
		{
			if(aJa == null)
				return null ;
			return aJa.toJSONString() ;
		}
		
		@BReverseMethod
		public static JSONArray reverse(Object aVal)
		{
			if(aVal == null)
				return null ;
			return new JSONArray(aVal.toString()) ;
		}
	}
}
