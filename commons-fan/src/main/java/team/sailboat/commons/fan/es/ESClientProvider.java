package team.sailboat.commons.fan.es;

import java.net.MalformedURLException;
import java.util.function.Supplier;

import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.http.HttpClient;
import team.sailboat.commons.fan.http.xca.ApiKeySigner;
import team.sailboat.commons.fan.text.XString;

public class ESClientProvider implements Supplier<ESClient>
{
	String mUrl ;
	String mApiKey ;
	
	ESClient mClient ;
	
	HttpClient mHttpClient ;
	
	/**
	 * 
	 * @param aUrl
	 * @param aApiKey
	 * @throws MalformedURLException
	 */
	public ESClientProvider(String aUrl
			, String aApiKey) throws MalformedURLException
	{
		mUrl = aUrl ;
		mApiKey = aApiKey ;
	}

	@Override
	public ESClient get()
	{
		if(mClient == null)
		{
			try
			{
				if(XString.isEmpty(mApiKey))
					mHttpClient = HttpClient.ofUrl(mUrl) ;
				else
				{
					mHttpClient = HttpClient.ofUrl(mUrl , mApiKey , null , new ApiKeySigner(), false) ;
				}
				mHttpClient.setFearure_encodeHeader(false) ;
				mClient = new ESClient(mHttpClient) ;
					
			}
			catch (MalformedURLException e)
			{
				WrapException.wrapThrow(e) ;
			}
		}
		return mClient ;
	}	
}
