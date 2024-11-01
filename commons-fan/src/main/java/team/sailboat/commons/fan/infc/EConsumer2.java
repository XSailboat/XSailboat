package team.sailboat.commons.fan.infc;

@FunctionalInterface
public interface EConsumer2<T1 , T2 , X extends Exception>
{
	void accept(T1 t1 , T2 t2) throws X ;
}