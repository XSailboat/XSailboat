package team.sailboat.commons.fan.http;

import java.net.HttpURLConnection;

class SignHttpClient extends DefaultHttpClient
{
	String mAppKey ;
	String mAppSecret ;
	ISigner mSigner ;

	protected SignHttpClient(String aHost, int aPort , String aAppKey , String aAppSecret
			, ISigner aSigner)
	{
		super(aHost, aPort);
		mAppKey = aAppKey ;
		mAppSecret = aAppSecret ;
		mSigner = aSigner ;
	}

	@Override
	protected HttpURLConnection buildConnection(Request aRequest) throws Exception
	{
		mSigner.sign(getContextPath() , aRequest, mAppKey, mAppSecret) ;
		return super.buildConnection(aRequest);
	}
}
