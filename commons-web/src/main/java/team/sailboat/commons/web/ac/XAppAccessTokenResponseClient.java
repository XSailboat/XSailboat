package team.sailboat.commons.web.ac;

import java.util.Base64;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.oauth2.client.endpoint.AbstractOAuth2AuthorizationGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

import com.nimbusds.jose.util.Base64URL;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.http.Request;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.text.XString;

public class XAppAccessTokenResponseClient implements OAuth2AccessTokenResponseClient<AbstractOAuth2AuthorizationGrantRequest>
{
	OAuthClientConf mClientConf ;
	
	public XAppAccessTokenResponseClient(OAuthClientConf aClientConf)
	{
		mClientConf = aClientConf ;
	}

	@Override
	public OAuth2AccessTokenResponse getTokenResponse(AbstractOAuth2AuthorizationGrantRequest aAuthorizationGrantRequest)
	{
		if(aAuthorizationGrantRequest instanceof CorsTokenGrantRequest corsReq)
		{
			//
			Request req = Request.POST().path(IAuthCenterConst.sGET_token)
					.queryParam("client_id", corsReq.getClientRegistration().getClientId())
					.queryParam("grant_type" , IAuthCenterConst.sGrantType_cork_token.getValue())
					.queryParam("token" , corsReq.getCorsToken())
					.queryParam("redirect_uri", corsReq.getClientRegistration().getRedirectUri()) ;		// 这个是必需的，它在此不起作用，只是因为它是/oauth2/token接口的必填参数
			try
			{
				JSONObject reply = (JSONObject)mClientConf.getAuthCenterClient().ask(req) ;
				// 等调用得到结果，认证服务器已经校验过签名了
				String sourceNote = new String(Base64.getUrlDecoder().decode(XString.lastSeg_i(corsReq.getCorsToken(), '.' , 1))
						, AppContext.sUTF8) ;
				JSONObject sourceNoteJo = JSONObject.of(sourceNote) ;
				String exceptReferer = sourceNoteJo.optString("referer") ;
				if(corsReq.getReferer() == null || !corsReq.getReferer().startsWith(exceptReferer))
				{
					throw new AuthenticationServiceException("不允许的调用源！") ;
				}
				
				String accessToken = reply.optString("access_token") ;
				String refreshToken = reply.optString("refresh_token") ;
				String payload = XString.seg_i(accessToken , '.' , 1) ;
				JSONObject payload_jobj = JSONObject.of(new String(Base64URL.from(payload).decode() , "UTF-8")) ;
				
				return OAuth2AccessTokenResponse.withToken(accessToken)
						.tokenType(TokenType.BEARER)
						.refreshToken(refreshToken)
						.expiresIn((int)(payload_jobj.optLong("exp") - payload_jobj.optLong("iat")))
						.build()
						;
				
						
			}
			catch(Exception e)
			{
			}
		}
		return null;
	}

}
