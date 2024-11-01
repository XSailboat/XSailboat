package team.sailboat.ms.base.kafka;

import java.util.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import team.sailboat.commons.ms.custom.PropertiesExSourceFactory;

@Component
@PropertySource(value="file:${app.config.path}" , ignoreResourceNotFound=false
		, encoding="utf-8" , name="kafka-config" , factory = PropertiesExSourceFactory.class)
@ConfigurationProperties(prefix = "kafka")
public class KafkaConf
{
	Properties mBaseConf ;
	
	
	public Properties getBaseConf()
	{
		return mBaseConf;
	}
	public void setBaseConf(Properties aBaseConf)
	{
		mBaseConf = aBaseConf;
	}
}
