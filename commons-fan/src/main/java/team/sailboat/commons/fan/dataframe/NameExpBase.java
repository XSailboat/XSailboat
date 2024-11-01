package team.sailboat.commons.fan.dataframe;

public abstract class NameExpBase implements NameExp
{
	protected String mName ;
	protected String mDataType ;
	
	public NameExpBase(String aName , String aDataType)
	{
		mName = aName ;
		mDataType = aDataType ;
	}
	
	@Override
	public String getName()
	{
		return mName;
	}
	
	@Override
	public String getDataType()
	{
		return mDataType ;
	}
}
