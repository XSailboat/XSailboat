package team.sailboat.ms.ac.filter;

import java.io.IOException;
import java.util.Map;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import team.sailboat.commons.fan.http.HttpStatus;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.ms.ac.AppConsts;
import team.sailboat.ms.ac.utils.ParameterRequestWrapper;

/**
 * 密码过期，重置
 *
 * @author yyl
 * @since 2024年8月13日
 */
public class ResetExpiredPasswdFilter extends OncePerRequestFilter
{
	RequestMatcher mReqMatcher ; 
	RequestMatcher mReqMatcher_api ; 
	Map<String , String> mResetPasswdUserNames ;
	
	public ResetExpiredPasswdFilter(Map<String , String> aResetPasswdUserNames)
	{
		mReqMatcher = new AntPathRequestMatcher(AppConsts.sPagePath_ResetExpiredPasswd) ;
		mReqMatcher_api = new AntPathRequestMatcher(AppConsts.sApiPath_login) ;
		mResetPasswdUserNames = aResetPasswdUserNames ;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest aRequest, HttpServletResponse aResponse, FilterChain aChain)
			throws ServletException, IOException
	{
		if(mReqMatcher.matches((HttpServletRequest)aRequest))
		{
			String authToken = aRequest.getParameter("authToken") ;
			if(XString.isEmpty(authToken))
				aResponse.sendError(HttpStatus.FORBIDDEN.value() , "无权访问！") ;
			
			String username = mResetPasswdUserNames.remove(authToken) ;
			if(username == null)
				aResponse.sendError(HttpStatus.FORBIDDEN.value() , "无权访问！") ;
			
			String newAuthToken = XString.randomString(32) ;
			aRequest.setAttribute("authToken" , newAuthToken) ;
			mResetPasswdUserNames.put(newAuthToken, username) ;
		}
		else if(mReqMatcher_api.matches((HttpServletRequest)aRequest))
		{
			String authToken = aRequest.getParameter("authToken") ;
			if(XString.isNotEmpty(authToken))
			{
				String dataStr = mResetPasswdUserNames.remove(authToken) ;
				Assert.notEmpty(dataStr , "无效的AuthToken：%s" , authToken) ;
				try
				{
					JSONObject jo = JSONObject.of(dataStr) ;
					ParameterRequestWrapper newReq = new ParameterRequestWrapper(aRequest) ;
					newReq.setParameter("codeId" , "XXXX");
					newReq.setParameter("username" , jo.optString("username")) ;
					newReq.setParameter("password" , jo.optString("password")) ;
					newReq.setMethod("POST");
					aRequest = newReq ;
				}
				catch(Exception e)
				{
					throw new IllegalArgumentException("无效的AuthToken:" + authToken) ;
				}
			}
		}
		aChain.doFilter(aRequest, aResponse);
	}
}
