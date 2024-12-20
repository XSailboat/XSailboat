package team.sailboat.ms.ac.frame;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import team.sailboat.ms.ac.utils.LoginFailStore;

public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler
{
	
	final LoginFailStore mLoginFailStore ;
	
	public CustomAuthenticationSuccessHandler(LoginFailStore aLoginFailStore)
	{
		mLoginFailStore = aLoginFailStore ;
	}
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws ServletException, IOException
	{
		super.onAuthenticationSuccess(request, response, authentication) ;
		HttpSession session = request.getSession(false) ;
		session.removeAttribute("remainRetryTimes") ;
		mLoginFailStore.clearLoginFail(request.getRemoteAddr()) ;
	}
}
