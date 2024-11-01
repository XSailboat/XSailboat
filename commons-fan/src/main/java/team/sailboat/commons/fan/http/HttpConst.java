package team.sailboat.commons.fan.http;

import java.util.Set;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.text.XString;

public interface HttpConst
{
	public static final String sHeaderName_Accept = "Accept" ;
	public static final String sHeaderName_ContentType = "Content-Type" ;
	public static final String sHeaderName_ContentLength = "Content-Length" ;
	public static final String sHeaderName_ContentMD5 = "Content-MD5" ;
	public static final String sHeaderName_Authorization = "Authorization" ;
	public static final String sHeaderName_X_Requested_With = "X-Requested-With" ; 
	public static final String sHeaderName_referer = "referer" ;
	public static final String sHeaderName_ContentDisposition = "Content-Disposition" ;
	public static final String sHeaderName_Connection = "Connection" ;
	public static final String sHeaderName_ContentEncoding = "Content-Encoding" ;
	
    public static final String sHeaderName_UserAgent = "User-Agent";
    public static final String sHeaderValue_UserAgent_x_HttpClient = "x-HttpClient" ;
    public static final String sHeaderValue_Accept_JSON = MediaType.APPLICATION_JSON_VALUE ;
    public static final String sHeaderName_Date = "Date";
    public static final String sHeaderName_Host = "Host" ;
    
    public static final String sHeaderName_BalanceStrategy = "Balance-Strategy" ;
    /**
     * 负载均衡不要把这个请求分散到多个实例
     */
    public static final String sHeaderValue_KeepOneService = "Keep-One-Service" ;
	
	public static final String sMethod_HEAD = "HEAD" ;
	public static final String sMethod_PUT = "PUT" ;
	public static final String sMethod_GET = "GET" ;
	public static final String sMethod_POST = "POST" ;
	public static final String sMethod_DELETE = "DELETE" ;
	public static final String sMethod_PATCH = "PATCH" ;
	
	public static final String sTLSv1_2 = "TLSv1.2" ;
	public static final String sSSLv2 = "SSLv2" ;
	public static final String sTLSv1_1 = "TLSv1.1" ;
	public static final String sSSLv3 = "SSLv3" ;
	
	static Set<String> sValidHttpMethods = XC.hashSet(sMethod_DELETE , sMethod_GET
			, sMethod_HEAD , sMethod_PATCH , sMethod_POST , sMethod_PUT) ;
	
	
	public static boolean isHaveBodyMethod(String aMethod)
	{
		return sMethod_POST.equalsIgnoreCase(aMethod)
				|| sMethod_PATCH.equalsIgnoreCase(aMethod)
				|| sMethod_PUT.equalsIgnoreCase(aMethod) ;
	}
	
	public static boolean isValidHttpMethod(String aMethod)
	{
		if(XString.isEmpty(aMethod))
			return false ;
		return sValidHttpMethods.contains(aMethod.toUpperCase()) ;
	}
}
