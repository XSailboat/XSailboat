package team.sailboat.commons.fan.exec;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadFactory implements ThreadFactory
{
	static final String nameSuffix = "]";
	
	final ThreadGroup group;
	final AtomicInteger threadNumber = new AtomicInteger(1);
	final String namePrefix;
	boolean mDaemon ;

	public CustomThreadFactory(String poolName , boolean aDaemon)
	{
		group = Thread.currentThread().getThreadGroup();
		namePrefix = poolName + "[";
		mDaemon = aDaemon ;
	}

	public CustomThreadFactory(String poolName, ThreadGroup threadGroup
			, boolean aDaemon)
	{
		group = threadGroup;
		namePrefix = poolName + "[";
		mDaemon = aDaemon ;
	}

	public ThreadGroup getThreadGroup()
	{
		return group;
	}

	public Thread newThread(Runnable r)
	{
		Thread t = new Thread(group,
				r,
				namePrefix +
						threadNumber.getAndIncrement()
						+
						nameSuffix,
				0);
		t.setDaemon(mDaemon);
		if (t.getPriority() != Thread.NORM_PRIORITY)
			t.setPriority(Thread.NORM_PRIORITY);
		return t;
	}
}
