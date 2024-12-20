package team.sailboat.base;

import java.net.URI;

import team.sailboat.commons.fan.http.HttpClient;
import team.sailboat.commons.fan.text.XString;

/**
 * 简单的基于Uri的HttpClient提供器
 *
 * @author yyl
 * @since 2024年12月6日
 */
public class SimpleHttpClientProvider extends HttpClientProvider
{
	
	SimpleHttpClientProvider(URI aUri , String aAppKey , String aAppSecret)
	{
		super(aAppKey) ;
		setServiceAddrs(new URI[] {aUri}) ;
		mAppKey = aAppKey ;
		mAppSecret = aAppSecret ;
	}
	
	@Override
	protected HttpClient createHttpClient(URI[] aServiceAddrs)
	{
		if(XString.isNotEmpty(mAppKey))
			return HttpClient.ofURI(aServiceAddrs[0] , mAppKey , mAppSecret , mSigner , true) ;
		else
			return super.createHttpClient(aServiceAddrs);
	}
}
