package team.sailboat.ms.crane.cmd;

import team.sailboat.commons.fan.http.HttpClient;

/**
 * 调用远端接口的命令
 *
 * @author yyl
 * @since 2024年10月19日
 */
public abstract class RestCmd extends Cmd
{
	protected HttpClient mClient ; 
	
	public void setRestClient(HttpClient aClient)
	{
		mClient = aClient ;
	}
}
