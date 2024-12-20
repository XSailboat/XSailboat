package team.sailboat.aviator.http;

import java.net.MalformedURLException;
import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import team.sailboat.commons.fan.http.HttpClient;
import team.sailboat.commons.fan.http.ISigner;
import team.sailboat.commons.fan.http.xca.AliyunAcsSigner;
import team.sailboat.commons.fan.http.xca.XAppSigner;
import team.sailboat.commons.fan.lang.XClassUtil;

/**
 * 
 *
 * @author yyl
 * @since 2024年12月6日
 */
public class Func_http_client extends AbstractFunction
{

	private static final long serialVersionUID = 1L;

	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aUrl)
	{
		return AviatorRuntimeJavaType.valueOf(HttpClient.ofURI(XClassUtil.toString(aUrl.getValue(aEnv)))) ;
	}
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aUrl
			, AviatorObject aAppKey
			, AviatorObject aAppSecret
			, AviatorObject aSignerType)
	{
		return call(aEnv, aUrl , aAppKey, aAppSecret, aSignerType, AviatorBoolean.TRUE) ;
	}
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aUrl
			, AviatorObject aAppKey
			, AviatorObject aAppSecret
			, AviatorObject aSignerType
			, AviatorObject aAsContextPath)
	{
		try
		{
			String signerType = XClassUtil.toString(aSignerType.getValue(aEnv)) ;
			ISigner signer = null ;
			if("xapp".equalsIgnoreCase(signerType))
			{
				signer = new XAppSigner() ;
			}
			else if("aliyun".equalsIgnoreCase(signerType))
			{
				signer = new AliyunAcsSigner() ;
			}
			else
				throw new IllegalArgumentException("无效的signerType："+signerType) ;
			return AviatorRuntimeJavaType.valueOf(HttpClient.ofURI(XClassUtil.toString(aUrl.getValue(aEnv))
					, XClassUtil.toString(aAppKey.getValue(aEnv))
					, XClassUtil.toString(aAppSecret.getValue(aEnv))
					, signer
					, XClassUtil.toBoolean(aAsContextPath.getValue(aEnv) , true))) ;
		}
		catch (MalformedURLException e)
		{
			throw new IllegalArgumentException(e) ;
		}
	}

	@Override
	public String getName()
	{
		return "http.client" ;
	}
}
