package team.sailboat.commons.fan.adapter;

import java.lang.reflect.Array;

import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.lang.JCommon;

public class TA_Strings implements ITypeAdapter<String[]>
{

	@Override
	public String[] apply(Object aT)
	{
		if(aT == null)
			return null ;
		if(aT.getClass().isArray())
		{
			int len = Array.getLength(aT) ;
			String[] result = new String[len] ;
			for(int i=0 ; i<len ; i++)
			{
				result[i] = JCommon.toString(Array.get(aT , i)) ;
			}
			return result ;
		}
		else
		{
			String str = aT.toString() ;
			return PropertiesEx.split(str) ;
		}
	}

	@Override
	public Class<String[]> getType()
	{
		return String[].class ;
	}

}