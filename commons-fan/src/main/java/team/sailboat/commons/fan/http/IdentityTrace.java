package team.sailboat.commons.fan.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.eazi.Eazialiable;
import team.sailboat.commons.fan.eazi.EntryOutput;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.JSONString;
import team.sailboat.commons.fan.json.ToJSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.ITextBuilder;
import team.sailboat.commons.fan.text.ITextOut;
import team.sailboat.commons.fan.text.XString;

public class IdentityTrace implements JSONString , ITextBuilder , ToJSONObject , Cloneable , Eazialiable
		, Consumer<Request>
{
	static final Object sMutex = new Object() ;
	
	static final Map<Class<?> , Method> sHeaderMethodMap = new HashMap<>() ;
	static final Map<Class<?> , Method> sHeaderGetMethodMap = new HashMap<>() ;
	
	public static final String sHeadName_XZ_Module = "XZ-Module" ;
	
	public static final String sHeadName_XZ_RootIP = "XZ-RootIP" ;
	
	public static final String sHeadName_XZ_UserName = "XZ-UserName" ;
	
	static IdentityTrace sLocal ;
	
	static String sLocalModuleName ;
	
	static IRequestWrapperBuilder[] sBuilders ;
	
	public static void registerRequestWrapperBuilder(IRequestWrapperBuilder aBuilder)
	{
		sBuilders = XC.merge(sBuilders, aBuilder) ;
	}
	
	public static void setLocalModuleName(String aModuleName)
	{
		sLocalModuleName = aModuleName ;
		if(sLocal != null)
		{
			sLocal.mModuleLink = sLocalModuleName ;
		}
	}
	
	public static String getLocalModuleName()
	{
		return sLocalModuleName;
	}
	
	public static IdentityTrace getLocal()
	{
		if(sLocal == null)
		{
			synchronized(sMutex)
			{
				if(sLocal == null)
					sLocal = new IdentityTrace(sLocalModuleName) ;
			}
		}
		return sLocal ;
	}
	
	/**
	 * 模块调用链，模块名之间用","分隔
	 */
	String mModuleLink ;
	
	/**
	 * 如果本机是请求的根，那么不设置，让根请求的接收者来设置根IP，这样能确保IP准确有效
	 */
	String mRootIP ;
	String mUserName ;
	
	public IdentityTrace()
	{
	}
	
	/**
	 * 
	 * @param aModuleLink
	 * @param aRootIP
	 */
	public IdentityTrace(String aModuleLink)
	{
		mModuleLink = aModuleLink ;
	}
	
	public IdentityTrace(String aModuleLink , String aRootIP , String aUeserName)
	{
		mModuleLink = aModuleLink ;
		mRootIP = aRootIP ;
		mUserName = aUeserName ;
	}
	
	public String getModuleLink()
	{
		return mModuleLink;
	}
	
	/**
	 * 插入前置的模块名
	 * @param aModuleName
	 * @return
	 */
	public IdentityTrace insertModuleName(String aModuleName)
	{
		Assert.notEmpty(aModuleName) ;
		if(XString.isEmpty(mModuleLink))
			mModuleLink = aModuleName ;
		else
			mModuleLink = aModuleName + "," + mModuleLink ;
		return this ;
	}
	
	public IdentityTrace pushModuleName(String aModuleName)
	{
		Assert.notEmpty(aModuleName) ;
		if(XString.isEmpty(mModuleLink))
			mModuleLink = aModuleName ;
		else
			mModuleLink += ","+aModuleName ;
		return this ;
	}
	
	public String getLastModuleName()
	{
		if(XString.isEmpty(mModuleLink))
			return null ;
		int i = mModuleLink.lastIndexOf(',') ;
		return i==-1?mModuleLink:mModuleLink.substring(i+1) ;
	}
	
	public IdentityTrace setModuleLink(String aModuleLink)
	{
		mModuleLink = aModuleLink;
		return this ;
	}
	
	public IdentityTrace setRootIP(String aRootIP)
	{
		mRootIP = aRootIP;
		return this ;
	}
	
	public String getRootIP()
	{
		return mRootIP;
	}
	
	public IdentityTrace setUserName(String aUserName)
	{
		mUserName = aUserName;
		return this ;
	}
	
	public String getUserName()
	{
		return mUserName;
	}
	
	public void apply(HttpURLConnection aHttpConn)
	{
		try
		{
			if(mModuleLink != null)
				aHttpConn.setRequestProperty(sHeadName_XZ_Module , URLEncoder.encode(mModuleLink , "UTF-8"));
			if(mRootIP != null)
				aHttpConn.setRequestProperty(sHeadName_XZ_RootIP, URLEncoder.encode(mRootIP , "UTF-8")) ;
			if(mUserName != null)
				aHttpConn.setRequestProperty(sHeadName_XZ_UserName, URLEncoder.encode(mUserName , "UTF-8")) ;
		}
		catch (UnsupportedEncodingException e)
		{
			WrapException.wrapThrow(e) ;
		}
	}
	
	@Override
	public void accept(Request aRequest)
	{
		if(mModuleLink != null)
			aRequest.setHeader(sHeadName_XZ_Module , mModuleLink);
		if(mRootIP != null)
			aRequest.setHeader(sHeadName_XZ_RootIP, mRootIP) ;
		if(mUserName != null)
			aRequest.setHeader(sHeadName_XZ_UserName, mUserName) ;
	}
	
	public void apply(Object aRequest)
	{
		if(aRequest instanceof Request)
			accept((Request)aRequest);
		else if(aRequest instanceof HttpURLConnection)
			apply((HttpURLConnection)aRequest) ; 
		else
		{
			IRequestWrapper request = wrapRequest(aRequest) ;
			if(mModuleLink != null)
				request.addHeader(sHeadName_XZ_Module , mModuleLink);
			if(mRootIP != null)
				request.addHeader(sHeadName_XZ_RootIP, mRootIP) ;
			if(mUserName != null)
				request.addHeader(sHeadName_XZ_UserName, mUserName) ;
		}
	}
	
	@Override
	public String toString()
	{
		return toJSONString() ;
	}

	@Override
	public String toJSONString()
	{
		return toJSONObject().toJSONString() ;
	}

	@Override
	public void output(ITextOut aTextOut)
	{
		aTextOut.u("模块链:").u(getModuleLink())
			.u("  ; 根IP：").u(getRootIP())
			.u("  ; 用户：").u(getUserName()) ;
	}
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return aJSONObj.put("type", getClass().getSimpleName())
				.put("moduleLink", mModuleLink)
				.put("rootIP", mRootIP)
				.put("user", mUserName) ;
	}
	
	@Override
	public IdentityTrace clone()
	{
		return new IdentityTrace(mModuleLink, mRootIP, mUserName) ;
	}

	@Override
	public void write(EntryOutput aOuts) throws IOException
	{
		if(mModuleLink != null)
			aOuts.write("moduleLink", mModuleLink) ;
		if(mRootIP != null)
			aOuts.write("rootIP", mRootIP) ;
		if(mUserName != null)
			aOuts.write("user", mUserName) ;
	}

	@Override
	public Runnable read(Map<String, Object> aMap) throws IOException
	{
		mModuleLink = (String)aMap.get("moduleLink") ;
		mRootIP = (String)aMap.get("rootIP") ;
		mUserName = (String)aMap.get("user") ;
		return null;
	}
	
	@Override
	public boolean equals(Object aObj)
	{
		if(aObj == this)
			return true ;
		if(aObj instanceof IdentityTrace)
		{
			return JCommon.equals(mRootIP, ((IdentityTrace)aObj).mRootIP)
					&& JCommon.equals(mUserName, ((IdentityTrace)aObj).mUserName)
					&& JCommon.equals(mModuleLink, ((IdentityTrace)aObj).mModuleLink) ;
		}
		return false ;
	}
	
	static IRequestWrapper wrapRequest(Object aReq)
	{
		if(sBuilders != null)
		{
			for(IRequestWrapperBuilder builder : sBuilders)
			{
				if(builder.match(aReq))
					return builder.build(aReq) ; 
			}
		}
		return new HttpRequestWrapper(aReq) ;
	}
	
	/**
	 * 返回结果必然不为null
	 * @param aRequest
	 * @return
	 */
	public static IdentityTrace get(Object aRequest)
	{
		if(aRequest == null)
			return null ;
		IRequestWrapper request = wrapRequest(aRequest) ;
		IdentityTrace trace = new IdentityTrace(request.getHeader(IdentityTrace.sHeadName_XZ_Module)
				, request.getHeader(IdentityTrace.sHeadName_XZ_RootIP)
				, request.getHeader(IdentityTrace.sHeadName_XZ_UserName)) ;
		if(XString.isEmpty(trace.getModuleLink()))
		{
			String userAgent = request.getHeader("user-agent") ;
			if(XString.isNotEmpty(userAgent))
				trace.pushModuleName(userAgent.length()>20 && !userAgent.contains("HttpClient")?"浏览器":userAgent) ;
			else
				trace.pushModuleName("未知客户端") ;
		}
		if(XString.isEmpty(trace.getRootIP()) || "0:0:0:0:0:0:0:1".equals(trace.getRootIP()))
		{
			String remoteAddr = request.getRemoteAddr() ;
			trace.setRootIP("0:0:0:0:0:0:0:1".equals(remoteAddr)?"127.0.0.1":remoteAddr) ;
		}
		return trace ;
	}
}
