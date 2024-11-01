package team.sailboat.commons.fan.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import team.sailboat.commons.fan.collection.CircularFifoQueue;
import team.sailboat.commons.fan.collection.ICircularBoundedQueue;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.struct.Tuples;

/**
 * 此线程用来处理不适合在其它线程做耗时操作的情形。将数据交由此线程来处理。
 * 如果处理任务不是很重，对及时要求不是很高，可以只有一个此线程实例
 * @author yyl
 * @version 1.0 
 * @since 2014-2-28
 */
public class MassDispatchThread extends Thread
{
	static final String sName = "收集派发处理线程" ;
	static int sIndex = 1 ;
	
	ICircularBoundedQueue<Entry<Integer , Object>> mQueue ;
	Map<Integer , IXListener[]> mLsns ;
	ReentrantLock mLock ;
	Condition mNotEmpty ;
	
	public MassDispatchThread(String aName)
	{
		super(aName) ;
		mLsns = new HashMap<Integer, IXListener[]>() ;
		mQueue = XC.synchronizedQueue(new CircularFifoQueue<>(10000)) ;
		mLock = new ReentrantLock(true) ;
		mNotEmpty = mLock.newCondition() ;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getHandle()
	{
		return sIndex++ ;
	}
	
	public void push(int aHandle , Object aData)
	{
		mLock.lock() ;
		try
		{
			mQueue.offer(Tuples.of(aHandle, aData)) ;
			mNotEmpty.signalAll() ;
		}
		finally
		{
			mLock.unlock() ;
		}
	}
	
	public void addCallback(int aHandle , IXListener aLsn)
	{
		IXListener[] lsns = mLsns.get(aHandle) ;
		if(lsns == null) lsns = new IXListener[]{aLsn} ;
		else
		{
			IXListener[] lsns0 = new IXListener[lsns.length+1] ;
			System.arraycopy(lsns, 0, lsns0, 0, lsns.length) ;
			lsns0[lsns.length] = aLsn ;
			lsns = lsns0 ;
		}
		mLsns.put(aHandle, lsns) ;
	}
	
	public void relaseHandle(int aHandle)
	{
		mLsns.remove(aHandle) ;
	}
	
	@Override
	public void run()
	{
		while(!isInterrupted())
		{
			mLock.lock() ;
			try
			{
				if(mQueue.size()<=0)
					mNotEmpty.await() ;
			}
			catch(Exception e)
			{}
			finally
			{
				mLock.unlock() ;
			}
			for(Entry<Integer , Object> entry :mQueue.poll(mQueue.size() , Entry.class))
			{
				IXListener[] lsns = mLsns.get(entry.getKey()) ;
				if(lsns != null)
				{
					XEvent event = new XEvent(entry.getValue(), entry.getKey()) ;
					for(IXListener lsn : lsns)
					{
						try
						{
							lsn.handle(event) ;
						}
						catch(Exception e)
						{
							e.printStackTrace() ;
						}
					}
				}
			}
		}
	}
}
