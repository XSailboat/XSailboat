package team.sailboat.commons.fan.http.xca;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.http.Base64;
import team.sailboat.commons.fan.http.ISigner;
import team.sailboat.commons.fan.http.Request;
import team.sailboat.commons.fan.http.URLCoder;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.log.Debug;
import team.sailboat.commons.fan.time.XTime;

/**
 * Acs是 Alibaba Could Service缩写
 * 目前还不可用
 *
 * @author yyl
 * @since 2019年11月15日
 */
public class AliyunAcsSigner implements ISigner
{
	public static final String HMAC_SHA1 = "HmacSHA1";
	
	public static final String sHeaderName_Authorization = "Authorization" ;
	
	URLCoder mURLCoder = URLCoder.getInstance_1() ;
	
	String mVersion = "2019-05-06" ;
	
	public AliyunAcsSigner()
	{
	}
	
	public AliyunAcsSigner(String aVersion)
	{
		mVersion = aVersion ;
	}

	@Override
	public void sign(String aContextPath , Request aRequest, String aAppKey, String aAppSecret) throws InvalidKeyException, NoSuchAlgorithmException
	{
		aRequest.urlCoder(mURLCoder) ;
		aRequest.setHeader("Date" , XTime.currentInGMT())
			.setHeader("x-acs-signature-nonce", UUID.randomUUID().toString())
			.setHeader("x-acs-signature-version", "1.0")
			.setHeader("x-acs-version", mVersion) ;
		String text = aRequest.getMethod() + "\n"
					+ aRequest.getHeaderValue_Accept("") + "\n"
					+ aRequest.getHeaderValue_ContentMD5("") + "\n"
					+ aRequest.getHeaderValue_ContentType("") + "\n"
					+ aRequest.getHeaderValue("Date")+ "\n"
					+ spliceCanonicalizedHeaders(aRequest) + "\n"
					+ spliceCanonicalizedResource(aRequest) ;
		Debug.cout("签名字符串："+text) ;
		Mac hmacSha1 = Mac.getInstance(HMAC_SHA1) ;
		byte[] keyBytes = aAppSecret.getBytes(AppContext.sUTF8);
        hmacSha1.init(new SecretKeySpec(keyBytes, 0, keyBytes.length, HMAC_SHA1));

        String signStr = new String(Base64.encodeBase64(
        		hmacSha1.doFinal(text.getBytes(AppContext.sUTF8))) , AppContext.sUTF8);
        aRequest.header(sHeaderName_Authorization, "acs "+aAppKey+":"+signStr) ;
	}
	
	static String spliceCanonicalizedHeaders(Request aRequest)
	{
		TreeMap<String, String> sortedMap = new TreeMap<>() ;
		for(String headerName : aRequest.getHeaderNames())
		{
			if(headerName.toLowerCase().startsWith("x-acs-"))
				sortedMap.put(headerName.toLowerCase() , aRequest.getHeaderValue(headerName).replaceAll("[\\t\\n\\r\\f]", " ").trim()) ;
		}
		if(sortedMap.isEmpty())
			return "" ;
		StringBuilder strBld = new StringBuilder() ;
		for(Entry<String , String> entry : sortedMap.entrySet())
		{
			if(strBld.length()>0)
				strBld.append("\n") ;
			strBld.append(entry.getKey())
					.append(":")
					.append(entry.getValue()) ;
		}
		return strBld.toString() ;
	}
	
	static String spliceCanonicalizedResource(Request aRequest)
	{
		StringBuilder strBld = new StringBuilder(aRequest.getPath()) ;
		String[] paramKeys = aRequest.getQueryParamKeys().toArray(JCommon.sEmptyStringArray) ;
		if(XC.isEmpty(paramKeys))
			return strBld.toString() ;
		strBld.append("?") ;
		Arrays.sort(paramKeys) ;
		boolean first = true ;
		for(String paramKey : paramKeys)
		{
			for(String val : aRequest.getQueryParamValues(paramKey))
			{
				if(first)
					first = false ;
				else
					strBld.append('&') ;
				strBld.append(paramKey) ;
				if(val != null)
					strBld.append('=').append(val) ;
			}
		}
		return strBld.toString() ;
	}

}
