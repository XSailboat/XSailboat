package team.sailboat.ms.base.dataset.tool;

import team.sailboat.base.def.WorkEnv;
import team.sailboat.commons.fan.http.HttpClient;
import team.sailboat.commons.fan.http.IRestClient;
import team.sailboat.commons.fan.http.Request;
import team.sailboat.commons.ms.MSApp;

public class DSClient implements IRestClient
{
	
	final String wsId ;
	final String mWorkEnv ;
	
	HttpClient mClient ;
	
	public DSClient(String aWsId , WorkEnv aWorkEnv)
	{
		wsId = aWsId ;
		mWorkEnv = aWorkEnv.name() ;
		mClient = HttpClient.of("localhost" , Integer.parseInt(MSApp.instance().getHttpPort())) ;
	}

	@Override
	public Object ask(Request aRequest) throws Exception
	{
		String path = aRequest.getPath() ;
		aRequest.path("/"+mWorkEnv + (path.charAt(0)=='/'?path:("/"+ path)))
			.header("x_scope" , wsId+mWorkEnv) ;
		return mClient.ask(aRequest) ; 
	}

}
