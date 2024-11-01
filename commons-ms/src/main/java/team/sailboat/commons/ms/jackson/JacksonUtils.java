package team.sailboat.commons.ms.jackson;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ConfigurableApplicationContext;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.dpa.anno.BForwardMethod;
import team.sailboat.commons.fan.dpa.anno.BReverseMethod;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONException;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.ms.ACKeys_Common;

public class JacksonUtils
{
	static ObjectMapper sObjectMapper ;
	static ObjectMapper sYamlObjectMapper ;
	
	public static ObjectMapper getObjectMapper()
	{
		if(sObjectMapper == null)
		{
			ConfigurableApplicationContext ctx = (ConfigurableApplicationContext)AppContext.get(ACKeys_Common.sSpringAppContext) ;
			if(ctx != null)
			{
				sObjectMapper = ctx.getBean(ObjectMapper.class) ;
			}
			if(sObjectMapper == null)
			{
				sObjectMapper = new ObjectMapper() ;
				// 遇到不认识的字段不要失败
				sObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES , false) ;
			}
		}
		return sObjectMapper ;
	}
	
	public static ObjectMapper getYamlObjectMapper()
	{
		if(sYamlObjectMapper == null)
		{
			YAMLFactory fac = YAMLFactory.builder()
					.disable(Feature.WRITE_DOC_START_MARKER)
					.build() ;
			sYamlObjectMapper = new ObjectMapper(fac) ;
		}
		return sYamlObjectMapper ;
	}
	
	public static <K , V> Map<K , V> asLinkedHashMapFromYaml(File aYamlFile , Class<?> aKeyClass , Class<?> aValueClass) throws StreamReadException, DatabindException, IOException
	{
		return getYamlObjectMapper().readValue(aYamlFile , TypeFactory.defaultInstance()
				.constructMapType(LinkedHashMap.class , aKeyClass , aValueClass)) ;
	}
	
	public static void storeToYaml(Object aBean , File aYamlFile) throws Exception
	{
		getYamlObjectMapper().writeValue(aYamlFile , aBean) ;
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
	
	/**
	 * 
	 * 加载yaml文件中的数据成为一个JSONObject对象
	 * 
	 * @param aYamlFile
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject asJSONObjectFromYaml(File aYamlFile) throws IOException
	{
		Yaml yaml = new Yaml() ;
		try(Reader reader = FileUtils.openReader(aYamlFile, "UTF-8"))
		{
			return JSONObject.of((Map<String , Object>)yaml.loadAs(reader , Map.class)) ;
		}
	}
	
	public static <T> T asBeanFromYaml(File aYamlFile , Class<T> aClass) throws Exception
	{
		return getYamlObjectMapper().readValue(aYamlFile , aClass) ;
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
	
	public static byte[] toBytes(Object aBean)
	{
		if(aBean == null)
			return null ;
		if(aBean instanceof ToJSONObject)
			return ((ToJSONObject)aBean).toJSONString().getBytes(AppContext.sUTF8) ;
		try
		{
			return getObjectMapper().writeValueAsBytes(aBean) ;
		}
		catch (JsonProcessingException e)
		{
			throw new JSONException(e) ;
		}
	}
	
	public static <T> String toString(Object aBean , TypeReference<T> aTypeReference)
	{
		if(aBean == null)
			return null ;
		try
		{
			return getObjectMapper().writerFor(aTypeReference).writeValueAsString(aBean) ;
		}
		catch (JsonProcessingException e)
		{
			throw new JSONException(e) ;
		}
	}
	

}
