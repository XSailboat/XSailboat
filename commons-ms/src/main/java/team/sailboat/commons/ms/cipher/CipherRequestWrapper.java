package team.sailboat.commons.ms.cipher;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.http.IDecoder;

public class CipherRequestWrapper extends HttpServletRequestWrapper
{
	
	Map<String, String[]> mParamMap ;
	
	IDecoder mDecoder ;

	public CipherRequestWrapper(HttpServletRequest aRequest , IDecoder aDecoder)
	{
		super(aRequest);
		mDecoder = aDecoder ;
	}
	
	@Override
	public String getParameter(String aName)
	{
		String[] vals = getParameterMap().get(aName) ;
		return XC.isEmpty(vals)?null:vals[0] ;
	}
	
	@Override
	public Map<String, String[]> getParameterMap()
	{
		if(mParamMap == null)
		{
			Map<String, String[]> sourceMap = super.getParameterMap() ;
			if(XC.isEmpty(sourceMap))
			{
				mParamMap = Collections.emptyMap() ;
			}
			else
			{
				// 需要对内容进行解密
				mParamMap = XC.linkedHashMap() ;
				for(Entry<String , String[]> entry : sourceMap.entrySet())
				{
					String[] sourceVals = entry.getValue() ;
					if(XC.isEmpty(sourceVals))
						mParamMap.put(entry.getKey() , sourceVals) ;
					else
					{
						String[] vals = new String[sourceVals.length] ;
						for(int i=0 ; i<sourceVals.length ; i++)
							vals[i] = mDecoder.decode(sourceVals[i]) ;
						mParamMap.put(entry.getKey() , vals) ;
 					}
				}
			}
		}
		return mParamMap ;
	}
	
	@Override
	public String[] getParameterValues(String aName)
	{
		return getParameterMap().get(aName) ;
	}
	
	@Override
	public ServletInputStream getInputStream() throws IOException
	{
		return new ServletInputStreamWrapper(super.getInputStream() , mDecoder) ;
	}

}
