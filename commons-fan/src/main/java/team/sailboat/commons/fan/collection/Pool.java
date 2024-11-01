package team.sailboat.commons.fan.collection;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Pool<T>
{
	Queue<T> mIdleList = new LinkedList<>() ;
	Queue<T> mUsingList = new LinkedList<>() ;
	
	final ReentrantLock mLock = new ReentrantLock() ;
	final Condition mHaveIdleCnd = mLock.newCondition() ;
	
	public Pool()
	{}
	
	@SuppressWarnings("unchecked")
	public Pool(T...aObjs)
	{
		XC.addAll(mIdleList, aObjs);
	}
	
	/**
	 * 正在被使用的对象数量
	 * @return
	 */
	public int getUsingSize()
	{
		return mUsingList.size() ;
	}
	
	private T get()
	{
		T ele = mIdleList.poll() ;
		mUsingList.add(ele) ;
		return ele ;
	}
	
	public T get(int aWaitTimes)
	{
		mLock.lock();
		try
		{
			if(!mIdleList.isEmpty())
			{
				return get() ;
			}
			else
			{
				if(aWaitTimes>0)
				{
					try
					{
						mHaveIdleCnd.await(aWaitTimes, TimeUnit.MILLISECONDS) ;
						return !mIdleList.isEmpty()?get():null ;
					}
					catch (InterruptedException e)
					{
						return null ;
					}
				}
				else
					return null ;
			}
		}
		finally
		{
			mLock.unlock();
		}
	}
	
	public void release(T aObj)
	{
		mLock.lock();
		try
		{
			if(mUsingList.remove(aObj))
			{
				mIdleList.add(aObj) ;
				mHaveIdleCnd.signal();
			}
		}
		finally
		{
			mLock.unlock(); 
		}
	}
}
