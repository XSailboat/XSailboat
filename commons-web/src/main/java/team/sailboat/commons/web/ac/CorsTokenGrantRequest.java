package team.sailboat.commons.web.ac;

import org.springframework.security.oauth2.client.endpoint.AbstractOAuth2AuthorizationGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 
 * CORS授权请求信息
 *
 * @author yyl
 * @since 2024年12月10日
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CorsTokenGrantRequest extends AbstractOAuth2AuthorizationGrantRequest
{
	String corsToken ;
	
	String referer ;

	public CorsTokenGrantRequest(ClientRegistration aClientRegistration
			, String aCorsToken
			, String aReferer)
	{
		super(IAuthCenterConst.sGrantType_cork_token , aClientRegistration);
		corsToken = aCorsToken ;
		referer = aReferer ;
	}

}
