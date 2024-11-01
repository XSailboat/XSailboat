package team.sailboat.commons.fan.http.xca;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.IMultiMap;
import team.sailboat.commons.fan.http.Base64;
import team.sailboat.commons.fan.http.HttpConst;
import team.sailboat.commons.fan.http.ISigner;
import team.sailboat.commons.fan.http.Request;
import team.sailboat.commons.fan.struct.Wrapper;

/**
 * 阿里云Dataphin（数据治理平台）
 *
 * @author yyl
 * @since 2023年7月27日
 */
public class AliyunDataphinSigner implements ISigner
{
	//签名算法HmacSha256
	public static final String HMAC_SHA256 = "HmacSHA256";

	//换行符
	public static final String LF = "\n";
	//串联符
	public static final String SPE1 = ",";
	//示意符
	public static final String SPE2 = ":";
	//连接符
	public static final String SPE3 = "&";
	//赋值符
	public static final String SPE4 = "=";
	//问号符
	public static final String SPE5 = "?";
	//默认请求超时时间,单位毫秒
	public static final int DEFAULT_TIMEOUT = 1000;
	//参与签名的系统Header前缀,只有指定前缀的Header才会参与到签名中
	public static final String CA_HEADER_TO_SIGN_PREFIX_SYSTEM = "x-ca-";

	//签名Header
	public static final String X_CA_SIGNATURE = "x-ca-signature";
	//所有参与签名的Header
	public static final String X_CA_SIGNATURE_HEADERS = "x-ca-signature-headers";
	//请求时间戳
	public static final String DATE = "date";
	//请求时间戳
	public static final String X_CA_TIMESTAMP = "x-ca-timestamp";
	//请求放重放Nonce,15分钟内保持唯一,建议使用UUID
	public static final String X_CA_NONCE = "x-ca-nonce";
	//APP KEY
	public static final String X_CA_KEY = "x-ca-key";

	public static final String X_CA_STAGE = "x-ca-stage";

	static final String X_CA_SIGNATURE_METHOD = "x-ca-signature-method";

	String mStage;

	public AliyunDataphinSigner()
	{
	}

	public AliyunDataphinSigner(String aStage)
	{
		String stage = aStage.toUpperCase();
		if (!("RELEASE".equals(stage)
				|| "TEST".equals(stage)
				|| "PRE".equals(stage)))
			throw new IllegalArgumentException("不合法的Stage名称：" + aStage);
		mStage = stage;
	}

	static String getHttpDateHeaderValue(Date date)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(date);
	}

	@Override
	public void sign(String aContextPath, Request aRequest, String aAppKey, String aAppSecret)
			throws InvalidKeyException, NoSuchAlgorithmException
	{
		//date,设置请求头中的时间戳
		Date current = new Date();
		aRequest.setHeader(DATE, getHttpDateHeaderValue(current))
				.setHeader(X_CA_TIMESTAMP, String.valueOf(current.getTime()))
				.setHeader(X_CA_KEY , aAppKey)
				.setHeader(X_CA_SIGNATURE_METHOD, HMAC_SHA256)
				.setHeader(X_CA_NONCE , UUID.randomUUID().toString())
				;
		
		//x-ca-stage,生产环境标识
		if (mStage != null)
			aRequest.setHeader(X_CA_STAGE, mStage);

		// MD5 可以不算
//		        String md5 = base64AndMD5(aRequest.getRawEntity().getBytes("UTF-8"));
//		        headers.put("content-md5", md5);
//		        System.out.println("生成header[content-md5]:"+md5);

		//x-ca-signature,签名用作服务器校验
		Wrapper<String> signHeaderNamesStrWrapper = new Wrapper<>();
		String stringToSign = buildStringToSign(aRequest.getMethod(),
				aRequest.getPath(),
				aRequest.getHeaderMap(),
				signHeaderNamesStrWrapper);
		
		String signature = sign(stringToSign, aAppSecret);
		aRequest.setHeader(X_CA_SIGNATURE, signature)
				.setHeader(X_CA_SIGNATURE_HEADERS, signHeaderNamesStrWrapper.get());
	}

	String sign(String stringToSign, String aAppSecret) throws NoSuchAlgorithmException, InvalidKeyException
	{
		Mac hmacSha256 = Mac.getInstance(HMAC_SHA256);
		byte[] keyBytes = aAppSecret.getBytes(AppContext.sUTF8);
		hmacSha256.init(new SecretKeySpec(keyBytes, 0, keyBytes.length, HMAC_SHA256));
		String signature = new String(Base64.encodeBase64(hmacSha256.doFinal(stringToSign.getBytes(AppContext.sUTF8))));
		return signature;
	}

	/**
	* 构建待签名字符串
	* @param method
	* @param path
	* @param headers
	* @param querys
	* @param bodys
	* @param signHeaderPrefixList
	* @return
	*/
	static String buildStringToSign(String method,
			String path,
			IMultiMap<String, String> aHeaderMap,
			Wrapper<String> aSignHeaderNamessStrWrapper)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(method).append(LF);

		//如果有@"Accept"头，这个头需要参与签名
		if (aHeaderMap.getFirst(HttpConst.sHeaderName_Accept) != null)
		{
			sb.append(aHeaderMap.getFirst(HttpConst.sHeaderName_Accept));
		}
		sb.append(LF);

		//如果有@"Content-MD5"头，这个头需要参与签名
		if (aHeaderMap.getFirst(HttpConst.sHeaderName_ContentMD5) != null)
		{
			sb.append(aHeaderMap.getFirst(HttpConst.sHeaderName_ContentMD5));
		}
		sb.append(LF);

		//如果有@"Content-Type"头，这个头需要参与签名
		if (aHeaderMap.getFirst(HttpConst.sHeaderName_ContentType) != null)
		{
			sb.append(aHeaderMap.getFirst(HttpConst.sHeaderName_ContentType));
		}
		sb.append(LF);

		//签名优先读取HTTP_CA_HEADER_DATE，因为通过浏览器过来的请求不允许自定义Date（会被浏览器认为是篡改攻击）
		if (aHeaderMap.getFirst(DATE) != null)
		{
			sb.append(aHeaderMap.getFirst(DATE));
		}
		sb.append(LF);

		//将headers合成一个字符串
		sb.append(buildHeaders(aHeaderMap, aSignHeaderNamessStrWrapper));

		sb.append(path + "?appKey=" + aHeaderMap.getFirst(X_CA_KEY) + "&env=PROD");
		return sb.toString();
	}

	/**
	* 构建待签名Http头
	*
	* @param headers 请求中所有的Http头
	* @param signHeaderPrefixList 自定义参与签名Header前缀
	* @return 待签名Http头
	*/
	static String buildHeaders(IMultiMap<String, String> aHeaderMap, Wrapper<String> aSignHeaderNamesStrWrapper)
	{
		//使用TreeMap,默认按照字母排序
		Map<String, String> headersToSign = new TreeMap<String, String>();

		for (Map.Entry<String, String> header : aHeaderMap.entrySet())
		{
			if (header.getKey().startsWith(CA_HEADER_TO_SIGN_PREFIX_SYSTEM))
			{
				headersToSign.put(header.getKey(), header.getValue());
			}
		}
		StringBuilder headersStrBld = new StringBuilder() ;
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> e : headersToSign.entrySet())
		{
			sb.append(e.getKey()).append(':').append(e.getValue()).append(LF);
			if(headersStrBld.length() > 0)
				headersStrBld.append(',') ;
			headersStrBld.append(e.getKey()) ;
		}
		aSignHeaderNamesStrWrapper.set(headersStrBld.toString()) ;
		return sb.toString();
	}
}