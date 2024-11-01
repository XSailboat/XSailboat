package team.sailboat.base;

import java.net.MalformedURLException;
import java.net.URL;

import team.sailboat.commons.fan.http.HttpClient;
import team.sailboat.commons.fan.text.XString;

public class SimpleHttpClientProvider extends HttpClientProvider
{
	
	SimpleHttpClientProvider(URL aUrl , String aAppKey , String aAppSecret) throws MalformedURLException
	{
		super(aAppKey) ;
		setServiceAddrs(new URL[] {aUrl}) ;
		mAppKey = aAppKey ;
		mAppSecret = aAppSecret ;
	}
	
	@Override
	protected HttpClient createHttpClient(URL[] aServiceAddrs) throws MalformedURLException
	{
		if(XString.isNotEmpty(mAppKey))
			return HttpClient.ofUrl(aServiceAddrs[0] , mAppKey , mAppSecret , mSigner , true) ;
		else
			return super.createHttpClient(aServiceAddrs);
	}
}
