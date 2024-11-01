package team.sailboat.commons.fan.infc;

@FunctionalInterface
public interface EqualsPredicate<T>
{
	 boolean equals(T t1 , T t2) ;
}
