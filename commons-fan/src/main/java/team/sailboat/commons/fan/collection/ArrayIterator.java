package team.sailboat.commons.fan.collection;

import java.util.Iterator;

public class ArrayIterator<E> implements SizeIter<E>
{
	
	final E[] mSource ;
	final int mSize ;
	
	public ArrayIterator(E[] aArray)
	{
		mSource = aArray ;
		mSize = mSource==null?0:mSource.length ;
	}

	@Override
	public Iterator<E> iterator()
	{
		return new _Iterator() ;
	}

	@Override
	public int size()
	{
		return mSize ;
	}

	@Override
	public boolean isEmpty()
	{
		return mSize == 0 ;
	}
	
	class _Iterator implements Iterator<E>
	{
		int mIndex = 0 ;
		
		@Override
		public boolean hasNext()
		{
			return mIndex<mSize ;
		}

		@Override
		public E next()
		{
			return mSource[mIndex++] ;
		}

		@Override
		public void remove()
		{
			throw new IllegalStateException("不支持此方法") ;
		}
	}

}
