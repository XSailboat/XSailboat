package team.sailboat.commons.ms.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import team.sailboat.commons.fan.http.HttpConst;
import team.sailboat.commons.fan.json.JSONException;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.JCommon;

public class WebUtils
{
	
	static final SimpleDateFormat sSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX") ;
	static
	{
		sSdf.setTimeZone(TimeZone.getDefault());
	}
	
	public static boolean isWebRequest(HttpServletRequest aReq)
	{
		String contentTypeHeader = aReq.getHeader(HttpConst.sHeaderName_ContentType);
    	String acceptHeader = aReq.getHeader(HttpConst.sHeaderName_Accept);
    	String xRequestedWith = aReq.getHeader(HttpConst.sHeaderName_X_Requested_With);
    	String referer = aReq.getHeader(HttpConst.sHeaderName_referer) ;
	
		return !(
				(contentTypeHeader != null && contentTypeHeader.contains("application/json"))
	              || (acceptHeader != null && acceptHeader.contains("application/json"))
	              || "XMLHttpRequest".equalsIgnoreCase(xRequestedWith)
	              || HttpConst.sHeaderValue_UserAgent_x_HttpClient.equalsIgnoreCase(aReq.getHeader(HttpConst.sHeaderName_UserAgent))
	              || (referer != null && referer.endsWith("swagger-ui.html"))
	             ) ;   	
	}
	
	public static void sendErrorInJSON(HttpServletRequest aReq , HttpServletResponse aRep , HttpStatus aStatus , Object aMsg) throws JSONException, IOException
	{
		aRep.setStatus(aStatus.value()) ;
  		aRep.getWriter().write(new JSONObject()
  				.put("timestamp" , sSdf.format(new Date()))
  				.put("status", aStatus.value())
  				.put("error", aStatus.getReasonPhrase())
  				.put("path" , aReq.getContextPath()+aReq.getServletPath())
  				.put("message", JCommon.toString(aMsg)).toJSONString()) ;
  		aRep.setContentType(MediaType.APPLICATION_JSON_VALUE) ;
	}
}
