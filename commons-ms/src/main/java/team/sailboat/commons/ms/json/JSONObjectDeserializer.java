package team.sailboat.commons.ms.json ;

import java.io.IOException;

import org.springframework.boot.jackson.JsonComponent;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import team.sailboat.commons.fan.json.JSONObject;

@JsonComponent(type = JSONObject.class)
public class JSONObjectDeserializer extends JsonDeserializer<JSONObject> implements JSONDeserializer
{

	public JSONObjectDeserializer()
	{	
	}
	
	@Override
	public Class<JSONObject> handledType()
	{
		return JSONObject.class ;
	}


	@Override
	public JSONObject deserialize(JsonParser aP, DeserializationContext aCtxt) throws IOException, JsonProcessingException
	{
	 	return (JSONObject)deserialize(aP) ;
	}	
	
}

