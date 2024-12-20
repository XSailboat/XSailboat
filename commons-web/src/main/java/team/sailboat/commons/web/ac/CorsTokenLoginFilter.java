package team.sailboat.commons.web.ac;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.http.URLCoder;
import team.sailboat.commons.fan.text.XString;

/**
 * 
 * CorsToken登录过滤器
 *
 * @author yyl
 * @since 2024年12月10日
 */
public class CorsTokenLoginFilter extends AbstractAuthenticationProcessingFilter
		implements ApplicationEventPublisherAware
{	
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	final URLCoder mURLCoder = URLCoder.getDefault() ;
	
	OAuthClientConf mClientConf ;
	
	public CorsTokenLoginFilter(OAuthClientConf aClientConf)
	{
		super(new RequestHeaderRequestMatcher("cors-token-auth")) ;
		mClientConf = aClientConf ;
	}
	
	
	/**
	 * 是否需要安全认证，如果访问的目标无需认证，或者已经登录
	 * 
	 * @param aRequest
	 * @param aResponse
	 * @return
	 */
	@Override
	protected boolean requiresAuthentication(HttpServletRequest aRequest, HttpServletResponse aResponse)
	{
		if(super.requiresAuthentication(aRequest, aResponse))
		{
			Authentication auth = SecurityContextHolder.getContext().getAuthentication() ;
			if(auth != null && auth instanceof CoupleAuthenticationToken)
			{
				CoupleAuthenticationToken ctoken = (CoupleAuthenticationToken)auth ;
				if(ctoken.isAuthenticated() && !ctoken.isExpired())
				{
					return false ;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest aRequest, HttpServletResponse aResponse)
			throws AuthenticationException, IOException, ServletException
	{
		String path = aRequest.getContextPath() ;
		if(path.equals(mClientConf.getLocalLoginPath()))
		{
			// 重定向
			return null ;
		}
		
		String error = aRequest.getParameter("error") ;
		if(XString.isNotEmpty(error))
		{
			if("access_denied".equals(error))
			{
				aResponse.sendRedirect(XString.msgFmt("{}/error_view?http-status=403&msg={}&url={}"
						, aRequest.getContextPath()
						, mURLCoder.encodeParam("您目前无权限访问此应用!")
						, aRequest.getRequestURL())) ;
				return null ;
			}
		}
		
		String corsToken = aRequest.getHeader("cors-token-auth") ;
		if(XString.isEmpty(corsToken))
			throw new AuthenticationServiceException("cors-token-auth不能为空！");
		
		XAppAccessTokenResponseClient accessTokenClient = mClientConf.getAccessTokenResponseClient() ;
		//
		CorsTokenGrantRequest req = new CorsTokenGrantRequest(mClientConf.getClientRegistration()
				, corsToken
				, aRequest.getHeader("referer")) ;
		
		try
		{
			OAuth2AccessTokenResponse accessTokenResp = accessTokenClient.getTokenResponse(req) ;
			
			AuthUser_AC user = AuthUser_AC.loadFromAC(accessTokenResp.getAccessToken().getTokenValue()
					, mClientConf.getAuthCenterClient()) ;
			CoupleAuthenticationToken token = new CoupleAuthenticationToken(user
					, mClientConf.getClientRegistration().getRegistrationId()
					, accessTokenResp.getAccessToken().getIssuedAt()
					, accessTokenResp.getAccessToken().getExpiresAt()) ;
			token.setAuthenticated(true) ;
			
			
			// 通过CorsToken方式登录上来时，不颁发当前应用的CorsToken
//			corsToken = jobj_userDetail.optString("corsToken") ;		// 当前应用的CorsToken
//			if(XString.isNotEmpty(corsToken))
//			{
//				corsToken = CorsTokenSignHelper.signCorsToken(corsToken, aRequest.getRequestURL() , mClientConf.getClientSecret()) ;
//				((HttpServletResponse)aResponse).addCookie(new Cookie("cors-token", corsToken)) ;
//			}
			return token ;
		}
		catch (Exception e)
		{
			mLogger.error(ExceptionAssist.getClearMessage(getClass(), e 
					, "连接的目标端是："+mClientConf.getAuthCenterClient())) ;
			if(e instanceof IOException)
				throw (IOException)e ;
			else
				throw new IOException(e) ;
		}
	}
}
