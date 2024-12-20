package team.sailboat.ms.ac.server;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import team.sailboat.commons.fan.collection.XC;

/**
 * 
 * 获得指定用户在指定ClientApp中所拥有的权限
 *
 * @author yyl
 * @since 2024年10月30日
 */
public interface IUserAuthsProvider
{
	/**
	 * 
	 * 获得指定用户在指定ClientApp中所拥有的权限
	 * 
	 * @param aUserId
	 * @param aClientAppId
	 * @return
	 */
	default Collection<GrantedAuthority> getAuthoritysOfUserInClientApp(String aUserId , String aClientAppId)
	{
		return XC.extractAsHashSet(getAuthorityCodesOfUserInClientApp(aUserId, aClientAppId)
				, SimpleGrantedAuthority::new) ;
	}
	
	Collection<String> getAuthorityCodesOfUserInClientApp(String aUserId , String aClientAppId) ;
}
