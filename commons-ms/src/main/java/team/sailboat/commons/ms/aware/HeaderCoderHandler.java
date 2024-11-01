package team.sailboat.commons.ms.aware;

import java.net.URLDecoder;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.RequestHeaderMethodArgumentResolver;

public class HeaderCoderHandler extends RequestHeaderMethodArgumentResolver
{
//	final URLCoder mCoder = URLCoder.getDefault() ;
	
	public HeaderCoderHandler(ConfigurableBeanFactory aBeanFactory)
	{
		super(aBeanFactory);
	}

	
	@Override
	protected Object resolveName(String aName, MethodParameter aParameter, NativeWebRequest aRequest) throws Exception
	{
		//这个地方认为不可能出现用中文做参数名的情况
//		String codedName = mCoder.encodeHeader(aName) ;
//		Object result = super.resolveName(codedName, aParameter, aRequest);
		Object result = super.resolveName(aName, aParameter, aRequest);
		if(result == null)
			return null ;
		if(result instanceof String)
			return URLDecoder.decode((String)result, "UTF-8") ;
		else if(result instanceof String[])
		{
			String[] strs = (String[])result ;
			for(int i=0 ; i<strs.length ; i++)
			{
				strs[i] = URLDecoder.decode(strs[i], "UTF-8") ;
			}
			return strs ;
		}
		else
			return result ;
	}
}
