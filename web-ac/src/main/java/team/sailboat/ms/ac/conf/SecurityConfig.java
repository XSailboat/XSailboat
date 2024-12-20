package team.sailboat.ms.ac.conf ;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.web.OAuth2ClientAuthenticationFilter;
import org.springframework.security.oauth2.server.authorization.web.OAuth2TokenEndpointFilter;
import org.springframework.security.oauth2.server.authorization.web.authentication.ClientSecretBasicAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.ClientSecretPostAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.DelegatingAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AuthorizationCodeAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2ClientCredentialsAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2RefreshTokenAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.Filter;
import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dpa.DRepository;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.ms.ACKeys_Common;
import team.sailboat.commons.ms.ac.ApiFinder4Controller;
import team.sailboat.commons.ms.ac.AppAuthenticationProvider;
import team.sailboat.commons.ms.ac.AppSignCheckerFilter;
import team.sailboat.commons.ms.ac.InnerProtectedApi;
import team.sailboat.commons.ms.crypto.RSAKeyPairMaker4JS;
import team.sailboat.commons.ms.xca.IAppSignChecker;
import team.sailboat.ms.ac.AppConfig;
import team.sailboat.ms.ac.AppConsts;
import team.sailboat.ms.ac.component.LoginUserRegisterRepo;
import team.sailboat.ms.ac.component.OAuth2AuthorizationCodeRequestAuthenticationProvider;
import team.sailboat.ms.ac.component.UserAuthoritiesChangeMonitor;
import team.sailboat.ms.ac.dbean.ClientApp;
import team.sailboat.ms.ac.filter.PasswordDecorderFilter;
import team.sailboat.ms.ac.filter.ResetExpiredPasswdFilter;
import team.sailboat.ms.ac.frame.CustomAuthenticationFailureHandler;
import team.sailboat.ms.ac.frame.CustomAuthenticationSuccessHandler;
import team.sailboat.ms.ac.oauth2server.AppSignAuthConverter;
import team.sailboat.ms.ac.oauth2server.CorsTokenAuthenticationConverter;
import team.sailboat.ms.ac.oauth2server.CorsTokenAuthenticationProvider;
import team.sailboat.ms.ac.plugin.LoginComponentProvider;
import team.sailboat.ms.ac.utils.LoginFailStore;

@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Configuration(proxyBeanMethods = false)
public class SecurityConfig
{
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ; 
	
	@Autowired
	AppConfig mAppConfig ;
	
	@Autowired
	RSAKeyPairMaker4JS mRSAMaker ;
	
	@Autowired
	DRepository mRepo ;
	
	@Qualifier("resetPasswdUsernames")
	@Autowired
	Map<String , String> mResetPasswdUserNames ;
	
	@Autowired
	LoginFailStore mLoginFailStore ;
	
	@Autowired
	OAuth2AuthorizationServerConfigurer mAuthorizationServerConfigurer ;
	
	@Autowired
	OAuth2AuthorizationCodeRequestAuthenticationProvider mAuthCodePvd ;
	
	/**
	 * 用户名、密码校验的认证检查提供器
	 */
	@Autowired
	DaoAuthenticationProvider mUserPswdAuthPvd ;
	
	/**
	 * 这些路径下的资源或API不用登录就能访问。	<br />
	 * 要么是公开的资源，要么是内部已经做了安全检查的
	 */
	String[] mOpenApiPaths ;
	
	@PostConstruct
	void _init()
	{
		String[] pkgs = (String[])AppContext.get(ACKeys_Common.sControllerPackages) ;
		Set<String> openApiPaths  = ApiFinder4Controller.getApiPaths(InnerProtectedApi.class , pkgs) ;
		XC.addAll(openApiPaths , "/"
				, "/foreign/**"
				, "/index"
				, "/error"
				, "/error_view"
				, "/dingLogin"
				, AppConsts.sViewPath_login					// 登陆页面
				, "/pwd_reset"
				, "/creteNewAccount/ofDing"				/*接口内部有安全限制*/
				, "/bindAccount/ding"					/*接口内部有安全限制*/
				, "/assets/**"
				, "/public/**") ;
		mOpenApiPaths = openApiPaths.toArray(JCommon.sEmptyStringArray) ;
	}
	
	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity aHttp 
			, RegisteredClientRepository registeredClientRepository
			, OAuth2AuthorizationConsentService authorizationConsentService
			, LoginUserRegisterRepo aLoginUserRegisterRepo
			, AuthenticationEventPublisher aEventPublisher
			, OAuth2AuthorizationService aAuthorizationService
			, IAppSignChecker aAppSignChecker
			, AppAuthenticationProvider aAppAuthPvd
			, UserAuthoritiesChangeMonitor aAuthsChangeLsn) throws Exception
	{
		String defaultSuccessUrl = "/manage" ;
		aHttp.addFilterBefore(new PasswordDecorderFilter(AppConsts.sApiPath_login
				, mRSAMaker
				, mLoginFailStore)
				, UsernamePasswordAuthenticationFilter.class) ;
		new LoginComponentProvider().injectLoginFilters(aHttp , defaultSuccessUrl) ;
		aHttp.addFilterBefore(new AppSignCheckerFilter(aAppSignChecker , aAppAuthPvd) , PasswordDecorderFilter.class) ;
//		aHttp.addFilterBefore(new ValidateCodeFilter(), PasswordDecorderFilter.class);
		aHttp.addFilterBefore(new ResetExpiredPasswdFilter(mResetPasswdUserNames) , AppSignCheckerFilter.class) ;
		
		RequestMatcher endpointsMatcher = mAuthorizationServerConfigurer
				.getEndpointsMatcher();
		CorsTokenAuthenticationProvider corsTokenPvd = new CorsTokenAuthenticationProvider(registeredClientRepository
				, aAuthorizationService
				, (id)->{
					ClientApp app = mRepo.getByBid(ClientApp.class , id) ;
					return app ==null?null:app.getAppSecret() ;
				}) ;
		mAuthorizationServerConfigurer.authorizationEndpoint((conf)->{
			conf.authenticationProvider(mUserPswdAuthPvd) ;
			conf.authenticationProvider(mAuthCodePvd) ;
			conf.authenticationProvider(corsTokenPvd) ;
			aHttp.setSharedObject(OAuth2AuthorizationService.class, aAuthorizationService) ;
		}) ;
		aHttp.authorizeHttpRequests(reg->{
			reg.requestMatchers(mOpenApiPaths)
				.permitAll()
				.anyRequest()
				.authenticated() ;
			})
			.csrf(csrf -> csrf.ignoringRequestMatchers("/public/**" , "/foreign/**" , "/token/cors")
					.ignoringRequestMatchers(endpointsMatcher))
			.oauth2ResourceServer(conf->{
				conf.jwt(jwtConf->{
					
				}) ;
			})
			.apply(mAuthorizationServerConfigurer)
			;
		
		CustomAuthenticationFailureHandler failureHandler = new CustomAuthenticationFailureHandler(mResetPasswdUserNames
				, mLoginFailStore) ;
		failureHandler.setDefaultFailureUrl(AppConsts.sViewPath_loginFailure) ;
		
		SecurityFilterChain chain = aHttp
				.sessionManagement(conf->{
					conf.sessionCreationPolicy(SessionCreationPolicy.ALWAYS) ;
				})
				.formLogin(conf->{
					conf.loginPage(AppConsts.sViewPath_login)
						.loginProcessingUrl(AppConsts.sApiPath_login)
						.defaultSuccessUrl(defaultSuccessUrl)	
						.failureHandler(failureHandler)
						.successHandler(new CustomAuthenticationSuccessHandler(mLoginFailStore)) ;
				})
				.logout(conf->{
					conf.logoutUrl("/logout")
						// 需要给各个用此账户登陆的用户客户端发送登出通知
//						.logoutSuccessHandler(null)
						.invalidateHttpSession(true)
						.logoutSuccessUrl(AppConsts.sViewPath_login)
						.logoutSuccessHandler(aAuthsChangeLsn)
						.deleteCookies("JSESSIONID")
						.logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
//						.permitAll()
						;
				})
				.build() ;
		
		@SuppressWarnings("unchecked")
		OAuth2TokenGenerator<OAuth2AccessToken> accessTokenGenerator = aHttp.getSharedObject(OAuth2TokenGenerator.class) ; 
		corsTokenPvd.setAccessTokenGenerator(accessTokenGenerator) ;
		
		List<Filter> filters =  chain.getFilters() ;
		if(XC.isNotEmpty(filters))
		{
			for(Filter filter : filters)
			{
				if(filter instanceof OAuth2ClientAuthenticationFilter)
				{
					OAuth2ClientAuthenticationFilter clientFilter = (OAuth2ClientAuthenticationFilter)filter ;
					AuthenticationConverter cvt = new DelegatingAuthenticationConverter(
							Arrays.asList(
									new AppSignAuthConverter(aAppSignChecker , aAppAuthPvd)
									, new ClientSecretBasicAuthenticationConverter()
									, new ClientSecretPostAuthenticationConverter()
//									, new PublicClientAuthenticationConverter()
									)) ;
				
					clientFilter.setAuthenticationConverter(cvt) ;
				}
				else if(filter instanceof OAuth2TokenEndpointFilter)
				{
					OAuth2TokenEndpointFilter tfilter = (OAuth2TokenEndpointFilter)filter ;
					tfilter.setAuthenticationConverter(new DelegatingAuthenticationConverter(
							Arrays.asList(new OAuth2AuthorizationCodeAuthenticationConverter(),
									new OAuth2RefreshTokenAuthenticationConverter(),
									new OAuth2ClientCredentialsAuthenticationConverter()
								, new CorsTokenAuthenticationConverter()))) ;
				}
			}
		}
		ProviderManager mng = (ProviderManager)aHttp.getSharedObject(AuthenticationManager.class) ;
		mng.setAuthenticationEventPublisher(aEventPublisher) ;
		
		return chain ;
	}	
}
