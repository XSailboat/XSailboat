package team.sailboat.base.ds;

import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;

/**
 * 数据来源。内部数据源还是外部数据源
 *
 * @author yyl
 * @since 2022年4月29日
 */
public enum DataOwner implements ToJSONObject
{
	Other("其它系统") ,
	Self("平台所属")
	;
	
	String mDisplayName ;
	
	private DataOwner(String aDisplayName)
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
