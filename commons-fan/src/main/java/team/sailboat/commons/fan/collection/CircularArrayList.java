package team.sailboat.commons.fan.collection;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;

public class CircularArrayList<E> extends AbstractList<E> implements IBoundedList<E>
{

	private transient E[] mEles;

	private transient int mStart = 0;

	private transient int mEnd = 0;

	private transient boolean mFull = false;

	int mCapacity ;
	
	boolean mAutoOverwrite = false ;

	/**
	 * 循环数组，当数组满以后，会自动从头覆盖
	 * @param aCapacity
	 */
	public CircularArrayList(int aCapacity)
	{
		this(aCapacity, true) ;
	}
	
	/**
	 * 
	 * @param aCapacity
	 * @param aAutoRewrite		如果设置为false，当循环数组满时，必须主动removeFirst，才能继续往里面添加
	 */
	@SuppressWarnings("unchecked")
	public CircularArrayList(int aCapacity , boolean aAutoOverwrite)
	{
		mEles = (E[]) new Object[aCapacity];
		mCapacity = aCapacity;
		mAutoOverwrite = aAutoOverwrite ;
	}

	@Override
	public boolean isFull()
	{
		return mFull;
	}
	
	/**
	 * 剩余容量
	 * @return
	 */
	public int remainCapacity()
	{
		if(mFull)
			return 0 ;
		if(mEnd>=mStart)
			return mCapacity - (mEnd-mStart) ;
		else
			return mStart-mEnd ;
	}

	@Override
	public boolean addAll(int aIndex, Collection<? extends E> aC)
	{
		Assert.notNull(aC);
		if(mAutoOverwrite)
			Assert.isTrue(aC.size() <= mCapacity, "参数指定的集合元素个 %1$d 超过了容量限制%2$d", aC.size(), mCapacity);
		else
		{
			int remain_capacity = remainCapacity() ;
			Assert.isTrue(aC.size() <= remain_capacity , "参数指定的集合元素个 %1$d 超过了剩余容量限制%2$d", aC.size(), remain_capacity);
		}
		aIndex %= mCapacity ;
			
		int i = (mStart + aIndex) % mCapacity;
		for (E ele : aC)
		{
			mEles[i++ % mCapacity] = ele;
		}
		int end = getVirtualEnd();
		if (i > end)
		{
			if (mFull)
				mStart = mEnd = i % mCapacity;
			else
			{
				mEnd = i % mCapacity;
				if (i - mStart >= mCapacity)
				{
					mFull = true;
					mStart = mEnd;
				}
			}
		}
		return true;
	}

	private int getVirtualEnd()
	{
		return mFull ? (mEnd <= mStart ? mEnd + mCapacity : mEnd) : (mEnd < mStart ? mEnd + mCapacity : mEnd);
	}

	@Override
	public E set(int aIndex, E aElement)
	{
		Assert.isTrue(aIndex < mCapacity && aIndex >= 0, "增加元素的位置%1$d 超过了容量%2$d", aIndex, mCapacity);
		int i = mStart + aIndex;
		int end = getVirtualEnd();
		if (i >= end)
		{
			if (i > end)
			{
				for (int k = end; k < i; k++)
					add(null);
			}
			add(aElement);
			return null;
		}
		else
		{
			E e = mEles[i % mCapacity];
			mEles[i % mCapacity] = aElement;
			return e;
		}
	}
	
	@Override
	public boolean add(E aE)
	{
		if(mAutoOverwrite || remainCapacity()>0)
		{
			_add(aE) ;
			return true ;
		}
		else
			return false ;
	}
	
	public E add_0(E aE)
	{
		if(mAutoOverwrite || remainCapacity()>0)
			return _add(aE);
		else
			throw new IllegalStateException("非AutoRewrite的循环数组，已经没有剩余容量继续添加元素") ;
	}
	
	private E _add(E aE)
	{
		E oldE = mEles[mEnd] ;
		mEles[mEnd] = aE ;
		if(mFull)
		{
			mStart = mEnd = toActualIndex(mEnd+1) ;
		}
		else
		{
			mEnd = toActualIndex(mEnd+1) ;
			if(mEnd == mStart)
				mFull = true ;
		}
		return oldE ;
	}

	@Override
	public void add(int aIndex, E aElement)
	{
		if(mAutoOverwrite || remainCapacity()>0)
		{
			aIndex %= mCapacity ;
			int size = size();
			Assert.isTrue(aIndex <= size, "插入元素的位置%1$d 越界%2$d", aIndex, size);
			if (aIndex == size)
				add(aElement);
			else
			{
				int end = getVirtualEnd();
				int i = end - 1;
				for (; i >= aIndex; i--)
				{
					mEles[(i + 1) % mCapacity] = mEles[i % mCapacity];
				}
				mEles[i] = aElement;
				if (mFull)
					mEnd = ++mStart;
				else
				{
					if (++mEnd == mStart)
						mFull = true;
				}
			}
		}
		else
			throw new IllegalStateException("非AutoRewrite的循环数组，已经没有剩余容量继续添加元素") ;
	}
	
	@Override
	public Object[] toArray()
	{
		int size = size() ;
		if(size == 0)
			return new Object[0] ;
		else
		{
			if(mEnd>mStart)
				return Arrays.copyOfRange(mEles, mStart, mEnd) ;
			else
			{
				Object[] array = new Object[size] ;
				System.arraycopy(mEles, mStart, array, 0, mCapacity-mStart);
				System.arraycopy(mEles, 0, array, mCapacity-mStart, mEnd) ;
				return array ;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] aA)
	{
		int size = size();
        T[] r = aA.length >= size ? aA :
                  (T[])java.lang.reflect.Array
                  .newInstance(aA.getClass().getComponentType(), size);
        if(size == 0)
        	return r ;
        else
        {
        	if(mEnd>mStart)
				System.arraycopy(mEles, mStart, r, 0, size) ;
			else
			{
				System.arraycopy(mEles, mStart, r, 0, mCapacity-mStart);
				System.arraycopy(mEles, 0, r, mCapacity-mStart, mEnd) ;
			}
        }
        return r ;
	}
	
	/**
	 * 移除头部的aNum个元素
	 * @param aNum
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public E[] removeHeads(int aNum)
	{
		if(aNum == 0)
			return (E[])new Object[0] ; 
		if(aNum == 1)
			return (E[])new Object[]{removeFirst()} ;
		int size = size() ;
		Assert.isTrue(size>=aNum && aNum>0);
		if(size == aNum)
		{
			E[] heads = (E[])toArray() ;
			clear();
			return heads ;
		}
		else
		{
			if(mEnd>mStart || mCapacity>=mStart+aNum)
			{
				E[] heads = Arrays.copyOfRange(mEles, mStart , mStart+aNum) ;
				Arrays.fill(mEles , mStart , mStart+aNum , null) ;
				mStart+=aNum ;
				return heads ;
			}
			else
			{
				E[] heads = (E[])new Object[aNum] ;
				System.arraycopy(mEles, mStart, heads, 0, mCapacity-mStart);
				Arrays.fill(mEles, mStart , mCapacity , null);
				int endPos = aNum+mStart-mCapacity ;
				System.arraycopy(mEles, 0, heads, mCapacity-mStart, endPos);
				Arrays.fill(mEles, 0 , endPos , null);
				return heads ;
			}
		}
	}
	
	@Override
	public void clear()
	{
		if(isEmpty())
			return ;
		if(mEnd>mStart)
			Arrays.fill(mEles , mStart , mEnd , null) ;
		else
		{
			Arrays.fill(mEles, mStart, mCapacity, null);
			Arrays.fill(mEles, 0 , mEnd , null);
		}
		mStart = mEnd = 0 ;
	}

	@Override
	public E remove(int aIndex)
	{
		if(aIndex == 0)
			return removeFirst() ;
		int size = size() ;
		Assert.betweenL_r(0, size, aIndex);
		int ai = toActualIndex(mStart+aIndex) ;
		final E ele = mEles[ai];
		if(ai == mEnd)
		{
			//移除的是最后一个
			mEnd = toActualIndex(--mEnd) ;
		}
		else
		{
			if(mEnd>mStart)
				System.arraycopy(mEles, ai+1, mEles, ai, mEnd-ai-1);
			else
			{
				if(mCapacity>ai+1)
					System.arraycopy(mEles, ai+1, mEles, ai, mCapacity-ai-1);
				mEles[mCapacity-1] = mEles[0] ;
				System.arraycopy(mEles, 1, mEles, 0, mEnd-1);
			}
		}
		mFull = false;
		return ele;
	}

	@Override
	public boolean isEmpty()
	{
		return !mFull && mStart == mEnd;
	}

	@Override
	public E removeFirst()
	{
		Assert.isTrue(!isEmpty());
		final E ele = mEles[mStart];
		mEles[mStart++] = null;
		if (mStart >= mCapacity)
			mStart = 0;
		mFull = false;
		return ele;
	}

	@Override
	public int indexOf(Object aO)
	{
		int end = getVirtualEnd();
		for (int i = mStart; i < end; i++)
			if (JCommon.equals(aO, mEles[toActualIndex(i)]))
				return i - mStart;
		return -1;
	}

	@Override
	public int lastIndexOf(Object aO)
	{
		int end = getVirtualEnd();
		for (int i = end - 1; i >= mStart; i--)
			if (JCommon.equals(aO, mEles[toActualIndex(i)]))
				return i - mStart;
		return -1;
	}
	
	@Override
	public E getLast()
	{
		return isEmpty()?null:mEles[toActualIndex(mEnd-1)] ;
	}
	
	@Override
	public E getFirst()
	{
		return isEmpty()?null:mEles[mStart] ;
	}

	@Override
	public E get(int aIndex)
	{
		return mEles[(aIndex + mStart) % mCapacity];
	}

	private final int toActualIndex(int aVirIndex)
	{
		return aVirIndex >= mCapacity ? 0 : (aVirIndex<0?aVirIndex+mCapacity:aVirIndex);
	}
	
	public Iterator<E> inverseIter()
	{
		return new Iterator<E>()
		{

			private int index = mEnd;
			private int lastReturnedIndex = -1;
			private boolean isFirst = mFull;

			public boolean hasNext()
			{
				return isFirst || index != mStart;
			}

			public E next()
			{
				if (!hasNext())
				{
					throw new NoSuchElementException();
				}
				isFirst = false;
				lastReturnedIndex = index;
				index = toActualIndex(--index);
				return mEles[index];
			}

			public void remove()
			{
				if (lastReturnedIndex == -1)
				{
					throw new IllegalStateException();
				}

				// First element can be removed quickly
				if (lastReturnedIndex == mStart)
				{
					CircularArrayList.this.removeFirst();
					lastReturnedIndex = -1;
					return;
				}

				int pos = lastReturnedIndex + 1;
				if (mStart < lastReturnedIndex && pos < mEnd)
				{
					// shift in one part
					System.arraycopy(mEles, pos, mEles, lastReturnedIndex, mEnd - pos);
				}
				else
				{
					// Other elements require us to shift the subsequent elements
					while (pos != mEnd)
					{
						if (pos >= mCapacity)
						{
							mEles[pos - 1] = mEles[0];
							pos = 0;
						}
						else
						{
							mEles[toActualIndex(--pos)] = mEles[pos];
							pos = toActualIndex(++pos);
						}
					}
				}

				lastReturnedIndex = -1;
				mEnd = toActualIndex(--mEnd);
				mEles[mEnd] = null;
				mFull = false;
				index = toActualIndex(--index);
			}

		};
	}

	@Override
	public Iterator<E> iterator()
	{
		return new Iterator<E>()
		{

			private int index = mStart;
			private int lastReturnedIndex = -1;
			private boolean isFirst = mFull;

			public boolean hasNext()
			{
				return isFirst || index != mEnd;
			}

			public E next()
			{
				if (!hasNext())
				{
					throw new NoSuchElementException();
				}
				isFirst = false;
				lastReturnedIndex = index;
				index = toActualIndex(++index);
				return mEles[lastReturnedIndex];
			}

			public void remove()
			{
				if (lastReturnedIndex == -1)
				{
					throw new IllegalStateException();
				}

				// First element can be removed quickly
				if (lastReturnedIndex == mStart)
				{
					CircularArrayList.this.removeFirst();
					lastReturnedIndex = -1;
					return;
				}

				int pos = lastReturnedIndex + 1;
				if (mStart < lastReturnedIndex && pos < mEnd)
				{
					// shift in one part
					System.arraycopy(mEles, pos, mEles, lastReturnedIndex, mEnd - pos);
				}
				else
				{
					// Other elements require us to shift the subsequent elements
					while (pos != mEnd)
					{
						if (pos >= mCapacity)
						{
							mEles[pos - 1] = mEles[0];
							pos = 0;
						}
						else
						{
							mEles[toActualIndex(pos-1)] = mEles[pos];
							pos = toActualIndex(pos+1);
						}
					}
				}

				lastReturnedIndex = -1;
				mEnd = toActualIndex(--mEnd);
				mEles[mEnd] = null;
				mFull = false;
				index = toActualIndex(--index);
			}

		};
	}

	@Override
	public int size()
	{
		return mFull ? mCapacity : (mEnd < mStart ? mEnd + mCapacity - mStart : mEnd - mStart);
	}

	@Override
	public int maxSize()
	{
		return mCapacity ;
	}

}
