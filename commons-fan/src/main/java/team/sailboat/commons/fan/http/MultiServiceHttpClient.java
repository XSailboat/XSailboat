package team.sailboat.commons.fan.http;

import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

public class MultiServiceHttpClient extends HttpClient
{
	
	int mSelectIndex = 0 ;
	
	List<ServiceAddress> mAddrs ;

	MultiServiceHttpClient(List<ServiceAddress> aAddrs)
	{
		super(aAddrs.get(0).getHost() , aAddrs.get(0).getPort()) ;
		mAddrs = Arrays.asList(aAddrs.toArray(new ServiceAddress[0])) ;
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
			final int startIndex = mSelectIndex ;
			while(true)
			{
				mSelectIndex = (mSelectIndex+1)%mAddrs.size() ;
				if(mSelectIndex == startIndex)
					break ;
				ServiceAddress servAddr = mAddrs.get(mSelectIndex) ;
				setHost(servAddr.getHost()) ;
				setPort(servAddr.getPort()) ;
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
		return "http" ;
	}
	
	
}
