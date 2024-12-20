package team.sailboat.login.extend.ding;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import com.dingtalk.api.response.OapiSnsGetuserinfoBycodeResponse.UserInfo;

import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.ms.ac.dbean.User;

public class DingCodeAuthenticationToken extends AbstractAuthenticationToken
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Object mPrincipal;
	
	Object mCredentials ;

	public DingCodeAuthenticationToken(String aCode)
	{
		super(null);
		mCredentials = aCode ;
		super.setAuthenticated(false);
	}
	
	public DingCodeAuthenticationToken(User aUser , String aCode)
	{
		super(aUser.getAuthorities());
		mPrincipal = aUser ;
		mCredentials = aCode ;
		super.setAuthenticated(true) ;
	}
	
	public DingCodeAuthenticationToken(UserInfo aUserInfo , String aCode)
	{
		super(null);
		mPrincipal = aUserInfo ;
		mCredentials = aCode ;
		super.setAuthenticated(false) ;
	}

	@Override
	public Object getCredentials()
	{
		return mCredentials ;
	}

	@Override
	public Object getPrincipal()
	{
		return mPrincipal ;
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException
	{
		Assert.isTrue(!isAuthenticated, "不能通过此方法将其设置为已认证，请使用构造方法！") ;
		super.setAuthenticated(false);
	}

	@Override
	public void eraseCredentials() 
	{
		super.eraseCredentials();
		mCredentials = null;
	}

}
