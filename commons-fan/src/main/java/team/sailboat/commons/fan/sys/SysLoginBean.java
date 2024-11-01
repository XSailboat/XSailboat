package team.sailboat.commons.fan.sys;

import java.io.StringWriter;

import team.sailboat.commons.fan.json.JSONException;
import team.sailboat.commons.fan.json.JSONWriter;

public class SysLoginBean
{
	/**
	 * 登录的用户名
	 */
	public final String mUserName ;
	/**
	 * windows下的会话名，linux下的tty
	 */
	public final String mSessionId ;
	
	/**
	 * 登录时间
	 */
	public final String mLoginTime ;
	
	/**
	 * 用户断开远程连接，但没有注销掉，则为false
	 * 处于连接状态，则为true
	 */
	public final boolean mActive ;

	public SysLoginBean(String aUserName, String aSessionId, String aLoginTime , boolean aActive)
	{
		mUserName = aUserName;
		mSessionId = aSessionId;
		mLoginTime = aLoginTime;
		mActive = aActive ;
	}
	
	@Override
	public String toString()
	{
		StringWriter writer = new StringWriter() ;
		JSONWriter jw = new JSONWriter(writer) ;
		try
		{
			jw.object() ;
			jw.key("用户名").value(mUserName)
				.key("会话ID").value(mSessionId)
				.key("登录时间").value(mLoginTime)
				.key("当前状态").value(mActive?"活动":"不活动") ;
			jw.endObject() ;
		}
		catch(JSONException je)
		{
			je.printStackTrace() ;
		}
		return writer.toString() ;
	}
	
	

}
