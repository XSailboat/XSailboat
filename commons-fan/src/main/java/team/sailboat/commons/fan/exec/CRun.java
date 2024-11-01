package team.sailboat.commons.fan.exec;

public interface CRun extends Runnable
{
	default boolean isClosing()
	{
		return CommonExecutor.isClosed() ;
	}
	
	public static Runnable wrap(Runnable aRun)
	{
		if(aRun == null)
			return null ;
		if(aRun instanceof CRun)
			return aRun ;
		
		return new CRunWrapper(aRun) ;
	}
	
	static class CRunWrapper implements CRun
	{
		final Runnable mRun ;
		
		public CRunWrapper(Runnable aRun)
		{
			mRun = aRun ;
		}
		
		@Override
		public void run()
		{
			if(!isClosing())
				mRun.run();
		}
	}

	
}
