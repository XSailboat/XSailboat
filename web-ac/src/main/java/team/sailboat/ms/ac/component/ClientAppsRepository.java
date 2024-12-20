package team.sailboat.ms.ac.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;

import team.sailboat.ms.ac.dbean.ClientApp;
import team.sailboat.ms.ac.server.ResourceManageServer;

/**
 * 
 * org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository的实现，
 * 它是一个桥接器，主要逻辑都在ResourceManageServer中
 *
 * @author yyl
 * @since 2024年11月7日
 */
@Component
public class ClientAppsRepository implements RegisteredClientRepository
{
	
	@Autowired
	ResourceManageServer mResMngServer ;
	
	@Autowired
	PasswordEncoder mPasswordEncoder ;
	
	@Value("${server.servlet.session.timeout}")
	int mSessionTimeToLive ;
	
	
	public ClientAppsRepository()
	{
	}

	@Override
	public void save(RegisteredClient aRegisteredClient)
	{
	}

	@Override
	public RegisteredClient findById(String aId)
	{
		ClientApp clientApp = mResMngServer.getClientAppDataMng().getClientApp(aId) ;
		return clientApp != null?clientApp.getSecurityClient(mPasswordEncoder 
				, mSessionTimeToLive/2) : null ;
	}

	@Override
	public RegisteredClient findByClientId(String aClientId)
	{
		ClientApp clientApp = mResMngServer.getClientAppDataMng().getClientAppByAppKey(aClientId) ;
		return clientApp != null?clientApp.getSecurityClient(mPasswordEncoder 
				, mSessionTimeToLive/2) : null ;
	}

}
