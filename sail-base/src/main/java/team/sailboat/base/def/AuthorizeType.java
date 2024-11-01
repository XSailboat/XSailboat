package team.sailboat.base.def;

import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;

public enum AuthorizeType implements ToJSONObject
{
	XAppSign("XApp签名") 
	;
	String mDisplayName ;
	
	private AuthorizeType(String aDisplayName)
	{
		mDisplayName = aDisplayName ;
	}
	
	public String getDisplayName()
	{
		return mDisplayName;
	}

	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return aJSONObj.put("name", name())
				.put("displayName" , getDisplayName()) ;
	}
}
