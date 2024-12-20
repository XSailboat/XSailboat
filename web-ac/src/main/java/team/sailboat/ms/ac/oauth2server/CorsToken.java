package team.sailboat.ms.ac.oauth2server;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;
import org.springframework.util.Assert;

public class CorsToken extends OAuth2AuthorizationGrantAuthenticationToken
{
	
	private static final long serialVersionUID = 1L;

	public static AuthorizationGrantType sGrantType_CorsToken = new AuthorizationGrantType("cors_token") ;
	
	private final String token ;
	
	String signText ;
	
	String signature ;

	public CorsToken(String aToken , String aSignText , String aSignature
			, Authentication clientPrincipal
			, Map<String, Object> additionalParameters)
	{
		super(sGrantType_CorsToken , clientPrincipal, additionalParameters);
		Assert.hasText(aToken , "code cannot be empty") ;
		this.token = aToken ;
		signature = aSignature ;
		signText = aSignText ;
	}

	/**
	 * Returns the authorization code.
	 *
	 * @return the authorization code
	 */
	public String getToken()
	{
		return token ;
	}
	
	public String getSignature()
	{
		return signature;
	}
	
	public String getSignText()
	{
		return signText;
	}
}
