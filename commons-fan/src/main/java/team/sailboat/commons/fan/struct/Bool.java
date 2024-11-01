package team.sailboat.commons.fan.struct;

public class Bool
{
	boolean mVal ;
	
	public Bool()
	{}
	
	public Bool(boolean aVal)
	{
		mVal = aVal ;
	}
	
	public Bool bOR(boolean aVal)
	{
		mVal |= aVal ;
		return this ;
	}
	
	public Bool bAND(boolean aVal)
	{
		mVal &= aVal ;
		return this ;
	}
	
	public boolean get()
	{
		return mVal ;
	}
	
	public boolean isTrue()
	{
		return mVal ;
	}
	
	public boolean isFalse()
	{
		return !mVal ;
	}
	
	public void set(boolean aVal)
	{
		mVal = aVal ;
	}
}
