package team.sailboat.commons.fan.http;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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

/**
 * 
 * Http请求
 *
 * @author yyl
 * @since 2024年11月27日
 */
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
	
	/**
     * 获取请求的HTTP方法（如GET, POST等）。
     * @return 请求方法
     */
	public String getMethod()
	{
		return mMethod;
	}
	
	/**
     * 设置请求的路径，如果路径不以'/'开头，则自动添加。
     * @param aPath 请求路径
     * @return 当前Request对象（支持链式调用）
     */
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
	
	/**
     * 使用参数格式化请求路径。
     * @param aPath 包含占位符的路径模板
     * @param aPathParamVals 用于替换路径模板中占位符的参数值
     * @return 当前Request对象（支持链式调用）
     */
	public Request path(String aPath , String... aPathParamVals)
	{
		mPath = XString.msgFmt(aPath , (Object[])aPathParamVals) ;
		return this ;
	}
	
	/**
     * 使用参数映射格式化请求路径。
     * @param aPath 包含占位符的路径模板
     * @param aParamValues 用于替换路径模板中占位符的参数映射
     * @return 当前Request对象（支持链式调用）
     */
	public Request path(String aPath , Map<String , Object> aParamValues)
	{
		mPath = XString.applyPlaceHolder(aPath, aParamValues) ;
		return this ;
	}
	
	/**
     * 允许对Request对象进行自定义处理。
     * @param aConsumer 自定义处理逻辑
     * @return 当前Request对象（支持链式调用）
     */
	public Request filter(Consumer<Request> aConsumer)
	{
		aConsumer.accept(this) ;
		return this ;
	}
	
	/**
     * 获取请求的路径。
     * @return 请求路径
     */
	public String getPath()
	{
		return mPath;
	}
	
	/**
     * 添加查询参数（非空值）。
     * @param aKey 查询参数的键
     * @param aValue 查询参数的值。如果aValue为Null，将忽略
     * @return 当前Request对象（支持链式调用）
     */
	public Request queryParam(String aKey , String aValue)
	{
		if(aValue != null)
			mUrlParamMap.put(aKey, aValue) ;
		return this ;
	}
	
	/**
     * 添加查询参数，如果值为空且指定忽略空值，则不进行添加。
     * @param aKey 查询参数的键
     * @param aValue 查询参数的值
     * @param aIgnoreEmpty 是否忽略空值
     * @return 当前Request对象（支持链式调用）
     */
	public Request queryParam(String aKey , String aValue
			, boolean aIgnoreEmpty)
	{
		if(aIgnoreEmpty && XString.isEmpty(aValue))
			return this ;
		mUrlParamMap.put(aKey, aValue) ;
		return this ;
	}
	
	/**
	 * 
     * 添加布尔查询参数，如果值为null且指定忽略null值，则不进行添加。
     * 
     * @param aKey 查询参数的键
     * @param aValue 查询参数的值
     * @param aIgnoreNull 是否忽略null值
     * @return 当前Request对象（支持链式调用）
     */
	public Request queryParam(String aKey , Boolean aValue
			, boolean aIgnoreNull)
	{
		if(aIgnoreNull && aValue == null)
			return this ;
		mUrlParamMap.put(aKey, JCommon.toString(aValue, "")) ;
		return this ;
	}
	
	public Request queryParam(String aKey ,int aValue)
	{
		return queryParam(aKey, Integer.toString(aValue)) ;
	}
	
	public Request queryParam(String aKey , boolean aValue)
	{
		return queryParam(aKey , Boolean.toString(aValue)) ;
	}
	
	public Request queryParam(String aKey ,long aValue)
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
     * 仅在查询参数不存在时添加。
     * @param aKey 查询参数的键
     * @param aValue 查询参数的值
     * @return 当前Request对象（支持链式调用）
     */
	public Request queryParamIfAbsent(String aKey , String aValue)
	{
		if(!mUrlParamMap.containsKey(aKey))
			mUrlParamMap.put(aKey, aValue) ;
		return this ;
	}
	
	public Collection<String> getQueryParamKeys()
	{
		return mUrlParamMap.keySet() ;
	}
	
	/**
	 * 
	 * 取得参数的键。<br />
	 * 包括url中的查询参数和form表单提交中的参数
	 * 
	 * @return
	 */
	public Collection<String> getParameterKeys()
	{
		Set<String> paramNames =  XC.hashSet(mUrlParamMap.keySet()) ;
		if(mFormParamMap != null)
			paramNames.addAll(mFormParamMap.keySet()) ;
		return paramNames ;
	}
	
	/**
	 * 
	 * 取得参数的值。<br />
	 * 包括url中的查询参数和form表单提交中的参数
	 * 
	 * @param aKey
	 * @return
	 */
	public SizeIter<String> getParameterValues(String aKey)
	{
		SizeIter<String> it = mUrlParamMap.get(aKey) ;
		return it == null?(JCommon.defaultIfNull(mFormParamMap.get(aKey), SizeIter::emptyIter)) : it ;
	}
	
	public SizeIter<String> getQueryParamValues(String aKey)
	{
		SizeIter<String> it = mUrlParamMap.get(aKey) ;
		return it == null?SizeIter.emptyIter():it ;
	}
	
	public String getQueryParamValue(String aKey)
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
	
	/**
     * 添加表单参数，将int值转换为字符串后添加
     * @param aKey 参数名
     * @param aValue 参数值
     * @return 当前Request对象，用于链式调用
     */
	public Request formParam(String aKey , int aValue)
	{
		formParam(aKey, Integer.toString(aValue)) ;
		return this ;
	}
	
	/**
     * 添加表单参数
     * @param aKey 参数名
     * @param aValue 参数值
     * @return 当前Request对象，用于链式调用
     * @throws AssertionError 如果当前请求方法不是POST
     */
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
     * 设置JSON字符串作为请求实体		<br />
     * 一旦设置，会清空FormParam
     * @param aText JSON字符串
     * @return 当前Request对象，用于链式调用
     */
	public Request setJsonEntity(String aText)
	{
		return setEntity(aText , MediaType.APPLICATION_JSON_UTF8_VALUE) ;
	}
	
	/**
     * 设置JSONObject作为请求实体
     * @param aJobj JSONObject对象
     * @return 当前Request对象，用于链式调用
     */
	public Request setJsonEntity(JSONObject aJobj)
	{
		return setJsonEntity(aJobj.toString()) ;
	}
	
	/**
     * 设置JSONArray作为请求实体
     * @param aJobj JSONArray对象
     * @return 当前Request对象，用于链式调用
     */
	public Request setJsonEntity(JSONArray aJobj)
	{
		return setJsonEntity(aJobj.toString()) ;
	}
	
	/**
     * 设置可转换为JSONObject的对象作为请求实体
     * @param aToJobj 可转换为JSONObject的对象
     * @return 当前Request对象，用于链式调用
     */
	public Request setJsonEntity(ToJSONObject aToJobj)
	{
		return setJsonEntity(aToJobj.toJSONObject().toString()) ;
	}
	
	/**
     * 设置文本实体，会清空FormParam
     * @param aText 文本内容
     * @return 当前Request对象，用于链式调用
     */
	public Request setTextEntity(String aText)
	{
		return setEntity(aText, MediaType.TEXT_PLAIN_VALUE) ;
	}
	
	/**
     * 设置请求实体
     * @param aText 实体内容
     * @param aMediaType 媒体类型
     * @return 当前Request对象，用于链式调用
     */
	protected Request setEntity(String aText , String aMediaType)
	{
		mFormParamMap = null ;
		setHeader(HttpConst.sHeaderName_ContentType , aMediaType) ;
		byte[] data = aText.getBytes(AppContext.sUTF8) ;
		mEntity = new FlexibleBInputStream(data) ;
		setHeader(HttpConst.sHeaderName_ContentLength , data.length) ;
		return this ;
	}
	
	/**
     * 设置输入流作为请求实体
     * @param aIns 输入流
     * @param aSize 输入流大小
     * @return 当前Request对象，用于链式调用
     */
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
	
	/**
     * 设置字节数组作为请求实体
     * @param aData 字节数组
     * @return 当前Request对象，用于链式调用
     */
	public Request setStreamEntity(byte[] aData)
	{
		mFormParamMap = null ;
		setHeader(HttpConst.sHeaderName_ContentType , MediaType.APPLICATION_OCTET_STREAM_VALUE) ;
		setHeader(HttpConst.sHeaderName_ContentLength , aData.length) ;
		mEntity = aData ;
		return this ;
	}
	
	/**
     * 设置多部分表单实体
     * @param aParts 多部分表单部分
     * @return 当前Request对象，用于链式调用
     */
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
	
	/**
     * 判断请求是否为多部分表单
     * @return 是否为多部分表单
     */
	public boolean isMultiPart()
	{
		return mEntity != null && mEntity instanceof MultiPartStream ;
	}
	
	/**
     * 获取原始实体
     * @return 原始实体
     */
	public Object getRawEntity()
	{
		return mEntity ;
	}
	
	/**
     * 获取实体输出流处理函数
     * @return 实体输出流处理函数
     */
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
	
	/**
     * 设置请求头
     * @param aKey 请求头名
     * @param aValue 请求头值
     * @return 当前Request对象，用于链式调用
     */
	public Request setHeader(String aKey , String aValue)
	{
		mHeaderMap.set(aKey, aValue) ;
		return this ;
	}
	
	/**
     * 设置请求头（值为long类型）
     * @param aKey 请求头名
     * @param aValue 请求头值
     * @return 当前Request对象，用于链式调用
     */
	public Request setHeader(String aKey , long aValue)
	{
		return setHeader(aKey, Long.toString(aValue)) ;
	}
	
	/**
     * 移除请求头
     * @param aKey 请求头名
     * @return 当前Request对象，用于链式调用
     */
	public Request removeHeader(String aKey)
	{
		mHeaderMap.removeAll(aKey) ;
		return this ;
	}
	
	/**
     * 添加请求头（如果已存在则覆盖）
     * @param aKey 请求头名
     * @param aValue 请求头值
     * @return 当前Request对象，用于链式调用
     */
	public Request header(String aKey , String aValue)
	{
		mHeaderMap.put(aKey, aValue) ;
		return this ;
	}
	
	/**
	 * 添加一个请求头。如果aIgnoreEmpty为false或者aValue不为空，则添加。
	 *
	 * @param aKey 请求头的键
	 * @param aValue 请求头的值
	 * @param aIgnoreEmpty 如果为true且aValue为空，则不添加请求头
	 * @return 返回当前的Request对象，支持链式调用
	 */
	public Request header(String aKey , String aValue , boolean aIgnoreEmpty)
	{
		if(!aIgnoreEmpty || XString.isNotEmpty(aValue))
			mHeaderMap.put(aKey, aValue) ;
		return this ;
	}
	
	/**
	 * 设置URL编码器。如果传入的aURLCoder不为null且与当前的mURLCoder不同，则更新。
	 *
	 * @param aURLCoder 新的URL编码器
	 * @return 返回当前的Request对象，支持链式调用
	 */
	public Request urlCoder(URLCoder aURLCoder)
	{
		if(aURLCoder != null && mURLCoder != aURLCoder)
			mURLCoder = aURLCoder ;
		return this ;
	}
	
	/**
	 * 获取请求头映射。
	 *
	 * @return 请求头映射
	 */
	public IMultiMap<String, String> getHeaderMap()
	{
		return mHeaderMap;
	}
	
	/**
	 * 获取表单参数的所有键。
	 *
	 * @return 表单参数的所有键的集合
	 */
	public Collection<String> getFormParamKeys()
	{
		return mFormParamMap.keySet() ;
	}
	
	/**
	 * 根据键获取表单参数的值。
	 *
	 * @param aKey 表单参数的键
	 * @return 表单参数的值
	 */
	public String getFormParamValue(String aKey)
	{
		return mFormParamMap.getFirst(aKey) ;
	}
	
	/**
	 * 获取表单参数的映射。
	 *
	 * @return 表单参数的映射
	 */
	public IMultiMap<String, String> getFormParamMap()
	{
		return mFormParamMap;
	}
	
	/**
	 * 获取URL参数的映射。
	 *
	 * @return URL参数的映射
	 */
	public IMultiMap<String, String> getQueryParamMap()
	{
		return mUrlParamMap;
	}
	
	/**
	 * 克隆当前的Request对象。
	 *
	 * @return 克隆后的Request对象
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
	
	/**
	 * 创建一个GET请求的Request对象。
	 *
	 * @return GET请求的Request对象
	 */
	public static Request GET()
	{
		return new Request(HttpConst.sMethod_GET) ;
	}
	
	/**
	 * 创建一个GET请求的Request对象，并设置URL。
	 *
	 * @param aUri 请求的URI
	 * @return GET请求的Request对象
	 */
	public static Request GET(URI aUri)
	{
		return _build(new Request(HttpConst.sMethod_GET) , aUri) ;
	}
	
	/**
	 * 创建一个POST请求的Request对象，并设置Content-Type为application/json。
	 *
	 * @return POST请求的Request对象
	 */
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
	
	/**
	 * 创建一个POST请求的Request对象，并设置URL和Content-Type为application/json。
	 *
	 * @param aUrl 请求的URL
	 * @return POST请求的Request对象
	 */
	public static Request POST(URI aUrl)
	{
		return _build(new Request(HttpConst.sMethod_POST) , aUrl) ;
	}
	
	/**
	 * 创建一个PUT请求的Request对象。
	 *
	 * @return PUT请求的Request对象
	 */
	public static Request PUT()
	{
		return new Request(HttpConst.sMethod_PUT) ;
	}
	
	/**
	 * 创建一个PUT请求的Request对象，并设置URL。
	 *
	 * @param aUri 请求的URI
	 * @return PUT请求的Request对象
	 */
	public static Request PUT(URI aUri)
	{
		return _build(new Request(HttpConst.sMethod_PUT) , aUri) ;
	}
	
	/**
	 * 根据传入的方法字符串创建一个Request对象。
	 *
	 * @param aMethod 请求的方法（如GET, POST等）
	 * @return 对应方法的Request对象
	 */
	public static Request method(String aMethod)
	{
		String method = aMethod.toUpperCase() ;
		Assert.isTrue(HttpConst.isValidHttpMethod(method)) ;
		return new Request(method) ;
	}
	
	/**
	 * 根据传入的Request对象和URL构建一个完整的Request对象。
	 *
	 * @param aRequest 原始的Request对象
	 * @param aUri 请求的URI
	 * @return 构建后的Request对象
	 */
	static Request _build(Request aRequest , URI aUri)
	{
		try
		{
			aRequest.path(URLDecoder.decode(aUri.getPath() , "UTF-8")) ;
		}
		catch (UnsupportedEncodingException e)
		{
			WrapException.wrapThrow(e) ;
		}
		String queryStr = aUri.getQuery() ;
		if(XString.isNotEmpty(queryStr))
		{
			IMultiMap<String, String> map = URLBuilder.parseQueryStr(aUri.getQuery()) ;
			for(Entry<String , String> entry : map.entrySet())
				aRequest.queryParam(entry.getKey(), entry.getValue()) ;
		}
		return aRequest ;
	}
	
	/**
	 * 创建一个HEAD请求的Request对象。
	 *
	 * @return HEAD请求的Request对象
	 */
	public static Request HEAD()
	{
		return new Request(HttpConst.sMethod_HEAD) ;
	}
	
	/**
	 * 创建一个DELETE请求的Request对象。
	 *
	 * @return DELETE请求的Request对象
	 */
	public static Request DELETE()
	{
		return new Request(HttpConst.sMethod_DELETE) ;
	}
	
	/**
	 * 创建一个DELETE请求的Request对象，并设置URL。
	 *
	 * @param aUri 请求的URI
	 * @return DELETE请求的Request对象
	 */
	public static Request DELETE(URI aUri)
	{
		return _build(new Request(HttpConst.sMethod_DELETE) , aUri) ;
	}
	
	/**
	 * 创建一个PATCH请求的Request对象。
	 *
	 * @return PATCH请求的Request对象
	 */
	public static Request PATCH()
	{
		return new Request(HttpConst.sMethod_PATCH) ;
	}
	
	/**
	 * 创建一个PATCH请求的Request对象，并设置URL。
	 *
	 * @param aUri 请求的URI
	 * @return PATCH请求的Request对象
	 */
	public static Request PATCH(URI aUri)
	{
		return _build(new Request(HttpConst.sMethod_PATCH) , aUri) ;
	}
}
