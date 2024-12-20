package team.sailboat.ms.ac.filter;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.ms.crypto.RSAKeyPairMaker4JS;
import team.sailboat.ms.ac.AppConsts;
import team.sailboat.ms.ac.utils.LoginFailStore;
import team.sailboat.ms.ac.utils.ParameterRequestWrapper;

@Order(value = 1)
public class PasswordDecorderFilter extends GenericFilterBean
{
	RequestMatcher mReqMatcher ; 
	RSAKeyPairMaker4JS mMaker ;
	
	final LoginFailStore mLoginFailStore ;

	public PasswordDecorderFilter(String aPathPattern , RSAKeyPairMaker4JS aRSAMaker
			, LoginFailStore aLoginFailStore)
	{
		mReqMatcher = new AntPathRequestMatcher(aPathPattern) ;
		mMaker = aRSAMaker ;
		mLoginFailStore = aLoginFailStore ;
	}
	
	@Override
	public void doFilter(ServletRequest aRequest, ServletResponse aResponse, FilterChain aChain)
			throws IOException, ServletException
	{
		if(mReqMatcher.matches((HttpServletRequest)aRequest))
		{
			// 检查是否登录失败次数过多
			String ip = aRequest.getRemoteAddr() ;
			if(mLoginFailStore.isLoginFailTooMore(ip))
			{
				mLoginFailStore.recordLoginFail(ip) ;
				// 登录失败次数太多，直接不让访问
				String msg = XString.msgFmt("登录失败次数超过{}次，请{}分钟之后再试！" , mLoginFailStore.getFailTimesLimit()
						, LoginFailStore.sTimeGapMi) ;
				HttpServletRequest req = (HttpServletRequest)aRequest ;
				req.getSession(false)
						.setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION , new LockedException(msg)) ;
				((HttpServletResponse)aResponse).sendRedirect(req.getContextPath() + AppConsts.sViewPath_loginFailure) ;
				return ;
			}
			//
			aRequest.getParameterMap() ;
			String password = aRequest.getParameter(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY) ;
			String codeId = aRequest.getParameter("codeId") ;
			if(XString.isNotEmpty(codeId) && XString.isNotEmpty(password))
			{
				try
				{
//					password = mMaker.decode4JS(codeId, password) ;
					if(!"XXXX".equals(codeId))
						password = mMaker.decrypt4js(codeId, password) ;
					ParameterRequestWrapper req = (aRequest instanceof ParameterRequestWrapper)?(ParameterRequestWrapper)aRequest
							:new ParameterRequestWrapper((HttpServletRequest) aRequest) ;
					req.setParameter(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY , password) ;
					aChain.doFilter(req , aResponse) ;
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		else
			aChain.doFilter(aRequest, aResponse);
	}

}
