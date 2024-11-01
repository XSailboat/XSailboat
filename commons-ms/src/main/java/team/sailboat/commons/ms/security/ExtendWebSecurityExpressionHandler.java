package team.sailboat.commons.ms.security;

import java.util.Enumeration;

import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;

import jakarta.servlet.http.HttpServletRequest;

public class ExtendWebSecurityExpressionHandler extends DefaultWebSecurityExpressionHandler
{
	final PermissionEvaluator mEvaluator = new SubspacePermissionEvaluator() ;
	
	public ExtendWebSecurityExpressionHandler()
	{
	}

	@Override
	protected StandardEvaluationContext createEvaluationContextInternal(Authentication aAuthentication,
			FilterInvocation aInvocation)
	{
		StandardEvaluationContext ctx = (StandardEvaluationContext) super.createEvaluationContextInternal(aAuthentication
				, aInvocation) ;
		HttpServletRequest req = aInvocation.getHttpRequest() ;
		Enumeration<String> e = req.getAttributeNames() ;
		while(e.hasMoreElements())
		{
			String name = e.nextElement() ;
			ctx.setVariable(name , req.getAttribute(name)) ;
		}
		return ctx;
	}
	
	@Override
	protected PermissionEvaluator getPermissionEvaluator()
	{
		return mEvaluator ;
	}

}
