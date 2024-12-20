package team.sailboat.commons.web.ac;

import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * 
 * Cors登录过滤器的配置工具
 *
 * @author yyl
 * @since 2024年12月10日
 */
public class CorsTokenLoginFilterConfigurer <B extends HttpSecurityBuilder<B>>
		extends AbstractAuthenticationFilterConfigurer<B, CorsTokenLoginFilterConfigurer<B>, CorsTokenLoginFilter>
{
	OAuthClientConf mClientConf ;
	
	public CorsTokenLoginFilterConfigurer(CorsTokenLoginFilter aAuthFilter , OAuthClientConf aClientConf)
	{
		super(aAuthFilter , null) ;
		mClientConf = aClientConf ;
	}

	@Override
	protected RequestMatcher createLoginProcessingUrlMatcher(String aLoginProcessingUrl)
	{
		return new RequestHeaderRequestMatcher("cors-token-auth") ;
	}

}
