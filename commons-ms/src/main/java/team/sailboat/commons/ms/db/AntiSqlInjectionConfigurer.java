package team.sailboat.commons.ms.db;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AntiSqlInjectionConfigurer  implements WebMvcConfigurer
{
	final AntiSqlInjectionInterceptor mInterceptor = new AntiSqlInjectionInterceptor() ;

	public AntiSqlInjectionConfigurer()
	{
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry aRegistry)
	{
		System.out.println("SQL防注入参数检查启用!!") ;
		aRegistry.addInterceptor(mInterceptor)
			.addPathPatterns("/**") ;
	}

}
