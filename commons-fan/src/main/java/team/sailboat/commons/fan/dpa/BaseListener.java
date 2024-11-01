package team.sailboat.commons.fan.dpa;

public abstract class BaseListener implements Listener
{
	boolean mDestroyed = false ;
	
	public boolean isDestroyed()
	{
		return mDestroyed ;
	}
	
	public void destroy()
	{
		mDestroyed = true ;
	}
}
