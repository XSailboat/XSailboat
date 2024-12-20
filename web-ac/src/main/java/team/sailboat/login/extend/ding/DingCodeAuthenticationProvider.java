package team.sailboat.login.extend.ding;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.dingtalk.api.response.OapiSnsGetuserinfoBycodeResponse.UserInfo;
import com.taobao.api.ApiException;

import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.ms.ac.dbean.User;
import team.sailboat.ms.ac.server.IUserDataManager;

public class DingCodeAuthenticationProvider implements AuthenticationProvider, InitializingBean
		, MessageSourceAware 
{	
	UserDetailsService mUserDetailsService;
	
	DingClient mDingClient ;
	
	public DingCodeAuthenticationProvider(DingClient aDingClient)
	{
		mDingClient = aDingClient ;
	}
	
	public DingCodeAuthenticationProvider(DingClient aDingClient, IUserDataManager aUserDataMng)
	{
		mDingClient = aDingClient ;
		mUserDetailsService = aUserDataMng ;
	}

	@Override
	public void setMessageSource(MessageSource aMessageSource)
	{
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Assert.notNull(mUserDetailsService, "A UserDetailsService must be set");
		Assert.isTrue(mUserDetailsService instanceof IUserDataManager);
	}

	@Override
	public Authentication authenticate(Authentication aAuthentication) throws AuthenticationException
	{
		Object principal = aAuthentication.getPrincipal() ;
		if(principal != null && principal instanceof User)
			return aAuthentication ;
	    // 通过扫描二维码，跳转指定的redirect_uri后，向url中追加的code临时授权码
	    String code = (String)aAuthentication.getCredentials() ;
	    UserInfo userInfo = null ;
		try
		{
			 userInfo = mDingClient.getUserInfoByCode(code) ;
		}
		catch (ApiException e)
		{
			throw new InternalAuthenticationServiceException("试图通过用授权码从钉钉的认证授权服务中获取用户信息失败！" , e) ;
		}
	    IUserDataManager userMng = (IUserDataManager)mUserDetailsService ;
	    User user = userMng.getUserByDingOpenId(userInfo.getOpenid()) ;
	    if(user == null)
	    {
	    	throw new NeedBindAccountException(userInfo) ;
	    }
	    // 如果用户有管理员权限
	    return new DingCodeAuthenticationToken(user , code) ;
	}

	@Override
	public boolean supports(Class<?> aAuthentication)
	{
		return DingCodeAuthenticationToken.class.isAssignableFrom(aAuthentication) ;
	}

	public void setUserDetailsService(UserDetailsService aUserDetailsService)
	{
		mUserDetailsService = aUserDetailsService;
	}
	protected UserDetailsService getUserDetailsService()
	{
		return mUserDetailsService;
	}
	
}
