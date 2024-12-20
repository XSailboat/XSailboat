package team.sailboat.base ;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.event.IXListener;
import team.sailboat.commons.fan.event.XEvent;
import team.sailboat.commons.fan.event.XListenerAssist;
import team.sailboat.commons.fan.http.HttpClient;
import team.sailboat.commons.fan.http.ISigner;
import team.sailboat.commons.fan.http.xca.XAppSigner;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;

/**
 * 
 * HttpClient提供器（抽象基类）
 *
 * @author yyl
 * @since 2024年12月19日
 */
public abstract class HttpClientProvider implements Supplier<HttpClient>
{
	static final Logger sLogger = LoggerFactory.getLogger(HttpClientProvider.class) ;
	protected static final Map<String, HttpClientProvider> sHttpClientPvdMap = XC.hashMap() ;
	
	String mName ;
	
	private HttpClient mHttpClient ;
	private URI[] mServiceAddrs ;
	
	String mAppKey ;
	String mAppSecret ;
	
	protected ISigner mSigner = new XAppSigner() ;
	
	final Map<String, HttpClient> mCachedClientMap =  XC.autoCleanHashMap_idle(10) ;
	final Map<String, HttpClient> mCached = XC.concurrentHashMap() ;
	
	private final XListenerAssist mServiceAddrChangedLsnAssist = new XListenerAssist() ;
	
	/**
	 * 改用HttpClientProvider(String aAppDirName)
	 * @param aZKSysProxy
	 * @param aAppDirName
	 * @throws Exception 
	 */
	protected HttpClientProvider(String aName)
	{
		mName = aName ;
	}
	
	/**
	 * 取得服务地址
	 * 
	 * @return
	 */
	public URI[] getServiceAddrs()
	{
		return mServiceAddrs ;
	}
	
	/**
	 * 设置服务地址
	 * 
	 * @param aServiceAddrs
	 */
	protected void setServiceAddrs(URI[] aServiceAddrs)
	{
		if(!JCommon.equals(mServiceAddrs, aServiceAddrs))
		{
			sLogger.info("{}的服务地址，从{}切换至{}" , mName 
					, XString.toString(",", mServiceAddrs) 
					, XString.toString(",", aServiceAddrs)) ;
			mServiceAddrs = aServiceAddrs ;
			mHttpClient = null ;
			mCachedClientMap.clear() ;
			notifyServiceAddrChanged() ;
		}
	}
	
	protected void notifyServiceAddrChanged()
	{
		mServiceAddrChangedLsnAssist.notifyLsns(new XEvent(this, 0));
	}
	
	public void addServiceAddrChangedListener(IXListener aLsn)
	{
		mServiceAddrChangedLsnAssist.addListener(aLsn) ;
	}
	
	public void removeServiceAddrChangedListener(IXListener aLsn)
	{
		mServiceAddrChangedLsnAssist.removeLsn(aLsn) ;
	}
	
	public String getAppKey()
	{
		return mAppKey;
	}
	public boolean setAppKey(String aAppKey)
	{
		if(JCommon.unequals(mAppKey, aAppKey))
		{
			mAppKey = aAppKey;
			mHttpClient = null ;
			return true ;
		}
		return false ;
	}
	
	public String getAppSecret()
	{
		return mAppSecret;
	}
	public boolean setAppSecret(String aAppSecret)
	{
		if(JCommon.unequals(mAppSecret, aAppSecret))
		{
			mAppSecret = aAppSecret;
			mHttpClient = null ;
			return true ;
		}
		return false ;
	}
	
	public void setSigner(ISigner aSigner)
	{
		if(mSigner != aSigner)
		{
			if(mHttpClient != null)
				mHttpClient = null ;
			mSigner = aSigner;
		}
	}
	
	
	@Override
	public HttpClient get()
	{
		if(mHttpClient == null && XC.isNotEmpty(mServiceAddrs))
		{
			mHttpClient = createHttpClient(mServiceAddrs) ;
		}
		return mHttpClient ;
	}
	
	protected HttpClient createHttpClient(URI[] aServiceAddrs)
	{
		return createHttpClient(aServiceAddrs, mAppKey, mAppSecret) ;
	}
	
	protected HttpClient createHttpClient(URI[] aServiceAddrs , String aAppKey , String aAppSecret)
	{
		Assert.notEmpty(aServiceAddrs , "未指定服务地址URL！") ;
		if(XString.isEmpty(mAppKey))
		{
			return HttpClient.ofMulti(aServiceAddrs , true) ;
		}
		else
		{
			Assert.isTrue(aServiceAddrs.length == 1 , "需要签名认证的服务暂不支持多服务地址！");
			return HttpClient.ofURI(aServiceAddrs[0] , aAppKey, aAppSecret , mSigner , true) ;
		}
	}
	
	public HttpClient getOrCreate(String aAppKey , String aAppSecret)
	{	
		if(JCommon.equals(aAppKey, aAppKey)
				&& JCommon.equals(mAppSecret, aAppSecret))
			return get() ;
		else
		{
			String key = aAppKey + "/"+aAppSecret ;
			HttpClient client = mCachedClientMap.get(aAppKey) ;
			if(client == null)
			{
				client = createHttpClient(mServiceAddrs, aAppKey, aAppSecret) ;
				mCachedClientMap.put(key , client) ;
			}
			return client ;
		}
	}
	
//	public HttpClient getOrCreate(String aUrl) throws MalformedURLException
//	{
//		URL url = new URL(aUrl) ;
//		String protocol = url.getProtocol().toLowerCase() ;
//		if("http".equals(protocol) || "https".equals(protocol))
//		{
//			int port = url.getPort() ;
//			if(port == -1)
//				port = url.getDefaultPort() ;
//			String key = protocol+"://"+url.getHost()+":"+port ;
//			
//			HttpClient client = mCachedClientMap.get(key) ;
//			if(client == null)
//			{
//				client = HttpClient.ofUrl(aUrl) ;
//				mCachedClientMap.put(key, client) ;
//			}
//			return client ;
//		}
//		throw new IllegalArgumentException("不支持的协议："+protocol) ;
//		
//	}
	
	/**
	 * 
	 * 构造指定uri的HttpClient提供器
	 * 
	 * @param aUri
	 * @return
	 */
	public static HttpClientProvider ofApp(String aUri)
	{
		return ofApp(aUri , null, null) ;
	}
	
	public static HttpClientProvider ofService(String aName , Supplier<String> aServiceAddrSupplier) throws MalformedURLException
	{
		return new DynamicHttpClientProvider(aName, aServiceAddrSupplier) ;
	}
	
	/**
	 * 
	 * @param aUri
	 * @param aAppKey				
	 * @param aAppSecret
	 * @return
	 */
	public static HttpClientProvider ofApp(String aUri , String aAppKey , String aAppSecret)
	{
		return new SimpleHttpClientProvider(URI.create(aUri) , aAppKey, aAppSecret) ;
	}
	
	/**
	 * 单实例，不会重复构造
	 * @param aAppDirName
	 * @return
	 * @throws Exception
	 */
	public static HttpClientProvider ofSysApp(String aAppDirName) throws Exception
	{
		return ZKHttpClientProvider.ofSysApp_0(aAppDirName , null) ;
	}
	
	/**
	 * 
	 * 平台应用的HttpClient提供器
	 * 
	 * @param aAppDirName
	 * @param aContextPath
	 * @return
	 * @throws Exception
	 */
	public static HttpClientProvider ofSysApp(String aAppDirName , String aContextPath) throws Exception
	{
		return ZKHttpClientProvider.ofSysApp_0(aAppDirName , aContextPath) ;
	}
	
	/**
	 * 平台缺省hdfs的HttpClient的提供器
	 * @return
	 * @throws Exception
	 */
	public static HttpClientProvider ofSysDefaultHdfs() throws Exception
	{
		return ZKHttpClientProvider.ofSysDefaultHdfs_0() ;
	}
	
	/**
	 * 平台API网关客户端HttpClient提供器,Https协议的
	 * 
	 * @param aAppKey
	 * @param aAppSecret
	 * @return
	 * @throws Exception
	 */
	public static HttpClientProvider ofGatewaySSL(String aAppKey , String aAppSecret) throws Exception
	{
		HttpClientProvider pvd = ZKHttpClientProvider.ofSysApp_SSL(SysConst.sAppName_SailMSGateway , null) ;
		if(pvd != null)
		{
			pvd.setAppKey(aAppKey) ;
			pvd.setAppSecret(aAppSecret) ;
		}
		return pvd ;
	}
	
	/**
	 * 平台API网关客户端HttpClient提供器
	 * 
	 * @param aAppKey
	 * @param aAppSecret
	 * @return
	 * @throws Exception
	 */
	public static HttpClientProvider ofGateway(String aAppKey , String aAppSecret) throws Exception
	{
		return ofGateway(aAppKey, aAppSecret, null) ;
	}
	
	/**
	 * 平台API网关客户端HttpClient提供器
	 * 
	 * @param aAppKey
	 * @param aAppSecret
	 * @param aContextPath
	 * @return
	 * @throws Exception
	 */
	public static HttpClientProvider ofGateway(String aAppKey , String aAppSecret , String aContextPath) throws Exception
	{
		HttpClientProvider pvd = ofSysApp(SysConst.sAppName_SailMSGateway , aContextPath) ;
		if(pvd != null)
		{
			pvd.setAppKey(aAppKey) ;
			pvd.setAppSecret(aAppSecret) ;
		}
		return pvd ;
	}

}
