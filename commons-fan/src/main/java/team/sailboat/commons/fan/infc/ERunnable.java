package team.sailboat.commons.fan.infc;

import team.sailboat.commons.fan.excep.WrapException;

public interface ERunnable <X extends Throwable>
{
	void run() throws X ;
	
	public static <X extends Throwable> Runnable wrap(ERunnable<X> aERun)
	{
		return ()->{
			try
			{
				aERun.run();
			}
			catch(Throwable e)
			{
				WrapException.wrapThrow(e) ;
			}
		} ;
	}
}
