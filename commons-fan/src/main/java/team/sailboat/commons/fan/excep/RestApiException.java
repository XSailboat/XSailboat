package team.sailboat.commons.fan.excep;

import java.net.URL;
import java.util.Date;

import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.fan.time.XTime;

public class RestApiException extends HttpException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String mRootExceptionClassName ;
	Date mTimestamp ;

	public RestApiException(int aStatusCode,
			String aHttpMethod ,
			String aDomain,
			String aPath,
			String aMsg ,
			String aRootExceptionClassName , 
			Date aTimestamp)
	{
		super(aStatusCode , aHttpMethod , aDomain, aPath, aMsg 
				, XString.splice("rootExceptionClassName:" , JCommon.defaultIfNull(aRootExceptionClassName , "") 
						, "；timestamp:" , JCommon.defaultIfNull(XTime.format$yyyyMMddHHmmss(aTimestamp) , "")));
		mRootExceptionClassName = aRootExceptionClassName ;
		mTimestamp = aTimestamp ;
	}
	
	public String getRootExceptionClassName()
	{
		return mRootExceptionClassName;
	}
	
	public Date getTimestamp()
	{
		return mTimestamp;
	}
	
	public static RestApiException create(String aHttpMethod , URL aUrl , int aStatus , String aMsg , String aRootExceptionClassName , Date aTimestamp) throws HttpException
	{
		return new RestApiException(aStatus , aHttpMethod
				, aUrl.getHost()+(aUrl.getPort()==-1?"":(":"+aUrl.getPort())) 
				, aUrl.getPath() 
				, aMsg
				, aRootExceptionClassName 
				, aTimestamp) ;
	}
	
	public static void createAndThrow(String aHttpMethod , URL aUrl , int aStatus , String aMsg , String aRootExceptionClassName , Date aTimestamp) throws HttpException
	{
		throw new RestApiException(aStatus , aHttpMethod
				, aUrl.getHost()+(aUrl.getPort()==-1?"":(":"+aUrl.getPort())) 
				, aUrl.getPath() 
				, aMsg
				, aRootExceptionClassName 
				, aTimestamp) ;
	}

}
