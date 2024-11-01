package team.sailboat.commons.fan.infc;

public interface EPredicate<T , X extends Throwable>
{
	 boolean test(T t) throws X ;
}
