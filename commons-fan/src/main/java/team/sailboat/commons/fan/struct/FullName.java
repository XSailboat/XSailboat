package team.sailboat.commons.fan.struct;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.lang.JCommon;

abstract class FullName
{
	final String mJoin ;
	
	String mPrefix ;
	
	String mLocalName ;
	
	String mName ;
	
	public FullName(String aJoin)
	{
		mJoin = aJoin ;
	}
	
	public FullName(String aJoin , String aPrefix , String aLocalName)
	{
		mJoin = aJoin ;
		mPrefix = aPrefix ;
		mLocalName = aLocalName ;
		mName = (mPrefix==null?"":mPrefix)+mJoin+(mLocalName == null?"":mLocalName) ;
	}
	
	public FullName(String aJoin , String aFullName)
	{
		mJoin = aJoin ;
		int i = aFullName.indexOf(mJoin) ;
		if(i == -1)
		{
			mPrefix = null ;
			mLocalName = aFullName ;
		}
		else
		{
			mPrefix = aFullName.substring(0, i) ;
			int start2 = i+aJoin.length() ;
			mLocalName = start2<aFullName.length()?aFullName.substring(start2):"" ;
		}
		mName = (mPrefix==null?"":mPrefix)+mJoin+(mLocalName == null?"":mLocalName) ;
	}
	
	public FullName prefix(String aPrefix)
	{
		if(JCommon.unequals(aPrefix , mPrefix))
			return newInstance(aPrefix, mLocalName) ;
		return this ;
	}
	
	public String getPrefix()
	{
		return mPrefix;
	}
	
	protected FullName newInstance(String aPrefix , String aLocalName)
	{
		try
		{
			return getClass().getConstructor(String.class , String.class).newInstance(aPrefix, aLocalName) ;
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e)
		{
			WrapException.wrapThrow(e) ;
			return null ;			// dead code
		}
	}
	
	protected  FullName transform(Function<String, String> aPrefixFunc , Function<String, String> aLocalNameFunc)
	{
		String prefix = null ;
		String localName = null ;
		if(aPrefixFunc != null)
			prefix = aPrefixFunc.apply(mPrefix) ;
		
		if(aLocalNameFunc != null)
			localName = aLocalNameFunc.apply(mLocalName) ;
		if(JCommon.unequals(prefix , mPrefix) || JCommon.unequals(localName, mLocalName))
			return newInstance(prefix, localName) ;
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
		if(aObj instanceof FullName)
			return toString().equals(aObj.toString()) ;
		else if(aObj instanceof String)
			return toString().equals((String)aObj) ;
		else
			return false ;
	}
}
