package team.sailboat.commons.ms.exceps ;

import team.sailboat.commons.fan.http.HttpStatus;

public class RestException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	HttpStatus mHttpStatus ;

	public RestException()
	{
	}

	public RestException(HttpStatus aHttpStatus , String aMessage)
	{
		super(aMessage);
		mHttpStatus = aHttpStatus ;
	}

	public RestException(HttpStatus aHttpStatus , Throwable aCause)
	{
		super(aCause);
		mHttpStatus = aHttpStatus ;
	}
	
	public HttpStatus getHttpStatus()
	{
		return mHttpStatus;
	}
	
	public int getHttpStatus(int aStatus)
	{
		return mHttpStatus == null?aStatus:mHttpStatus.value() ;
	}
	
	public static final RestException ofDeprecated()
	{
		return new RestException(HttpStatus.GONE , "此接口已经永久废除") ;
	}
}
