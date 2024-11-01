package team.sailboat.commons.fan.http;

import java.net.HttpURLConnection;

class SignHttpsClient extends DefaultHttpsClient
{
	String mAppKey ;
	String mAppSecret ;
	ISigner mSigner ;

	protected SignHttpsClient(String aHost, int aPort , String aAppKey , String aAppSecret
			, ISigner aSigner , String aTSLv)
	{
		super(aHost, aPort , aTSLv);
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
