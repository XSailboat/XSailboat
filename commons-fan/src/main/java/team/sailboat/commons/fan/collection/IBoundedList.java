package team.sailboat.commons.fan.collection;

import java.util.List;

public interface IBoundedList<E> extends List<E> , BoundedCollection<E>
{
	E getLast() ;
	
	E getFirst() ;
	
	E removeFirst() ;
}
