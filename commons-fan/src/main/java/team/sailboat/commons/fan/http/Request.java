package team.sailboat.commons.fan.http;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.HashMultiMap;
import team.sailboat.commons.fan.collection.IMultiMap;
import team.sailboat.commons.fan.collection.SizeIter;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.infc.EConsumer;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.serial.FlexibleBInputStream;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.fan.text.XString;

public class Request implements Cloneable
{
	URLCoder mURLCoder = URLCoder.getDefault() ;
	
	String mMethod ;
	String mPath ;
	
	IMultiMap<String , String> mUrlParamMap = new HashMultiMap<>() ;
	
	IMultiMap<String , String> mHeaderMap = new HashMultiMap<>() ;
	
	IMultiMap<String , String> mFormParamMap ;
	
	Object mEntity ;
	
	private Request(String aMethod)
	{
		mMethod = aMethod ;
		accept_any() ;
	}
	
	public String getMethod()
	{
		return mMethod;
	}
	
	public Request path(String aPath)
	{
		if(aPath != null)
		{
			aPath = XString.trim(aPath) ;
			if(!aPath.isEmpty() && aPath.charAt(0) != '/')
			{
				aPath = "/"+aPath ;
			}
		}
		mPath = aPath ;
		return this ;
	}
	
	public Request path(String aPath , String... aPathParamVals)
	{
		mPath = XString.msgFmt(aPath , (Object[])aPathParamVals) ;
		return this ;
	}
	
	public Request path(String aPath , Map<String , Object> aParamValues)
	{
		mPath = XString.applyPlaceHolder(aPath, aParamValues) ;
		return this ;
	}
	
	public Request filter(Consumer<Request> aConsumer)
	{
		aConsumer.accept(this) ;
		return this ;
	}
	
	public String getPath()
	{
		return mPath;
	}
	
	/**
	 * 
	 * @param aKey
	 * @param aValue			如果aValue为Null，将忽略
	 * @return
	 */
	public Request queryParam(String aKey , String aValue)
	{
		if(aValue != null)
			mUrlParamMap.put(aKey, aValue) ;
		return this ;
	}
	
	/**
	 * 用queryParam代替
	 **/
	@Deprecated
	public Request urlParam(String aKey , String aValue)
	{
		return queryParam(aKey, aValue) ;
	}
	
	public Request queryParam(String aKey , String aValue
			, boolean aIgnoreEmpty)
	{
		if(aIgnoreEmpty && XString.isEmpty(aValue))
			return this ;
		mUrlParamMap.put(aKey, aValue) ;
		return this ;
	}
	
	public Request queryParam(String aKey , Boolean aValue
			, boolean aIgnoreNull)
	{
		if(aIgnoreNull && aValue == null)
			return this ;
		mUrlParamMap.put(aKey, JCommon.toString(aValue, "")) ;
		return this ;
	}
	
	/**
	 * 用queryParam代替
	 **/
	@Deprecated
	public Request urlParam(String aKey , String aValue
			, boolean aIgnoreEmpty)
	{
		return queryParam(aKey, aValue, aIgnoreEmpty) ;
	}
	
	public Request queryParam(String aKey ,int aValue)
	{
		return queryParam(aKey, Integer.toString(aValue)) ;
	}
	
	public Request queryParam(String aKey , boolean aValue)
	{
		return queryParam(aKey , Boolean.toString(aValue)) ;
	}
	
	/**
	 * 用queryParam代替
	 **/
	@Deprecated
	public Request urlParam(String aKey ,int aValue)
	{
		return queryParam(aKey, Integer.toString(aValue)) ;
	}
	
	public Request queryParam(String aKey ,long aValue)
	{
		return queryParam(aKey, Long.toString(aValue)) ;
	}
	
	/**
	 * 用queryParam代替
	 **/
	@Deprecated
	public Request urlParam(String aKey ,long aValue)
	{
		return queryParam(aKey, Long.toString(aValue)) ;
	}
	
	public Request queryParam(String aKey ,double aValue)
	{
		return queryParam(aKey, Double.toString(aValue)) ;
	}
	
	public Request queryParam(String aKey , float aValue)
	{
		return queryParam(aKey, Float.toString(aValue)) ;
	}
	
	/**
	 * 用queryParam代替
	 **/
	@Deprecated
	public Request urlParam(String aKey ,double aValue)
	{
		return queryParam(aKey, Double.toString(aValue)) ;
	}
	
	public Request queryParamIfAbsent(String aKey , String aValue)
	{
		if(!mUrlParamMap.containsKey(aKey))
			mUrlParamMap.put(aKey, aValue) ;
		return this ;
	}
	
	/**
	 * 用queryParam代替
	 **/
	@Deprecated
	public Request urlParamIfAbsent(String aKey , String aValue)
	{
		return queryParamIfAbsent(aKey, aValue) ;
	}
	
	public Collection<String> getUrlParamKeys()
	{
		return mUrlParamMap.keySet() ;
	}
	
	public SizeIter<String> getUrlParamValues(String aKey)
	{
		SizeIter<String> it = mUrlParamMap.get(aKey) ;
		return it == null?SizeIter.emptyIter():it ;
	}
	
	public String getUrlParamValue(String aKey)
	{
		return mUrlParamMap.getFirst(aKey) ;
	}
	
	public Collection<String> getHeaderNames()
	{
		return mHeaderMap.keySet() ;
	}
	
	public SizeIter<String> getHeaderValues(String aKey)
	{
		return mHeaderMap.get(aKey) ;
	}
	
	public String getHeaderValue(String aKey)
	{
		return mHeaderMap.getFirst(aKey) ;
	}
	
	public Request accept_APPLICATION_JSON_UTF8()
	{
		return setHeader(HttpConst.sHeaderName_Accept , MediaType.APPLICATION_JSON_UTF8_VALUE) ;
	}
	
	public Request accept_APPLICATION_JSON()
	{
		return setHeader(HttpConst.sHeaderName_Accept , MediaType.APPLICATION_JSON_VALUE) ;
	}
	
	public Request accept_any()
	{
		return setHeader(HttpConst.sHeaderName_Accept , "*/*") ;
	}
	
	public Request accept_TEXT_PLAIN()
	{
		return setHeader(HttpConst.sHeaderName_Accept , MediaType.TEXT_PLAIN_VALUE) ;
	}
	
	public String getHeaderValue_Accept(String aDefaultVal)
	{
		return JCommon.defaultIfNull(getHeaderValue(HttpConst.sHeaderName_Accept) , aDefaultVal) ;
	}
	
	public String getHeaderValue_ContentMD5(String aDefaultVal)
	{
		return JCommon.defaultIfNull(getHeaderValue(HttpConst.sHeaderName_ContentMD5) , aDefaultVal) ;
	}
	
	public String getHeaderValue_ContentType(String aDefaultVal)
	{
		return JCommon.defaultIfNull(getHeaderValue(HttpConst.sHeaderName_ContentType) , aDefaultVal) ;
	}
	
	public Request formParam(String aKey , int aValue)
	{
		formParam(aKey, Integer.toString(aValue)) ;
		return this ;
	}
	
	public Request formParam(String aKey , String aValue)
	{
		Assert.isTrue(HttpConst.sMethod_POST.equals(mMethod)
				, "此方法只能应用于POST方式提交的HTTP请求，当前请求方法是%s" , mMethod);
		mEntity = null ;
		setHeader(HttpConst.sHeaderName_ContentType, MediaType.APPLICATION_FORM_URLENCODED_VALUE) ;
		if(mFormParamMap == null)
			mFormParamMap = new HashMultiMap<>() ;
		mFormParamMap.put(aKey, aValue) ;
		return this ;
	}
	
	/**
	 * 一旦设置，会清空FormParam
	 * @param aText
	 * @return
	 */
	public Request setJsonEntity(String aText)
	{
		return setEntity(aText , MediaType.APPLICATION_JSON_UTF8_VALUE) ;
	}
	
	public Request setJsonEntity(JSONObject aJobj)
	{
		return setJsonEntity(aJobj.toString()) ;
	}
	
	public Request setJsonEntity(JSONArray aJobj)
	{
		return setJsonEntity(aJobj.toString()) ;
	}
	
	public Request setJsonEntity(ToJSONObject aToJobj)
	{
		return setJsonEntity(aToJobj.toJSONObject().toString()) ;
	}
	/**
	 * 一旦设置，会清空FormParam
	 * @param aText
	 * @return
	 */
	public Request setTextEntity(String aText)
	{
		return setEntity(aText, MediaType.TEXT_PLAIN_VALUE) ;
	}
	
	protected Request setEntity(String aText , String aMediaType)
	{
		mFormParamMap = null ;
		setHeader(HttpConst.sHeaderName_ContentType , aMediaType) ;
		byte[] data = aText.getBytes(AppContext.sUTF8) ;
		mEntity = new FlexibleBInputStream(data) ;
		setHeader(HttpConst.sHeaderName_ContentLength , data.length) ;
		return this ;
	}
	
	public Request setStreamEntity(InputStream aIns , long aSize)
	{
		mFormParamMap = null ;
		setHeader(HttpConst.sHeaderName_ContentType , MediaType.APPLICATION_OCTET_STREAM_VALUE) ;
		if(aSize>=0)
			setHeader(HttpConst.sHeaderName_ContentLength , aSize) ;
		else
			removeHeader(HttpConst.sHeaderName_ContentLength) ;
		mEntity = aIns ;
		return this ;
	}
	
	public Request setStreamEntity(byte[] aData)
	{
		mFormParamMap = null ;
		setHeader(HttpConst.sHeaderName_ContentType , MediaType.APPLICATION_OCTET_STREAM_VALUE) ;
		setHeader(HttpConst.sHeaderName_ContentLength , aData.length) ;
		mEntity = aData ;
		return this ;
	}
	
	public Request setMultiPartEntity(EntityPart... aParts)
	{
		mFormParamMap = null ;
		if(XC.isNotEmpty(aParts))
		{
			setHeader(HttpConst.sHeaderName_ContentType , MediaType.MULTIPART_FORM_DATA_VALUE+";boundary="+MultiPartStream.sBoundary) ;
			removeHeader(HttpConst.sHeaderName_ContentLength) ;
			setHeader(HttpConst.sHeaderName_Connection , "Keep-Alive") ;
			mEntity = new MultiPartStream(aParts) ;
		}
		else
			mEntity = null ;
		return this ;
	}
	
	public boolean isMultiPart()
	{
		return mEntity != null && mEntity instanceof MultiPartStream ;
	}
	
	public Object getRawEntity()
	{
		return mEntity ;
	}
	
	@SuppressWarnings({ "unchecked", "resource" })
	public EConsumer<DataOutputStream , IOException> getEntity()
	{
		Object entity = mEntity ; 
		if(entity == null)
		{
			if(mFormParamMap != null && !mFormParamMap.isEmpty())
			{
				String query = mURLCoder.formatEncodeParams(mFormParamMap) ;
				byte[] data = query.getBytes(AppContext.sUTF8) ;
				entity = new FlexibleBInputStream(data) ;
				setHeader(HttpConst.sHeaderName_ContentLength , data.length) ;
			}
			else
			{
				setHeader(HttpConst.sHeaderName_ContentLength , 0) ;
				return null ;
			}
		} else if(entity instanceof FlexibleBInputStream)
			entity = ((FlexibleBInputStream)mEntity).clone() ;
		else if(entity instanceof MultiPartStream)
			return (EConsumer<DataOutputStream, IOException>) entity ;
		else if(entity instanceof byte[])
		{
			entity = new ByteArrayInputStream((byte[])entity) ;
		}
		Object entity_0 = entity ;
		return (outs)->StreamAssist.transfer_cc((InputStream)entity_0 , outs);
	}
	
	public Request setHeader(String aKey , String aValue)
	{
		mHeaderMap.set(aKey, aValue) ;
		return this ;
	}
	
	public Request setHeader(String aKey , long aValue)
	{
		return setHeader(aKey, Long.toString(aValue)) ;
	}
	
	public Request removeHeader(String aKey)
	{
		mHeaderMap.removeAll(aKey) ;
		return this ;
	}
	
	public Request header(String aKey , String aValue)
	{
		mHeaderMap.put(aKey, aValue) ;
		return this ;
	}
	
	public Request header(String aKey , String aValue , boolean aIgnoreEmpty)
	{
		if(!aIgnoreEmpty || XString.isNotEmpty(aValue))
			mHeaderMap.put(aKey, aValue) ;
		return this ;
	}
	
	public Request urlCoder(URLCoder aURLCoder)
	{
		if(aURLCoder != null && mURLCoder != aURLCoder)
			mURLCoder = aURLCoder ;
		return this ;
	}
	
	public IMultiMap<String, String> getHeaderMap()
	{
		return mHeaderMap;
	}
	
	public Collection<String> getFormParamKeys()
	{
		return mFormParamMap.keySet() ;
	}
	
	public String getFormParamValue(String aKey)
	{
		return mFormParamMap.getFirst(aKey) ;
	}
	
	public IMultiMap<String, String> getFormParamMap()
	{
		return mFormParamMap;
	}
	
	public IMultiMap<String, String> getUrlParamMap()
	{
		return mUrlParamMap;
	}
	
	/**
	 * 
	 */
	public Request clone()
	{
		Request clone = new Request(mMethod) ;
		clone.mURLCoder = mURLCoder ;
		clone.mPath = mPath ;
		clone.mUrlParamMap.putAll(mUrlParamMap) ;
		clone.mHeaderMap.putAll(mHeaderMap) ;
		if(mFormParamMap != null)
			clone.mFormParamMap = new HashMultiMap<>(mFormParamMap) ;
		
		if(mEntity != null && mEntity instanceof FlexibleBInputStream)
		{
			clone.mEntity = ((FlexibleBInputStream)mEntity).clone() ;
		}
		return clone ;
	}
	
	public static Request GET()
	{
		return new Request(HttpConst.sMethod_GET) ;
	}
	
	public static Request GET(URL aUrl)
	{
		return _build(new Request(HttpConst.sMethod_GET) , aUrl) ;
	}
	
	public static Request POST()
	{
		// yyl @ 2022-11-26
		// 因为有的安全设备会对POST请求进行检查，要求其必需有Content-Length。
		// 如果缺省将Content-Length设置为0，DoOutput为true，不设置Content-Type的话，
		// 基础库会自动将它设置为 form请求，这样就会使得签名字符串客户端构造出来的和服务端
		// 构造出来的不一致，致使身份验证失败。
		// HttpURLConntection不能自己设置Conent-Length，当有DoOuput时，需要自己getOutputStream，
		// 根据里面的数据量HttpUrlConnection自己设置（可能不包括流式Content-Type，未验证）
		// 设置一下缺省的Content-Type。
		// 这里设置为APPLICATION_JSON_VALUE，避免Body接收Bean，但没有设置bean，如果是其它MediaType会出错
		return new Request(HttpConst.sMethod_POST)
				.header(HttpConst.sHeaderName_ContentType , MediaType.APPLICATION_JSON_VALUE) ;
	}
	
	public static Request POST(URL aUrl)
	{
		return _build(new Request(HttpConst.sMethod_POST) , aUrl) ;
	}
	
	public static Request PUT()
	{
		return new Request(HttpConst.sMethod_PUT) ;
	}
	
	public static Request PUT(URL aUrl)
	{
		return _build(new Request(HttpConst.sMethod_PUT) , aUrl) ;
	}
	
	public static Request method(String aMethod)
	{
		String method = aMethod.toUpperCase() ;
		Assert.isTrue(HttpConst.isValidHttpMethod(method)) ;
		return new Request(method) ;
	}
	
	static Request _build(Request aRequest , URL aUrl)
	{
		try
		{
			aRequest.path(URLDecoder.decode(aUrl.getPath() , "UTF-8")) ;
		}
		catch (UnsupportedEncodingException e)
		{
			WrapException.wrapThrow(e) ;
		}
		String queryStr = aUrl.getQuery() ;
		if(XString.isNotEmpty(queryStr))
		{
			IMultiMap<String, String> map = IURLBuilder.parseQueryStr(aUrl.getQuery()) ;
			for(Entry<String , String> entry : map.entrySet())
				aRequest.queryParam(entry.getKey(), entry.getValue()) ;
		}
		return aRequest ;
	}
	
	public static Request HEAD()
	{
		return new Request(HttpConst.sMethod_HEAD) ;
	}
	
	public static Request DELETE()
	{
		return new Request(HttpConst.sMethod_DELETE) ;
	}
	
	public static Request DELETE(URL aUrl)
	{
		return _build(new Request(HttpConst.sMethod_DELETE) , aUrl) ;
	}
	
	public static Request PATCH()
	{
		return new Request(HttpConst.sMethod_PATCH) ;
	}
	
	public static Request PATCH(URL aUrl)
	{
		return _build(new Request(HttpConst.sMethod_PATCH) , aUrl) ;
	}
}
