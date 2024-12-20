package team.sailboat.ms.ac.ext.sailboat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import team.sailboat.commons.ms.custom.PropertiesExSourceFactory;
import team.sailboat.ms.ac.AppConfig;

@PropertySource(value="file:${app.config.path}" , ignoreResourceNotFound=false
	, encoding="utf-8" , name="app-config-sailboat" , factory = PropertiesExSourceFactory.class)
public class AppConfig_Sailboat extends AppConfig
{
	@Value( "${ha.zookeeper.quorum}")
	String mZookeeperQuorum ;
	
	public String getZookeeperQuorum()
	{
		return mZookeeperQuorum;
	}
}
