package team.sailboat.commons.fan.jfilter;

public class FilterField
{
	String mName ;
	String mType ;
	Object[] mArgs ;
	
	public FilterField()
	{
	}
	
	public FilterField(String aName)
	{
		mName = aName ;
	}
	
	public String getName()
	{
		return mName;
	}
	
	public String getType()
	{
		return mType;
	}
}
