package team.sailboat.commons.fan.http.xca;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.http.HttpConst;
import team.sailboat.commons.fan.http.ISigner;
import team.sailboat.commons.fan.http.Request;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;

public class XAppSigner implements ISigner
{
	public static final String sDefaultSignAlg = sSignAlg_HmacSHA256 ;
	
	//签名Header
    public static final String X_CA_SIGNATURE = "X-Ca-Signature";
    //所有参与签名的Header
    public static final String X_CA_SIGNATURE_HEADERS = "X-Ca-Signature-Headers";
    //请求时间戳
    public static final String X_CA_TIMESTAMP = "X-Ca-Timestamp";
    //请求放重放Nonce,15分钟内保持唯一,建议使用UUID
    public static final String X_CA_NONCE = "X-Ca-Nonce";
    //APP KEY
    public static final String X_CA_KEY = "X-Ca-Key";
    
    public static final String X_CA_SIGNATURE_ALGORITHM = "X-Ca-Signature-Algorithm" ;
    
    public static final String sNL = "\n" ;
    
    public static final String Authorization = "Authorization" ;
    
    public static final String APPCODE = "APPCODE" ;

    public XAppSigner()
	{
	}
    
	@Override
	public void sign(String aContextPath , Request aReq, String aAppKey, String aAppSecret) throws Exception
	{
		aReq.setHeader(X_CA_KEY , aAppKey) ;
		aReq.setHeader(X_CA_NONCE, UUID.randomUUID().toString()) ;
		aReq.setHeader(X_CA_TIMESTAMP , System.currentTimeMillis()) ;
		// 检查签名
		StringBuilder signStrBld = new StringBuilder() ;
		signStrBld.append(aReq.getMethod().toUpperCase()).append(sNL)
			.append(FileUtils.getPath(aContextPath , aReq.getPath())).append(sNL) ;
		// 拼接参数
		spliceParams(aReq, signStrBld) ;
		
		signStrBld.append(sNL) ;
		spliceHeaders(aReq, signStrBld, HttpConst.sHeaderName_Accept 
				, HttpConst.sHeaderName_ContentMD5
				, HttpConst.sHeaderName_ContentType 
				, HttpConst.sHeaderName_Date) ;
		signStrBld.append(sNL) ;
		spliceHeaders(aReq, signStrBld, XAppSigner.X_CA_NONCE
				, XAppSigner.X_CA_KEY 
				, XAppSigner.X_CA_TIMESTAMP
				, XAppSigner.X_CA_SIGNATURE_HEADERS
				, XAppSigner.X_CA_SIGNATURE_ALGORITHM) ;
		signStrBld.append(sNL) ;
		String headersStr = aReq.getHeaderValue(XAppSigner.X_CA_SIGNATURE_HEADERS) ;
		if(XString.isNotEmpty(headersStr))
		{
			spliceHeaders(aReq, signStrBld, headersStr.split(",")) ;
		}
		String signAlg = JCommon.defaultIfEmpty(aReq.getHeaderValue(XAppSigner.X_CA_SIGNATURE_ALGORITHM), sDefaultSignAlg) ;
		String signStr = signStrBld.toString() ;
		String sign = sign(signStr, signAlg, aAppSecret) ;
		//
		aReq.setHeader(X_CA_SIGNATURE , sign) ;
	}
	
	public static String sign(String aText , String aSignAlg , String aSecret) throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException
	{
		Mac alg = Mac.getInstance(aSignAlg);
		byte[] secBs = aSecret.getBytes("UTF-8") ;
		alg.init(new SecretKeySpec(secBs , 0, secBs.length, aSignAlg));
		byte[] md5Result = alg.doFinal(aText.getBytes("UTF-8"));
		return Base64.getEncoder().encodeToString(md5Result);
	}
	
	static void spliceParams(Request aReq , StringBuilder aStrBld)
	{
		String[] keys = aReq.getUrlParamKeys().toArray(JCommon.sEmptyStringArray) ;
		Arrays.sort(keys) ;
		boolean first = true ;
		for(String key : keys)
		{
			if(!first)
			{
				aStrBld.append('&') ;
			}
			else
				first = false ;
			aStrBld.append(key) ;
			String[] vs = aReq.getUrlParamValues(key).toArray(JCommon.sEmptyStringArray) ;
			if(XC.isNotEmpty(vs))
			{
				if(vs.length == 1)
				{
					if(vs[0] != null)
					{
						aStrBld.append('=')
							.append(vs[0]) ;
					}
				}
				else
				{
					Arrays.sort(vs) ;
					for(int i=0 ; i<vs.length ; i++)
					{
						if(i>0)
						{
							aStrBld.append('&')
								.append(key) ;
						}
						if(vs[i] != null)
						{
							aStrBld.append('=')
								.append(vs[i]) ;
						}
					}
				}
			}
		}
	}
	
	static void spliceHeaders(Request aReq , StringBuilder aStrBld , String...aHeaders)
	{
		boolean first = true ;
		for(String header : aHeaders)
		{
			if(first)
				first = false ;
			else
				aStrBld.append(';') ;
			aStrBld.append(header).append(':').append(JCommon.defaultIfNull(aReq.getHeaderValue(header), "")) ;	
		}
	}

}
