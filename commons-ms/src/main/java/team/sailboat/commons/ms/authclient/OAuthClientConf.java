package team.sailboat.commons.ms.authclient;

import java.net.URI;
import java.net.URL;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import team.sailboat.commons.fan.http.HttpClient;
import team.sailboat.commons.fan.http.URLBuilder;
import team.sailboat.commons.fan.http.xca.XAppSigner;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.ms.MSApp;

public class OAuthClientConf
{
	String mClientId ;
	String mClientSecret ;
	String[] mScopes ;
//	String mRedirectUrl ;
	String mCodeCallbackUrl ;
	String mCodeCallbackPath ;
	String mLocalLoginPath ;
	/**
	 * 通知刷新用户权限的回调通知
	 */
	String mRefreshUserAuthoritesCallbackPath = "/oauth2/refresh_auths" ;

	URL mBaseUrl ;
	
	String mAuthServerTokenPath = "/oauth2/token" ;
	
	String mAuthServerAuthPath = "/oauth2/authorize" ;
	
	String mBlankLoginPath = "/commons/blank_login.html" ;
	
	
	String mBlankLoginPageHtml = "<!DOCTYPE html>\n"
			+ "<html>\n"
			+ "<head>\n"
			+ "<meta charset=\"UTF-8\">\n"
			+ "<title>Insert title here</title>\n"
			+ "</head>\n"
			+ "<body>\n"
			+ "<script type=\"text/javascript\">\n"
			+ "  if (navigator.userAgent.indexOf('MSIE') > 0) { // close IE\n"
			+ "     if (navigator.userAgent.indexOf('MSIE 6.0') > 0) {\n"
			+ "        window.opener = null;\n"
			+ "        window.close();\n"
			+ "     } else {\n"
			+ "        window.open('', '_top');\n"
			+ "        window.top.close();\n"
			+ "     }\n"
			+ "  } else { // close chrome;It is effective when it is only one.\n"
			+ "     window.opener = null;\n"
			+ "     window.open('', '_self');\n"
			+ "     window.close();\n"
			+ "  }\n"
			+ "</script>\n"
			+ "</body>\n"
			+ "</html>" ;
	
	HttpClient mAuthCenterClient ;
	
	public OAuthClientConf()
	{
	}
	
	public OAuthClientConf(String aClientId , String aClientSecret //, String aRedirectUrl
			, String aCodeCallbackUrl
			, String aLocalLoginPath
			, URL aBaseUrl
			, String... aScopes)
	{
		mClientId = aClientId ;
		mClientSecret = aClientSecret ;
//		mRedirectUrl = aRedirectUrl ;
		mCodeCallbackUrl = aCodeCallbackUrl ;
		mCodeCallbackPath = MSApp.codePath(URI.create(mCodeCallbackUrl).getPath()) ;
		mLocalLoginPath = aLocalLoginPath ;
		mBaseUrl = aBaseUrl ;
		mScopes = aScopes ;
	}
	
	public HttpClient getAuthCenterClient()
	{
		if(mAuthCenterClient == null)
			mAuthCenterClient = HttpClient.ofUrl(mBaseUrl 
					, getClientId() , getClientSecret() , new XAppSigner() , true) ;
		return mAuthCenterClient;
	}

	public String getClientId()
	{
		return mClientId;
	}
	public void setClientId(String aClientId)
	{
		mClientId = aClientId;
	}

	public String getClientSecret()
	{
		return mClientSecret;
	}
	public void setClientSecret(String aClientSecret)
	{
		mClientSecret = aClientSecret;
	}
	
//	public String getRedirectUrl()
//	{
//		return mRedirectUrl;
//	}
//	public void setRedirectUrl(String aRedirectUrl)
//	{
//		mRedirectUrl = aRedirectUrl;
//	}
	
	public String getBlankLoginPath()
	{
		return mBlankLoginPath;
	}
	
	public String getBlankLoginPageHtml()
	{
		return mBlankLoginPageHtml;
	}
	
	public String getCodeCallbackUrl()
	{
		return mCodeCallbackUrl;
	}
	public void setCodeCallbackUrl(String aCodeCallbackUrl)
	{
		mCodeCallbackUrl = aCodeCallbackUrl;
	}
	public String getCodeCallbackPath()
	{
		return mCodeCallbackPath;
	}
	
	public String getLocalLoginPath()
	{
		return mLocalLoginPath;
	}
	public void setLocalLoginPath(String aLocalLoginPath)
	{
		mLocalLoginPath = aLocalLoginPath ;
	}
	
	public String[] getScopes()
	{
		return mScopes;
	}
	public void setScopes(String[] aScopes)
	{
		mScopes = aScopes;
	}
	
//	public String getAuthServerHost()
//	{
//		return mAuthServerHost;
//	}
//	public void setAuthServerHost(String aAuthServerHost)
//	{
//		mAuthServerHost = aAuthServerHost;
//	}
//	
//	public int getAuthServerPort()
//	{
//		return mAuthServerPort;
//	}
//	public void setAuthServerPort(int aAuthServerPort)
//	{
//		mAuthServerPort = aAuthServerPort;
//	}
	
	public String getAuthServerTokenPath()
	{
		return mAuthServerTokenPath ;
	}
	public void setAuthServerTokenPath(String aAuthServerTokenPath)
	{
		mAuthServerTokenPath = aAuthServerTokenPath ;
	}
	
	public String getAuthServerAuthPath()
	{
		return mAuthServerAuthPath;
	}
	public void setAuthServerAuthPath(String aAuthServerAuthPath)
	{
		mAuthServerAuthPath = aAuthServerAuthPath;
	}
	
	public void setRefreshUserAuthoritesCallbackPath(String aRefreshUserAuthoritesCallbackPath)
	{
		mRefreshUserAuthoritesCallbackPath = aRefreshUserAuthoritesCallbackPath;
	}
	public String getRefreshUserAuthoritesCallbackPath()
	{
		return mRefreshUserAuthoritesCallbackPath;
	}
	
	public String getAuthServerAuthUrl(String aState)
	{
		return new URLBuilder().protocol(mBaseUrl.getProtocol())
				.host(mBaseUrl.getHost())
				.port(mBaseUrl.getPort())
				.path(mBaseUrl.getPath() + mAuthServerAuthPath)
				.queryParams("response_type" , "code")
				.queryParams("client_id" , getClientId())
				.queryParams("scope" , XString.toString(" ", getScopes()))
				.queryParams("state" , aState)
				.queryParams("redirect_uri", getCodeCallbackUrl())
				.toString() ;
	}
	
	public void applyTo(HttpSecurity aHttp) throws Exception
	{
		aHttp.addFilterBefore(new CorsTokenLoginFilter(this) , UsernamePasswordAuthenticationFilter.class) ;
		aHttp.addFilterBefore(new OAuthCodeCallbackFilter(this) , CorsTokenLoginFilter.class) ;
		aHttp.addFilterBefore(new LoginFilter(this) , OAuthCodeCallbackFilter.class) ; 
		aHttp.addFilterAfter(new AjaxLoginFilter(getBlankLoginPath()) , ExceptionTranslationFilter.class) ;
		aHttp.authorizeHttpRequests(reg->{
			reg.requestMatchers(MSApp.codePath(getCodeCallbackPath()) 
					, getLocalLoginPath()
					, getRefreshUserAuthoritesCallbackPath())
				.permitAll() ; 
		}).formLogin(conf->{
			conf.loginPage(getLocalLoginPath()) ;
		}).csrf(conf->{
			conf.ignoringRequestMatchers(getRefreshUserAuthoritesCallbackPath()) ;
		})
//				.oauth2Login(oauth2Login -> oauth2Login.loginPage(getLoginUrl())) ;
				;
	}
}
