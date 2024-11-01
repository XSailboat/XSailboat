package team.sailboat.commons.fan.http.xca;

import team.sailboat.commons.fan.http.ISigner;
import team.sailboat.commons.fan.http.Request;

public class ApiKeySigner implements ISigner
{

	@Override
	public void sign(String aContextPath, Request aRequest, String aApiKey, String aAppSecret) throws Exception
	{
		aRequest.header("Authorization", "ApiKey "+aApiKey) ;
	}

}
