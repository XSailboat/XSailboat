package team.sailboat.commons.web.ac;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.http.ISigner;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.text.XString;

/**
 * 
 * CorsToken签名工具
 *
 * @author yyl
 * @since 2024年12月10日
 */
public class CorsTokenSignHelper
{
	public static String signCorsToken(String aCorsToken , StringBuffer aRequestUrl
			, String aAppSecret) throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException
	{
		if(XString.isNotEmpty(aCorsToken))
		{
			String corsToken = aCorsToken ;
			// 加上Referer
			StringBuffer strBuf = aRequestUrl ;
			int idx = strBuf.indexOf("/", 8) ;
			String referer = strBuf.substring(0 , idx == -1?strBuf.length():idx) ; 
			
			//
			corsToken += "." + Base64.getUrlEncoder().withoutPadding()
					.encodeToString(new JSONObject().put("referer", referer)
							.put("sign-ts" , System.currentTimeMillis())
							.put("sign-alg" , ISigner.sSignAlg_HmacSHA256)
							.toJSONString()
							.getBytes(AppContext.sUTF8)) ;
			// 对它进行签名
			String signature = ISigner.signForUrlNoPadding(corsToken , ISigner.sSignAlg_HmacSHA256
					, aAppSecret) ;
			corsToken += "." + signature ;
			
			return corsToken ;
		}
		return aCorsToken ;
	}
}
