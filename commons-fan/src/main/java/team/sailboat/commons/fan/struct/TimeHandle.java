package team.sailboat.commons.fan.struct;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.function.Supplier;

import team.sailboat.commons.fan.exec.DefaultAutoCleaner;
import team.sailboat.commons.fan.infc.ESupplier;
import team.sailboat.commons.fan.time.XTime;

public class TimeHandle<T>
{
	SoftReference<T> mSRHandle ;
	WeakReference<T> mWRHandle ;
	
	int mHoldTimeInSeconds ;
	boolean mReleased = false ;
	long mLastTimeStamp ;
	int mMaxValidTime ;
	Wrapper<Boolean> mCleaned ;
	
	public TimeHandle(int aHoldTimeInSeconds)
	{
		this(null , aHoldTimeInSeconds) ;
	}
	
	public TimeHandle(int aHoldTimeInSeconds , int aMaxValidTimeInSeconds)
	{
		this(null , aHoldTimeInSeconds , aMaxValidTimeInSeconds) ;
	}
	
	public TimeHandle(T aEle , int aHoldTimeInSeconds)
	{
		this(aEle , aHoldTimeInSeconds , -1) ;
	}
	
	public TimeHandle(T aEle , int aHoldTimeInSeconds , int aMaxValidTimeInSeconds)
	{
		mHoldTimeInSeconds = aHoldTimeInSeconds ;
		if(aMaxValidTimeInSeconds>0)
			mMaxValidTime = Math.max(mHoldTimeInSeconds, aMaxValidTimeInSeconds) ;
		else
			mMaxValidTime = 0 ;
		if(aEle != null)
			set(aEle);
	}
	
	private void set(T aEle)
	{
		if(mCleaned != null && !mCleaned.get().booleanValue())
			mCleaned.set(true) ;
		//因为能set的条件是
		mCleaned = new Wrapper<Boolean>(false) ;
		mSRHandle = new SoftReference<T>(aEle) ;
		mReleased = false ;
		new DefaultAutoCleaner(mHoldTimeInSeconds, this::clean , mCleaned::get);
		mLastTimeStamp = System.currentTimeMillis() ;
	}
	
	public long getLastTimeStamp()
	{
		return mLastTimeStamp ;
	}
	
	public synchronized boolean clockFromNow()
	{
		if(!mCleaned.get().booleanValue())
		{
			mLastTimeStamp = System.currentTimeMillis() ;
			return true ;
		}
		return false ;
	}
	
	public synchronized T getOrRebuild(Supplier<T> aSupplier)
	{
		T v = get() ;
		if(v == null)
		{
			v = aSupplier.get() ;
			if(v != null)
				set(v) ;
		}
		return v ;
	}
	
	public synchronized <X extends Throwable> T getOrRebuildE(ESupplier<T , X> aSupplier) throws X
	{
		T v = get() ;
		if(v == null)
		{
			v = aSupplier.get() ;
			if(v != null)
				set(v) ;
		}
		return v ;
	}
	
	synchronized void clean()
	{
		if(XTime.pass(mLastTimeStamp , mHoldTimeInSeconds*1000)
				&& !mCleaned.get().booleanValue())
		{
			System.out.println("被清理");
			mCleaned.set(true) ;
			T handle = mSRHandle.get() ;
			if(handle != null)
				mWRHandle = new WeakReference<T>(handle) ;
			mSRHandle = null ;
		}
	}
	
	public T get()
	{
		if(!mReleased)
		{
			if(mSRHandle != null)
				return mSRHandle.get() ;
			else
			{
				if(mWRHandle != null && (mMaxValidTime==0
						|| !XTime.pass(mLastTimeStamp, mMaxValidTime*1000)))
				{
					T v = mWRHandle.get() ;
					if(v == null)
					{
						mWRHandle = null ;
						mReleased = true ;
					}
					return v ;
				}
				else
				{
					mWRHandle = null ;
					mReleased = true ;
					return null ;
				}
			}
		}
		return null ;
	}
}
