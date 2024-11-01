package team.sailboat.commons.fan.http;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public interface ISigner
{
	public static final String sSignAlg_HmacSHA256 = "HmacSHA256" ;
	
	void sign(String aContextPath , Request aRequest , String aAppKey , String aAppSecret) throws Exception ;
	
	public static String signForUrlNoPadding(String aText , String aSignAlg , String aSecret) throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException
	{
		Mac alg = Mac.getInstance(aSignAlg);
		byte[] secBs = aSecret.getBytes("UTF-8") ;
		alg.init(new SecretKeySpec(secBs , 0, secBs.length, aSignAlg));
		byte[] md5Result = alg.doFinal(aText.getBytes("UTF-8"));
		return Base64.getUrlEncoder().withoutPadding().encodeToString(md5Result);
	}
}
