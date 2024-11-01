package team.sailboat.commons.ms.security;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import team.sailboat.commons.fan.text.XString;

public class SubspacePermissionEvaluator implements PermissionEvaluator
{
	
	static Logger sLogger = LoggerFactory.getLogger(SubspacePermissionEvaluator.class) ;

	@Override
	public boolean hasPermission(Authentication aAuthentication, Object aTargetDomainObject, Object aPermission)
	{
		return aAuthentication.getAuthorities().contains(new SimpleGrantedAuthority(
				XString.splice(aTargetDomainObject , ":" , aPermission))) ;
	}

	@Override
	public boolean hasPermission(Authentication aAuthentication,
			Serializable aTargetId,
			String aTargetType,
			Object aPermission)
	{
		sLogger.warn("未实现此方法，拒绝所有权限判定！");
		return false ;
	}

}
