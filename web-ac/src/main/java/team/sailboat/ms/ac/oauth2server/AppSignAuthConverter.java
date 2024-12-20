package team.sailboat.ms.ac.oauth2server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.web.authentication.AuthenticationConverter;

import jakarta.servlet.http.HttpServletRequest;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.http.xca.XAppSigner;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.ms.ac.AppAuthenticationProvider;
import team.sailboat.commons.ms.xca.IAppSignChecker;

public class AppSignAuthConverter implements AuthenticationConverter
{
	public static final ClientAuthenticationMethod sCUM_AppSecretSign = new ClientAuthenticationMethod("APP_SECRET_SIGN") ;
	static final Logger sLogger = LoggerFactory.getLogger(AppSignAuthConverter.class) ; 
	
	final IAppSignChecker mAppSignChecker ;
	AppAuthenticationProvider mAppAuthPvd ;
	
	
	public AppSignAuthConverter(IAppSignChecker aAppSignChecker , AppAuthenticationProvider aAppAuthPvd)
	{
		mAppSignChecker = aAppSignChecker ;
		mAppAuthPvd = aAppAuthPvd ;
	}

	@Override
	public Authentication convert(HttpServletRequest aRequest)
	{
		String appKey = aRequest.getHeader(XAppSigner.X_CA_KEY) ;
		if(XString.isNotEmpty(appKey))
		{
			aRequest.setAttribute("AppSigneChecked", true) ;
			try
			{
				return mAppAuthPvd.apply(mAppSignChecker.check(aRequest)) ;
			}
			catch (Exception e)
			{
				sLogger.error(ExceptionAssist.getClearMessage(getClass(), e)) ;
				throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST));
			}
		}
		return null;
	}
}
