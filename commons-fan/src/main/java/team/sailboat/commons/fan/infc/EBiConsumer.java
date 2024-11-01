package team.sailboat.commons.fan.infc;

@FunctionalInterface
public interface EBiConsumer<T , U , X extends Throwable>
{
	void accept(T t, U u) throws X ;
}
