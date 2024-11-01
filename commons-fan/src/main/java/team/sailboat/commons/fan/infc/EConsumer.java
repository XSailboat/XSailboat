package team.sailboat.commons.fan.infc;

import java.util.function.Consumer;

import team.sailboat.commons.fan.excep.WrapException;

@FunctionalInterface
public interface EConsumer<T , X extends Throwable>
{
	void accept(T t) throws X ;
	
	public static <T , X extends Throwable> Consumer<T> noException(EConsumer<T, X> aEConsumer)
	{
		return (t)->{
			try
			{
				aEConsumer.accept(t);
			}
			catch (Throwable e)
			{
				throw new WrapException(e) ;
			}
		} ;
	}
}
