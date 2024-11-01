package team.sailboat.commons.fan.json;

import team.sailboat.commons.fan.lang.JCommon;

public interface ToJSONObject extends JSONString
{
	default JSONObject toJSONObject()
	{
		return setTo(new JSONObject()) ;
	}
	
	JSONObject setTo(JSONObject aJSONObj) ;
	
	@Override
	default String toJSONString()
	{
		return JCommon.toString(toJSONObject()) ;
	}
	
	public static JSONObject toJSONObject(ToJSONObject aToJSON)
	{
		return aToJSON != null?aToJSON.toJSONObject():null ;
	}
	
	@Override
	default String toString(int indentFactor, int indent) throws JSONException
	{
		return toJSONObject().toString(indentFactor, indent) ;
	}
}
