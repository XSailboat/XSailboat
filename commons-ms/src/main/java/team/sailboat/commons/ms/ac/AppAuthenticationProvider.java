package team.sailboat.commons.ms.ac;

import org.springframework.security.core.Authentication;

import team.sailboat.commons.ms.xca.AppCertificate;

@FunctionalInterface
public interface AppAuthenticationProvider
{	
	Authentication apply(AppCertificate aCertificate) ;
}
