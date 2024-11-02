package team.sailboat.bd.base.kafka ;

import java.util.Properties;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import team.sailboat.commons.fan.collection.AutoCleanHashMap;
import team.sailboat.commons.fan.collection.PropertiesEx;

public class KafkaAdminManager
{
	final AutoCleanHashMap<String, AdminClient> mAdminClientMap = AutoCleanHashMap.withExpired_Idle(10, true) ;
	final PropertiesEx mDefaultKafkaConf = new PropertiesEx() ;
	
	public KafkaAdminManager(Properties aBaseConf)
	{
		if(aBaseConf != null)
			mDefaultKafkaConf.putAll(aBaseConf) ;
		mDefaultKafkaConf.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
	}
	
	/**
	 * AdminClient单实例， 非线程安全，外面要注意同步
	 * @param aServerAddr
	 * @return
	 */
	public AdminClient getAdminClient(String aServerAddr)
	{
		AdminClient adminClient = mAdminClientMap.get(aServerAddr) ;
		if(adminClient == null)
		{
			synchronized (aServerAddr.intern())
			{
				adminClient = mAdminClientMap.get(aServerAddr) ;
				if(adminClient == null)
				{
					PropertiesEx prop = new PropertiesEx() ;
					prop.putAll(mDefaultKafkaConf) ;
					prop.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG , aServerAddr) ;
					adminClient = AdminClient.create(prop) ;
					mAdminClientMap.put(aServerAddr, adminClient) ;
				}
			}
		}
		return adminClient ;
	}
}
