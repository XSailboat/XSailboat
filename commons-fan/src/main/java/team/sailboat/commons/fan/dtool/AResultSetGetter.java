package team.sailboat.commons.fan.dtool;

public abstract class AResultSetGetter implements IResultSetGetter
{
	int mIndex ;
	
	public AResultSetGetter(int aIndex)
	{
		mIndex = aIndex ;
	}
	
	public abstract AResultSetGetter clone() ;
	
	@Override
	public void setIndex(int aIndex)
	{
		mIndex = aIndex ;
	}
}