package team.sailboat.ms.base.dataset.tool;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.IMultiMap;
import team.sailboat.commons.fan.excep.HttpException;
import team.sailboat.commons.fan.excep.RestApiException;
import team.sailboat.commons.fan.http.HttpConst;
import team.sailboat.commons.fan.http.HttpUtils;
import team.sailboat.commons.fan.http.IRestClient;
import team.sailboat.commons.fan.http.Request;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONException;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.serial.FlexibleBInputStream;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.ms.ACKeys_Common;

public class RestTplClient implements IRestClient
{
	
	final String wsId ;
	MockMvc client ;
	Field contentField ;
	
	public RestTplClient(String aWsId)
	{
		wsId = aWsId ;
		client = MockMvcBuilders.webAppContextSetup((WebApplicationContext)AppContext.get(ACKeys_Common.sSpringAppContext))
				.build() ;
		contentField = XClassUtil.getField(MockHttpServletResponse.class , "content") ;
		contentField.setAccessible(true); ;
	}

	@Override
	public Object ask(Request aRequest) throws Exception
	{
		MvcResult result = client.perform(convert(aRequest))
				.andReturn() ;
		
		MockHttpServletResponse resp = result.getResponse() ;
		if(HttpConst.sMethod_HEAD.equalsIgnoreCase(result.getRequest().getMethod()))
		{
			return HttpStatus.valueOf(resp.getStatus()) ;
		}
		 
		if(HttpUtils.isError(resp.getStatus()))
		{
			MockHttpServletRequest req = result.getRequest() ;
			String msg = resp.getContentAsString() ;
			if(XString.isNotEmpty(msg) && msg.charAt(0) == '{')
			{
				try
				{
					JSONObject msgJo = JSONObject.of(msg) ;
					if(msgJo.has("message") && msgJo.has("rootExceptionClass"))
					{
						RestApiException.createAndThrow(req.getMethod() 
								, new URL(req.getRequestURL().toString())
								, resp.getStatus() 
								, msgJo.optString("message")
								, msgJo.optString("rootExceptionClass"), new Date(msgJo.optLong("timestamp"))) ;
						return null ;	// dead code
					}
				}
				catch (JSONException e)
				{}
			}
			HttpException.createAndThrow(req.getMethod() , new URL(req.getRequestURL().toString()) 
					, resp.getStatus() ,  msg) ;
			return null ;		// dead code
		}
		
		String contentType = resp.getContentType() ;
		if(XString.isNotEmpty(contentType) 
				&& (MediaType.APPLICATION_OCTET_STREAM.isCompatibleWith(MediaType.valueOf(contentType))
						|| MediaType.IMAGE_PNG_VALUE.equalsIgnoreCase(contentType)
						|| MediaType.IMAGE_GIF_VALUE.equalsIgnoreCase(contentType)
						|| MediaType.IMAGE_JPEG_VALUE.equalsIgnoreCase(contentType)))
		{
			return (InputStream)contentField.get(resp) ;
		}
		String content = resp.getContentAsString() ;
		
		if(XString.isNotEmpty(contentType) && contentType.toLowerCase().contains("json"))
		{
			content = content.trim() ;
			if(content.startsWith("["))
				return new JSONArray(content) ;
			else if(content.startsWith("{"))
				return JSONObject.of(content) ;
			else if(XString.isEmpty(content))
				return null ;
		}
		return content ;
	}
	
	RequestBuilder convert(Request aReq)
	{
		MockHttpServletRequestBuilder builder = null ;
		switch(aReq.getMethod())
		{
		case HttpConst.sMethod_GET:
			builder = MockMvcRequestBuilders.get(aReq.getPath()) ;
			break ;
		case HttpConst.sMethod_POST:
			builder = MockMvcRequestBuilders.post(aReq.getPath()) ;
			break ;
		}
		IMultiMap<String, String> paramMap = aReq.getUrlParamMap() ;
		if(paramMap != null)
		{
			for(String key : paramMap.keySet())
			{
				String[] values = paramMap.get(key).toArray(JCommon.sEmptyStringArray) ;
				builder.queryParam(key, values) ;
			}
		}
		paramMap = aReq.getFormParamMap() ;
		if(paramMap != null)
		{
			builder.contentType(aReq.getHeaderValue_ContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) ;
			for(String key : paramMap.keySet())
			{
				String[] values = paramMap.get(key).toArray(JCommon.sEmptyStringArray) ;
				builder.queryParam(key, values) ;
			}
		}
		paramMap = aReq.getHeaderMap() ;
		if(paramMap != null)
		{
			builder.contentType(aReq.getHeaderValue_ContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) ;
			for(String key : paramMap.keySet())
			{
				String[] values = paramMap.get(key).toArray(JCommon.sEmptyStringArray) ;
				builder.header(key, (Object[])values) ;
			}
		}
		Object entity = aReq.getRawEntity() ;
		if(entity != null && entity instanceof FlexibleBInputStream)
		{
			builder.content(((FlexibleBInputStream)entity).getBufData()) ;
		}
		return builder.header("invoke-scope", wsId) ;
	}

}
