package team.sailboat.base.msg;

import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;

public enum TextPatternType implements ToJSONObject
{
	RawRegex("正则表达式") ,
	
	Contains("包含字符串") ,
	
	ContainsAny("包含其中之一") ,
	
	SqlLike("LIKE形式") ,
	
	NotEmpty("非空") ,
	;
	
	String displayName ;
	
	private TextPatternType(String aDisplayName)
	{
		displayName = aDisplayName ;
	}
	
	public String getDisplayName()
	{
		return displayName;
	}
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return aJSONObj.put("name", name())
				.put("displayName" , displayName)
				;
	}
}
