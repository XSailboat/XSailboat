package team.sailboat.commons.fan.adapter;

import java.lang.reflect.Array;
import java.util.Date;

import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.time.XTime;

public class TA_String implements ITypeAdapter<String>
{

	@Override
	public String apply(Object aT)
	{
		if(aT == null)
			return null ;
		if(aT instanceof String)
			return (String)aT ;
		if(aT.getClass().isArray())
		{
			if(aT instanceof byte[])
			{
				return new String((byte[])aT) ;
			}
			else
			{
				StringBuilder strBld = new StringBuilder() ;
				int len = Array.getLength(aT) ;
				for(int i=0 ; i<len ; i++)
				{
					if(i>0)
						strBld.append(',') ;
					strBld.append(JCommon.toString(Array.get(aT, i))) ;
				}
				return strBld.toString() ;
			}
		}
		if(aT instanceof Date)
			return XTime.format$yyyyMMddHHmmssSSS((java.util.Date) aT, "1970-01-01 00:00:00.000") ;
		return aT.toString() ;
	}

	@Override
	public Class<String> getType()
	{
		return String.class ;
	}

}