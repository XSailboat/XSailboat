package team.sailboat.commons.fan.exec;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import team.sailboat.commons.fan.infc.YRunnable;
import team.sailboat.commons.fan.lang.Assert;

public class WeakTasksRun implements Runnable
{
	List<WeakReference<YRunnable>> mWeakRefList = new LinkedList<>() ;
	ReferenceQueue<Runnable> mQueue = new ReferenceQueue<>() ;
	
	public synchronized void addTask(YRunnable aTask)
	{
		Assert.notNull(aTask, "WeakTask.addTask方法的Runnable类型参数不能为null") ;
		mWeakRefList.add(new WeakReference<YRunnable>(aTask, mQueue)) ;
	}

	@Override
	public synchronized void run()
	{
		Set<Reference<?>> refSet = new HashSet<>() ;
		Reference<?> ref = null ;
		while((ref = mQueue.poll()) != null)
			refSet.add(ref) ;
		Iterator<WeakReference<YRunnable>> it = mWeakRefList.iterator() ;
		while(it.hasNext())
		{
			WeakReference<YRunnable> wr = it.next() ;
			if(refSet.contains(wr))
				it.remove(); 
			else
			{
				YRunnable run = wr.get() ;
				if(run != null && !run.isClosed())
				{
					try
					{
						run.run();
					}
					catch(Exception e)
					{}
				}
				else
					it.remove(); 
			}
		}
	}

}
