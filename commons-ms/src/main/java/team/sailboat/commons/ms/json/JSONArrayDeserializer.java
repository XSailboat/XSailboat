package team.sailboat.commons.ms.json ;

import java.io.IOException;

import org.springframework.boot.jackson.JsonComponent;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import team.sailboat.commons.fan.json.JSONArray;

@JsonComponent(type = JSONArray.class)
public class JSONArrayDeserializer extends JsonDeserializer<JSONArray> implements JSONDeserializer
{

	public JSONArrayDeserializer()
	{	
	}
	
	@Override
	public Class<JSONArray> handledType()
	{
		return JSONArray.class ;
	}


	@Override
	public JSONArray deserialize(JsonParser aP, DeserializationContext aCtxt) throws IOException, JsonProcessingException
	{
	 	return (JSONArray)deserialize(aP) ;
	}	
	
}

