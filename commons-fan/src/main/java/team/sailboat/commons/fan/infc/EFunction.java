package team.sailboat.commons.fan.infc;

import java.util.function.Function;

import team.sailboat.commons.fan.excep.WrapException;

@FunctionalInterface
public interface EFunction<T , R , X extends Throwable> {

    /**
     * Applies this function to the given argument.
     *
     * @param value the function argument
     * @return the function result
     */
    R apply(T value) throws X ;
    
    public static <T , R , E extends Throwable> Function<T , R> silence(EFunction<T, R, E> aE)
    {
    	return (t)->{
    		
			try
			{
				return aE.apply(t) ;
			}
			catch(Throwable e)
    		{
    			WrapException.wrapThrow(e) ;
    			return null ;				//dead code
    		}
    	} ;
    }
}