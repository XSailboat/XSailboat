package team.sailboat.commons.fan.http;

import java.net.URI;
import java.util.Set;

import team.sailboat.commons.fan.collection.HashMultiMap;
import team.sailboat.commons.fan.collection.IMultiMap;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.infc.IterateOpCode;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.First;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.RegexUtils;
import team.sailboat.commons.fan.text.XString;

public class URLBuilder implements IURLBuilder
{
	final URLCoder mURLCoder = URLCoder.getDefault() ;
	
	String mProtocol ;
	int mPort = -1 ;
	String mHost ;
	String mPath ;
	IMultiMap<String , String> mParamsMap = new HashMultiMap<>() ;
	
	public URLBuilder()
	{
	}
	
	public URLBuilder(URI aUri)
	{
		mProtocol = aUri.getScheme() ;
		mHost = aUri.getHost() ;
		mPort = aUri.getPort() ;
		mPath = aUri.getPath() ;
		if(mHost == null && mPath != null)
		{
			if("localhost".equalsIgnoreCase(mPath))
			{
				mHost = "localhost" ;
				mPath = null ;
			}
			else if(RegexUtils.checkIPv4(mPath))
			{
				mHost = mPath ;
				mPath = null ;
			}
		}
		Assert.notEmpty(mHost, "主机地址为空") ;
		appendRawQuery(aUri.getQuery()) ;
	}
	
	public URLBuilder(String aURL)
	{
		this(URI.create(aURL)) ;
	}
	
	public URLBuilder appendRawQuery(String aRawQuery)
	{
		return appendRawQuery(aRawQuery, null) ;
	}
	
	public URLBuilder appendRawQuery(String aRawQuery , Set<String> aExecludeParamNames)
	{
		if(XString.isNotEmpty(aRawQuery))
		{
			String[] segs = aRawQuery.split("&") ;
			for(String seg : segs)
			{
				String[] pv = seg.split("=") ;
				if(aExecludeParamNames != null && aExecludeParamNames.contains(pv[0]))
					continue ;
				if(pv.length == 2)
					mParamsMap.put(pv[0] , pv[1]) ;
				else if(pv.length == 1)
				{
					if(!mParamsMap.containsKey(pv[0]))
						mParamsMap.put(pv[0] , null) ;
				}
			}
		}
		return this ;
	}
	
	@Override
	public URLBuilder protocol(String aProtocol)
	{
		Assert.notBlank(aProtocol , "协议[%s]不能为空或空白字符串" , aProtocol) ;
		mProtocol = aProtocol.trim() ;
		return this ;
	}
	
	@Override
	public URLBuilder host(String aHost)
	{
		Assert.notBlank(aHost , "主机[%s]不能为空或空白字符串" , aHost) ;
		mHost = FileUtils.toCommonPath(aHost.trim()) ;
		return this ;
	}
	
	@Override
	public URLBuilder port(int aPort)
	{
		Assert.isTrue(aPort>0 || aPort == -1 , "端口号不能为"+aPort) ;
		mPort = aPort ;
		return this ;
	}
	
	@Override
	public URLBuilder path(String aPath)
	{
		mPath = aPath!=null?FileUtils.toCommonPath(aPath.trim()):null ;
		if(mPath != null && mPath.startsWith("/"))
			mPath = mPath.substring(1) ;
		return this ;
	}
	
	@Override
	public URLBuilder queryParams(String aKey , Object...aVals)
	{
		mParamsMap.putAll(aKey, JCommon.toStringsIgnoreNull(aVals)) ;
		return this ;
	}
	
	@Override
	public URLBuilder replaceQueryParams(String aKey , Object...aVals)
	{
		mParamsMap.set(aKey, JCommon.toStringsIgnoreNull(aVals)) ;
		return this ;
	}
	
	@Override
	public URLBuilder clearAllQueryParams()
	{
		mParamsMap.clear();
		return this ;
	}
	
	@Override
	public String toString()
	{
		StringBuilder strBld = new StringBuilder() ;
		strBld.append(mProtocol==null?"<未知协议>":mProtocol)
			.append("://").append(mHost==null?"<未知主机>":mHost)
			.append(mPort==-1?"":":"+mPort) ;
		if(XString.isNotEmpty(mPath))
		{
			if(mPath.charAt(0) != '/')
				strBld.append('/');
			strBld.append(mPath) ;
		}
		if(!mParamsMap.isEmpty())
		{
			strBld.append('?') ;
			First first = new First() ;
			mParamsMap.iterateEntry((key , val)->{
				if(!first.checkDo())
					strBld.append('&') ;
				strBld.append(mURLCoder.encodeParam(key)) ;
				if(!val.isEmpty())
					strBld.append('=')
							.append(mURLCoder.encodeParam(val)) ;
				return IterateOpCode.sContinue ;
			}) ;
		}
		return strBld.toString();
	}
	
	@Override
	public String getHost()
	{
		return mHost ;
	}
	
	@Override
	public String getProtocol()
	{
		return mProtocol ;
	}
	
	@Override
	public int getPort()
	{
		return mPort ;
	}
	
	@Override
	public String getPath()
	{
		return mPath;
	}
	
	@Override
	public IMultiMap<String, String> getQueryParamsMap()
	{
		return mParamsMap ;
	}
	

}
