package team.sailboat.ms.base.proxy;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import team.sailboat.base.ZKSysProxy;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.http.IdentityTrace;
import team.sailboat.commons.fan.http.URLBuilder;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.fan.sys.XNet;
import team.sailboat.commons.fan.text.XString;

public class HttpRequestProxy
{
	static Map<String, String> sOuterNetModuleAddrMap ;
	
	HttpServletRequest mRequest ;
	HttpServletResponse mResponse ;
	
	
	
	public HttpRequestProxy(HttpServletRequest aRequest , HttpServletResponse aResponse)
	{
		mRequest = aRequest ;
		mResponse = aResponse ;
	}
	
	public void doRequest() throws Exception
	{
		String path = mRequest.getServletPath() ;
		String seg = XString.seg_i(path, '/', 1) ;
		if("proxy".equals(seg))
		{
			_proxy(path) ;
		}
		else if("redirect".equals(seg))
		{
			String moduleName = XString.seg_i(path, '/', 2) ;
			String targetPath = "/"+XString.substringRight(path, '/', true, 2) ;
	        String newURLStr = getAddr(mRequest.getRemoteAddr() , moduleName, targetPath) ;
	        
	        Map<String, String[]> paramMap = mRequest.getParameterMap() ;
	        if(XC.isNotEmpty(paramMap))
	        {
	        	URLBuilder urlBld = new URLBuilder(newURLStr) ;
		        for(Entry<String, String[]> entry : paramMap.entrySet())
		        {
		        	if(entry.getValue() != null)
		        	{
		        		for(String val : entry.getValue())
		        		{
		        			urlBld.queryParams(entry.getKey(), val) ;
		        		}
		        	}
		        }
		        newURLStr = urlBld.toString() ;
	        }
	        mResponse.sendRedirect(newURLStr) ;
		}
		else
			throw new IllegalArgumentException(XString.msgFmt("请求路径[{}]不合法，第1段必须是proxy或者redirect，而现在是[{}]"
					, path , seg)) ;
	}
	
	void _proxy(String aPath) throws Exception
	{
		String moduleName = XString.seg_i(aPath, '/', 2) ;
		String targetPath = "/"+XString.substringRight(aPath, '/', true, 2) ;

        String newURLStr = getAddr(moduleName, targetPath) ;
        URL url = URI.create(newURLStr).toURL() ;
        
        IdentityTrace trace = IdentityTrace.get(mRequest) ;
        trace.pushModuleName(System.getProperty("spring.application.name")) ;
        	
        HttpURLConnection urlConn = (HttpURLConnection)url.openConnection() ;
        try
        {
	        urlConn.setRequestMethod(mRequest.getMethod()) ;
	        urlConn.setDoOutput(true) ; 
	 
	        Enumeration<String> headerNames = mRequest.getHeaderNames() ;
	        while(headerNames.hasMoreElements())
	        {
	        	String name = headerNames.nextElement() ;
	        	if(!"host".equalsIgnoreCase(name) && !"content-length".equalsIgnoreCase(name))
	    			urlConn.setRequestProperty(name , mRequest.getHeader(name)) ;
	    	}
	        
	        trace.apply(urlConn) ;
	    	
	    	if("post".equalsIgnoreCase(mRequest.getMethod()))
	    	{
	    		Map<String , String[]> paramMap = mRequest.getParameterMap() ;
	        	if(XC.isNotEmpty(paramMap))
	        	{
	        		String encoding = mRequest.getCharacterEncoding() ;
	        		ByteArrayOutputStream bouts = new ByteArrayOutputStream(10240) ;
	    			try(OutputStreamWriter writer = new OutputStreamWriter(bouts, encoding))
	    			{
	    				boolean first = true ;
	    				for(Entry<String , String[]> entry : paramMap.entrySet())
	    				{
	    					if(first)
	    						first = false ;
	    					else
	    						writer.append("&") ;
	    					if(XC.isEmpty(entry.getValue()))
	    						writer.append(URLEncoder.encode(entry.getKey() , encoding)) ;
	    					else
	    					{
	    						for(String val : entry.getValue())
	    						{
	    							writer.append(URLEncoder.encode(entry.getKey() , encoding))
	    									.append('=').append(URLEncoder.encode(val , encoding)) ;
	    						}
	    					}
	    				}
	    			}
	    			urlConn.setRequestProperty("Content-Length", String.valueOf(bouts.size()));
	    			urlConn.getOutputStream().write(bouts.toByteArray()) ;
	    			StreamAssist.close(urlConn.getOutputStream()) ;
	        	}
	    	}
	    	
	    	int responseCode = urlConn.getResponseCode() ;
	    	int contentLen = urlConn.getContentLength() ;
	    	String contentType = urlConn.getContentType() ;
	    	mResponse.setStatus(responseCode) ;
	    	mResponse.setContentLength(contentLen) ;
	    	mResponse.setContentType(contentType) ;
	    	
	    	StreamAssist.transfer_cc(responseCode==200?urlConn.getInputStream():urlConn.getErrorStream() 
	    			, mResponse.getOutputStream()) ;
        }
        finally
        {
        	urlConn.disconnect();
        }
	}
	
	String getAddr(String aModuleName , String aTargetPath) throws Exception
	{
		JSONObject jo = ZKSysProxy.getSysDefault().getRegisteredWebModule(aModuleName) ;
		return jo.optString("serviceAddr")+aTargetPath ;
	}
	
	String getAddr(String aClientIp , String aModuleName , String aTargetPath) throws Exception
	{
		if(XC.isEmpty(sOuterNetModuleAddrMap) || XNet.isSameWithLocalIp(aClientIp, 16))
			return getAddr(aModuleName, aTargetPath) ;
		else
			return sOuterNetModuleAddrMap.get(aModuleName)+aTargetPath ;
	}
}
