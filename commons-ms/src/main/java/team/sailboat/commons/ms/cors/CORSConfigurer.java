package team.sailboat.commons.ms.cors ;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CORSConfigurer implements WebMvcConfigurer
{
	@Override
	public void addCorsMappings(CorsRegistry aRegistry)
	{
		System.out.println("CORSConfigurer");
//		aRegistry.addMapping("/**") ;
		 aRegistry.addMapping("/**")
		         .allowedOrigins("*")
		         .allowedMethods("GET", "POST")
		         .allowCredentials(false).maxAge(3600);
	}
}
