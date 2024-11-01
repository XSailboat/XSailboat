package team.sailboat.commons.ms.aware;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import team.sailboat.commons.fan.collection.XC;

public class ExtExcepMsgReturnConverter extends AbstractHttpMessageConverter<Map<String, ?>>
{

	public ExtExcepMsgReturnConverter()
	{
		super(Charset.forName("UTF-8") , MediaType.TEXT_PLAIN) ;
	}

	@Override
	protected boolean supports(Class<?> aClazz)
	{
		return Map.class.isAssignableFrom(aClazz) ;
	}

	@Override
	protected void writeInternal(Map<String, ?> aT, HttpOutputMessage aOutputMessage)
			throws IOException, HttpMessageNotWritableException
	{
		byte[] data = getContent(aT) ;
		if(XC.isNotEmpty(data))
			aOutputMessage.getBody().write(data);
	}

	@Override
	protected Long getContentLength(Map<String, ?> aT, MediaType aContentType) throws IOException
	{
		return Long.valueOf(XC.count(getContent(aT))) ;
	}
	
	protected byte[] getContent(Map<String, ?> aT)
	{
		byte[] data = (byte[])aT.get("messageInBytes") ;
		if(data == null)
		{
			String msg = (String)aT.get("message") ;
			data = msg.getBytes(getDefaultCharset()) ;
			((Map)aT).put("messageInBytes", data) ;
		}
		return data ;
	}

	@Override
	protected Map<String, ?> readInternal(Class<? extends Map<String, ?>> aClazz, HttpInputMessage aInputMessage)
			throws IOException, HttpMessageNotReadableException
	{
		return null;
	}

}
