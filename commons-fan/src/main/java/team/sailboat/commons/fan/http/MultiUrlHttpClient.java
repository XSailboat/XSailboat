package team.sailboat.commons.fan.http;

import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Map.Entry;

import team.sailboat.commons.fan.lang.Assert;

public class MultiUrlHttpClient extends HttpClient
{
	
	int mSelectIndex = 0 ;
	
	boolean mPathAsContextPath ;
	
	URI[] mUris ;
	

	MultiUrlHttpClient(URI[] aUris , boolean aPathAsContextPath)
	{
		super() ;
		Assert.notEmpty(aUris , "未指定需要连接的URL！");
		mUris = aUris ;
		mPathAsContextPath = aPathAsContextPath ;
		select(mSelectIndex);
	}
	
	protected void select(int aIndex)
	{
		mSelectIndex = mSelectIndex % mUris.length ;
		URI uri = mUris[mSelectIndex] ;
		setHost(uri.getHost()) ;
		setPort(uri.getPort() == -1 ? ("https".equals(uri.getScheme())?443:80) : uri.getPort()) ;
		if(mPathAsContextPath)
			setContextPath(uri.getPath()) ;
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
			for(int i=1 ; i<mUris.length ; i++)
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
		return mUris[mSelectIndex].getScheme() ;
	}
	
	
}
