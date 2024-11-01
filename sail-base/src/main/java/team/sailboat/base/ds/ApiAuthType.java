package team.sailboat.base.ds;

import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;

public enum ApiAuthType implements ToJSONObject
{
	XApp("XApp签名算法") ,
	AppCode("AppCode认证") ,
	AliyunApp("阿里云API网关") ,
	NoAuth("无认证")
	;
	
	String mDisplayName ;
	
	private ApiAuthType(String aDisplayName)
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
		return aJSONObj.put("name" , name())
				.put("displayName" , mDisplayName)
				;
	}
}
