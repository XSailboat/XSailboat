package team.sailboat.commons.fan.collection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import team.sailboat.commons.fan.lang.XClassUtil;

/**
 * 自动维护次序。元素的排列符合Z序，在指定位置的添加时，最终结果会受到Z序影响
 * ，而不是一般List的添加结果
 *
 * @author yyl
 * @version 1.0 
 * @since 2014-3-25
 */
public class IndexedList<E> extends XRWLockList<E> implements Cloneable
{
	public static final int sType_Method = 0 ;
	public static final int sType_Field = 1 ;
	
	int mType = 0 ;
	String mName ;
	Method mSetterMethod ;
	Method mGetterMethod ;
	Field mField ;
	/**
	 * 
	 * @param aType
	 * @param aName			如果是方法
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws NoSuchFieldException 
	 */
	public IndexedList(int aType , String aName) throws NoSuchMethodException, SecurityException, NoSuchFieldException
	{
		super() ;
		construct(aType, aName) ;
	}
	
	public IndexedList(int aType , String aName , int aInitCap) throws NoSuchMethodException, SecurityException, NoSuchFieldException
	{
		super(aInitCap) ;
		construct(aType, aName) ;
	}
	
	protected void construct(int aType , String aName) throws NoSuchMethodException, SecurityException, NoSuchFieldException
	{
		mType = aType ;
		mName = aName ;
		if(mType != sType_Method && mType != sType_Field)
			throw new IllegalArgumentException("不合法的类型") ;
		Class<?> clazz = mEles.getClass().getComponentType() ;
		if(clazz != null)
			init(clazz);
	}
	
	protected void init(Class<?> aClass) throws NoSuchMethodException, SecurityException, NoSuchFieldException
	{
		if(mType == sType_Method)
		{
			mSetterMethod = aClass.getMethod(XClassUtil.getMethodName("set" , mName) , Integer.TYPE) ;
			mGetterMethod = aClass.getMethod(XClassUtil.getMethodName("get", mName)) ;
			if(mSetterMethod == null || mGetterMethod == null)
				throw new IllegalStateException("没有同时具备指定名称的get/set方法") ;
		}
		else
		{
			mField = XClassUtil.getField(aClass , mName) ;			
			if(mField == null)
				throw new IllegalStateException(aClass.getName()+" 没有具备指定名称的字段") ;
			mField.setAccessible(true) ;
		}
	}
	
	protected void setIndex(Object aObj , int aIndex)
	{
		try
		{
			if(mField != null)
				mField.set(aObj, aIndex) ;
			else if(mSetterMethod != null)
				mSetterMethod.invoke(aObj, aIndex) ;
			else
			{
				init(aObj.getClass()) ;
				if(mField == null && mSetterMethod == null)
					throw new IllegalStateException() ;
				else
					setIndex(aObj ,  aIndex);
			}	
		}
		catch(Exception e)
		{
			throw new IllegalStateException(e) ;
		}
	}
	
	protected int getIndex(Object aObj)
	{
		try
		{
			if(mField != null)
				return mField.getInt(aObj) ;
			else
				return (Integer)mGetterMethod.invoke(aObj) ;
		}
		catch(Exception e)
		{
			throw new IllegalStateException(e) ;
		}
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
	
	public E getLast()
	{
		return mEles.size()>0?mEles.get(mEles.size()-1):null ;
	}

	@Override
	public boolean add(E aE)
	{
		if(aE==null)
			throw new IllegalArgumentException("不能为null") ;
		mWriteLock.lock() ;
		try
		{
			mEles.add(aE) ;
			setIndex(aE , mEles.size()-1) ;
			return true ;
		}
		finally
		{
			mWriteLock.unlock() ;
		}
	}
	
	protected void maintainIndex(int aFrom)
	{
		for(int i= aFrom ; i<mEles.size() ; i++)
			setIndex(mEles.get(i) , i) ;
	}

	@Override
	public boolean remove(Object aO)
	{
		mWriteLock.lock() ;
		try
		{
			int index = indexOf(aO) ;
			boolean result = false ;
			if(index != -1)
			{
				result = mEles.remove(aO) ;
				if(result)
				{
					for(int i = index ; i<mEles.size() ; i++)
					{
						setIndex(mEles.get(i) , i) ;
					}
				}
			}
			return result ;
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
			for(E e : aC)
			{
				mEles.add(e) ;
				setIndex(e , mEles.size()-1) ;
			}
			return true ;
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
			boolean result = mEles.addAll(aIndex, aC) ;
			maintainIndex(aIndex) ;
			return result ;
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
			boolean result = mEles.removeAll(aC) ;
			for(int i=0 ; i<mEles.size() ; i++)
			{
				setIndex(mEles.get(i) , i) ;
			}
			return result ;
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
			boolean result = mEles.retainAll(aC) ;
			for(int i=0 ; i<mEles.size() ; i++)
			{
				setIndex(mEles.get(i) , i) ;
			}
			return result ;
		}
		finally
		{
			mWriteLock.unlock() ;
		}
	}

	@Override
	public void clear()
	{
		mReadLock.lock() ;
		try
		{
			mEles.clear() ;
		}
		finally
		{
			mReadLock.unlock() ;
		}
	}

	@Override
	public E get(int aIndex)
	{
		mReadLock.lock() ;
		try
		{
			return (E) mEles.get(aIndex) ;
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
			if(aIndex>=0 && aIndex<mEles.size())
			{
				setIndex(aElement , aIndex) ;
				return (E)mEles.set(aIndex, aElement) ;
			}
			else
				return null ;
		}
		finally
		{
			mWriteLock.unlock() ;
		}
	}

	/**
	 * 线程安全的
	 */
	@Override
	public void add(int aIndex, E aE)
	{
		mWriteLock.lock() ;
		try
		{
			mEles.add(aE) ;
			setIndex(aE , mEles.size()-1) ;
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
			E e = (E) mEles.remove(aIndex) ;
			maintainIndex(aIndex) ;
			return e ;
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

	/**
	 * 不能用该迭代器对列表进行结构性修改
	 */
	@Override
	public ListIterator<E> listIterator()
	{
		return (ListIterator<E>) mEles.listIterator() ;
	}
	
	/**
	 * 不能用该迭代器对列表进行结构性修改
	 */
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
	
	@Override
	public IndexedList<E> clone()
	{
		IndexedList<E> list;
		try
		{
			list = new IndexedList<E>(mType , mName , Math.max(8, mEles.size()));
			for(E e : mEles)
				list.mEles.add(e) ;
			return list ;
		}
		catch (Exception e1)
		{
			return null ;
		}
	}
}
