package team.sailboat.ms.crane;

import java.util.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import team.sailboat.commons.ms.custom.PropertiesExSourceFactory;

@Component
@PropertySource(value="file:${app.config.path}" , ignoreResourceNotFound=false
		, encoding="utf-8" , name="app-config" , factory = PropertiesExSourceFactory.class)
@ConfigurationProperties(prefix = "store")
public class StoreAppConf
{
	Properties mAppConf ;
	
	
	public Properties getAppConf()
	{
		return mAppConf;
	}
	public void setAppConf(Properties aAppConf)
	{
		mAppConf = aAppConf;
	}
}
