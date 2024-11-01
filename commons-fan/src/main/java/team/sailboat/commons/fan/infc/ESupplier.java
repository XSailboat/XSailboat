package team.sailboat.commons.fan.infc;

public interface ESupplier<T , X extends Throwable>
{
	T get() throws X ;
}
