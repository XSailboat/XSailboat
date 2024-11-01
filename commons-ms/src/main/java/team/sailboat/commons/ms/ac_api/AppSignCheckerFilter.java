package team.sailboat.commons.ms.ac_api;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import team.sailboat.commons.fan.excep.HttpException;
import team.sailboat.commons.fan.http.xca.XAppSigner;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.ms.xca.IAppSignChecker;

public class AppSignCheckerFilter extends OncePerRequestFilter
{
	final IAppSignChecker mSignChecker ;
	
	AppAuthenticationProvider mAuthenticationPvd ;

	public AppSignCheckerFilter(IAppSignChecker aSignChecker , AppAuthenticationProvider aAuthenticationPvd)
	{
		mSignChecker = aSignChecker ;
		mAuthenticationPvd = aAuthenticationPvd ;
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest aRequest, HttpServletResponse aResponse
			, FilterChain aFilterChain) throws ServletException, IOException
	{
		if(!Boolean.TRUE.equals(aRequest.getAttribute("AppSigneChecked")))
		{
			String signature = aRequest.getHeader(XAppSigner.X_CA_SIGNATURE) ;
			if(XString.isNotEmpty(signature))
			{	
				try
				{
					Authentication auth = mAuthenticationPvd.apply(mSignChecker.check(aRequest)) ;
					if(auth != null)
					{
						SecurityContext ctx = SecurityContextHolder.getContext() ;
						if(ctx == null)
						{
							ctx = SecurityContextHolder.createEmptyContext() ;
							ctx.setAuthentication(auth) ;
							SecurityContextHolder.setContext(ctx) ;
							
						}
						else
						{
							Authentication auth_1 = ctx.getAuthentication() ;
							if(auth_1 == null || "anonymousUser".equals(auth.getPrincipal()))
							{
								ctx.setAuthentication(auth) ;
							}
						}
					}
				}
				catch(HttpException e)
				{
					aResponse.sendError(e.getStatus().value(), e.getRawMessage());
					return ;
				}
			}
		}
		aFilterChain.doFilter(aRequest, aResponse) ;
	}
}
