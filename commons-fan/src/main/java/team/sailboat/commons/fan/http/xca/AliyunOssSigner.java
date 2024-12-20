package team.sailboat.commons.fan.http.xca;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.IMultiMap;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.http.Base64;
import team.sailboat.commons.fan.http.HttpConst;
import team.sailboat.commons.fan.http.ISigner;
import team.sailboat.commons.fan.http.Request;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.struct.Tuples;
import team.sailboat.commons.fan.struct.Wrapper;
import team.sailboat.commons.fan.text.XString;

public class AliyunOssSigner implements ISigner
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
    
	 //签名Header
    public static final String Authorization = "Authorization";
    
    String mStage ;
    
    public AliyunOssSigner()
	{
	}
    
    public AliyunOssSigner(String aStage)
	{
    	String stage = aStage.toUpperCase() ;
    	if(!("RELEASE".equals(stage) 
    			|| "TEST".equals(stage)
    			|| "PRE".equals(stage)))
    		throw new IllegalArgumentException("不合法的Stage名称："+aStage) ;
    	mStage = stage ;
	}
    
	@Override
	public void sign(String aContextPath , Request aRequest, String aAppKey, String aAppSecret) throws InvalidKeyException, NoSuchAlgorithmException
	{		 
		Tuples.T2<String, String> signEntry = sign(aAppSecret, aRequest.getMethod() 
				, FileUtils.getPath(aContextPath, aRequest.getPath()) 
				, aRequest.getHeaderMap()
				, aRequest.getQueryParamMap()
				, aRequest.getFormParamMap()
				, new ArrayList<>()) ;
    	
    	aRequest.setHeader(Authorization , signEntry.getKey()) ;
	}

    /**
     * 计算签名
     *
     * @param secret APP密钥
     * @param method HttpMethod
     * @param path
     * @param headers
     * @param querys
     * @param bodys
     * @param signHeaderPrefixList 自定义参与签名Header前缀
     * @return 签名后的字符串
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     */
    public static Tuples.T2<String , String> sign(String secret, String method, String path, 
    							IMultiMap<String, String> aHeaderMap , 
    							IMultiMap<String, String> querys, 
    							IMultiMap<String, String> bodys, 
    							List<String> signHeaderPrefixList) throws NoSuchAlgorithmException, InvalidKeyException
    {
        Mac hmacSha256 = Mac.getInstance(HMAC_SHA256);
        byte[] keyBytes = secret.getBytes(AppContext.sUTF8);
        hmacSha256.init(new SecretKeySpec(keyBytes, 0, keyBytes.length, HMAC_SHA256));

        Wrapper<String> signHeaderNamesStrWrapper = new Wrapper<>() ;
        
        String signStr = new String(Base64.encodeBase64(
        		hmacSha256.doFinal(
        				buildStringToSign(method, path, aHeaderMap , querys, bodys, signHeaderPrefixList , signHeaderNamesStrWrapper)
        					.getBytes(AppContext.sUTF8))) , AppContext.sUTF8);
        return Tuples.of(signStr, signHeaderNamesStrWrapper.get()) ;
        
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
    static String buildStringToSign(String method, String path
    		, IMultiMap<String, String> aHeaderMap
    		, IMultiMap<String, String> querys
    		, IMultiMap<String, String> bodys
    		, List<String> signHeaderPrefixList
    		, Wrapper<String> aSignHeaderNamessStrWrapper) {
        StringBuilder sb = new StringBuilder();

        sb.append(method.toUpperCase()).append(LF);
        if (null != aHeaderMap)
        {
        	String val = aHeaderMap.getFirst(HttpConst.sHeaderName_ContentMD5) ;
        	if (null != val)
                sb.append(val);
            
            sb.append(LF);
            val = aHeaderMap.getFirst(HttpConst.sHeaderName_ContentType) ;
            if (null != val)
                sb.append(val);
            
            sb.append(LF);
            val = aHeaderMap.getFirst(HttpConst.sHeaderName_Date) ;
            if (null != val)
                sb.append(val);
        }
        sb.append(LF);
        sb.append(buildHeaders(aHeaderMap , signHeaderPrefixList));
        sb.append(buildResource(path, querys, bodys));
        
        return sb.toString();
    }

    /**
     * 构建待签名Path+Query+BODY
     *
     * @param path
     * @param querys
     * @param bodys
     * @return 待签名
     */
    private static String buildResource(String path , IMultiMap<String, String> aQueryMap 
    		, IMultiMap<String, String> aBodyMap)
    {
    	StringBuilder sb = new StringBuilder();
    	
    	if (!XString.isBlank(path)) {
    		sb.append(path);
        }
        Map<String, String> sortMap = new TreeMap<String, String>();
        if (null != aQueryMap)
        {
        	for (Map.Entry<String, String> query : aQueryMap.entrySet())
        	{
        		if (!XString.isBlank(query.getKey())) {
        			sortMap.put(query.getKey(), query.getValue());
                }
        	}
        }
        
        if (null != aBodyMap)
        {
        	for (Map.Entry<String, String> body : aBodyMap.entrySet())
        	{
        		if (!XString.isBlank(body.getKey()))
        			sortMap.put(body.getKey(), body.getValue());
        	}
        }
        
        StringBuilder sbParam = new StringBuilder();
        for (Map.Entry<String, String> item : sortMap.entrySet()) {
    		if (!XString.isBlank(item.getKey())) {
    			if (0 < sbParam.length()) {
    				sbParam.append(SPE3);
    			}
    			sbParam.append(item.getKey());
    			if (!XString.isBlank(item.getValue())) {
    				sbParam.append(SPE4).append(item.getValue());
    			}
            }
    	}
        if (0 < sbParam.length()) {
        	sb.append(SPE5);
        	sb.append(sbParam);
        }
        
        return sb.toString();
    }

    /**
     * 构建待签名Http头
     *
     * @param headers 请求中所有的Http头
     * @param signHeaderPrefixList 自定义参与签名Header前缀
     * @return 待签名Http头
     */
    static String buildHeaders(IMultiMap<String, String> aHeaderMap 
    		, List<String> signHeaderPrefixList)
    {
    	StringBuilder sb = new StringBuilder();
		if (null != aHeaderMap)
		{
			String[] headerNames = aHeaderMap.keySet().toArray(JCommon.sEmptyStringArray) ;
			Arrays.sort(headerNames) ;
			for (String headerName : headerNames)
			{
				headerName = headerName.toLowerCase() ;
                if (isHeaderToSign(headerName , signHeaderPrefixList))
                {
                	sb.append(headerName);
                	String val = aHeaderMap.getFirst(headerName) ;
                    if (!XString.isBlank(val))
                    {
                    	sb.append(SPE2);
                    	sb.append(val);
                    }
                    
                    sb.append(LF);
                }
            }
		}
        return sb.toString() ;
    }

    /**
     * Http头是否参与签名 return
     */
    private static boolean isHeaderToSign(String headerName, List<String> signHeaderPrefixList)
    {
	    return headerName.startsWith("x-oss-") ;
    }
}
