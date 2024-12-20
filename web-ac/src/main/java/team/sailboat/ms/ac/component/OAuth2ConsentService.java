package team.sailboat.ms.ac.component;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.stereotype.Component;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.ms.ac.dbean.ClientApp;
import team.sailboat.ms.ac.dbean.User;
import team.sailboat.ms.ac.server.IClientAppDataManager;
import team.sailboat.ms.ac.server.IUserDataManager;
import team.sailboat.ms.ac.server.ResourceManageServer;

@Component
public class OAuth2ConsentService implements OAuth2AuthorizationConsentService
{
	@Autowired
	ResourceManageServer mResMngServer ;
	

	@Override
	public void save(OAuth2AuthorizationConsent aAuthorizationConsent)
	{
		String appId = aAuthorizationConsent.getRegisteredClientId() ;
		IUserDataManager userDataMng = mResMngServer.getUserDataMng() ;
		User user = userDataMng.loadUserByUsername(aAuthorizationConsent.getPrincipalName()) ;
		userDataMng.recordConsentScopes(user.getId(), appId, aAuthorizationConsent.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority).map((scope)->scope.replaceFirst("SCOPE_", "")).collect(Collectors.toList())) ;
	}

	@Override
	public void remove(OAuth2AuthorizationConsent aAuthorizationConsent)
	{
		String appId = aAuthorizationConsent.getRegisteredClientId() ;
		IUserDataManager userDataMng = mResMngServer.getUserDataMng() ;
		User user = userDataMng.loadUserByUsername(aAuthorizationConsent.getPrincipalName()) ;
		userDataMng.recordConsentScopes(user.getId(), appId, null) ;
	}

	@Override
	public OAuth2AuthorizationConsent findById(String aRegisteredClientId, String aPrincipalName)
	{
		if(XString.isEmpty(aRegisteredClientId))
			return null ;
		IClientAppDataManager clientAppDataMng = mResMngServer.getClientAppDataMng() ;
		ClientApp app = clientAppDataMng.getClientApp(aRegisteredClientId) ;
		if(XString.isEmpty(aPrincipalName))
			return null ;
		IUserDataManager userDataMng = mResMngServer.getUserDataMng() ;
		User user = userDataMng.loadUserByUsername(aPrincipalName) ;
		String[] scopes = userDataMng.getScopesOfUserConsent(user.getId() , app.getId()) ;
		if(XC.isNotEmpty(scopes))
		{
			
			return OAuth2AuthorizationConsent.withId(aRegisteredClientId, aPrincipalName)
					.authorities((authSet)->{
						for(String scope : scopes)
						{
							authSet.add(new SimpleGrantedAuthority("SCOPE_" + scope)) ;
						}
					})
					.build();
		}
		return null ;
	}

}
