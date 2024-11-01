package team.sailboat.commons.fan.event;


public abstract class XComplexLsn implements IXListener 
{

	boolean mDispose ;
	boolean mEnabled = true ;
	
	
	public void setEnabled(boolean aEnabled)
	{
		mEnabled = aEnabled ;
	}
	
	public boolean isEnabled()
	{
		return mEnabled ;
	}
	
	public void dispose()
	{
		mDispose = true ;
	}
	
	public boolean isDisposed()
	{
		return mDispose ;
	}
	
}
