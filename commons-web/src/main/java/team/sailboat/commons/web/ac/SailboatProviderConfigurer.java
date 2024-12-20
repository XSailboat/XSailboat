package team.sailboat.commons.web.ac;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties.Provider;
import org.springframework.stereotype.Component;

@Component
public class SailboatProviderConfigurer implements BeanPostProcessor
{
	
	@Override
	public Object postProcessAfterInitialization(Object aBean, String aBeanName) throws BeansException
	{
		if(aBean instanceof OAuth2ClientProperties clientProps)
		{
			Provider pvd = clientProps.getProvider().get(IAuthCenterConst.sClientResitrationId) ;
			if(pvd == null)
			{
				pvd = new Provider() ;
				// 下面提供的地址是无意义的，只是为了占位不报错，OAuthClientConf里面会重写
				pvd.setAuthorizationUri("http://localhost:12000/SailAC/oauth2/authorize") ;
				pvd.setTokenUri("http://localhost:12000/SailAC/oauth2/token") ;
				pvd.setUserInfoUri("http://localhost:12000/SailAC/oauth2/user/info") ;
				pvd.setUserNameAttribute("displayName") ;
				clientProps.getProvider().put(IAuthCenterConst.sClientResitrationId , pvd) ;
			}
		}
		return aBean ;
	}
}
