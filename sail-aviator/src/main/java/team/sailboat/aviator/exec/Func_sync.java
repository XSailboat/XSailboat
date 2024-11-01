package team.sailboat.aviator.exec;

import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;

import team.sailboat.commons.fan.excep.WrapException;

public class Func_sync extends AbstractFunction
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aArg1 , AviatorObject aFunc)
	{
		final Object mutex = aArg1.getValue(aEnv) ;
		if(mutex == null || !(aFunc instanceof AviatorFunction))
		{
			return aFunc ;
		}
		else
		{
			synchronized (mutex)
			{
				try
				{
					return ((AviatorFunction)aFunc).call() ;
				}
				catch (Exception e)
				{
					WrapException.wrapThrow(e) ;
					return null ;			// dead code
				}
			}
		}
	}

	@Override
	public String getName()
	{
		return "sync" ;
	}

}