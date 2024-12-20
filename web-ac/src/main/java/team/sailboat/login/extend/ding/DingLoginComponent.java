package team.sailboat.login.extend.ding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import team.sailboat.ms.ac.AppConfig;
import team.sailboat.ms.ac.plugin.ILoginComponent;
import team.sailboat.ms.ac.server.ResourceManageServer;

@Component
public class DingLoginComponent implements ILoginComponent
{
	@Autowired
	AppConfig mAppConfig ;
	
	@Autowired
	ResourceManageServer mResMngServer ;
	
	DingAuthenticationFilter mDingAuthFilter ;
	
	DingClient mDingClient ;
	
	
	public DingLoginComponent()
	{
		mDingAuthFilter = new DingAuthenticationFilter() ;
	}
	
	public DingClient getDingClient()
	{
		if(mDingClient == null)
		{
			mDingClient = new DingClient(mAppConfig.getDingAppKey(), mAppConfig.getDingAppSecret()) ;
		}
		return mDingClient;
	}
	
	@Override
	public boolean isEnabled()
	{
		return mAppConfig.isDingLoginEnable() ;
		
	}

	@Override
	public void injectFilter(HttpSecurity aHttp , String aDefaultSuccessUrl)
	{
		aHttp.addFilterAfter(mDingAuthFilter , UsernamePasswordAuthenticationFilter.class) ;
		ProviderManager pvdMng = new ProviderManager(new DingCodeAuthenticationProvider(getDingClient() 
				, mResMngServer.getUserDataMng())) ;
		mDingAuthFilter.setAuthenticationManager(pvdMng) ; 
		mDingAuthFilter.setSuccessUrl(aDefaultSuccessUrl) ;
	}

}
