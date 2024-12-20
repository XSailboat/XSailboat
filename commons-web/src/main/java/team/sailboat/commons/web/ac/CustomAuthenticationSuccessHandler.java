package team.sailboat.commons.web.ac;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.text.XString;

/**
 * 
 * 在原来的基础上，添加让浏览器存储cookie项的头
 *
 * @author yyl
 * @since 2024年12月10日
 */
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler
{
	final static Logger sLogger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class) ;
	
	OAuthClientConf mClientConf ;
	
	public CustomAuthenticationSuccessHandler(OAuthClientConf aClientConf)
	{
		mClientConf = aClientConf ;
	}
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest aRequest, HttpServletResponse aResponse,
			Authentication authentication) throws ServletException, IOException
	{
		super.onAuthenticationSuccess(aRequest, aResponse, authentication) ;
		OAuth2User user = ((OAuth2AuthenticationToken)authentication).getPrincipal() ;
		if(user instanceof AuthUser_AC)
		{
			String corsToken = user.getAttribute(AuthUser_AC.sAK_corsToken) ;
			if(XString.isNotEmpty(corsToken))
			{
				try
				{
					corsToken = CorsTokenSignHelper.signCorsToken(corsToken, aRequest.getRequestURL()
							, mClientConf.getClientSecret()) ;
					((HttpServletResponse)aResponse).addCookie(new Cookie("cors-token", corsToken)) ;
				}
				catch (InvalidKeyException | NoSuchAlgorithmException | IllegalStateException
						| UnsupportedEncodingException e)
				{
					sLogger.error(ExceptionAssist.getClearMessage(getClass(), e , "签名CorsToken出现异常！")) ;
				}	
				
			}
		}
	}
}
