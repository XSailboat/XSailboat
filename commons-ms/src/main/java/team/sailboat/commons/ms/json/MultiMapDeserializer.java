package team.sailboat.commons.ms.json ;

import java.io.IOException;

import org.springframework.boot.jackson.JsonComponent;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import team.sailboat.commons.fan.collection.HashMultiMap;
import team.sailboat.commons.fan.collection.IMultiMap;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;

@JsonComponent(type = IMultiMap.class)
public class MultiMapDeserializer extends JsonDeserializer<IMultiMap> implements JSONDeserializer
{

	public MultiMapDeserializer()
	{	
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Class<IMultiMap> handledType()
	{
		return IMultiMap.class ;
	}


	@Override
	public IMultiMap deserialize(JsonParser aP, DeserializationContext aCtxt) throws IOException, JsonProcessingException
	{
	 	JSONObject jo = (JSONObject)deserialize(aP) ;
	 	IMultiMap<String, Object> map = new HashMultiMap<String, Object>() ;
	 	for(String key : jo.keyArray())
	 	{
	 		JSONArray ja = jo.optJSONArray(key) ;
	 		if(ja != null)
	 		{
	 			ja.forEach((obj)->map.put(key, obj)) ;
	 		}
	 		else
	 			map.put(key, jo.opt(key)) ;
	 	}
	 	return map ;
	}	
	
}

