package team.sailboat.commons.ms.security;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.FilterInvocation;

@Configuration
public class ExtendSELConfiguration implements BeanPostProcessor
{
	ExtendWebSecurityExpressionHandler mExpressionHandler = new ExtendWebSecurityExpressionHandler() ;
	
	@Primary
	@Bean
	SecurityExpressionHandler<FilterInvocation> _webSecurityExpressionHandler()
	{
		return mExpressionHandler ;
	}

	@Bean
	RemoveDefaultWSEHProcessor _removeDefaultWSEHProcessor()
	{
		return new RemoveDefaultWSEHProcessor() ;
	}
	
	@Override
	public Object postProcessAfterInitialization(Object aBean, String aBeanName) throws BeansException
	{
		if(aBean instanceof HttpSecurity)
		{
			HttpSecurity http = (HttpSecurity)aBean ;
			try
			{
//				http.authorizeRequests().expressionHandler(mExpressionHandler) ;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return aBean ;
	}
}
