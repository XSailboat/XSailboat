package team.sailboat.commons.fan.http.xca;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.http.Base64;
import team.sailboat.commons.fan.http.ISigner;
import team.sailboat.commons.fan.http.Request;
import team.sailboat.commons.fan.http.URLCoder;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.log.Debug;
import team.sailboat.commons.fan.text.XString;

public class AliyunApiGatewayMngSigner implements ISigner
{
	public static final String HMAC_SHA1 = "HmacSHA1";
	
	public static final String sParamKey_Signature = "Signature" ;
	
	URLCoder mURLCoder = URLCoder.getInstance_1() ;

	@Override
	public void sign(String aContextPath , Request aRequest, String aAppKey, String aAppSecret) throws InvalidKeyException, NoSuchAlgorithmException
	{
		aRequest.urlCoder(mURLCoder) ;
		String path = FileUtils.getPath(aContextPath, aRequest.getPath()) ;
		String text = aRequest.getMethod()+"&"+(XString.isEmpty(path)?mURLCoder.encodeParam("/"):mURLCoder.splitEncodePath(aRequest.getPath())) 
				+"&" + mURLCoder.encodeParam(mURLCoder.formatEncodeParams(aRequest.getQueryParamMap(), true)) ;
		Debug.cout("签名字符串："+text) ;
		Mac hmacSha1 = Mac.getInstance(HMAC_SHA1) ;
		byte[] keyBytes = (aAppSecret+"&").getBytes(AppContext.sUTF8);
        hmacSha1.init(new SecretKeySpec(keyBytes, 0, keyBytes.length, HMAC_SHA1));

        String signStr = new String(Base64.encodeBase64(
        		hmacSha1.doFinal(text.getBytes(AppContext.sUTF8))) , AppContext.sUTF8);
        aRequest.queryParam(sParamKey_Signature , signStr) ;
	}
	
	static String spliceUrlParams(Request aRequest)
	{
		String[] paramKeys = aRequest.getQueryParamKeys().toArray(JCommon.sEmptyStringArray) ;
		Arrays.sort(paramKeys) ;
		StringBuilder strBld = new StringBuilder() ;
		for(String paramKey : paramKeys)
		{
			for(String val : aRequest.getQueryParamValues(paramKey))
			{
				if(strBld.length()>0)
					strBld.append('&') ;
				strBld.append(paramKey) ;
				if(val != null)
					strBld.append('=').append(val) ;
			}
		}
		return strBld.toString() ;
	}

}
