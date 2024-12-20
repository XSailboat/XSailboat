package team.sailboat.commons.web.ac;

import java.time.Instant;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 
 *
 * @author yyl
 * @since 2024年12月10日
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CoupleAuthenticationToken extends OAuth2AuthenticationToken
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Instant issueTime ;
	Instant expiredTime ;
	
	boolean forceExpired = false ;
	
	public CoupleAuthenticationToken(AuthUser_AC aUser
			, String aAuthorizedClientRegistrationId
			, Instant aIssueTime , Instant aExpiredTime)
	{
		super(aUser , null , aAuthorizedClientRegistrationId) ;
		
		issueTime = aIssueTime ;
		expiredTime = aExpiredTime ;
	}
	
	public boolean isExpired()
	{
		return forceExpired || (expiredTime != null && expiredTime.isBefore(Instant.now())) ;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Collection<GrantedAuthority> getAuthorities()
	{
		return (Collection<GrantedAuthority>) ((AuthUser_AC)getPrincipal()).getAuthorities() ;
	}
	

}
