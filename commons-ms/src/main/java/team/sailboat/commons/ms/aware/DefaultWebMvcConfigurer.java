package team.sailboat.commons.ms.aware;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import team.sailboat.commons.ms.access.RedirectInterceptor;

@Configuration
public class DefaultWebMvcConfigurer implements WebMvcConfigurer 
{
	public DefaultWebMvcConfigurer()
	{
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry aRegistry)
	{
		aRegistry.addInterceptor(new RedirectInterceptor()) ;
	}
	
	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> aConverters)
	{
		aConverters.add(new ExtExcepMsgReturnConverter()) ;
		aConverters.add(new StringsConverterOfHttpBody()) ;
	}
	
}
