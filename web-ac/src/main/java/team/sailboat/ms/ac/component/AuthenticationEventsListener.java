package team.sailboat.ms.ac.component;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.fan.time.XTime;
import team.sailboat.ms.ac.dbean.User;
import team.sailboat.ms.ac.server.ResourceManageServer;

/**
 * 
 * 认证事件监听器
 *
 * @author yyl
 * @since 2024年11月20日
 */
@Component
public class AuthenticationEventsListener
{
	static final OAuth2TokenType AUTHORIZATION_CODE_TOKEN_TYPE = new OAuth2TokenType(OAuth2ParameterNames.CODE) ;
	
	final Logger mLogger = LoggerFactory.getLogger("AccessLog") ;
	
	LoginUserRegisterRepo mLoginRepo ;
	
	OAuth2AuthorizationService mAuthorizationService ;
	
	@Autowired
	AccessStatistics mAccessSts ;
	
	@Autowired
	ResourceManageServer mResMngServer ;
	
	public AuthenticationEventsListener(LoginUserRegisterRepo aLoginRepo
			, OAuth2AuthorizationService aAuthorizationService)
	{
		mLoginRepo = aLoginRepo ;
		mAuthorizationService = aAuthorizationService ;
	}
	
	@EventListener
    public void onSuccess(AuthenticationSuccessEvent aEvent )
	{
		Authentication auth = aEvent.getAuthentication() ;
		if(auth instanceof OAuth2AccessTokenAuthenticationToken)
		{
			OAuth2AccessTokenAuthenticationToken token = (OAuth2AccessTokenAuthenticationToken)auth ;
			// 缓存userId和clientid，过期时间之间的关系
			OAuth2AccessToken accessToken = token.getAccessToken() ;
			Date issueTime = Date.from(accessToken.getIssuedAt())  ;
			Date expiredTime = Date.from(accessToken.getExpiresAt())  ;
			RegisteredClient registeredClient = token.getRegisteredClient() ;
			
			HttpServletRequest req = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest() ;
			String code = req.getParameter("code") ;
			if(XString.isNotEmpty(code))
			{
				OAuth2Authorization userAuth = mAuthorizationService.findByToken(code ,  AUTHORIZATION_CODE_TOKEN_TYPE) ;
				User user = (User)((Authentication)userAuth.getAttribute("java.security.Principal")).getPrincipal() ;
			
				mLoginRepo.recordLogin(user.getId() , registeredClient.getId() 
						, issueTime , expiredTime , userAuth.getId()) ;
				mLogger.info("向应用 {}[{}] 颁发用户 {}[{}] 的令牌，颁发时间：{} , 过期时间：{}" , registeredClient.getClientName()
						, registeredClient.getId()
						, user.getRealName()
						, user.getId()
						, XTime.format$yyyyMMddHHmmss(issueTime)
						, XTime.format$yyyyMMddHHmmss(expiredTime)) ;		
				
				mAccessSts.recordOneVisit(registeredClient.getId() , user.getId()) ;
			}
		}
		else if(auth instanceof UsernamePasswordAuthenticationToken ut)
		{
			User user = (User)ut.getPrincipal() ;
			String clientAppId = mResMngServer.getClientAppId_SailAC() ;
			// 用户在认证中心登录页面，输入用户名密码登录成功
			mLogger.info("用户 {}[{}] 登录了认证中心"
					, user.getRealName()
					, user.getId()) ;		
			
			mAccessSts.recordOneVisit(clientAppId , user.getId()) ;
		}
    }
	
	@EventListener
    public void onFailure(AbstractAuthenticationFailureEvent aEvent)
	{
		StringBuilder msgBld = new StringBuilder("用户登录失败！原因：[")
				.append(aEvent.getException().getClass().getSimpleName())
				.append("]").append(aEvent.getException().getMessage())
				.append("。源信息：[用户名]").append(aEvent.getAuthentication().getPrincipal()) ;
		Object details = aEvent.getAuthentication().getDetails() ;
		if(details instanceof WebAuthenticationDetails)
		{
			msgBld.append("，[地址]").append(((WebAuthenticationDetails) details).getRemoteAddress()) ;
		}
		mLogger.info(msgBld.toString()) ;
	}
}
