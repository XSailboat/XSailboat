package team.sailboat.commons.ms.authclient;

import io.swagger.v3.oas.annotations.media.Schema;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.ms.bean.ISwaggerJBean;

@Schema(title = "UserBrief" , description = "大数据平台的用户")
public class UserBrief implements ISwaggerJBean
{
	String mId ;
	String mRealName ;
	String mDepartment ;
	
	@Schema(description = "用户id")
	public String getId()
	{
		return mId;
	}
	public void setId(String aId)
	{
		mId = aId;
	}
	
	@Schema(description = "真实姓名")
	public String getRealName()
	{
		return mRealName;
	}
	public void setRealName(String aRealName)
	{
		mRealName = aRealName;
	}
	
	@Schema(description = "所属部门名称")
	public String getDepartment()
	{
		return mDepartment;
	}
	public void setDepartment(String aDepartment)
	{
		mDepartment = aDepartment;
	}
	
	public static UserBrief of(JSONObject aJo)
	{
		UserBrief user = new UserBrief() ;
		user.setId(aJo.optString("id")) ;
		user.setRealName(aJo.optString("realName")) ;
		user.setDepartment(aJo.optString("department")) ;
		return user ;
	}
}
