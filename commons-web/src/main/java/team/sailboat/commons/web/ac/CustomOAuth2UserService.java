package team.sailboat.commons.web.ac;

import java.io.UnsupportedEncodingException;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import team.sailboat.commons.fan.excep.WrapException;

/**
 * 
 * 自定义的UserService，将DefaultOAuth2User对象转成自定义的AuthUser_AC。		<br />
 * 从而能从AccessToken中加载权限
 *
 * @author yyl
 * @since 2024年12月5日
 */
public class CustomOAuth2UserService extends DefaultOAuth2UserService
{
	@Override
	public OAuth2User loadUser(OAuth2UserRequest aUserRequest) throws OAuth2AuthenticationException
	{
		try
		{
			return AuthUser_AC.from(super.loadUser(aUserRequest) , aUserRequest.getAccessToken().getTokenValue()) ;
		}
		catch (UnsupportedEncodingException e)
		{
			WrapException.wrapThrow(e) ;		// 基本不可能出的异常
			return null ;						// dead code
		}
	}
}
