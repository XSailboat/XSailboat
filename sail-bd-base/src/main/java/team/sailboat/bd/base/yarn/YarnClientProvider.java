package team.sailboat.bd.base.yarn;

import java.util.function.Supplier;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YarnClientProvider	implements Supplier<YarnClient>
{
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	final Object mMutex = new Object() ;
	
	YarnClient mYarnClient ;
	
	Configuration mConf ;
	
	public YarnClientProvider(Configuration aConf)
	{
		mConf = aConf ;
	}

	@Override
	public YarnClient get()
	{
		if(mYarnClient == null)
		{
			synchronized (mMutex)
			{
				if(mYarnClient == null)
				{
					YarnConfiguration yarnConf = new YarnConfiguration(mConf);
					YarnClient yarnClient = YarnClient.createYarnClient();
					yarnClient.init(yarnConf);
					yarnClient.start();
					mLogger.info("YarnClient已经构建。");
					mYarnClient = yarnClient ;
				}
			}
		}
		return mYarnClient ;
	}

}
