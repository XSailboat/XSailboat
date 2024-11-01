package team.sailboat.commons.fan.collection;

import java.util.ArrayList;

public class ExtensibleList<E> extends ArrayList<E>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ExtensibleList()
	{
		super() ;
	}
	
	public ExtensibleList(int aCapacity)
	{
		super(aCapacity) ;
	}
	
	public E set(int index, E element)
	{
		if(index == size())
		{
			add(element) ;
			return null ;
		}
		else if(index>size())
		{
			int size = size() ;
			for(int i=size ; i<index ; i++)
				add(null) ;
			add(element) ;
			return null ;
		}
		else
			return super.set(index, element) ;
	}

}
