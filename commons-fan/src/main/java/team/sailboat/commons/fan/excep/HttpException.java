package team.sailboat.commons.fan.excep;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import team.sailboat.commons.fan.http.HttpStatus;
import team.sailboat.commons.fan.text.XString;

public class HttpException extends IOException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	HttpStatus mStatus ;
	String mMethod ;
	String mDomain ;
	String mPath ;
	String mMessage ;
	
	public HttpException(int aStatusCode , String aHttpMethod , String aDomain , String aPath , String aMsg)
	{
		this(aStatusCode , aHttpMethod , aDomain , aPath , aMsg , null) ;
	}
	
	protected HttpException(int aStatusCode , String aHttpMethod , String aDomain , String aPath , String aMsg , String aAppendExcepMsg)
	{
		super(XString.splice("HttpCode:" , aStatusCode , "；HttpMethod:" , aHttpMethod , "；URL:" ,aDomain , aPath , "；" , aMsg == null?"":("message："+aMsg) ,aAppendExcepMsg == null?"":("；"+aAppendExcepMsg))) ;
		mStatus = HttpStatus.valueOf(aStatusCode) ;
		mMethod = aHttpMethod ;
		mDomain = aDomain ;
		mPath = aPath ;
		mMessage = aMsg ;
	}
	
	public HttpStatus getStatus()
	{
		return mStatus;
	}
	
	public String getRawMessage()
	{
		return mMessage ;
	}
	
	public String getDomain()
	{
		return mDomain;
	}
	
	public String getPath()
	{
		return mPath;
	}
	
	public String getDisplayMessage()
	{
		return mMessage ;
	}
	
	public String getMethod()
	{
		return mMethod;
	}
	
	public static HttpException create(String aHttpMethod , URL aUrl , int aStatus , String aMsg) throws HttpException
	{
		return new HttpException(aStatus , aHttpMethod 
				, aUrl.getHost()+(aUrl.getPort()==-1?"":(":"+aUrl.getPort())) , aUrl.getPath() 
				, aMsg) ;
	}
	
	public static void createAndThrow(String aHttpMethod , URL aUrl , int aStatus , String aMsg) throws HttpException
	{
		throw new HttpException(aStatus , aHttpMethod 
				, aUrl.getHost()+(aUrl.getPort()==-1?"":(":"+aUrl.getPort())) , aUrl.getPath() 
				, aMsg) ;
	}
	
	public static HttpException create(String aHttpMethod , URI aUri , int aStatus , String aMsg) throws HttpException
	{
		return new HttpException(aStatus , aHttpMethod 
				, aUri.getHost()+(aUri.getPort()==-1?"":(":"+aUri.getPort())) , aUri.getPath() 
				, aMsg) ;
	}
	
	public static void createAndThrow(String aHttpMethod , URI aUri , int aStatus , String aMsg) throws HttpException
	{
		throw new HttpException(aStatus , aHttpMethod 
				, aUri.getHost()+(aUri.getPort()==-1?"":(":"+aUri.getPort())) , aUri.getPath() 
				, aMsg) ;
	}
}
