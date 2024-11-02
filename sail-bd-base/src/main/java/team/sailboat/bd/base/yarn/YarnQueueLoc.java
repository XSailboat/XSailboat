package team.sailboat.bd.base.yarn;

import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;

public enum YarnQueueLoc implements ToJSONObject
{
	Center("中心集群") ,
	Edge("边缘集群")
	;
	
	String mDisplayName ;
	
	private YarnQueueLoc(String aDisplayName)
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
				.put("displayName" , mDisplayName) ;
	}
}
