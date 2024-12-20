package team.sailboat.commons.fan.http;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Set;

import team.sailboat.commons.fan.collection.HashMultiMap;
import team.sailboat.commons.fan.collection.IMultiMap;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.infc.IterateOpCode;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.First;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.RegexUtils;
import team.sailboat.commons.fan.text.XString;

/**
 * 
 * URL构造器
 *
 * @author yyl
 * @since 2024年12月7日
 */
public class URLBuilder
{
	final URLCoder mURLCoder = URLCoder.getDefault() ;
	
	String mProtocol ;
	int mPort = -1 ;
	String mHost ;
	String mPath ;
	IMultiMap<String , String> mParamsMap = new HashMultiMap<>() ;
	
	protected URLBuilder()
	{
	}
	
	protected URLBuilder(URI aUri)
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
	
	/**
	 * 
	 * 设置协议
	 * 
	 * @param aProtocol
	 * @return
	 */
	public URLBuilder protocol(String aProtocol)
	{
		Assert.notBlank(aProtocol , "协议[%s]不能为空或空白字符串" , aProtocol) ;
		mProtocol = aProtocol.trim() ;
		return this ;
	}
	
	/**
	 * 
	 * 设置主机
	 * 
	 * @param aHost
	 * @return
	 */
	public URLBuilder host(String aHost)
	{
		Assert.notBlank(aHost , "主机[%s]不能为空或空白字符串" , aHost) ;
		mHost = FileUtils.toCommonPath(aHost.trim()) ;
		return this ;
	}
	
	/**
	 * 设置端口
	 * @param aPort		端口号为-1时，表示使用指定协议缺省的端口。端口将不会出现在URL串中
	 * @return
	 */
	public URLBuilder port(int aPort)
	{
		Assert.isTrue(aPort>0 || aPort == -1 , "端口号不能为"+aPort) ;
		mPort = aPort ;
		return this ;
	}
	
	/**
	 * 设置API路径
	 * @param aPath
	 * @return
	 */
	public URLBuilder path(String aPath)
	{
		mPath = aPath!=null?FileUtils.toCommonPath(aPath.trim()):null ;
		if(mPath != null && mPath.startsWith("/"))
			mPath = mPath.substring(1) ;
		return this ;
	}
	
	/**
	 * URL中附带的参数
	 * 
	 * @param aKey
	 * @param aVals
	 * @return
	 */
	public URLBuilder queryParams(String aKey , Object...aVals)
	{
		mParamsMap.putAll(aKey, JCommon.toStringsIgnoreNull(aVals)) ;
		return this ;
	}
	
	/**
	 * 
	 * 设置查询参数。如果已经存在会替换掉原来的，不是追加
	 *	
	 * @param aKey
	 * @param aVals
	 * @return
	 */
	public URLBuilder setQueryParams(String aKey , Object...aVals)
	{
		mParamsMap.set(aKey, JCommon.toStringsIgnoreNull(aVals)) ;
		return this ;
	}
	
	/**
	 * 清除URL附带的查询参数
	 * @return
	 */
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
	
	public String getHost()
	{
		return mHost ;
	}
	
	public String getProtocol()
	{
		return mProtocol ;
	}
	
	public int getPort()
	{
		return mPort ;
	}
	
	public String getPath()
	{
		return mPath;
	}
	
	public IMultiMap<String, String> getQueryParamsMap()
	{
		return mParamsMap ;
	}
	
	public static IMultiMap<String, String> parseQueryStr(String aQueryStr)
	{
		IMultiMap<String, String> map = new HashMultiMap<>() ;
		if(XString.isNotEmpty(aQueryStr))
			parseQueryStr(aQueryStr, map) ;
		return map ;
	}
	
	public static void parseQueryStr(String aQueryStr , IMultiMap<String , String> aMap)
	{
		synchronized (aMap)
		{
			String key = null;
			String value = null;
			int mark = -1;
			for (int i = 0; i < aQueryStr.length(); i++)
			{
				char c = aQueryStr.charAt(i);
				switch (c)
				{
				case '&':
					int l = i - mark - 1;
					value = l == 0 ? "": decodeParamValue(aQueryStr.substring(mark + 1, i)) ;
					mark = i;
					if (key != null)
					{
						aMap.put(key, value);
					}
					else if (value != null && value.length() > 0)
					{
						aMap.put(value, "");
					}
					key = null;
					value = null;
					break;
				case '=':
					if (key != null)
						break;
					key = aQueryStr.substring(mark + 1, i);
					mark = i;
					break;
				}
			}

			if (key != null)
			{
				int l = aQueryStr.length() - mark - 1;
				value = l == 0 ? "": decodeParamValue(aQueryStr.substring(mark + 1)) ;
				aMap.put(key, value);
			}
			else if (mark < aQueryStr.length())
			{
				key = aQueryStr.substring(mark + 1);
				if (key != null && key.length() > 0)
				{
					aMap.put(key, "");
				}
			}
		}
	}
	
	static String decodeParamValue(String aParamValue)
	{
		try
		{
			return URLDecoder.decode(aParamValue, "UTF-8") ;
		}
		catch (UnsupportedEncodingException e)
		{
			WrapException.wrapThrow(e) ;
			return null ;		// dead code
		}
	}
	
	/**
	 * 
	 * 创建一个新的URL构建器
	 * 
	 * @return
	 */
	public static URLBuilder create()
	{
		return new URLBuilder() ;
	}
	
	public static URLBuilder create(URI aUri)
	{
		return new URLBuilder(aUri) ;
	}
	
	public static URLBuilder create(String aUri)
	{
		return new URLBuilder(URI.create(aUri)) ;
	}
}
