package team.sailboat.ms.crane;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import lombok.Data;
import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.ms.custom.PropertiesExSourceFactory;

@Data
@Component
@PropertySource(value={"file:${app.config.path}"} , ignoreResourceNotFound=false
		, encoding="utf-8" , name="app-config" , factory = PropertiesExSourceFactory.class)
@ConfigurationProperties
public class AppConfig
{
	
//	Properties modules ;
	
	/**
	 * 里面的键是不带sys_params.这个前缀的
	 */
	PropertiesEx sys_params ;
	
}
