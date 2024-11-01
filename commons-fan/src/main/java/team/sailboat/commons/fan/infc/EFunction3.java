package team.sailboat.commons.fan.infc;

@FunctionalInterface
public interface EFunction3<T1 , T2 , T3 , R , X extends Exception>
{
	R apply(T1 t1 , T2 t2 , T3 t3) throws X ;
}