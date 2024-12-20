package team.sailboat.ms.ac.oauth2server;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.http.ISigner;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;

public class CorsTokenAuthenticationProvider implements AuthenticationProvider
{
	static final Logger sLogger = LoggerFactory.getLogger(CorsTokenAuthenticationProvider.class) ;
	
	static final OAuth2TokenType AUTHORIZATION_CODE_TOKEN_TYPE = new OAuth2TokenType(OAuth2ParameterNames.CODE);

	static final StringKeyGenerator TOKEN_GENERATOR = new Base64StringKeyGenerator(
			Base64.getUrlEncoder().withoutPadding(),
			96);

	final OAuth2AuthorizationService mAuthorizationService;
	final RegisteredClientRepository mRegisteredClientRepository;
	final Function<String , String> mClientSecretPvd ;
	OAuth2TokenGenerator<OAuth2AccessToken> mAccessTokenGenerator = null ;
//	private ProviderSettings providerSettings;

	public CorsTokenAuthenticationProvider(RegisteredClientRepository aRegisteredClientRepository
			, OAuth2AuthorizationService aAuthorizationService
			, Function<String , String> aClientSecretPvd)
	{
		Assert.notNull(aAuthorizationService, "authorizationService cannot be null");
		mRegisteredClientRepository = aRegisteredClientRepository ;
		mAuthorizationService = aAuthorizationService;
		mClientSecretPvd = aClientSecretPvd ;
	}
	
	public final void setAccessTokenGenerator(OAuth2TokenGenerator<OAuth2AccessToken> aTokenGenerator)
	{
		Assert.notNull(aTokenGenerator , "OAuth2AccessTokenGenrator为null!");
		mAccessTokenGenerator = aTokenGenerator ;
	}

//	@Autowired(required = false)
//	protected void setProviderSettings(ProviderSettings providerSettings)
//	{
//		this.providerSettings = providerSettings;
//	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException
	{
		CorsToken token = (CorsToken) authentication;

		OAuth2ClientAuthenticationToken clientPrincipal = getAuthenticatedClientElseThrowInvalidClient(
				token);
		RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();

		OAuth2Authorization tokenSourceClientAuth = mAuthorizationService.findByToken(token.getToken()
				, AUTHORIZATION_CODE_TOKEN_TYPE);
		if (tokenSourceClientAuth == null)
		{
			throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT , "无法通过token找到认证信息"
					, null));
		}
		OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationCode = tokenSourceClientAuth.getToken(
				OAuth2AuthorizationCode.class) ;
		if (!authorizationCode.isActive())
		{
			throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT , "认证信息已经失效" , null));
		}
		
		// 验证签名
		if(XString.isNotEmpty(token.getSignature()))
		{
			RegisteredClient tokenSourceClient = mRegisteredClientRepository.findById(tokenSourceClientAuth.getRegisteredClientId()) ;
			try
			{
				String signature = ISigner.signForUrlNoPadding(token.getSignText() , ISigner.sSignAlg_HmacSHA256 , mClientSecretPvd.apply(tokenSourceClient.getId())) ;
				if(JCommon.unequals(token.getSignature() , signature))
				{
					sLogger.error("签名验证不通过！") ;
					throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT));
				}
			}
			catch (InvalidKeyException | NoSuchAlgorithmException | IllegalStateException
					| UnsupportedEncodingException e)
			{
				sLogger.info(ExceptionAssist.getClearMessage(getClass(), e)) ;
			}
		}

//		String issuer = this.providerSettings != null ? this.providerSettings.issuer() : null;
		Set<String> authorizedScopes = tokenSourceClientAuth.getAuthorizedScopes() ;

		JwsHeader.Builder headersBuilder = headers();
		JwtClaimsSet.Builder claimsBuilder = accessTokenClaims(
				registeredClient,
				"sailboat.org" ,
				tokenSourceClientAuth.getPrincipalName(),
				authorizedScopes);

		// @formatter:off
		JwtEncodingContext context = JwtEncodingContext.with(headersBuilder, claimsBuilder)
				.registeredClient(registeredClient)
				.principal(tokenSourceClientAuth.getAttribute(Principal.class.getName()))
				.authorization(tokenSourceClientAuth)
				.authorizedScopes(authorizedScopes)
				.tokenType(OAuth2TokenType.ACCESS_TOKEN)
				.authorizationGrantType(CorsToken.sGrantType_CorsToken)
				.authorizationGrant(token)
				.build();
		// @formatter:on
		
		OAuth2AccessToken accessToken = mAccessTokenGenerator.generate(context) ;

		OAuth2RefreshToken refreshToken = null;
		if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN))
		{
			refreshToken = generateRefreshToken(
					registeredClient.getTokenSettings().getRefreshTokenTimeToLive());
		}
		return new OAuth2AccessTokenAuthenticationToken(
				registeredClient,
				clientPrincipal,
				accessToken,
				refreshToken);
	}

	@Override
	public boolean supports(Class<?> authentication)
	{
		return CorsToken.class.isAssignableFrom(authentication);
	}

	static OAuth2ClientAuthenticationToken getAuthenticatedClientElseThrowInvalidClient(Authentication authentication)
	{
		OAuth2ClientAuthenticationToken clientPrincipal = null;
		if (OAuth2ClientAuthenticationToken.class.isAssignableFrom(authentication.getPrincipal().getClass()))
		{
			clientPrincipal = (OAuth2ClientAuthenticationToken) authentication.getPrincipal();
		}
		if (clientPrincipal != null && clientPrincipal.isAuthenticated())
		{
			return clientPrincipal;
		}
		throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT));
	}

	static OAuth2RefreshToken generateRefreshToken(Duration tokenTimeToLive)
	{
		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(tokenTimeToLive);
		return new OAuth2RefreshToken(TOKEN_GENERATOR.generateKey(), issuedAt, expiresAt);
	}

	static JwsHeader.Builder headers()
	{
		return JwsHeader.with(SignatureAlgorithm.RS256);
	}

	static JwtClaimsSet.Builder accessTokenClaims(RegisteredClient registeredClient,
			String issuer,
			String subject,
			Set<String> authorizedScopes)
	{

		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(registeredClient.getTokenSettings().getAccessTokenTimeToLive());

		// @formatter:off
		JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder();
		if (StringUtils.hasText(issuer)) {
			claimsBuilder.issuer(issuer);
		}
		claimsBuilder
				.subject(subject)
				.audience(Collections.singletonList(registeredClient.getClientId()))
				.issuedAt(issuedAt)
				.expiresAt(expiresAt)
				.notBefore(issuedAt);
		if (!CollectionUtils.isEmpty(authorizedScopes)) {
			claimsBuilder.claim(OAuth2ParameterNames.SCOPE, authorizedScopes);
		}
		// @formatter:on

		return claimsBuilder;
	}

}
