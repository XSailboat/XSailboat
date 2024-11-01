package team.sailboat.commons.ms.json;

import java.io.IOException;
import java.util.Stack;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;

import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;

public interface JSONDeserializer
{
	default Object deserialize(JsonParser aP) throws IOException, JsonProcessingException
	{
	 	Stack<Object> stack = new Stack<Object>() ;
	 	JsonToken token = aP.currentToken() ;
	 	Object current = null ;
	 	int flag = 0 ;
	 	String key = null ;
	 	do
	 	{
	 		switch(token)
	 		{
	 		case START_OBJECT:
	 			JSONObject c = new JSONObject() ;
	 			if(flag == 1)
	 				((JSONObject)current).put(key, c) ;
	 			else if(flag == 2)
	 				((JSONArray)current).put(c) ;
	 			if(flag != 0)
	 				stack.push(current) ;
	 			current = c ;
	 			flag = 1 ;
	 			break ;
	 		case FIELD_NAME:
	 			key = aP.getText() ;
	 			break ;
	 		case VALUE_STRING:
	 			if(flag == 1)
	 				((JSONObject)current).put(key, aP.getValueAsString()) ;
	 			else if(flag == 2)
	 				((JSONArray)current).put(aP.getValueAsString()) ;
	 			break ;
	 		case VALUE_NUMBER_INT:
	 			if(flag == 1)
	 				((JSONObject)current).put(key, aP.getValueAsLong()) ;
	 			else if(flag == 2)
	 				((JSONArray)current).put(aP.getValueAsLong()) ;
	 			break ;
	 		case VALUE_NUMBER_FLOAT:
	 			if(flag == 1)
	 				((JSONObject)current).put(key, aP.getValueAsDouble()) ;
	 			else if(flag == 2)
	 				((JSONArray)current).put(aP.getValueAsDouble()) ;
	 			break ;
	 		case VALUE_TRUE:
	 			if(flag == 1)
	 				((JSONObject)current).put(key, Boolean.TRUE) ;
	 			else if(flag == 2)
	 				((JSONArray)current).put(Boolean.TRUE) ;
	 			break ;
	 		case VALUE_FALSE:
	 			if(flag == 1)
	 				((JSONObject)current).put(key, Boolean.FALSE) ;
	 			else if(flag == 2)
	 				((JSONArray)current).put(Boolean.FALSE) ;
	 			break ;
	 		case VALUE_NULL:
	 			if(flag == 1)
	 				((JSONObject)current).put(key, null , true) ;
	 			else if(flag == 2)
	 				((JSONArray)current).put((Object)null) ;
	 			break ;
	 		case START_ARRAY:
	 			JSONArray a = new JSONArray() ;
	 			if(flag == 1)
	 				((JSONObject)current).put(key, a) ;
	 			else if(flag == 2)
	 				((JSONArray)current).put(a) ;
	 			stack.push(current) ;
	 			current = a ;
	 			flag = 2 ;
	 			break ;
	 		case END_ARRAY:
	 		case END_OBJECT:
	 			if(!stack.isEmpty())
	 			{
	 				current = stack.pop() ;
	 				if(current instanceof JSONObject)
	 					flag = 1 ;
	 				else if(current instanceof JSONArray)
	 					flag = 2 ;
	 			}
	 			else
	 				flag = 0 ;
	 			break ;
	 		default:
	 			
	 		}
	 	} while((token = aP.nextToken()) != null) ;
	 	
	 	return current ;
	}	
}
