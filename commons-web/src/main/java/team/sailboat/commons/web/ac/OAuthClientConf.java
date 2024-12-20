package team.sailboat.commons.web.ac;

import java.io.IOException;
import java.net.URI;
import java.util.function.BiFunction;

import org.fest.reflect.core.Reflection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistration.ProviderDetails;
import org.springframework.security.oauth2.client.registration.ClientRegistration.ProviderDetails.UserInfoEndpoint;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import team.sailboat.commons.fan.http.HttpClient;
import team.sailboat.commons.fan.http.URLBuilder;
import team.sailboat.commons.fan.http.xca.XAppSigner;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;

/**
 * 
 * 认证中心客户端程序的配置
 *
 * @author yyl
 * @since 2024年12月6日
 */
@Component
@Data
public class OAuthClientConf
{	
	/**
	 * 授权码回调地址选择器
	 */
	BiFunction<String[] , ServletRequest , String> redirectUriSelector = (uris , req)->{
		if(uris.length == 1)
			return uris[0] ;
		if(uris.length == 0)
			throw new IllegalStateException("没有设置授权码回调地址!") ;
		String protocol = req.isSecure()? "https:":"http:" ;
		for(int i=0 ; i<uris.length ; i++)
		{
			if(uris[i].startsWith(protocol))
				return uris[i] ;
		}
		return uris[0] ;
	} ;
	
	@Autowired
	OAuth2AuthorizedClientRepository authorizedClientRepository ;
	
	@Setter(value = AccessLevel.NONE)
	@Autowired
	ClientRegistrationRepository clientRegistrationRepo ;
	
	@Getter(value = AccessLevel.NONE)
	@Setter(value = AccessLevel.NONE)
	@Autowired
	OAuth2ClientProperties clientProps ;
	
	/**
	 * SailAC客户端连接配置信息
	 */
	ClientRegistration clientRegistration ;
	
	/**
	 * 认证中心给的AppKey
	 */
	@Value("${spring.security.oauth2.client.registration.sailboat.client-id}")
	String clientId ;
	/**
	 * 认证中心给的AppSecret
	 */
	@Value("${spring.security.oauth2.client.registration.sailboat.client-secret}")
	String clientSecret ;
	/**
	 * 获取的用户信息范围		<br />
	 * 可取值在IAuthCenterConst中有定义
	 */
	@Value("${spring.security.oauth2.client.registration.sailboat.scope}")
	String[] scopes ;
	
	@Getter(value = AccessLevel.NONE)
	@Setter(value = AccessLevel.NONE)
	@Value("${sailboat.service-uri:}")
	String authCenterServiceUri ;
	
	/**
	 * 认证中心服务地址，带ContextPath
	 */
	URI authCenterUri ;
	
	/**
	 * 可以注册1个或2个授权码回调地址。			<br />
	 * 注册两个时，通常是用在同时支持http和https服务时，以http方式访问，选择http形式的回调地址；
	 * 以https方式访问时，选择https形式的回调地址。			<br />
	 * 
	 * 如果有需要，也可以实现自己的回调地址选择器，并设置进来
	 */
	@Value("${spring.security.oauth2.client.registration.sailboat.redirect-uri}")
	String[] redirectUris ;
	
	/**
	 * 本地应用的登录路径。
	 */
	final String localLoginPath = "/oauth2/authorization/sailboat" ;
	
	/**
	 * 通知刷新用户权限的回调通知
	 */
	final String refreshUserAuthoritesCallbackPath = "/oauth2/refresh_auths" ;
	
	/**
	 * 空白登录页，当进行某个操作，发现超时时，可以通过用一个新的tab打开这个空白页，进行登录
	 */
	final String blankLoginPath = "/oauth2/blank_login.html" ;
	
	
	@Getter(value = AccessLevel.NONE)
	final String mBlankLoginPageHtml = "<!DOCTYPE html>\n"
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
	
	HttpClient authCenterClient ;
	
	final XAppAccessTokenResponseClient accessTokenResponseClient = new XAppAccessTokenResponseClient(this) ;
	
	public OAuthClientConf()
	{	
	}
	
	@PostConstruct
	void _init()
	{
		clientRegistration = clientRegistrationRepo.findByRegistrationId(IAuthCenterConst.sClientResitrationId) ;
		if(XString.isNotEmpty(authCenterServiceUri)
				&& authCenterUri == null)
		{
			setAuthCenterUri(URI.create(authCenterServiceUri)) ;
		}
	}
	
	public void setAuthCenterUri(URI aUri)
	{
		if(JCommon.unequals(aUri , authCenterUri))
		{
			authCenterUri = aUri ;
			// 用反射来重写ClientRegistration里面的属性
			ProviderDetails detail = clientRegistration.getProviderDetails() ;
			Reflection.field("authorizationUri")
					.ofType(String.class)
					.in(detail)
					.set(getAuthorizationUri()) ;
					;
			Reflection.field("tokenUri")
				.ofType(String.class)
				.in(detail)
				.set(getTokenUri()) ;
				;		
				
			UserInfoEndpoint uie = detail.getUserInfoEndpoint() ;
			Reflection.field("uri")
				.ofType(String.class)
				.in(uie)
				.set(getUserInfoUri()) ;
				;		
		}
	}
	
	public String getAuthorizationUri()
	{
		return authCenterUri.toString()+IAuthCenterConst.sGET_authorize ;
	}
	
	public String getTokenUri()
	{
		return authCenterUri.toString()+IAuthCenterConst.sGET_token ;
	}
	
	public String getUserInfoUri()
	{
		return authCenterUri.toString()+IAuthCenterConst.sGET_userInfo ;
	}
	
	/**
	 * 获取认证中心服务的HttpClient
	 * @return
	 */
	public HttpClient getAuthCenterClient()
	{
		if(authCenterClient == null)
			authCenterClient = HttpClient.ofURI(authCenterUri
					, getClientId() , getClientSecret() , new XAppSigner() , true) ;
		return authCenterClient;
	}
	
	public String getRedirectUri(ServletRequest aReq)
	{
		return redirectUriSelector.apply(redirectUris, aReq) ;
	}
	
	/**
	 * 
	 * 构造认证中心的认证登录uri			<br />
	 * 用以登录，取得授权码的请求
	 * 
	 * @param aState
	 * @param aReq					当前的请求
	 * @return
	 */
	public String getAuthCenterAuthorizeUri(String aState , ServletRequest aReq)
	{
		return URLBuilder.create(authCenterUri)
				.path(authCenterUri.getPath()+IAuthCenterConst.sGET_authorize)
				.queryParams("response_type" , "code")
				.queryParams("client_id" , getClientId())
				.queryParams("scope" , XString.toString(" ", getScopes()))
				.queryParams("state" , aState)
				.queryParams("redirect_uri" , getRedirectUri(aReq))
				.toString() ;
	}
	
	public void applyTo(HttpSecurity aHttp) throws Exception
	{
		CorsTokenLoginFilter corsTokenLoginFilter = new CorsTokenLoginFilter(this) ;
		CorsTokenLoginFilterConfigurer<HttpSecurity> conf0 = new CorsTokenLoginFilterConfigurer<HttpSecurity>(corsTokenLoginFilter, this);
		conf0.failureHandler(new AuthenticationFailureHandler()
		{	
			@Override
			public void onAuthenticationFailure(HttpServletRequest aRequest,
					HttpServletResponse aResponse,
					AuthenticationException aException) throws IOException, ServletException
			{
			}
		});
		conf0.setBuilder(aHttp);		// 不设置，先configure会报错
		aHttp.addFilterBefore(corsTokenLoginFilter , UsernamePasswordAuthenticationFilter.class) ;
		aHttp.addFilterAfter(new AjaxLoginFilter(getBlankLoginPath()) , ExceptionTranslationFilter.class) ;
		aHttp.addFilterAfter(new RefreshUserStateFilter(this) , OAuth2LoginAuthenticationFilter.class) ;
		aHttp.oauth2Login(conf->{
			conf.successHandler(new CustomAuthenticationSuccessHandler(this)) ;
		}).csrf(conf->{
			conf.ignoringRequestMatchers(getRefreshUserAuthoritesCallbackPath()) ;
		})
		;
		
		conf0.configure(aHttp) ;
	}
}
