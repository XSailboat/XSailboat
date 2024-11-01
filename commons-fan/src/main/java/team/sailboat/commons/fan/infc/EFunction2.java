package team.sailboat.commons.fan.infc;

import java.util.function.BiFunction;

import team.sailboat.commons.fan.excep.WrapException;

@FunctionalInterface
public interface EFunction2<T1 , T2 , R , X extends Throwable>
{
	R apply(T1 t1 , T2 t2) throws X ;
	
	public static <T1 , T2 , R , E extends Throwable> BiFunction<T1 , T2 , R> silence(EFunction2<T1 , T2 , R, E> aE)
    {
    	return (t1, t2)->{
    		
			try
			{
				return aE.apply(t1 , t2) ;
			}
			catch(Throwable e)
    		{
    			WrapException.wrapThrow(e) ;
    			return null ;				//dead code
    		}
    	} ;
    }
}