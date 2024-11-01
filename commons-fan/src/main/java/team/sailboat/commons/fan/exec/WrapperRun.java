package team.sailboat.commons.fan.exec;

class WrapperRun implements CRun
{
	
	ThreadContextServant mServant ;
	Runnable mWorker ;
	
	WrapperRun(ThreadContextServant aServant , Runnable aWorker)
	{
		mServant = aServant ;
		mWorker = aWorker ;
	}

	@Override
	public void run()
	{
		if(isClosing())
			return ;
		mServant.init();
		try
		{
			mWorker.run();
		}
		finally
		{
			mServant.destroy() ;
		}
	}

}
