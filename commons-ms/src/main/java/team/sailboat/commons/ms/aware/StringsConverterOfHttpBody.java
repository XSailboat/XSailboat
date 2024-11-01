package team.sailboat.commons.ms.aware;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.lang.XClassUtil;

public class StringsConverterOfHttpBody extends AbstractHttpMessageConverter<String[]>
{
	
	

	public StringsConverterOfHttpBody()
	{
		super(AppContext.sUTF8 , MediaType.TEXT_PLAIN) ;
	}
	
	@Override
	protected boolean supports(Class<?> aClazz)
	{
		return aClazz.isArray() && XClassUtil.isBasicJavaDataType(aClazz.getComponentType()) ;
	}

	@Override
	protected String[] readInternal(Class<? extends String[]> aClazz, HttpInputMessage aInputMessage)
			throws IOException, HttpMessageNotReadableException
	{
		char[] buf = new char[10240] ;
		try(InputStreamReader reader = new InputStreamReader(aInputMessage.getBody() , AppContext.sUTF8))
		{
			int len = 0 ;
			List<String> list = XC.arrayList() ;
			StringBuilder strBld = new StringBuilder() ;
			while((len = reader.read(buf)) != -1)
			{
				for(int i=0 ; i<len ; i++)
				{
					if(buf[i] != ',')
						strBld.append(buf[i]) ;
					else
					{
						if(strBld.length()>0)
							list.add(strBld.toString()) ;
						strBld.setLength(0) ;
					}
				}		
			}
			if(strBld.length()>0)
				list.add(strBld.toString()) ;
			return list.toArray(JCommon.sEmptyStringArray) ;
		}
	}

	@Override
	protected void writeInternal(String[] aT, HttpOutputMessage aOutputMessage)
			throws IOException, HttpMessageNotWritableException
	{
	}

	
}
