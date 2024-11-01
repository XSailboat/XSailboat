package team.sailboat.commons.fan.collection;

import java.util.Collection;
import java.util.Iterator;

public abstract class SyncCollection<E> implements Collection<E>
{
	protected Collection<E> mC ;
	protected final Object mMutex = new Object() ;
	
	public SyncCollection(Collection<E> aC)
	{
		mC = aC ;
	}

	@Override
	public int size()
	{
		synchronized (mMutex)
		{
			return mC.size() ;
		}
	}

	@Override
	public boolean isEmpty()
	{
		synchronized(mMutex)
		{
			return mC.isEmpty() ;
		}
	}

	@Override
	public boolean contains(Object aO)
	{
		synchronized (mMutex)
		{
			return mC.contains(aO);
		}
	}

	@Override
	public Iterator<E> iterator()
	{
		synchronized (mMutex)
		{
			return mC.iterator();
		}
	}

	@Override
	public Object[] toArray()
	{
		synchronized (mMutex)
		{
			return mC.toArray() ;
		}
	}

	@Override
	public <T> T[] toArray(T[] aA)
	{
		synchronized (mMutex)
		{
			return mC.toArray(aA);
		}
	}

	@Override
	public boolean add(E aE)
	{
		synchronized (mMutex)
		{				
			return mC.add(aE);
		}
	}

	@Override
	public boolean remove(Object aO)
	{
		synchronized (mMutex)
		{
			return mC.remove(aO) ;				
		}
	}

	@Override
	public boolean containsAll(Collection<?> aC)
	{
		synchronized (mMutex)
		{				
			return mC.containsAll(aC) ;
		}
	}

	@Override
	public boolean addAll(Collection<? extends E> aC)
	{
		synchronized (mMutex)
		{
			return mC.addAll(aC) ;
		}
	}

	@Override
	public boolean removeAll(Collection<?> aC)
	{
		synchronized (mMutex)
		{				
			return mC.removeAll(aC);
		}
	}

	@Override
	public boolean retainAll(Collection<?> aC)
	{
		synchronized (mMutex)
		{
			return mC.retainAll(aC) ;
		}
	}

	@Override
	public void clear()
	{
		synchronized (mMutex)
		{
			mC.clear();
		}
	}
}
