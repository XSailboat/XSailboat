package team.sailboat.base.msg;

import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;

public enum MsgType implements ToJSONObject
{
	
	Event("事件️" , "日志记录/事件") ,
	
	Task("任务" , "向目标发送特定指令，要求其执行某项任务") ,
	
	Confirm("确认" , "因另一消息，而人为/程序产生的自动化消息，作为对先前特定消息的回应") ,
	
	Cancel("取消/作废" , "因另一消息，而人为/程序产生的自动化消息，表示取消/作废特定消息")
	;
	
	final String displayName ;
	final String description ;
	
	private MsgType(String aDisplayName , String aDescription)
	{
		displayName = aDisplayName ;
		description = aDescription ;
	}
	
	public String getDisplayName()
	{
		return displayName;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return aJSONObj.put("name" , name())
				.put("displayName" , displayName)
				.put("description" , description)
				;
	}
}
