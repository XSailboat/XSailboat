package team.sailboat.commons.ms.authclient;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.nimbusds.jose.util.Base64URL;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.excep.HttpException;
import team.sailboat.commons.fan.http.HttpClient;
import team.sailboat.commons.fan.http.Request;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.fan.time.XTime;
import team.sailboat.commons.ms.xca.AppKeySecret;
import team.sailboat.commons.ms.xca.IAppSignChecker;
import team.sailboat.commons.ms.xca.XAppSignChecker;

public class LoginFilter implements Filter
{
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	final Object mMutex = new Object() ;
	
	OAuthClientConf mClientConf ;
	RequestMatcher mLoginMatcher ;
	RequestMatcher mRefreshAuthsMatcher ;
//	RequestMatcher mBlankLoginMatcher ;
	
	HttpClient mAuthClient ;
	
	final Set<String> mNeedRefreshUserIds = Collections.synchronizedSet(XC.hashSet()) ;
	
	IAppSignChecker mAppSignChecker ;
	
	AppKeySecret mOAuthClientApp ;
	
	public LoginFilter(OAuthClientConf aClientConf)
	{
		mClientConf = aClientConf ;
//		mBlankLoginMatcher = new AntPathRequestMatcher(aClientConf.getBlankLoginPath()) ;
		mLoginMatcher = new AntPathRequestMatcher(mClientConf.getLocalLoginPath()) ;
		mAuthClient = mClientConf.getAuthCenterClient() ;
		
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
			if(mLoginMatcher.matches(httpReq))
			{
				Authentication auth = SecurityContextHolder.getContext().getAuthentication() ;
				if(auth != null && auth.isAuthenticated())
				{
					((HttpServletResponse)aResponse).sendError(HttpStatus.BAD_REQUEST.value(), "单会话登陆并发过大！");
				}
				else
				{
					String state = UUID.randomUUID().toString() ;
					httpReq.getSession().setAttribute("oauth_state", state) ;
	//				System.out.println("写入state："+state) ;
					((HttpServletResponse)aResponse).sendRedirect(mClientConf.getAuthServerAuthUrl(state)) ;
				}
				return ;
			}
			else if(mRefreshAuthsMatcher != null && mRefreshAuthsMatcher.matches(httpReq))
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
					if(mNeedRefreshUserIds.remove(ctoken.getPrincipal().getId()))
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
								//
								Request req = Request.POST().path(mClientConf.getAuthServerTokenPath()).queryParam("client_id", mClientConf.getClientId())
//										.queryParam("client_secret" , mClientSecret)
										.queryParam("grant_type" , "refresh_token")
										.queryParam("refresh_token" , ctoken.getRefreshToken())
										.queryParam("redirect_uri", mClientConf.getCodeCallbackUrl()) ;
								try
								{
									JSONObject reply = (JSONObject)mAuthClient.ask(req) ;
									String accessToken = reply.optString("access_token") ;
									String refreshToken = reply.optString("refresh_token") ;
									String payload = XString.seg_i(accessToken , '.' , 1) ;
									JSONObject payload_jobj = new JSONObject(new String(Base64URL.from(payload).decode() , "UTF-8")) ;
									Date iat = new Date(payload_jobj.optLong("iat")*1000) ;
									Date exp = new Date(payload_jobj.optLong("exp")*1000) ;
									JSONArray ja = payload_jobj.optJSONArray("auths") ;
									JSONObject jobj_userDetail = payload_jobj.optJSONObject("detail") ;
									
									User user = ctoken.getPrincipal() ;
									user.setUsername(payload_jobj.optString("sub"));
									user.setAuthorities(ja!=null?ja.toStringArray():null) ;
									String realName = jobj_userDetail.optString("realName") ;
									user.setRealName(realName) ;
									user.setSex(jobj_userDetail.optString("sex"));
									user.setAdditionProperties(jobj_userDetail) ;
									
									ctoken.setAccessToken(accessToken) ;
									ctoken.setRefreshToken(refreshToken) ;
									ctoken.setExpiredTime(exp);
									ctoken.setIssueTime(iat) ;
									ctoken.setAuthenticated(true) ;
									ctoken.setForceExpired(false) ;
									
									String corsToken = jobj_userDetail.optString("corsToken") ;
									if(XString.isNotEmpty(corsToken))
									{
										corsToken = CorsTokenSignHelper.signCorsToken(corsToken, httpReq.getRequestURL() , mOAuthClientApp.getAppSecret()) ;	
										((HttpServletResponse)aResponse).addCookie(new Cookie("cors-token", corsToken)) ;
									}
									
									mLogger.info("刷新了用户[{}]的令牌，原先的过期时间是:{}" , user.getUsername() , XTime.format$yyyyMMddHHmmss(ctoken.getExpiredTime())) ;
								}
								catch (Exception e)
								{
									mLogger.error(ExceptionAssist.getClearMessage(getClass(), e)) ;
									ctoken.setAuthenticated(false) ;
								}
							}
						}
					}
				}
			}
		}
		
		try
		{
			aChain.doFilter(aRequest, aResponse) ;
		}
		finally
		{
			AppContext.removeThreadLocal("user_subspaceid") ;
		}
	}
}
