package team.sailboat.ms.ac.conf;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import jakarta.annotation.PostConstruct;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.ms.ac.AppAuthenticationProvider;
import team.sailboat.commons.ms.ac.IApiPredicate;
import team.sailboat.commons.ms.xca.AppCertificateType;
import team.sailboat.commons.ms.xca.AppKeySecret;
import team.sailboat.commons.ms.xca.XAppSignChecker;
import team.sailboat.commons.web.ac.IAuthCenterConst;
import team.sailboat.ms.ac.AppConsts;
import team.sailboat.ms.ac.Jwks;
import team.sailboat.ms.ac.component.OAuth2AuthorizationCodeRequestAuthenticationProvider;
import team.sailboat.ms.ac.dbean.User;
import team.sailboat.ms.ac.oauth2server.AppSignAuthConverter;
import team.sailboat.ms.ac.server.IClientAppDataManager;
import team.sailboat.ms.ac.server.ResourceManageServer;

/**
 * 
 * 认证服务器的组件构建配置
 *
 * @author yyl
 * @since 2024年11月7日
 */
@Configuration
public class OAuthServerConf
{
	
	@Autowired
	ResourceManageServer mResMngServer ;
	
	final OAuth2AuthorizationServerConfigurer mAuthorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer() ;
	
	@PostConstruct
	void _init()
	{
		// 自己写授权确认页面
		mAuthorizationServerConfigurer.authorizationEndpoint((conf)->{
			conf.consentPage(AppConsts.sViewPath_consent) ;
		}) ;
	}
	
	@Bean
	IApiPredicate _apiPredicate()
	{
		return mResMngServer.getClientAppDataMng() ;
	}
	
	@Bean
	OAuth2AuthorizationServerConfigurer _oauth2AuthorizationServerConfigurer()
	{
		return mAuthorizationServerConfigurer ;
	}
	
	@Bean
	DaoAuthenticationProvider daoAuthenticationProvider()
	{
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(new BCryptPasswordEncoder());
        daoAuthenticationProvider.setUserDetailsService(mResMngServer.getUserDataMng()) ;
        return daoAuthenticationProvider;
	}
	
	@Bean
	public JWKSource<SecurityContext> jwkSource()
	{
		RSAKey rsaKey = Jwks.generateRsa();
		JWKSet jwkSet = new JWKSet(rsaKey);
		return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
	}

	@Bean
	public JwtDecoder jwtDecoder(JWKSource<SecurityContext> aJwkSource)
	{
		return OAuth2AuthorizationServerConfiguration.jwtDecoder(aJwkSource);
	}
	
	@Bean
	public JwtEncoder jwtEncoder(JWKSource<SecurityContext> aJwkSource)
	{
		return new NimbusJwtEncoder(aJwkSource) ;
	}
	
	@Bean
	OAuth2TokenGenerator<? extends OAuth2Token>  _tokenGenerator(JwtEncoder aJwtEncoder
			, OAuth2AuthorizationCodeRequestAuthenticationProvider aAuthCodePvd)
	{
		JwtGenerator tokenGenerator = new JwtGenerator(aJwtEncoder) ;
		tokenGenerator.setJwtCustomizer(new AccessTokenCustomizer(aAuthCodePvd)) ;
		return tokenGenerator ;
	}
	
	class AccessTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext>
	{
		final Logger mLogger = LoggerFactory.getLogger(getClass()) ;

		OAuth2AuthorizationCodeRequestAuthenticationProvider mAuthCodePvd ;
		
		AccessTokenCustomizer(OAuth2AuthorizationCodeRequestAuthenticationProvider aAuthCodePvd)
		{
			mAuthCodePvd = aAuthCodePvd ;
		}
		
		@Override
		public void customize(JwtEncodingContext aContext)
		{
			Authentication auth = (Authentication)aContext.getPrincipal() ;
			User user = (User)auth.getPrincipal() ;
			Collection<String> authCodes = mResMngServer.getClientAppDataMng()
					.getAuthorityCodesOfUserInClientApp(user.getId() , aContext.getRegisteredClient().getId()) ;
			OAuth2AuthorizationCode corsToken = mAuthCodePvd.createAuthorizationCode(120) ;
			aContext.getClaims().claim("auths" , authCodes)
					.claim("userId" , user.getId())				// 用户id
					.claim(IAuthCenterConst.sTokenReply_corsToken , corsToken.getTokenValue());
		
			// 创建跨域访问的授权码
			OAuth2Authorization auth0 = aContext.get(OAuth2Authorization.class) ;
			OAuth2Authorization auth1 = OAuth2Authorization.from(auth0)
				.token(corsToken)
				.id(UUID.randomUUID().toString())
				.build() ;
			mLogger.info("发出token：{}" , corsToken.getTokenValue()) ;
			mAuthCodePvd.getAuthorizationService().save(auth1);
		}
	}
	
	@Bean
	AppAuthenticationProvider _appAuthenticationProvider()
	{
		return (appCertificate)->{
			Assert.isTrue(appCertificate.getType() == AppCertificateType.AppKeySecret
					, "目前紧支持AppKey-AppSecret模式！");
			AppKeySecret aks = (AppKeySecret)appCertificate ;
			OAuth2ClientAuthenticationToken token = new OAuth2ClientAuthenticationToken(aks.getAppKey()
					, ClientAuthenticationMethod.CLIENT_SECRET_BASIC
					, aks.getAppSecret()
					, Collections.singletonMap("converter", AppSignAuthConverter.sCUM_AppSecretSign)) ;
			token.setDetails(aks.getAppId()) ;
			return token ;
		} ;
	}
	
	@Bean
	public AuthenticationEventPublisher _authenticationEventPublisher(ApplicationEventPublisher applicationEventPublisher)
	{
	    return new DefaultAuthenticationEventPublisher(applicationEventPublisher);
	}
	
	@Bean
	OAuth2AuthorizationService _authorizationService()
	{
		return new InMemoryOAuth2AuthorizationService() ;
	}
	
	@Bean
	XAppSignChecker _appSignChecker()
	{
		final IClientAppDataManager clientAppDataMng = mResMngServer.getClientAppDataMng() ;
		return new XAppSignChecker((appKey , httpReq)->{
			RequestMappingHandlerMapping handlerMapping = clientAppDataMng.getInvokableApiMapping(appKey) ;
			try
			{
				return handlerMapping != null && handlerMapping.getHandler(httpReq) != null ;
			}
			catch (Exception e)
			{
				WrapException.wrapThrow(e) ;
				return false ;
			}
		}, clientAppDataMng::getClientAppByAppKey) ;
	}
}
