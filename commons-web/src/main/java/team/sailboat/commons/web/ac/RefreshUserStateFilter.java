package team.sailboat.commons.web.ac;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.endpoint.DefaultRefreshTokenTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.excep.HttpException;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.fan.time.XTime;
import team.sailboat.commons.ms.xca.AppKeySecret;
import team.sailboat.commons.ms.xca.IAppSignChecker;
import team.sailboat.commons.ms.xca.XAppSignChecker;

/**
 * 刷新用户状态的过滤器			<br />
 * 1. 当一个用户在这个应用的角色、权限发生改变时，会通知这个应用，刷新这个用户的状态（权限）		<br />
 * 2. 当用户在其它应用中退出时，会通知这个应用，刷新这个用户的状态（登录状态）。当用户下次访问这个应用的页面时起作用
 * 
 *
 * @author yyl
 * @since 2024年12月10日
 */
public class RefreshUserStateFilter implements Filter
{
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	final Object mMutex = new Object() ;
	
	OAuthClientConf mClientConf ;
	RequestMatcher mRefreshAuthsMatcher ;
	
	final Set<String> mNeedRefreshUserIds = Collections.synchronizedSet(XC.hashSet()) ;
	
	IAppSignChecker mAppSignChecker ;
	
	AppKeySecret mOAuthClientApp ;
	
	/**
	 * 利用RefreshToken换取新的AccessToken的客户端
	 */
	DefaultRefreshTokenTokenResponseClient mRefreshClient = new DefaultRefreshTokenTokenResponseClient() ;
	
	public RefreshUserStateFilter(OAuthClientConf aClientConf)
	{
		mClientConf = aClientConf ;
		
		String path = mClientConf.getRefreshUserAuthoritesCallbackPath() ;
		if(XString.isNotEmpty(path))
		{
			mRefreshAuthsMatcher = new AntPathRequestMatcher(path) ;
 		}
		mOAuthClientApp = new AppKeySecret(null , mClientConf.getClientId() , mClientConf.getClientSecret()) ;
		mAppSignChecker = new XAppSignChecker(null, (appKey)->{
			return appKey.equals(mOAuthClientApp.getAppKey())?mOAuthClientApp:null ;
		}) ;
	}

	@Override
	public void doFilter(ServletRequest aRequest, ServletResponse aResponse, FilterChain aChain)
			throws IOException, ServletException
	{
		HttpServletRequest httpReq = (HttpServletRequest)aRequest ;
		HttpSession session = httpReq.getSession() ;
		Object mutex = session.getAttribute("LoginFilterMutex") ;
		if(mutex == null)
		{
			synchronized (mMutex)
			{
				mutex = session.getAttribute("LoginFilterMutex") ;
				if(mutex == null)
				{
					mutex = new Object() ;
					session.setAttribute("LoginFilterMutex", mutex) ;
				}
			}
		}
		synchronized (mutex)
		{
			if(mRefreshAuthsMatcher != null && mRefreshAuthsMatcher.matches(httpReq))
			{
				// 此处得验证一下调用者的合法性，方法是用AppSecret，对它进行解密
				try
				{
					mAppSignChecker.check(httpReq) ;
				}
				catch(HttpException e)
				{
					mLogger.error(ExceptionAssist.getClearMessage(getClass(), e)) ;
					((HttpServletResponse)aResponse).sendError(e.getStatus().value(), e.getRawMessage()) ;
					return ;
				}
				
				// 从中取出用户id
				String content = StreamAssist.readString(httpReq.getInputStream() , httpReq.getContentLength() , "UTF-8") ;
				
				JSONArray ja = new JSONArray(content) ;
				ja.forEach((ele)->mNeedRefreshUserIds.add((String)ele)) ;
				return ;
			}
			else
			{
				// 检查token是否过期，过期了的话就拿着refreshToken去换新token
				Authentication auth = SecurityContextHolder.getContext().getAuthentication() ;
				if(auth != null && auth instanceof CoupleAuthenticationToken)
				{
					CoupleAuthenticationToken ctoken = (CoupleAuthenticationToken)auth ;
					if(mNeedRefreshUserIds.remove(((AuthUser_AC)ctoken.getPrincipal()).getId()))
					{
						ctoken.setForceExpired(true) ;
						SecurityContextHolder.getContext().setAuthentication(null) ;
					}
					if(ctoken.isExpired())
					{
						synchronized (ctoken)
						{
							if(ctoken.isExpired())
							{
								AuthUser_AC user = (AuthUser_AC)ctoken.getPrincipal() ;
								OAuth2AuthorizedClient oldClient = mClientConf.getAuthorizedClientRepository()
										.loadAuthorizedClient(ctoken.getAuthorizedClientRegistrationId()
												, ctoken
												, httpReq) ;
								OAuth2RefreshTokenGrantRequest req = new OAuth2RefreshTokenGrantRequest(oldClient.getClientRegistration()
										, oldClient.getAccessToken()
										, oldClient.getRefreshToken()) ;
								OAuth2AccessTokenResponse accessTokenResp = mRefreshClient.getTokenResponse(req) ;
								OAuth2AccessToken accessToken = accessTokenResp.getAccessToken() ;
								OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(oldClient.getClientRegistration()
										, ctoken.getName()
										, accessToken
										, accessTokenResp.getRefreshToken()) ;
								AuthUser_AC.refreshAuthorities(user , accessToken.getTokenValue()) ;
								mClientConf.getAuthorizedClientRepository()
										.saveAuthorizedClient(authorizedClient , ctoken
												, httpReq
												, (HttpServletResponse)aResponse);
								
								ctoken.setIssueTime(accessToken.getIssuedAt()); 
								ctoken.setExpiredTime(accessToken.getExpiresAt());
								
								String corsToken = user.getAttribute(AuthUser_AC.sAK_corsToken) ;
								if(XString.isNotEmpty(corsToken))
								{
									try
									{
										corsToken = CorsTokenSignHelper.signCorsToken(corsToken, httpReq.getRequestURL() , mOAuthClientApp.getAppSecret()) ;
										((HttpServletResponse)aResponse).addCookie(new Cookie("cors-token", corsToken)) ;
									}
									catch (InvalidKeyException | NoSuchAlgorithmException | IllegalStateException
											| UnsupportedEncodingException e)
									{
										mLogger.error(ExceptionAssist.getClearMessage(getClass(), e , "签名CorsToken出现异常！")) ;
									}	
									
								}
								mLogger.info("刷新了用户[{}]的令牌，原先的过期时间是:{}" , user.getName() , XTime.format$yyyyMMddHHmmss(ctoken.getExpiredTime().getEpochSecond())) ;
							}
						}
					}
				}
			}
		}
		
		aChain.doFilter(aRequest, aResponse) ;
	}
}
