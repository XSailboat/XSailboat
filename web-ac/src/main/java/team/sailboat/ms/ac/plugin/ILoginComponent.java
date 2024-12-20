package team.sailboat.ms.ac.plugin;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public interface ILoginComponent
{
	boolean isEnabled() ;
	
	void injectFilter(HttpSecurity aHttp , String aDefaultSuccessUrl) ;
}
