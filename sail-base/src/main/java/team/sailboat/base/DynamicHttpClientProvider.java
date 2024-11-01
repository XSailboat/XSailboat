package team.sailboat.base;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Supplier;

import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.http.HttpClient;
import team.sailboat.commons.fan.lang.JCommon;

public class DynamicHttpClientProvider extends HttpClientProvider
{
	
	Supplier<String> mServiceAddrSupplier ;
	String mCurrentServiceAddr ;
	HttpClient mHttpClient ;
	
	DynamicHttpClientProvider(String aName
			, Supplier<String> aServiceAddrSupplier) throws MalformedURLException
	{
		super(aName) ;
		mServiceAddrSupplier = aServiceAddrSupplier ;
	}
	
	@Override
	public HttpClient get()
	{
		String serviceAddr = mServiceAddrSupplier.get() ;
		if(JCommon.unequals(mServiceAddrSupplier, serviceAddr))
		{
			try
			{
				mHttpClient = HttpClient.ofUrl(serviceAddr) ;
			}
			catch (MalformedURLException e)
			{
				WrapException.wrapThrow(e) ;
			}
			mCurrentServiceAddr = serviceAddr ;
		}
		return mHttpClient ;
	}
	
	@Override
	protected void setServiceAddrs(URL[] aServiceAddrs)
	{
		throw new UnsupportedOperationException("不支持的操作") ;
 	}
}
