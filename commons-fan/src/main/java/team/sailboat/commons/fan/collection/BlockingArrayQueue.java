package team.sailboat.commons.fan.collection;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import team.sailboat.commons.fan.lang.Assert;

/**
* 
* 阻塞的有界队列，后台是一个循环数组（CircularArrayList）。这个队列可以是固定大小的，也可以是增长的。
* <p>
* This queue is uses a variant of the two lock queue algorithm to provide an efficient queue or list backed by a growable circular array.
* </p>
* <p>
* Unlike {@link java.util.concurrent.ArrayBlockingQueue}, this class is able to grow and provides a blocking put call.
* </p>
* <p>
* The queue has both a capacity (the size of the array currently allocated) and a max capacity (the maximum size that may be allocated), which defaults to
* {@link Integer#MAX_VALUE}.
* </p>
* 
* @param <E>
*            The element type
*/
public class BlockingArrayQueue<E> extends AbstractList<E> implements BlockingQueue<E>
{
	/**
	 * Default initial capacity, 128.
	 */
	public static final int DEFAULT_CAPACITY = 128;
	/**
	 * Default growth factor, 64.
	 */
	public static final int DEFAULT_GROWTH = 64;

	private final int _maxCapacity;
	private final int _growCapacity;
	
	int mHeadIndex ;
	int mTailIndex ;
	private final Lock _tailLock = new ReentrantLock();
	private final AtomicInteger _size = new AtomicInteger();
	private final Lock _headLock = new ReentrantLock();
	private final Condition _notEmpty = _headLock.newCondition();
	private Object[] _elements;
	/**
	 * 当队列满时，仍添加元素，是否覆盖头部数据，头往后移。缺省时false，抛出异常		<br />
	 * 在没有指定为有界队列的情况下，队列的界限是Integer.MAX_VALUE，可以认为是达不到这个容量的，所以在此种情形下，此参数可以认为无效
	 */
	boolean mOverwriteWhenOverflow = false ;

	/**
	 * Creates an unbounded {@link BlockingArrayQueue} with default initial capacity and grow factor.
	 * 
	 * @see #DEFAULT_CAPACITY
	 * @see #DEFAULT_GROWTH
	 */
	public BlockingArrayQueue()
	{
		_elements = new Object[DEFAULT_CAPACITY];
		_growCapacity = DEFAULT_GROWTH;
		_maxCapacity = Integer.MAX_VALUE;
	}

	/**
	 * 创建一个有界的，不增长的阻塞循环队列。
	 * @param maxCapacity					最大容量
	 * @param aOverwriteWhenOverflow		当队列满时，仍添加元素，是否覆盖头部数据，头往后移。缺省时false，抛出异常
	 */
	public BlockingArrayQueue(int maxCapacity , boolean aOverwriteWhenOverflow)
	{
		_elements = new Object[maxCapacity];
		_growCapacity = -1;
		_maxCapacity = maxCapacity;
		mOverwriteWhenOverflow = aOverwriteWhenOverflow ;
	}

	/**
	 * Creates an unbounded {@link BlockingArrayQueue} that grows by the given parameter.
	 * 
	 * @param capacity
	 *            the initial capacity
	 * @param growBy
	 *            the growth factor
	 */
	public BlockingArrayQueue(int capacity, int growBy)
	{
		_elements = new Object[capacity];
		_growCapacity = growBy;
		_maxCapacity = Integer.MAX_VALUE;
	}

	/**
	 * Create a bounded {@link BlockingArrayQueue} that grows by the given parameter.
	 * 
	 * @param capacity
	 *            the initial capacity
	 * @param growBy
	 *            the growth factor
	 * @param maxCapacity
	 *            the maximum capacity
	 */
	public BlockingArrayQueue(int capacity, int growBy, int maxCapacity)
	{
		if (capacity > maxCapacity)
			throw new IllegalArgumentException();
		_elements = new Object[capacity];
		_growCapacity = growBy;
		_maxCapacity = maxCapacity;
	}

	/*----------------------------------------------------------------------------*/
	/* Collection methods */
	/*----------------------------------------------------------------------------*/

	@Override
	public void clear()
	{

		_tailLock.lock();
		try
		{

			_headLock.lock();
			try
			{
				mHeadIndex = 0;
				mTailIndex = 0;
				_size.set(0);
			}
			finally
			{
				_headLock.unlock();
			}
		}
		finally
		{
			_tailLock.unlock();
		}
	}

	@Override
	public int size()
	{
		return _size.get();
	}

	@Override
	public Iterator<E> iterator()
	{
		return listIterator();
	}

	/*----------------------------------------------------------------------------*/
	/* Queue methods */
	/*----------------------------------------------------------------------------*/

	/**
	 * 移出一个头部元素。如果队列为空，将返回null
	 */
	@SuppressWarnings("unchecked")
	@Override
	public E poll()
	{
		if (_size.get() == 0)
			return null;

		E e = null;

		_headLock.lock(); // Size cannot shrink
		try
		{
			if (_size.get() > 0)
			{
				final int head = mHeadIndex ;
				e = (E) _elements[head];
				_elements[head] = null;
				mHeadIndex = (head + 1) % _elements.length;
				if (_size.decrementAndGet() > 0)
					_notEmpty.signal();
			}
		}
		finally
		{
			_headLock.unlock();
		}
		return e;
	}

	/**
	 * 获取头部元素。如果队列为空，将返回null
	 */
	@SuppressWarnings("unchecked")
	@Override
	public E peek()
	{
		if (_size.get() == 0)
			return null;

		E e = null;

		_headLock.lock(); // Size cannot shrink
		try
		{
			if (_size.get() > 0)
				e = (E) _elements[mHeadIndex];
		}
		finally
		{
			_headLock.unlock();
		}
		return e;
	}

	/**
	 * 移出一个头部元素。如果队列为空，将抛出异常
	 */
	@Override
	public E remove()
	{
		E e = poll();
		if (e == null)
			throw new NoSuchElementException();
		return e;
	}

	/**
	 * 获取头部元素。如果队列为空，将抛出异常
	 */
	@Override
	public E element()
	{
		E e = peek();
		if (e == null)
			throw new NoSuchElementException();
		return e;
	}

	/*----------------------------------------------------------------------------*/
	/* 以下都是阻塞的方法 */
	/*----------------------------------------------------------------------------*/

	/**
	 * 在队尾添加元素。成功返回true，否则返回false
	 */
	@Override
	public boolean offer(E e)
	{
		Objects.requireNonNull(e);

		boolean notEmpty = false;
		_tailLock.lock(); // Size cannot grow... only shrink
		try
		{
			int size = _size.get();
			if (size >= _maxCapacity)
			{
				if(mOverwriteWhenOverflow)
				{
					// 覆盖掉头部
					_headLock.lock();
					try
					{
						Assert.isTrue((mTailIndex + 1) % size == mHeadIndex) ;
						mTailIndex = mHeadIndex ;
						mHeadIndex = (mHeadIndex + 1) % _elements.length;
						_elements[mTailIndex] = e ;
						_notEmpty.signal();
					}
					finally
					{
						_headLock.unlock();
					}
					return true ;
				}
				return false;
			}

			// Should we expand array?
			if (size == _elements.length)
			{
				_headLock.lock();
				try
				{
					if (!grow())
						return false;
				}
				finally
				{
					_headLock.unlock();
				}
			}

			// Re-read head and tail after a possible grow
			int tail = mTailIndex ;
			_elements[tail] = e;
			mTailIndex = (tail + 1) % _elements.length;
			notEmpty = _size.getAndIncrement() == 0;
		}
		finally
		{
			_tailLock.unlock();
		}

		if (notEmpty)
		{
			_headLock.lock();
			try
			{
				_notEmpty.signal();
			}
			finally
			{
				_headLock.unlock();
			}
		}

		return true;
	}

	/**
	 * 如果添加失败，将抛出异常。添加成功将返回true
	 */
	@Override
	public boolean add(E e)
	{
		if (offer(e))
			return true;
		else
			throw new IllegalStateException();
	}

	/**
	 * 当队列满的时候，此方法将等待，直到有存下这个元素的位置.		<br />
	 * 尚未实现
	 */
	@Override
	public void put(E o) throws InterruptedException
	{
		// The mechanism to await and signal when the queue is full is not implemented
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean offer(E o, long timeout, TimeUnit unit) throws InterruptedException
	{
		// The mechanism to await and signal when the queue is full is not implemented
		throw new UnsupportedOperationException();
	}

	/**
	 * 从头部移出一个元素。如果队列为空，将会等待
	 */
	@SuppressWarnings("unchecked")
	@Override
	public E take() throws InterruptedException
	{
		E e = null;

		_headLock.lockInterruptibly(); // Size cannot shrink
		try
		{
			try
			{
				while (_size.get() == 0)
				{
					_notEmpty.await();
				}
			}
			catch (InterruptedException ie)
			{
				_notEmpty.signal();
				throw ie;
			}

			final int head = mHeadIndex ;
			e = (E) _elements[head];
			_elements[head] = null;
			mHeadIndex = (head + 1) % _elements.length;

			if (_size.decrementAndGet() > 0)
				_notEmpty.signal();
		}
		finally
		{
			_headLock.unlock();
		}
		return e;
	}

	@SuppressWarnings("unchecked")
	@Override
	public E poll(long time, TimeUnit unit) throws InterruptedException
	{
		long nanos = unit.toNanos(time);
		E e = null;

		_headLock.lockInterruptibly(); // Size cannot shrink
		try
		{
			try
			{
				while (_size.get() == 0)
				{
					if (nanos <= 0)
						return null;
					nanos = _notEmpty.awaitNanos(nanos);
				}
			}
			catch (InterruptedException x)
			{
				_notEmpty.signal();
				throw x;
			}

			int head = mHeadIndex ;
			e = (E) _elements[head];
			_elements[head] = null;
			mHeadIndex = (head + 1) % _elements.length;

			if (_size.decrementAndGet() > 0)
				_notEmpty.signal();
		}
		finally
		{
			_headLock.unlock();
		}
		return e;
	}

	@Override
	public boolean remove(Object o)
	{

		_tailLock.lock();
		try
		{

			_headLock.lock();
			try
			{
				if (isEmpty())
					return false;

				final int head = mHeadIndex;
				final int tail = mTailIndex;
				final int capacity = _elements.length;

				int i = head;
				while (true)
				{
					if (Objects.equals(_elements[i], o))
					{
						remove(i >= head ? i - head : capacity - head + i);
						return true;
					}
					++i;
					if (i == capacity)
						i = 0;
					if (i == tail)
						return false;
				}
			}
			finally
			{
				_headLock.unlock();
			}
		}
		finally
		{
			_tailLock.unlock();
		}
	}

	@Override
	public int remainingCapacity()
	{

		_tailLock.lock();
		try
		{

			_headLock.lock();
			try
			{
				return getCapacity() - size();
			}
			finally
			{
				_headLock.unlock();
			}
		}
		finally
		{
			_tailLock.unlock();
		}
	}

	@Override
	public int drainTo(Collection<? super E> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int drainTo(Collection<? super E> c, int maxElements)
	{
		throw new UnsupportedOperationException();
	}

	/*----------------------------------------------------------------------------*/
	/* List methods */
	/*----------------------------------------------------------------------------*/

	@SuppressWarnings("unchecked")
	@Override
	public E get(int index)
	{

		_tailLock.lock();
		try
		{

			_headLock.lock();
			try
			{
				if (index < 0 || index >= _size.get())
					throw new IndexOutOfBoundsException("!(" + 0 + "<" + index + "<=" + _size + ")");
				int i = mHeadIndex + index;
				int capacity = _elements.length;
				if (i >= capacity)
					i -= capacity;
				return (E) _elements[i];
			}
			finally
			{
				_headLock.unlock();
			}
		}
		finally
		{
			_tailLock.unlock();
		}
	}

	@Override
	public void add(int index, E e)
	{
		if (e == null)
			throw new NullPointerException();

		_tailLock.lock();
		try
		{

			_headLock.lock();
			try
			{
				final int size = _size.get();

				if (index < 0 || index > size)
					throw new IndexOutOfBoundsException("!(" + 0 + "<" + index + "<=" + _size + ")");

				if (index == size)
				{
					add(e);
				}
				else
				{
					if (mTailIndex == mHeadIndex)
						if (!grow())
							throw new IllegalStateException("full");

					// Re-read head and tail after a possible grow
					int i = mHeadIndex + index;
					int capacity = _elements.length;

					if (i >= capacity)
						i -= capacity;

					_size.incrementAndGet();
					int tail = mTailIndex;
					mTailIndex = tail = (tail + 1) % capacity;

					if (i < tail)
					{
						System.arraycopy(_elements, i, _elements, i + 1, tail - i);
						_elements[i] = e;
					}
					else
					{
						if (tail > 0)
						{
							System.arraycopy(_elements, 0, _elements, 1, tail);
							_elements[0] = _elements[capacity - 1];
						}

						System.arraycopy(_elements, i, _elements, i + 1, capacity - i - 1);
						_elements[i] = e;
					}
				}
			}
			finally
			{
				_headLock.unlock();
			}
		}
		finally
		{
			_tailLock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public E set(int index, E e)
	{
		Objects.requireNonNull(e);

		_tailLock.lock();
		try
		{

			_headLock.lock();
			try
			{
				if (index < 0 || index >= _size.get())
					throw new IndexOutOfBoundsException("!(" + 0 + "<" + index + "<=" + _size + ")");

				int i = mHeadIndex + index;
				int capacity = _elements.length;
				if (i >= capacity)
					i -= capacity;
				E old = (E) _elements[i];
				_elements[i] = e;
				return old;
			}
			finally
			{
				_headLock.unlock();
			}
		}
		finally
		{
			_tailLock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public E remove(int index)
	{

		_tailLock.lock();
		try
		{

			_headLock.lock();
			try
			{
				if (index < 0 || index >= _size.get())
					throw new IndexOutOfBoundsException("!(" + 0 + "<" + index + "<=" + _size + ")");

				int i = mHeadIndex + index;
				int capacity = _elements.length;
				if (i >= capacity)
					i -= capacity;
				E old = (E) _elements[i];

				int tail = mTailIndex;
				if (i < tail)
				{
					System.arraycopy(_elements, i + 1, _elements, i, tail - i);
					--mTailIndex;
				}
				else
				{
					System.arraycopy(_elements, i + 1, _elements, i, capacity - i - 1);
					_elements[capacity - 1] = _elements[0];
					if (tail > 0)
					{
						System.arraycopy(_elements, 1, _elements, 0, tail);
						--mTailIndex;
					}
					else
					{
						mTailIndex = capacity - 1;
					}
					_elements[mTailIndex] = null;
				}

				_size.decrementAndGet();

				return old;
			}
			finally
			{
				_headLock.unlock();
			}
		}
		finally
		{
			_tailLock.unlock();
		}
	}

	@Override
	public ListIterator<E> listIterator(int index)
	{

		_tailLock.lock();
		try
		{

			_headLock.lock();
			try
			{
				Object[] elements = new Object[size()];
				if (size() > 0)
				{
					int head = mHeadIndex;
					int tail = mTailIndex;
					if (head < tail)
					{
						System.arraycopy(_elements, head, elements, 0, tail - head);
					}
					else
					{
						int chunk = _elements.length - head;
						System.arraycopy(_elements, head, elements, 0, chunk);
						System.arraycopy(_elements, 0, elements, chunk, tail);
					}
				}
				return new Itr(elements, index);
			}
			finally
			{
				_headLock.unlock();
			}
		}
		finally
		{
			_tailLock.unlock();
		}
	}

	/*----------------------------------------------------------------------------*/
	/* Additional methods */
	/*----------------------------------------------------------------------------*/

	/**
	 * @return the current capacity of this queue
	 */
	public int getCapacity()
	{
		_tailLock.lock();
		try
		{
			return _elements.length;
		}
		finally
		{
			_tailLock.unlock();
		}
	}

	/**
	 * @return the max capacity of this queue, or -1 if this queue is unbounded
	 */
	public int getMaxCapacity()
	{
		return _maxCapacity;
	}

	/*----------------------------------------------------------------------------*/
	/* Implementation methods */
	/*----------------------------------------------------------------------------*/

	private boolean grow()
	{
		if (_growCapacity <= 0)
			return false;

		_tailLock.lock();
		try
		{

			_headLock.lock();
			try
			{
				final int head = mHeadIndex;
				final int tail = mTailIndex;
				final int newTail;
				final int capacity = _elements.length;

				Object[] elements = new Object[capacity + _growCapacity];

				if (head < tail)
				{
					newTail = tail - head;
					System.arraycopy(_elements, head, elements, 0, newTail);
				}
				else if (head > tail || _size.get() > 0)
				{
					newTail = capacity + tail - head;
					int cut = capacity - head;
					System.arraycopy(_elements, head, elements, 0, cut);
					System.arraycopy(_elements, 0, elements, cut, tail);
				}
				else
				{
					newTail = 0;
				}

				_elements = elements;
				mHeadIndex = 0;
				mTailIndex = newTail;
				return true;
			}
			finally
			{
				_headLock.unlock();
			}
		}
		finally
		{
			_tailLock.unlock();
		}
	}

	private class Itr implements ListIterator<E>
	{
		private final Object[] _elements;
		private int _cursor;

		public Itr(Object[] elements, int offset)
		{
			_elements = elements;
			_cursor = offset;
		}

		@Override
		public boolean hasNext()
		{
			return _cursor < _elements.length;
		}

		@SuppressWarnings("unchecked")
		@Override
		public E next()
		{
			return (E) _elements[_cursor++];
		}

		@Override
		public boolean hasPrevious()
		{
			return _cursor > 0;
		}

		@SuppressWarnings("unchecked")
		@Override
		public E previous()
		{
			return (E) _elements[--_cursor];
		}

		@Override
		public int nextIndex()
		{
			return _cursor + 1;
		}

		@Override
		public int previousIndex()
		{
			return _cursor - 1;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void set(E e)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void add(E e)
		{
			throw new UnsupportedOperationException();
		}
	}
}
