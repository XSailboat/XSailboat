package team.sailboat.commons.fan.collection;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * 具有读写锁的List
 *
 * @author yyl
 * @version 1.0 
 * @since 2014-3-25
 */
public class XRWLockList<E> extends AbstractList<E>
{
	public final ReadLock mReadLock ;
	public final WriteLock mWriteLock ;
	ReentrantReadWriteLock mLock ;
	
	protected ArrayList<E> mEles ;
	
	public XRWLockList()
	{
		mLock = new ReentrantReadWriteLock() ;
		mReadLock = mLock.readLock() ;
		mWriteLock = mLock.writeLock() ;
		mEles = new ArrayList<E>() ; ;
	}
	
	public XRWLockList(int aInitCap)
	{
		mLock = new ReentrantReadWriteLock() ;
		mReadLock = mLock.readLock() ;
		mWriteLock = mLock.writeLock() ;
		mEles = new ArrayList<E>(aInitCap) ;
	}
	
	public E getFirst()
	{
		return mEles.isEmpty()?null:mEles.get(0) ;
	}
	
	public E getLast()
	{
		return mEles.isEmpty()?null:mEles.get(mEles.size()-1) ;
	}
	
	@Override
	public int size()
	{
		return mEles.size() ;
	}
	
	@Override
	public boolean isEmpty()
	{
		return mEles.isEmpty() ;
	}

	/**
	 * 线程安全的
	 */
	@Override
	public boolean contains(Object aO)
	{
		mReadLock.lock() ;
		try
		{
			return mEles.contains(aO) ;
		}
		finally
		{
			mReadLock.unlock() ;
		}
	}

	/**
	 * 不能使用该迭代器对列表进行结构性修改
	 */
	@Override
	public Iterator<E> iterator()
	{
		return (Iterator<E>) mEles.iterator() ;
	}

	@Override
	public Object[] toArray()
	{
		mReadLock.lock() ;
		try
		{
			return mEles.toArray() ;
		}
		finally
		{
			mReadLock.unlock() ;
		}
	}

	@Override
	public <T> T[] toArray(T[] aA)
	{
		mReadLock.lock() ;
		try
		{
			return mEles.toArray(aA) ;
		}
		finally
		{
			mReadLock.unlock() ;
		}
	}

	@Override
	public boolean add(E aE)
	{
		if(aE==null)
			throw new IllegalArgumentException("不能为null") ;
		mWriteLock.lock() ;
		try
		{
			return mEles.add(aE) ;
		}
		finally
		{
			mWriteLock.unlock() ;
		}
	}

	@Override
	public boolean remove(Object aO)
	{
		mWriteLock.lock() ;
		try
		{
			return mEles.remove(aO) ;
		}
		finally
		{
			mWriteLock.unlock() ;
		}
	}

	@Override
	public boolean containsAll(Collection<?> aC)
	{
		mReadLock.lock() ;
		try
		{
			return mEles.containsAll(aC) ;
		}
		finally
		{
			mReadLock.unlock() ;
		}
	}

	@Override
	public boolean addAll(Collection<? extends E> aC)
	{
		mWriteLock.lock() ;
		try
		{
			return mEles.addAll(aC) ;
		}
		finally
		{
			mWriteLock.unlock() ;
		}
	}

	@Override
	public boolean addAll(int aIndex, Collection<? extends E> aC)
	{
		mWriteLock.lock() ;
		try
		{
			return mEles.addAll(aIndex , aC) ;
		}
		finally
		{
			mWriteLock.unlock() ;
		}
	}

	@Override
	public boolean removeAll(Collection<?> aC)
	{
		mWriteLock.lock() ;
		try
		{
			return mEles.removeAll(aC) ;
		}
		finally
		{
			mWriteLock.unlock() ;
		}
	}

	@Override
	public boolean retainAll(Collection<?> aC)
	{
		mWriteLock.lock() ;
		try
		{
			return mEles.retainAll(aC) ;
		}
		finally
		{
			mWriteLock.unlock() ;
		}
	}

	@Override
	public void clear()
	{
		mWriteLock.lock() ;
		try
		{
			mEles.clear() ;
		}
		finally
		{
			mWriteLock.unlock() ;
		}
	}
	
	/**
	 * 不同步的
	 * @param aIndex
	 * @return
	 */
	public E getSwift(int aIndex)
	{
		return mEles.get(aIndex) ;
	}

	@Override
	public E get(int aIndex)
	{
		mReadLock.lock() ;
		try
		{
			return mEles.get(aIndex) ;
		}
		finally
		{
			mReadLock.unlock() ;
		}
	}

	@Override
	public E set(int aIndex, E aElement)
	{
		mWriteLock.lock() ;
		try
		{
			return mEles.set(aIndex, aElement) ;
		}
		finally
		{
			mWriteLock.unlock() ;
		}
	}

	@Override
	public void add(int aIndex, E aElement)
	{
		mWriteLock.lock() ;
		try
		{
			mEles.add(aIndex, aElement) ;
		}
		finally
		{
			mWriteLock.unlock() ;
		}
	}

	@Override
	public E remove(int aIndex)
	{
		mWriteLock.lock() ;
		try
		{
			return mEles.remove(aIndex) ;
		}
		finally
		{
			mWriteLock.unlock() ;
		}
	}

	@Override
	public int indexOf(Object aO)
	{
		mReadLock.lock() ;
		try
		{
			return mEles.indexOf(aO) ;
		}
		finally
		{
			mReadLock.unlock() ;
		}
	}

	@Override
	public int lastIndexOf(Object aO)
	{
		mReadLock.lock() ;
		try
		{
			return mEles.lastIndexOf(aO) ;
		}
		finally
		{
			mReadLock.unlock() ;
		}
	}

	@Override
	public ListIterator<E> listIterator()
	{
		return (ListIterator<E>) mEles.listIterator() ;
	}

	@Override
	public ListIterator<E> listIterator(int aIndex)
	{
		return (ListIterator<E>) mEles.listIterator(aIndex) ;
	}

	@Override
	public List<E> subList(int aFromIndex, int aToIndex)
	{
		mReadLock.lock() ;
		try
		{
			return (List<E>) mEles.subList(aFromIndex, aToIndex) ;
		}
		finally
		{
			mReadLock.unlock() ;
		}
	}
}
