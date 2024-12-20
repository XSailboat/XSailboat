package team.sailboat.ms.ac.server;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

/**
 * 
 * 获得指定用户在认证中心所拥有的权限
 *
 * @author yyl
 * @since 2024年10月30日
 */
@FunctionalInterface
public interface IUserAuthsProviderInAuthCenter
{
	Collection<GrantedAuthority> getAuthoritysOfUserInClientApp(String aUserId) ;
}
