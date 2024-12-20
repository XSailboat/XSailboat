package team.sailboat.ms.ac.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer
{
	@Override
	public void addViewControllers(ViewControllerRegistry aRegistry)
	{
		WebMvcConfigurer.super.addViewControllers(aRegistry);
	}
}
