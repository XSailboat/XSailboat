package team.sailboat.commons.fan.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import team.sailboat.commons.fan.collection.HashMultiMap;
import team.sailboat.commons.fan.collection.IMultiMap;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.text.XString;

public interface IURLBuilder
{
	public static IURLBuilder create()
	{
		return new URLBuilder() ;
	}
	
	public static IURLBuilder create(String aURL)
	{
		return new URLBuilder(aURL) ;
	}
	
	IURLBuilder protocol(String aProtocol) ;
	
	IURLBuilder host(String aHost) ;
	
	IURLBuilder port(int aPort) ;
	
	IURLBuilder path(String aPath) ;
	
	IURLBuilder queryParams(String aKey , Object...aVals) ;
	
	IURLBuilder replaceQueryParams(String aKey , Object...aVals) ;
	
	IURLBuilder clearAllQueryParams() ;
	
	String toString() ;
	
	String getProtocol() ;
	String getHost() ;
	int getPort() ;
	String getPath() ;
	
	IMultiMap<String , String> getQueryParamsMap() ;
	
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
}
