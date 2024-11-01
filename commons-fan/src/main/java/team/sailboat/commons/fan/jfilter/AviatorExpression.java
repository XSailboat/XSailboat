package team.sailboat.commons.fan.jfilter;

import java.util.Collections;
import java.util.Map;

import team.sailboat.commons.fan.collection.XC;

public class AviatorExpression
{
	String mValue ;
	
	Map<String , Object> mParamMap ;
	
	int mParamCount = 0 ;
	
	public AviatorExpression()
	{
	}
	
	public void setValue(String aValue)
	{
		mValue = aValue;
	}
	
	public String getValue()
	{
		return mValue;
	}
	
	public String addParam(Object aValue)
	{
		String paramName = "$_"+mParamCount++ ;
		if(mParamMap == null)
			mParamMap = XC.linkedHashMap() ;
		mParamMap.put(paramName, aValue) ;
		return paramName ;
	}
	
	public Map<String, Object> getParamMap()
	{
		return mParamMap == null?Collections.emptyMap():mParamMap ;
	}
	
	public static class Param
	{
		String mName ;
		
		Object mValue ;
		
		public Param(String aName , Object aValue)
		{
			mName = aName ;
			mValue = aValue ;
		}
		
		public String getName()
		{
			return mName;
		}
		
		public Object getValue()
		{
			return mValue;
		}
	}
	
}
