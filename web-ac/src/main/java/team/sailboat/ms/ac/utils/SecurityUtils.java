package team.sailboat.ms.ac.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;

import team.sailboat.ms.ac.dbean.User;

/**
 * 安全控制相关的工具
 *
 * @author yyl
 * @since 2024年12月11日
 */
public class SecurityUtils
{
	static final String sUN_anonymousUser = "anonymousUser" ;
	
	public static boolean isActive(UserDetails aUser)
	{
		return aUser != null && aUser.isAccountNonExpired() && aUser.isAccountNonLocked()
				&& aUser.isCredentialsNonExpired() && aUser.isEnabled() ;
	}
	
	/**
	 * 
	 * 是否是匿名用户
	 * 
	 * @param aPrincipal
	 * @return
	 */
	public static boolean isAnonymousUser(Object aPrincipal)
	{
		return aPrincipal == null || sUN_anonymousUser.equals(aPrincipal) ;
	}
	
	public static User checkUser()
	{
		Object principal =  SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(isAnonymousUser(principal))
		{
			throw new OAuth2AuthenticationException(OAuth2ErrorCodes.ACCESS_DENIED) ;
		}
		return (User)principal ;
	}
}
