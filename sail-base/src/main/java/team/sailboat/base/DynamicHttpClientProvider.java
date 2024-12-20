package team.sailboat.base;

import java.net.URI;
import java.util.function.Supplier;

import team.sailboat.commons.fan.http.HttpClient;
import team.sailboat.commons.fan.lang.JCommon;

/**
 * 
 * 动态HttpClient提供器
 *
 * @author yyl
 * @since 2024年12月6日
 */
public class DynamicHttpClientProvider extends HttpClientProvider
{
	
	Supplier<String> mServiceAddrSupplier ;
	String mCurrentServiceAddr ;
	HttpClient mHttpClient ;
	
	DynamicHttpClientProvider(String aName , Supplier<String> aServiceAddrSupplier)
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
			mHttpClient = HttpClient.ofURI(serviceAddr) ;
			mCurrentServiceAddr = serviceAddr ;
		}
		return mHttpClient ;
	}
	
	@Override
	protected void setServiceAddrs(URI[] aServiceAddrs)
	{
		throw new UnsupportedOperationException("不支持的操作") ;
 	}
}
