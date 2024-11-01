package team.sailboat.commons.ms.authclient;

import java.io.IOException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.nimbusds.jose.util.Base64URL;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.http.Request;
import team.sailboat.commons.fan.http.URLCoder;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.text.XString;

public class OAuthCodeCallbackFilter extends AbstractAuthenticationProcessingFilter
{	
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	final URLCoder mURLCoder = URLCoder.getDefault() ;
	
	OAuthClientConf mClientConf ;
	
//	String mEP_token = "/oauth2/token" ;
	
	public OAuthCodeCallbackFilter(OAuthClientConf aClientConf)
	{
		super(new AntPathRequestMatcher(aClientConf.getCodeCallbackPath())) ;
		
		mClientConf = aClientConf ;
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
		
		String code = aRequest.getParameter("code") ;
		if(XString.isEmpty(code))
			throw new AuthenticationServiceException("code不能为空！");
		String state = aRequest.getParameter("state") ;
//		System.out.println("参数返回state:"+state) ;
		if(XString.isEmpty(state))
			throw new AuthenticationServiceException("state不能为空！");
		// 校验state的一致性
//		System.out.println("session中取得state："+aRequest.getSession().getAttribute("oauth_state"));
		if(!state.equals(aRequest.getSession().getAttribute("oauth_state")))
			throw new AuthenticationServiceException("state无效！") ;
		
		//
		Request req = Request.POST().path(mClientConf.getAuthServerTokenPath()).queryParam("client_id", mClientConf.getClientId())
//				.queryParam("client_secret" , mClientSecret)
				.queryParam("grant_type" , "authorization_code")
				.queryParam("code" , code)
				.queryParam("redirect_uri", mClientConf.getCodeCallbackUrl()) ;		// 这个是必需的
		try
		{
			JSONObject reply = (JSONObject)mClientConf.getAuthCenterClient().ask(req) ;
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
			
			String corsToken = jobj_userDetail.optString("corsToken") ;
			if(XString.isNotEmpty(corsToken))
			{
				corsToken = CorsTokenSignHelper.signCorsToken(corsToken, aRequest.getRequestURL() , mClientConf.getClientSecret()) ;	
				((HttpServletResponse)aResponse).addCookie(new Cookie("cors-token", corsToken)) ;
			}
			
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
