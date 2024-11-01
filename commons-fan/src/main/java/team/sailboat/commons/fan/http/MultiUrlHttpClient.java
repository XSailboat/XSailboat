package team.sailboat.commons.fan.http;

import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Map.Entry;

import team.sailboat.commons.fan.lang.Assert;

public class MultiUrlHttpClient extends HttpClient
{
	
	int mSelectIndex = 0 ;
	
	boolean mPathAsContextPath ;
	
	URL[] mUrls ;
	

	MultiUrlHttpClient(URL[] aUrls , boolean aPathAsContextPath)
	{
		super() ;
		Assert.notEmpty(aUrls , "未指定需要连接的URL！");
		mUrls = aUrls ;
		mPathAsContextPath = aPathAsContextPath ;
		select(mSelectIndex);
	}
	
	protected void select(int aIndex)
	{
		mSelectIndex = mSelectIndex % mUrls.length ;
		URL url = mUrls[mSelectIndex] ;
		setHost(url.getHost()) ;
		setPort(url.getPort() == -1 ? url.getDefaultPort() : url.getPort()) ;
		if(mPathAsContextPath)
			setContextPath(url.getPath()) ;
	}
	
	@Override
	protected Entry<HttpURLConnection, Integer> doRequest_0(Request aRequest) throws Exception
	{
		try
		{
			return super.doRequest_0(aRequest);
		}
		catch(SocketTimeoutException e)
		{
			for(int i=1 ; i<mUrls.length ; i++)
			{
				select(mSelectIndex+1) ;
				try
				{
					return super.doRequest_0(aRequest) ;
				}
				catch(SocketTimeoutException e1)
				{}
			}
			throw e ;
		}
	}


	@Override
	public String getProtocol()
	{
		return mUrls[mSelectIndex].getProtocol() ;
	}
	
	
}
