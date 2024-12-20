package team.sailboat.ms.ac.frame;

import java.io.IOException;
import java.util.Map;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.ms.ac.AppConsts;
import team.sailboat.ms.ac.utils.LoginFailStore;

/**
 * 
 *
 * @author yyl
 * @since 2024年11月4日
 */
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler
{
	final Map<String, String> mResetPasswdUserNames ;
	
	final LoginFailStore mLoginFailStore ;
	
	public CustomAuthenticationFailureHandler(Map<String, String> aResetPasswdUserNames
			, LoginFailStore aLoginFailStore)
	{
		mResetPasswdUserNames = aResetPasswdUserNames ;
		mLoginFailStore = aLoginFailStore ;
	}
	

	@Override
	public void onAuthenticationFailure(HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException
	{
		if (exception instanceof CredentialsExpiredException)
		{
			String token = XString.randomString(32);
			mResetPasswdUserNames.put(token, request.getParameter("username"));
			getRedirectStrategy().sendRedirect(request, response, AppConsts.sPagePath_ResetExpiredPasswd + "?authToken=" + token);
		}
		else
		{
			if(exception instanceof BadCredentialsException)
			{
				// 错误的凭据
				int remainRetryTimes = mLoginFailStore.recordLoginFail(request.getRemoteAddr()) ;
				request.getSession().setAttribute("remainRetryTimes" , remainRetryTimes) ;
			}
			super.onAuthenticationFailure(request, response, exception);
		}
	}
}
