package team.sailboat.commons.fan.collection;

import java.util.Queue;

public interface ICircularBoundedQueue<E> extends BoundedCollection<E> , Queue<E>
{
	E[] poll(int aLen , Class<?> aComponentClass) ;
	
	E get(int index) ;
	
	E[] get(int aOffset , int aLen , Class<?> aComponentType) ;
}
