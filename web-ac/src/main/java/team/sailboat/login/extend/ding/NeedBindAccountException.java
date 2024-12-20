package team.sailboat.login.extend.ding;

import org.springframework.security.core.AuthenticationException;

import com.dingtalk.api.response.OapiSnsGetuserinfoBycodeResponse.UserInfo;

public class NeedBindAccountException extends AuthenticationException
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	UserInfo mUserInfo ;
	
	public NeedBindAccountException(UserInfo aUserInfo)
	{
		super("钉钉扫码通过，请先绑定账号！");
		
		mUserInfo = aUserInfo ;
	}
	
	public UserInfo getUserInfo()
	{
		return mUserInfo;
	}

}
