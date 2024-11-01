package team.sailboat.commons.fan.http;

import java.util.Set;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.Assert;

public class HttpUtils
{	
	static Set<String> sSupportBodyMethods = XC.hashSet("POST"
			, "PUT" , "PATCH") ;
	
	public static boolean isOK(int aStatusCode)
	{
		return aStatusCode>=200 && aStatusCode<300 ;
	}
	
	public static boolean isError(int aStatusCode)
	{
		return aStatusCode>=300 && aStatusCode<600 ;
	}
	
	public static boolean isSupportHttpBody(String aMethod)
	{
		Assert.notEmpty(aMethod);
		return sSupportBodyMethods.contains(aMethod.toUpperCase()) ;
	}
}
