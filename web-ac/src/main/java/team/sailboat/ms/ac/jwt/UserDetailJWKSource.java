package team.sailboat.ms.ac.jwt;

import java.util.List;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWKSecurityContext;
import com.nimbusds.jose.proc.SecurityContext;

public class UserDetailJWKSource implements JWKSource<JWKSecurityContext>
{

	@Override
	public List<JWK> get(JWKSelector aJwkSelector, JWKSecurityContext aContext) throws KeySourceException
	{
		
		return null;
	}

}
