package team.sailboat.commons.fan.http;

public class DefaultHttpClient extends HttpClient
{
	
	
	DefaultHttpClient(String aHost , int aPort)
	{
		super(aHost, aPort) ;
	}

	@Override
	public String getProtocol()
	{
		return "http" ;
	}
	
	
}
