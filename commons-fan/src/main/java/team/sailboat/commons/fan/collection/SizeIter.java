package team.sailboat.commons.fan.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.IntSupplier;

import team.sailboat.commons.fan.lang.JCommon;

public interface SizeIter<T> extends Iterable<T>
{
	static SizeIter sEmptyIter = new EmptyIter<>() ;
	
	int size() ;
	boolean isEmpty() ;
	
	@SuppressWarnings("unchecked")
	default <U> U[] toArray(U[] aArray)
	{
		int size = size() ;
		if (aArray.length < size)
            aArray = (U[])java.lang.reflect.Array.newInstance(
                                aArray.getClass().getComponentType(), size);
        int i = 0;
        Iterator<T> it = iterator() ;
		while(it.hasNext() && i<size)
		{
			aArray[i++] = (U) it.next() ;
		}
        
        if (aArray.length > size)
            aArray[size] = null;
        
        return aArray;
	}
	
	default boolean contains(T aEle)
	{
		for(T ele : this)
		{
			if(JCommon.equals(aEle, ele))
				return true ;
		}
		return false ;
	}
	
	
	@SafeVarargs
	public static <T> SizeIter<T> create(T... aArray)
	{
		return new ArrayIterator<>(aArray) ;
	}
	
	public static <T> SizeIter<T> create(Collection<T> aC)
	{
		return new _SIter<>(aC, aC::size) ;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> SizeIter<T> emptyIter()
	{
		return sEmptyIter ;
	}
	
	public static <T> SizeIter<T> createUnmodified(Collection<T> aC)
	{
		return new _SIter<>(aC, aC::size , true) ;
	}
	
	static class _UnmodifiedIt<T> implements Iterator<T>
	{
		Iterator<T> mIt ;

		public _UnmodifiedIt(Iterator<T> aIt)
		{
			mIt = aIt ;
		}
		
		@Override
		public boolean hasNext()
		{
			return mIt.hasNext() ;
		}

		@Override
		public T next()
		{
			return mIt.next() ;
		}
	}
	
	static class _SIter<T> implements SizeIter<T>
	{
		Iterable<T> mIt ;
		IntSupplier mSizeSupplier ;
		boolean mWrapUnmodify = false ;
		
		public _SIter(Iterable<T> aIt , IntSupplier aSizeSupplier)
		{
			this(aIt , aSizeSupplier , false) ;
		}
		
		public _SIter(Iterable<T> aIt , IntSupplier aSizeSupplier , boolean aWrapUnmodify)
		{
			mIt = aIt ;
			mSizeSupplier = aSizeSupplier ;
			mWrapUnmodify = aWrapUnmodify ;
		}

		@Override
		public Iterator<T> iterator()
		{
			return mWrapUnmodify?new _UnmodifiedIt<>(mIt.iterator()):mIt.iterator() ;
		}

		@Override
		public int size()
		{
			return mSizeSupplier.getAsInt() ;
		}

		@Override
		public boolean isEmpty()
		{
			return mSizeSupplier.getAsInt() == 0 ;
		}
		
	}
	
	static class EmptyIter<E> implements Iterator<E> , SizeIter<E>
	{
		
		public EmptyIter()
		{
		}
		
		@Override
		public boolean hasNext()
		{
			return false ;
		}

		@Override
		public E next()
		{
			throw new IllegalStateException("没有下一个元素！") ;
		}

		@Override
		public void remove()
		{
			throw new IllegalStateException("不支持此方法") ;
		}

		@Override
		public Iterator<E> iterator()
		{
			return this ;
		}

		@Override
		public int size()
		{
			return 0 ;
		}

		@Override
		public boolean isEmpty()
		{
			return true ;
		}

	}
}
