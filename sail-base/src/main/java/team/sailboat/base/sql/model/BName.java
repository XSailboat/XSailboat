package team.sailboat.base.sql.model;

import java.util.function.Function;

import team.sailboat.commons.fan.lang.JCommon;

public class BName
{
	String mPrefix ;
	
	String mLocalName ;
	
	String mName ;
	
	public BName()
	{
	}
	
	public BName(String aPrefix , String aLocalName)
	{
		mPrefix = aPrefix ;
		mLocalName = aLocalName ;
		mName = (mPrefix==null?"":mPrefix)+"."+(mLocalName == null?"":mLocalName) ;
	}
	
	public BName prefix(String aPrefix)
	{
		if(JCommon.unequals(aPrefix , mPrefix))
			return new BName(aPrefix, mLocalName) ;
		return this ;
	}
	
	public String getPrefix()
	{
		return mPrefix;
	}
	
	public BName transform(Function<String, String> aPrefixFunc , Function<String, String> aLocalNameFunc)
	{
		String prefix = null ;
		String localName = null ;
		if(aPrefixFunc != null)
			prefix = aPrefixFunc.apply(mPrefix) ;
		
		if(aLocalNameFunc != null)
			localName = aLocalNameFunc.apply(mLocalName) ;
		if(JCommon.unequals(prefix , mPrefix) || JCommon.unequals(localName, mLocalName))
			return new BName(prefix, localName) ;
		return this ;
	}
	
	public String getLocalName()
	{
		return mLocalName;
	}
	
	public String getName()
	{
		return mName ;
	}
	
	@Override
	public int hashCode()
	{
		return getName().hashCode() ;
	}
	
	@Override
	public String toString()
	{
		return getName() ;
	}
	
	@Override
	public boolean equals(Object aObj)
	{
		if(aObj instanceof BName)
			return toString().equals(aObj.toString()) ;
		else if(aObj instanceof String)
			return toString().equals((String)aObj) ;
		else
			return false ;
	}
}
