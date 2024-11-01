package team.sailboat.commons.fan.infc;

@FunctionalInterface
public interface EConsumer3<T1 , T2 , T3 , X extends Exception>
{
	void accept(T1 t1 , T2 t2 , T3 t3) throws X ;
}