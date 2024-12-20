package team.sailboat.ms.ac.oauth2server;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import team.sailboat.commons.fan.text.XString;

public class CorsTokenAuthenticationConverter implements AuthenticationConverter
{

	@Nullable
	@Override
	public Authentication convert(HttpServletRequest request)
	{
		// grant_type (REQUIRED)
		String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
		if (!CorsToken.sGrantType_CorsToken.getValue().equals(grantType))
		{
			return null;
		}

		Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

		MultiValueMap<String, String> parameters = getParameters(request);

		// code (REQUIRED)
		String corsToken = parameters.getFirst("token");
		if (!StringUtils.hasText(corsToken)
				|| parameters.get("token").size() != 1
				// 先注释掉，等都更新之后去除注释
//				|| XString.count(corsToken, '.', 0) < 2
				)
		{
			throwError(OAuth2ErrorCodes.INVALID_REQUEST
					, OAuth2ParameterNames.CODE
					, "CorsToken方式登录的相关说明文档");
		}
		
		// 
		String signature = XString.lastSeg_i(corsToken , '.', 0) ;
		String signText = XString.substringLeft(corsToken, '.' , false, 0) ;
		String corsToken1 = XString.substringLeft(corsToken, '.', false, 1) ;
		corsToken = corsToken1==null?corsToken:corsToken1 ;

		// @formatter:off
		Map<String, Object> additionalParameters = parameters
				.entrySet()
				.stream()
				.filter(e -> !e.getKey().equals(OAuth2ParameterNames.GRANT_TYPE) &&
						!e.getKey().equals(OAuth2ParameterNames.CLIENT_ID) &&
						!e.getKey().equals("token"))
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0)));
        // @formatter:on

		return new CorsToken(corsToken , signText , signature
				, clientPrincipal, additionalParameters);
	}

	static MultiValueMap<String, String> getParameters(HttpServletRequest request)
	{
		Map<String, String[]> parameterMap = request.getParameterMap();
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>(parameterMap.size());
		parameterMap.forEach((key, values) -> {
			if (values.length > 0)
			{
				for (String value : values)
				{
					parameters.add(key, value);
				}
			}
		});
		return parameters;
	}

	static void throwError(String errorCode, String parameterName, String errorUri)
	{
		OAuth2Error error = new OAuth2Error(errorCode, "OAuth 2.0 Parameter: " + parameterName, errorUri);
		throw new OAuth2AuthenticationException(error);
	}
}
