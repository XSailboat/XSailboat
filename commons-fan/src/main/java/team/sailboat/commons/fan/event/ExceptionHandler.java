package team.sailboat.commons.fan.event;

public abstract class ExceptionHandler implements IExceptionHandler
{
	protected boolean mEnabled = true ;

	@Override
	public void setEnabled(boolean aEnabled)
	{
		mEnabled = aEnabled ;
	}

	@Override
	public boolean isEnabled()
	{
		return mEnabled ;
	}

}
