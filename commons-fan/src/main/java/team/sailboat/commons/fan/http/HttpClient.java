package team.sailboat.commons.fan.http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;

import team.sailboat.commons.fan.collection.ArrayIterator;
import team.sailboat.commons.fan.collection.IMultiMap;
import team.sailboat.commons.fan.collection.SizeIter;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.infc.EConsumer;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.log.Debug;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.fan.struct.Tuples;
import team.sailboat.commons.fan.text.XString;

public abstract class HttpClient implements IRestClient
{
	
	static Field sHttpConn_MethodField = null ;
	
	protected String mContextPath ;
	
	private String mUrlPrefix ;
	
	protected int mConnectTimeout = 500 ;
	
	protected int mReadTimeout = 60_000 ;
	
	Function<Request, Request> mRequestHandler ;
	
	ICoder mCoder = URLCoder.getDefault() ;
	
	String mHost ;
	
	int mPort ;
	
	boolean mFearure_encodeHeader = true ;
	
	protected HttpClient()
	{}
	
	protected HttpClient(String aHost , int aPort)
	{
		mHost = aHost ;
		mPort = aPort ;
	}
	
	public void setFearure_encodeHeader(boolean aFearure_encodeHeader)
	{
		mFearure_encodeHeader = aFearure_encodeHeader;
	}
	public boolean isFearure_encodeHeader()
	{
		return mFearure_encodeHeader;
	}
	
	public void setCoder(ICoder aCoder)
	{
		if(aCoder == null)
			aCoder = URLCoder.getDefault() ;
		if(mCoder != aCoder)
			mCoder = aCoder;
	}
	
	public void setReadTimeout(int aReadTimeout)
	{
		mReadTimeout = aReadTimeout;
	}
	
	protected void setHost(String aHost)
	{
		if(JCommon.unequals(mHost, aHost))
		{
			mHost = aHost;
			mUrlPrefix = null ;
		}
	}
	
	protected void setPort(int aPort)
	{
		if(mPort != aPort)
		{
			mPort = aPort;
			mUrlPrefix=null ;
		}
	}
	
	public abstract String getProtocol() ;
	
	public String getHost()
	{
		return mHost ;
	}
	
	public int getPort()
	{
		return mPort ;
	}
	
	public boolean isDefaultPort()
	{
		return getPort() <= 0 ? true : ((getPort()==80 && "http".equals(getProtocol())) 
				|| (getPort()== 443 && "https".equals(getProtocol())) ? true : false) ; 
	}
	
	public final String getUrlPrefix()
	{
		if(mUrlPrefix == null)
		{
			mUrlPrefix = XString.splice(getProtocol() , "://" , getHost() , (isDefaultPort()?"":":"+getPort())
					, getContextPath()) ;
		}
		return mUrlPrefix ;
	}
	
	public String getContextPath()
	{
		return mContextPath ;
	}
	
	public void setContextPath(String aContextPath)
	{
		if(XString.isBlank(aContextPath))
			aContextPath = null ;
		else
		{
			aContextPath = FileUtils.toCommonPath(aContextPath) ;
			if("/".equals(aContextPath))
				aContextPath = null ;
		}
		if(JCommon.unequals(aContextPath, mContextPath))
		{
			mContextPath = aContextPath ;
			mUrlPrefix = null ;
		}
	}
	
	public HttpClient connectTimeout(int aTimeInMillSecs)
	{
		mConnectTimeout = aTimeInMillSecs ;
		return this ;
	}
	
	public void send(Request aRequest) throws Exception
	{
		doRequest_0(aRequest) ;
	}
	
	public boolean execute(Request aRequest) throws Exception
	{
		Entry<HttpURLConnection, Integer> result = doRequest_0(aRequest) ;
		return HttpUtils.isOK(result.getValue())  ;
	}
	
	public Object ask(Request aRequest) throws Exception
	{
		return extract(doRequest(aRequest) 
				, HttpConst.sMethod_HEAD.equals(aRequest.getMethod())) ;
	}
	
	public InputStream askAsStream(Request aRequest) throws Exception
	{
		return doRequest(aRequest).getInputStream() ;
	}
	
	public InputStream askAsStream(Request aRequest , Consumer<IMultiMap<String, String>> aRespHeaderConsumer) throws Exception
	{
		HttpEnvelop envelop = doRequest(aRequest) ;
		aRespHeaderConsumer.accept(envelop.getResponseHeadersMap()) ;
		return envelop.getInputStream() ;
	}
	
	public JSONObject askJo(Request aRequest) throws Exception
	{
		aRequest.setHeader(HttpConst.sHeaderName_Accept, HttpConst.sHeaderValue_Accept_JSON) ;
		return (JSONObject)ask(aRequest) ;
	}
	
	public JSONArray askJa(Request aRequest) throws Exception
	{
		aRequest.setHeader(HttpConst.sHeaderName_Accept, HttpConst.sHeaderValue_Accept_JSON) ;
		return (JSONArray)ask(aRequest) ;
	}
	
	public Object ask(Request aRequest , Consumer<IMultiMap<String, String>> aRespHeaderConsumer) throws Exception
	{
		HttpEnvelop envelop = doRequest(aRequest) ;
		aRespHeaderConsumer.accept(envelop.getResponseHeadersMap()) ;
		return extract(envelop , HttpConst.sMethod_HEAD.equals(aRequest.getMethod())) ;
	}
	
	public HttpEnvelop doRequest(Request aRequest) throws Exception
	{
		Entry<HttpURLConnection , Integer> result = doRequest_0(aRequest) ;
		return new HttpEnvelop(result.getKey() , aRequest.getHeaderMap() , result.getValue()) ;
	}
	
	protected Entry<HttpURLConnection, Integer> doRequest_0(Request aRequest) throws Exception
	{
		HttpURLConnection httpConn = buildConnection(aRequest) ;
		try
		{
			return Tuples.of(httpConn , httpConn.getResponseCode())  ;
		}
		catch(ConnectException e)
		{
			WrapException.wrapThrow(e , "连接失败，目标：{}" , toString());
			return null ;				// dead code
		}
	}
	
	public String askForString(Request aRequest) throws Exception
	{
		return extractString(doRequest(aRequest)) ;
	}
	
	public String askForString(Request aRequest , String aDefaultIfEmpty) throws Exception
	{
		return JCommon.defaultIfEmpty(askForString(aRequest) , aDefaultIfEmpty) ;
	}
	
	public String askForString(Request aRequest , Consumer<IMultiMap<String, String>> aRespHeaderConsumer) throws Exception
	{
		HttpEnvelop envelop = doRequest(aRequest) ;
		aRespHeaderConsumer.accept(envelop.getResponseHeadersMap()) ;
		return extractString(envelop) ;
	}
	
	public static Object extract(HttpEnvelop aEnvelop , boolean aHeadReq) throws IOException
	{
		if(aHeadReq)
			return HttpStatus.valueOf(aEnvelop.getReplyCode()) ;
		String contentType = aEnvelop.getContentType() ;
		if(XString.isNotEmpty(contentType) 
				&& (MediaType.APPLICATION_OCTET_STREAM.isCompatibleWith(MediaType.valueOf(contentType))
						|| MediaType.IMAGE_PNG_VALUE.equalsIgnoreCase(contentType)
						|| MediaType.IMAGE_GIF_VALUE.equalsIgnoreCase(contentType)
						|| MediaType.IMAGE_JPEG_VALUE.equalsIgnoreCase(contentType)))
			return aEnvelop.getInputStream() ;
		String content = aEnvelop.getContent() ;
		if(content == null || (content.isEmpty() && contentType == null))
			return null ;
		content = content.trim() ;
		if(XString.isNotEmpty(contentType) && contentType.toLowerCase().contains("json"))
		{
			if(content.startsWith("["))
				return new JSONArray(content) ;
			else if(content.startsWith("{"))
				return new JSONObject(content) ;
			else if(XString.isEmpty(content))
				return null ;
		}
		return content ;
	}
	
	static String extractString(HttpEnvelop aEnvelop) throws IOException
	{
		String contentType = aEnvelop.getContentType() ;
		if(XString.isNotEmpty(contentType) 
				&& MediaType.APPLICATION_OCTET_STREAM.isCompatibleWith(MediaType.valueOf(contentType)))
			throw new IllegalStateException("返回的是流，不能自动解析成字符串!") ;
		return aEnvelop.getContent() ;
	}
	
	protected HttpURLConnection buildConnection(Request aRequest) throws Exception
	{
		if(mRequestHandler != null)
			aRequest = mRequestHandler.apply(aRequest) ;
		
		HttpURLConnection httpConn = createConnection(aRequest) ;
		httpConn.setConnectTimeout(mConnectTimeout) ;
		httpConn.setReadTimeout(mReadTimeout) ;
		if(HttpConst.sMethod_PATCH.equals(aRequest.getMethod()))
		{
			//对于Spring boot微服务来说此种方法无效
//			httpConn.setRequestMethod(HttpConst.sMethod_POST) ;
//			httpConn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
			
			if(sHttpConn_MethodField == null)
			{
				sHttpConn_MethodField = XClassUtil.getField(httpConn.getClass() , "method") ;
				sHttpConn_MethodField.setAccessible(true) ;
			}
			sHttpConn_MethodField.set(httpConn, HttpConst.sMethod_PATCH) ;
		}
		else
			httpConn.setRequestMethod(aRequest.getMethod()) ;
		httpConn.setRequestProperty(HttpConst.sHeaderName_UserAgent , HttpConst.sHeaderValue_UserAgent_x_HttpClient) ;
		injectHeaders(httpConn , aRequest) ;
		injectEntity(httpConn, aRequest) ;
		return httpConn ;
	}
	
	protected HttpURLConnection createConnection(Request aRequest) throws IOException
	{
		URL url = URI.create(getUrlPrefix()+getURLSuffix(aRequest)).toURL() ;
		Debug.cout("URL地址：%1$s-%2$s" , aRequest.getMethod() , url.toString());
		return (HttpURLConnection)url.openConnection() ;
	}
	
	private void setContentLength(HttpURLConnection aConn , String aLen)
	{
		aConn.setRequestProperty(mCoder.encodeHeader(HttpConst.sHeaderName_ContentLength) 
				, mCoder.encodeHeaderValue(aLen));
	}
	
	protected void injectEntity(HttpURLConnection aConn , Request aRequest) throws IOException
	{
		try
		{
			EConsumer<DataOutputStream , IOException> entity = aRequest.getEntity() ;
			if(entity == null)
			{
				if(XString.isNotEmpty(aRequest.getHeaderValue(HttpConst.sHeaderName_ContentType)))
				{
					aConn.setDoOutput(true) ;
					StreamAssist.close(aConn.getOutputStream()) ;
				}
				else
				{
					aConn.setDoOutput(false) ;
				}
			}
			else
			{
				if(XString.isEmpty(aConn.getRequestProperty(HttpConst.sHeaderName_ContentLength)))
				{
					String cl = aRequest.getHeaderValue(HttpConst.sHeaderName_ContentLength) ;
					if(XString.isNotEmpty(cl))
					{
						setContentLength(aConn, cl);
					}
				}
				aConn.setDoOutput(true) ;
				if(aRequest.isMultiPart())
				{
					aConn.setDoInput(true) ;
					aConn.setUseCaches(false) ;
				}
				DataOutputStream douts = mCoder.wrap(aConn.getOutputStream()) ;
				entity.accept(douts) ;
			}
		}
		catch(SocketTimeoutException|ConnectException e)
		{
			WrapException.wrapThrow(e , "连接服务[{}]出现异常！" , aConn.getURL().toString()) ;
		}
	}
	
	String encodeHeaderName(String aName)
	{
		return mFearure_encodeHeader?mCoder.encodeHeader(aName)
				:aName ;
	}
	
	String encodeHeaderValue(String aText)
	{
		return mFearure_encodeHeader?mCoder.encodeHeaderValue(aText)
				:aText ;
	}
	
	protected void injectHeaders(HttpURLConnection aConn , Request aRequest)
	{
		Collection<String> headerNames = aRequest.getHeaderNames() ;
		if(XC.isEmpty(headerNames))
			return ;
		for(String headerName : headerNames)
		{
			for(String val : aRequest.getHeaderValues(headerName))
			{
				aConn.addRequestProperty(encodeHeaderName(headerName)
						, encodeHeaderValue(val)) ;
			}
		}
		if(!URLCoder.sName.equals(mCoder.getName()))
		{
			aConn.setRequestProperty(encodeHeaderName(ICoder.sHeaderName_CipherAlgo) , mCoder.getName()) ;
		}
		
//		if(XString.isEmpty(aRequest.getHeaderValue(HttpConst.sHeaderName_ContentLength))
//				&& aRequest.isMultiPart())
//		{
//			aConn.addRequestProperty("Transfer-Encoding", "chunked");
//		}
	}
	
	protected String getURLSuffix(Request aRequest)
	{
		StringBuilder strBld = new StringBuilder() ;
		String path = aRequest.getPath() ;
		if(XString.isNotEmpty(path) && !"/".equals(path))
		{
			if(!path.startsWith("/"))
				strBld.append('/') ;
			strBld.append(mCoder.splitEncodePath(path)) ;
		}
		//参数
		Collection<String> keys = aRequest.getUrlParamKeys() ;
		if(XC.isNotEmpty(keys))
		{
			boolean first = true ;
			for(String key : keys)
			{
				SizeIter<String> sit = aRequest.getUrlParamValues(key) ;
				if(sit == null || sit.isEmpty())
					sit = new ArrayIterator<String>(new String[] {null}) ;
				for(String val : sit)
				{
					if(first)
					{
						strBld.append("?") ;
						first = false ;
					}
					else
						strBld.append("&") ;
					strBld.append(mCoder.encodeParam(key)) ;
					if(val != null)
					{
						strBld.append("=")
								.append(mCoder.encodeParamValue(val)) ;
					}
				}
			}
		}
		return strBld.toString() ;
	}
	
	@Override
	public String toString()
	{
		return getProtocol() + "://"+getHost()+":"+getPort() + JCommon.defaultIfEmpty(getContextPath() , "") ;
	}
	
	/**
	 * ofUrl(aUrl , false)
	 * @param aUrl
	 * @return
	 */
	public static HttpClient ofUrl(URL aUrl)
	{
		return ofUrl(aUrl, false) ;
	}
	
	public static HttpClient ofUrl(URL aUrl , boolean aPathAsContextPath)
	{
		HttpClient client = null ;
		if("http".equals(aUrl.getProtocol()))
		{
			client = HttpClient.of(aUrl.getHost() , aUrl.getPort()) ;
		}
		else if("https".equals(aUrl.getProtocol()))
			client = HttpClient.ofSSL(aUrl.getHost() , aUrl.getPort()) ;
		else
			throw new IllegalArgumentException("不支持的协议："+aUrl.getProtocol()) ;
		if(aPathAsContextPath && XString.isNotEmpty(aUrl.getPath()))
			client.setContextPath(aUrl.getPath()) ;
		return client ;
	}
	
	public static HttpClient ofUrl(URL aUrl , String aAppKey , String aAppSecret , ISigner aSigner)
	{
		return ofUrl(aUrl, aAppKey, aAppSecret, aSigner, false) ;
	}
	
	public static HttpClient ofUrl(String aUrl , String aAppKey , String aAppSecret , ISigner aSigner
			, boolean aPathAsContextPath) throws MalformedURLException
	{
		return ofUrl(new URL(aUrl) , aAppKey, aAppSecret, aSigner, aPathAsContextPath) ;
	}
	
	public static HttpClient ofUrl(URL aUrl , String aAppKey , String aAppSecret , ISigner aSigner
			, boolean aPathAsContextPath)
	{
		HttpClient client = null ;
		if("http".equals(aUrl.getProtocol()))
		{
			client = HttpClient.of(aUrl.getHost() , aUrl.getPort() , aAppKey , aAppSecret , aSigner) ;
		}
		else if("https".equals(aUrl.getProtocol()))
			client = HttpClient.ofSSL(aUrl.getHost() , aUrl.getPort() , aAppKey , aAppSecret , aSigner) ;
		else
			throw new IllegalArgumentException("不支持的协议："+aUrl.getProtocol()) ;
		if(aPathAsContextPath)
			client.setContextPath(aUrl.getPath()) ;
		return client ;
	}
	
	public static HttpClient ofUrl(String aUrl) throws MalformedURLException
	{
		return ofUrl(new URL(aUrl)) ;
	}
	
	public static HttpClient ofUrl(String aUrl , boolean aPathAsContextPath) throws MalformedURLException
	{
		return ofUrl(new URL(aUrl) , aPathAsContextPath) ;
	}
	
	public static HttpClient of(String aHost)
	{
		return new DefaultHttpClient(aHost, 80) ;
	}
	
	public static HttpClient ofSSL(String aHost)
	{
		return new DefaultHttpsClient(aHost, 443) ;
	}
	
	public static HttpClient ofSSL(String aHost , int aPort)
	{
		return new DefaultHttpsClient(aHost, aPort) ;
	}
	
	public static HttpClient ofSSL(String aHost , int aPort , String aContextPath)
	{
		HttpClient client = ofSSL(aHost, aPort) ;
		client.setContextPath(aContextPath) ;
		return client ;
	}
	
	public static HttpClient of(String aHost , int aPort)
	{
		return new DefaultHttpClient(aHost, aPort) ;
	}
	
	/**
	 * 格式：host1:port1,host2:port2
	 * @param aServiceAddrs
	 * @return		Http客户端，非Https
	 */
	public static HttpClient ofMulti(String aServiceAddrs)
	{
		Assert.notEmpty(aServiceAddrs , "服务地址不能为空！");
		List<ServiceAddress> addrList = ServiceAddress.parse(aServiceAddrs, 80) ;
		Assert.notEmpty(addrList , "无法从 %s 中解析出服务地址！" , aServiceAddrs) ;
		return addrList.size() == 1 ? new MultiServiceHttpClient(addrList) 
				: of(addrList.get(0).mHost, addrList.get(0).mPort) ;
	}
	
	/**
	 * 
	 * @param aIps
	 * @param aPort
	 * @return		Http客户端，非Https
	 */
	public static HttpClient ofMulti(String[] aIps , int aPort)
	{
		Assert.notEmpty(aIps , "服务地址不能为空！");
		if(aIps.length == 1)
			return of(aIps[0] , aPort) ;
		else
			return new MultiServiceHttpClient(XC.extractNotNull(aIps, (ip)->new ServiceAddress(ip, aPort))) ;
	}
	
	/**
	 * 
	 * @param aUrls
	 * @param aPathAsContextPath			URL中的路径作为ContextPath使用
	 * @return		Http或Https客户端
	 */
	public static HttpClient ofMulti(URL[] aUrls , boolean aPathAsContextPath)
	{
		Assert.notEmpty(aUrls , "没有指定所需连接的URL！") ;
		if(aUrls.length == 1)
			return ofUrl(aUrls[0], aPathAsContextPath) ;
		else
		{
			return new MultiUrlHttpClient(aUrls, aPathAsContextPath) ;
		}
	}
	
	public static HttpClient of(String aHost , int aPort , String aContextPath)
	{
		HttpClient client = of(aHost, aPort) ;
		client.setContextPath(aContextPath) ;
		return client ;
	}
	
	public static HttpClient of(String aProtocol , String aHost , int aPort , String aContextPath)
	{
		if("http".equalsIgnoreCase(aProtocol))
			return of(aHost, aPort, aContextPath) ;
		else if("https".equalsIgnoreCase(aProtocol))
			return ofSSL(aHost, aPort, aContextPath) ;
		else
			throw new IllegalArgumentException("未支持的协议："+aProtocol) ;
	}
	
	public static HttpClient of(String aHost , String aAppKey , String aAppSecret , ISigner aSigner)
	{
		return new SignHttpClient(aHost, 80 , aAppKey, aAppSecret , aSigner) ; 
	}
	
	public static HttpClient ofSSL(String aHost , int aPort , String aAppKey , String aAppSecret , ISigner aSigner)
	{
		return new SignHttpsClient(aHost, aPort , aAppKey, aAppSecret , aSigner , null) ; 
	}
	
	public static HttpClient ofSSL(String aHost , int aPort , String aAppKey , String aAppSecret , ISigner aSigner , String aTLSv)
	{
		return new SignHttpsClient(aHost, aPort , aAppKey, aAppSecret , aSigner , aTLSv) ; 
	}
	
	public static HttpClient of(String aHost , int aPort , String aAppKey , String aAppSecret
			, ISigner aSigner)
	{
		return new SignHttpClient(aHost, aPort , aAppKey, aAppSecret , aSigner) ; 
	}
	
	public void setRequestHandler(Function<Request, Request> aRequestHandler)
	{
		mRequestHandler = aRequestHandler;
	}
}
