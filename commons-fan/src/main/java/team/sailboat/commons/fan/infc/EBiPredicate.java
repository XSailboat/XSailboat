package team.sailboat.commons.fan.infc;

@FunctionalInterface
public interface EBiPredicate<T , U , X extends Throwable>
{
	 boolean test(T t, U u) throws X ;
}
