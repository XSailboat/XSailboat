package team.sailboat.commons.ms.authclient;

import java.io.IOException;
import java.util.Base64;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.core.log.LogMessage;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.NullRememberMeServices;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import com.nimbusds.jose.util.Base64URL;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.http.Request;
import team.sailboat.commons.fan.http.URLCoder;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.text.XString;

public class CorsTokenLoginFilter extends GenericFilterBean
		implements ApplicationEventPublisherAware
{	
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	final URLCoder mURLCoder = URLCoder.getDefault() ;
	
	OAuthClientConf mClientConf ;
	
	protected ApplicationEventPublisher eventPublisher;
	
	SessionAuthenticationStrategy sessionStrategy = new NullAuthenticatedSessionStrategy() ;
	
	RememberMeServices rememberMeServices = new NullRememberMeServices() ;
	
	AuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();
	
//	String mEP_token = "/oauth2/token" ;
	
	RequestMatcher requiresAuthenticationRequestMatcher;
	
	public CorsTokenLoginFilter(OAuthClientConf aClientConf)
	{
		this.requiresAuthenticationRequestMatcher = new RequestHeaderRequestMatcher("cors-token-auth") ;
		
		mClientConf = aClientConf ;
	}
	
	/**
	 * 是否需要安全认证，如果访问的目标无需认证，或者已经登录
	 * @param request
	 * @param response
	 * @return
	 */
	protected boolean requireAuth(HttpServletRequest request, HttpServletResponse response)
	{
		if (this.requiresAuthenticationRequestMatcher.matches(request))
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
	
	public void doFilter(ServletRequest aRequest, ServletResponse aResponse, FilterChain chain)
			throws IOException, ServletException
	{
		HttpServletRequest request = (HttpServletRequest)aRequest ;
		HttpServletResponse response = (HttpServletResponse)aResponse ;
		
		if (!requireAuth(request, response))
		{
			chain.doFilter(request, response);
			return;
		}
		
		try
		{
			Authentication authResult = attemptAuthentication(request, response);
			if (authResult == null)
			{
				// return immediately as subclass has indicated that it hasn't completed
				return;
			}
			this.sessionStrategy.onAuthentication(authResult, request, response);
			
			SecurityContextHolder.getContext().setAuthentication(authResult);
			if (this.logger.isDebugEnabled()) {
				this.logger.debug(LogMessage.format("Set SecurityContextHolder to %s", authResult));
			}
			this.rememberMeServices.loginSuccess(request, response, authResult);
			if (this.eventPublisher != null)
			{
				this.eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(authResult, this.getClass()));
			}
			clearAuthenticationAttributes(request) ;
			
			chain.doFilter(request, response) ;
		}
		catch (InternalAuthenticationServiceException failed)
		{
			this.logger.error("An internal error occurred while trying to authenticate the user.", failed);
			unsuccessfulAuthentication(request, response, failed);
		}
		catch (AuthenticationException ex)
		{
			// Authentication failed
			unsuccessfulAuthentication(request, response, ex);
		}
	}

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
		
		//
		Request req = Request.POST().path(mClientConf.getAuthServerTokenPath()).queryParam("client_id", mClientConf.getClientId())
//				.queryParam("client_secret" , mClientSecret)
				.queryParam("grant_type" , "cors_token")
				.queryParam("token" , corsToken)
				.queryParam("redirect_uri", mClientConf.getCodeCallbackUrl()) ;		// 这个是必需的，它在此不起作用，只是因为它是/oauth2/token接口的必填参数
		try
		{
			JSONObject reply = (JSONObject)mClientConf.getAuthCenterClient().ask(req) ;
			// 等调用得到结果，认证服务器已经校验过签名了
			String sourceNote = new String(Base64.getUrlDecoder().decode(XString.lastSeg_i(corsToken, '.' , 1))
					, AppContext.sUTF8) ;
			JSONObject sourceNoteJo = new JSONObject(sourceNote) ;
			String exceptReferer = sourceNoteJo.optString("referer") ;
			String currentReferer = aRequest.getHeader("referer") ;
			if(currentReferer == null || !currentReferer.startsWith(exceptReferer))
			{
				throw new AuthenticationServiceException("不允许的调用源！") ;
			}
			
			String accessToken = reply.optString("access_token") ;
			String refreshToken = reply.optString("refresh_token") ;
			String payload = XString.seg_i(accessToken , '.' , 1) ;
			JSONObject payload_jobj = new JSONObject(new String(Base64URL.from(payload).decode() , "UTF-8")) ;
			Date iat = new Date(payload_jobj.optLong("iat")*1000) ;
			Date exp = new Date(payload_jobj.optLong("exp")*1000) ;
			JSONArray ja = payload_jobj.optJSONArray("auths") ;
			JSONObject jobj_userDetail = payload_jobj.optJSONObject("detail") ;
			User user = new User(jobj_userDetail.optString("id") 
					, payload_jobj.optString("sub") 
					, ja!=null?ja.toStringArray():null) ;
			String realName = jobj_userDetail.optString("realName") ;
			user.setRealName(realName) ;
			user.setSex(jobj_userDetail.optString("sex"));
			user.setAdditionProperties(jobj_userDetail) ;
			CoupleAuthenticationToken token = new CoupleAuthenticationToken(accessToken , refreshToken
					, user
					, iat , exp) ;
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
	
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException, ServletException {
		SecurityContextHolder.clearContext();
		this.logger.trace("Failed to process authentication request", failed);
		this.logger.trace("Cleared SecurityContextHolder");
		this.logger.trace("Handling authentication failure");
		this.rememberMeServices.loginFail(request, response);
		this.failureHandler.onAuthenticationFailure(request, response, failed);
	}
	
	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher aApplicationEventPublisher)
	{
		this.eventPublisher = aApplicationEventPublisher ;
	}

	public void clearAuthenticationAttributes(HttpServletRequest request)
	{
		HttpSession session = request.getSession(false);
		if (session != null)
		{
			session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		}
	}
}
