package team.sailboat.commons.fan.infc;

@FunctionalInterface
public interface EPredicate2<T1 , T2 , X extends Exception>
{
	 boolean test(T1 t1 , T2 t2) throws X ;
}
