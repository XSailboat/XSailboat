package team.sailboat.commons.ms.xca;

import static team.sailboat.commons.fan.http.xca.XAppSigner.sNL;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletRequest;
import team.sailboat.commons.fan.collection.AutoCleanHashMap;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.HttpException;
import team.sailboat.commons.fan.http.HttpConst;
import team.sailboat.commons.fan.http.IdentityTrace;
import team.sailboat.commons.fan.http.xca.XAppSigner;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;

public class XAppSignChecker implements IAppSignChecker
{

	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	/**
	 * 十分钟
	 */
	static final int mTimeBias = 10 * 60 * 1000 ;
	
	final Map<String, Object> mOnceIdMap = AutoCleanHashMap.withExpired_Created(10) ;
	
	BiPredicate<String, HttpServletRequest> mCanVisitPred ;
	
	Function<String, IClientApp> mAppByAppKey ;
	
	public XAppSignChecker(BiPredicate<String, HttpServletRequest> aCanVisitPred
			, Function<String, IClientApp> aAppByAppKey)
	{
		mCanVisitPred = aCanVisitPred ;
		mAppByAppKey = aAppByAppKey ;
		Assert.notNull(mAppByAppKey) ;
	}
	
	/**
	 * 检查不通过将抛出异常
	 * @param aReq
	 * @return   返回appKey和appSecret
	 * @throws HttpException
	 * @throws IllegalStateException
	 * @throws UnsupportedEncodingException
	 */
	public AppCertificate check(HttpServletRequest aReq) throws HttpException, IllegalStateException, UnsupportedEncodingException
	{
		//
		String signSpecial = aReq.getHeader(XAppSigner.X_CA_SIGNATURE) ;
		// 时间检查
		String timeStr = aReq.getHeader(XAppSigner.X_CA_TIMESTAMP) ;
		if(XString.isEmpty(timeStr))
		{
			throw new HttpException(HttpStatus.BAD_REQUEST.value() , aReq.getMethod() , null , null
					, XString.msgFmt("缺少头信息：{}！" , XAppSigner.X_CA_TIMESTAMP)) ;
		}
		long timestamp = Long.parseLong(timeStr) ;
		if(Math.abs(System.currentTimeMillis()-timestamp)>mTimeBias)
		{
			throw new HttpException(HttpStatus.BAD_REQUEST.value() , aReq.getMethod() , null , null
					, "请求过期！");
		}
		// 防重放检查
		String onceId = aReq.getHeader(XAppSigner.X_CA_NONCE) ;
		if(mOnceIdMap.put(onceId, JCommon.sNullObject) != null)
		{
			throw new HttpException(HttpStatus.BAD_REQUEST.value() , aReq.getMethod() , null , null
					, XString.msgFmt("{}重复！" , onceId)) ;
		}
		// 检查X-Ca-Key是否有效
		String appKey = aReq.getHeader(XAppSigner.X_CA_KEY) ;
		if(XString.isEmpty(appKey))
		{
			throw new HttpException(HttpStatus.BAD_REQUEST.value() , aReq.getMethod() , null , null
					, XString.msgFmt("缺少头信息：{}！" , XAppSigner.X_CA_KEY));
		}
		// 检查指定app是否有权访问指定接口
		if(mCanVisitPred != null)
		{
			if(!mCanVisitPred.test(appKey, aReq))
			{
				mLogger.warn("拒绝访问！" + IdentityTrace.get(aReq).toString()) ; 
				throw new HttpException(HttpStatus.FORBIDDEN.value() , aReq.getMethod() , null , null
						, XString.msgFmt("禁止访问：{}-{}！" , aReq.getMethod() , aReq.getServletPath())) ;
			}
		}
		
		// 检查密钥
		IClientApp app = mAppByAppKey.apply(appKey) ;
		String appSecret = null ;
		if(app != null)
			appSecret = app.getAppSecret() ;
		if(XString.isEmpty(appSecret))
		{
			throw new HttpException(HttpStatus.BAD_REQUEST.value() , aReq.getMethod() , null , null
					, XString.msgFmt("无效的{}！" , XAppSigner.X_CA_KEY));
		}
		
		// 检查签名
		StringBuilder signStrBld = new StringBuilder() ;
		signStrBld.append(aReq.getMethod().toUpperCase()).append(sNL)
			.append(aReq.getContextPath()+aReq.getServletPath()).append(sNL) ;
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
		String headersStr = aReq.getHeader(XAppSigner.X_CA_SIGNATURE_HEADERS) ;
		if(XString.isNotEmpty(headersStr))
		{
			spliceHeaders(aReq, signStrBld, headersStr.split(",")) ;
		}
		String signAlg = JCommon.defaultIfEmpty(aReq.getHeader(XAppSigner.X_CA_SIGNATURE_ALGORITHM)
				, XAppSigner.sDefaultSignAlg) ;
		String signStr = signStrBld.toString() ;
		Mac hmacSha256 = null ;
		try
		{
			hmacSha256 = Mac.getInstance(signAlg);
			byte[] secBs = appSecret.getBytes("UTF-8") ;
			hmacSha256.init(new SecretKeySpec(secBs , 0, secBs.length, signAlg));
		}
		catch(NoSuchAlgorithmException e)
		{
			throw new HttpException(HttpStatus.BAD_REQUEST.value() , aReq.getMethod() , null , null
					, "不支持的算法："+signAlg) ;
		}
		catch(InvalidKeyException e)
		{
			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR.value() , aReq.getMethod() , null , null
					, "服务出现错误！") ;
		}
		byte[] md5Result = hmacSha256.doFinal(signStr.getBytes("UTF-8"));
		String sign = Base64.getEncoder().encodeToString(md5Result);
		if(JCommon.unequals(signSpecial , sign))
		{
			throw new HttpException(HttpStatus.FORBIDDEN.value() , aReq.getMethod() , null , null
					, "") ;
		}
		return new AppKeySecret(app.getId() , app.getAppKey() , app.getAppSecret()) ;
	}
	
	static void spliceParams(HttpServletRequest aReq , StringBuilder aStrBld)
	{
		Map<String, String[]> paramMap = aReq.getParameterMap() ;
		String[] keys = paramMap.keySet().toArray(JCommon.sEmptyStringArray) ;
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
			String[] vs = paramMap.get(key) ;
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
	
	static void spliceHeaders(HttpServletRequest aReq , StringBuilder aStrBld , String...aHeaders)
	{
		boolean first = true ;
		for(String header : aHeaders)
		{
			if(first)
				first = false ;
			else
				aStrBld.append(';') ;
			aStrBld.append(header).append(':').append(JCommon.defaultIfNull(aReq.getHeader(header), "")) ;	
		}
	}
}
