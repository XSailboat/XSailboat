package team.sailboat.login.extend.ding;

import java.io.IOException;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.ms.ac.dbean.User;

public class DingAuthenticationFilter extends AbstractAuthenticationProcessingFilter 
{
	
	private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER 
			= new AntPathRequestMatcher("/dingLogin") ;
	
	String mPN_Code = "code" ;
	
	String mSuccessUrl ;
	
	public DingAuthenticationFilter() 
	{
		super(DEFAULT_ANT_PATH_REQUEST_MATCHER);
	}

//	public DingAuthenticationFilter(@Autowired AuthenticationManager authenticationManager) 
//	{
//		super(DEFAULT_ANT_PATH_REQUEST_MATCHER, authenticationManager);
//	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest aRequest, HttpServletResponse aResponse)
			throws AuthenticationException, IOException, ServletException
	{
		String code = obtainCode(aRequest);
		DingCodeAuthenticationToken authRequest = null ;
		if(XString.isEmpty(code))
		{
			HttpSession session = aRequest.getSession() ;
			User user = (User)session.getAttribute("DingLoginUser") ;
			if(user != null)
			{
				authRequest = new DingCodeAuthenticationToken(user, (String)session.getAttribute("dingCode")) ;
				session.removeAttribute("DingLoginUser") ;
				session.removeAttribute("dingCode") ;
			}
			else
				throw new AuthenticationServiceException("ding认证的code不能为空！");
		}
		else
			authRequest = new DingCodeAuthenticationToken(code) ;
		return this.getAuthenticationManager().authenticate(authRequest);
	}
	
	protected String obtainCode(HttpServletRequest aRequest)
	{
		return aRequest.getParameter(mPN_Code);
	}
	
	public void setSuccessUrl(String aSuccessUrl)
	{
		mSuccessUrl = aSuccessUrl;
		((AbstractAuthenticationTargetUrlRequestHandler)getSuccessHandler()).setDefaultTargetUrl(mSuccessUrl);  ;
	}
	
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest aRequest,
			HttpServletResponse aResponse,
			AuthenticationException aFailed) throws IOException, ServletException
	{
		if(aFailed instanceof NeedBindAccountException)
		{
			HttpSession session = aRequest.getSession() ;
			session.setAttribute("dingCodeUserInfo" , ((NeedBindAccountException) aFailed).getUserInfo());
			session.setAttribute("dingCode", obtainCode(aRequest)) ;
			aRequest.getRequestDispatcher("/bind_acccount").forward(aRequest, aResponse) ;
		}
		else
			super.unsuccessfulAuthentication(aRequest, aResponse, aFailed);
	}

}
