package team.sailboat.commons.fan.exec;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.Pool;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.log.Log;
import team.sailboat.commons.fan.struct.Wrapper;

/**
 * 公共的执行器，避免过多空闲进程
 *
 * @author yl
 * @version 1.0 
 * @since 2016年3月3日
 */
public class CommonExecutor
{
	private static int sPerfectNum = -1 ;
	static ScheduledExecutorService sScheduledExecutor ;
	static ScheduledExecutorService sDaemonScheduledExecutor ;
	static ExecutorService sCachedExecutor ;
	static boolean sClosed = false ;
	
	static final ThreadGroup sRootThreadGroup = new ThreadGroup("CommonExecutor线程分组") ;
	
	/**
	 * 键是作为SomoothExecTask同类的表示
	 */
	static Map<String , SmoothExecTask> sSmoothExecTaskMap = new Hashtable<>() ;
	
	static final Map<String, ThreadGroup> sThreadGroupMap = new HashMap<String, ThreadGroup>() ;
	
	public static ThreadGroup getThreadGroup(String aGroupName)
	{
		ThreadGroup threadGroup = sThreadGroupMap.get(aGroupName) ;
		if(threadGroup == null)
		{
			synchronized (aGroupName.intern())
			{
				threadGroup = sThreadGroupMap.get(aGroupName) ;
				if(threadGroup == null)
				{
					threadGroup = new ThreadGroup(sRootThreadGroup , aGroupName) ;
					sThreadGroupMap.put(aGroupName , threadGroup) ;
				}
			}
		}
		return threadGroup ;
	}
	
	static int getPerfectCoreThreadNum()
	{
		if(sPerfectNum == -1)
		{
			sPerfectNum = Runtime.getRuntime().availableProcessors()/2 ;
			sPerfectNum = Math.min(sPerfectNum , 3) ;
		}
		return sPerfectNum ;
	}
	
	static ScheduledExecutorService getScheduledExecutor()
	{
		if(sScheduledExecutor == null)
			sScheduledExecutor = Executors.newScheduledThreadPool(getPerfectCoreThreadNum()) ;
		return sScheduledExecutor ;
	}
	
	static ScheduledExecutorService getDaemonScheduledExecutor()
	{
		if(sDaemonScheduledExecutor == null)
			sDaemonScheduledExecutor = new ScheduledThreadPoolExecutor(1 
					, new CustomThreadFactory("公共定时器", true)) ;
		return sDaemonScheduledExecutor ;
	}
	
	/**
	 * 内部已经变成虚拟线程执行
	 * @return
	 */
	private static ExecutorService getCachedExecutor()
	{
		if(sCachedExecutor == null)
		{
//			sCachedExecutor = new ThreadPoolExecutor(1, Math.max(Runtime.getRuntime().availableProcessors()*2 , 32) ,
//                    60L, TimeUnit.SECONDS,
//                    new SynchronousQueue<Runnable>());
			sCachedExecutor = Executors.newVirtualThreadPerTaskExecutor() ;
		}
		return sCachedExecutor ;
	}
	
	/**
	 * 在指定时间之后执行一次
	 * @param aRun
	 * @param aDelay
	 * @param aTimeUnit
	 */
	public static ScheduledFuture<?> exec(Runnable aRun , int aDelay , TimeUnit aTimeUnit)
	{
		return getScheduledExecutor().schedule(CRun.wrap(aRun) , aDelay, aTimeUnit) ;
	}
	
	/**
	 * 在指定的时刻执行。如果指定的时刻before当前时刻，则立即执行 </br>
	 * 
	 * @param aRun
	 * @param aTime
	 * @return 如果安排了定时调度，则返回ScheduledFuture，否则立即执行的话，将返回null
	 */
	public static ScheduledFuture<?> execAt(Runnable aRun , Date aTime)
	{
		Assert.notNull(aTime , "执行时间不能为空！") ;
		long waitTime = aTime.getTime() - System.currentTimeMillis() ;
		if(waitTime > 50)
			return exec(aRun, (int)waitTime , TimeUnit.MILLISECONDS) ;
		else
		{
			if(waitTime > 0)
				JCommon.sleep((int)waitTime) ;
			// 立即执行
			exec(aRun) ;
			return null ;
		}
	}
	
	public static ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
            long initialDelay,
            long period,
            TimeUnit unit)
	{
		return getScheduledExecutor().scheduleAtFixedRate(CRun.wrap(command), initialDelay, period, unit) ;
	}
	
	/**
	 * 
	 * @param command
	 * @param initialDelay		初始延时的时间单位和delay的时间相同
	 * @param delay				
	 * @param unit				时间单位
	 * @return
	 */
	public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
            long initialDelay,
            long delay,
            TimeUnit unit)
	{
		return getScheduledExecutor().scheduleWithFixedDelay(CRun.wrap(command), initialDelay, delay, unit) ;
	}
	
	public static ScheduledFuture<?> scheduleWithFixedDelayDaemon(Runnable command,
            long initialDelay,
            long delay,
            TimeUnit unit)
	{
		return getDaemonScheduledExecutor().scheduleWithFixedDelay(CRun.wrap(command) , initialDelay, delay, unit) ;
	}
	
	/**
	 * 
	 * @param aThreadName
	 * @param aCommand
	 * @param initialDelay	初始延时的时间单位和delay的时间相同
	 * @param delay
	 * @param unit
	 * @return
	 */
	public static ScheduledFuture<?> scheduleWithFixedDelay(String aThreadName , Runnable aCommand,
            long initialDelay,
            long delay,
            TimeUnit unit)
	{
		return scheduleWithFixedDelay(new NamedRun(aThreadName, aCommand), initialDelay, delay, unit) ;
	}
	
	/**
	 * 使用虚拟线程，异步执行指定任务
	 * @param aRunner
	 */
	public static void exec(Runnable aRunner)
	{
		int tryTimes = 0 ;
		RejectedExecutionException e1 = null ;
		while(tryTimes++<100)
		{
			try
			{
				getCachedExecutor().execute(CRun.wrap(aRunner)) ;
				return ;
			}
			catch(RejectedExecutionException e)
			{
				e1 = e ;
				JCommon.sleep(100);
				continue ;
			}
		}
		throw e1 ;
	}
	
	/**
	 * 
	 * @param aRunner
	 * @param aUseCurrentThreadServant		是否使用当前线程的上下文初始化和清理工具。如果当前线程的上下文工具不是可以被继承的，那么将不能被用于子线程
	 */
	public static void exec(Runnable aRunner , boolean aUseCurrentThreadServant)
	{
		if(!aUseCurrentThreadServant)
			exec(aRunner) ;
		else
			exec(ThreadContextServant.wrap(aRunner)) ;
	}
	
	public static void exec(Runnable aRunner , ThreadContextServant aServant)
	{
		exec(ThreadContextServant.wrap(aRunner, aServant)) ;
	}
	
	public static <V> Future<V> exec(Callable<V> aCallable)
	{
		int tryTimes = 0 ;
		RejectedExecutionException e1 = null ;
		while(tryTimes++<100)
		{
			try
			{
				return getCachedExecutor().submit(aCallable) ;
			}
			catch(RejectedExecutionException e)
			{
				e1 = e ;
				JCommon.sleep(100);
				continue ;
			}
		}
		throw e1 ;
	}
	
	/**
	 * 异步执行，最多等待指定毫秒数以后，获取结果，如果获取到就返回，如果不能获取到就返回null
	 * @param aCallable
	 * @param aMaxWaitTime
	 * @return
	 */
	public static <V> Wrapper<V> exec(Callable<V> aCallable , int aMaxWaitTime)
	{
		int tryTimes = 0 ;
		while(tryTimes++<100)
		{
			try
			{
				final Future<V> future = getCachedExecutor().submit(aCallable) ;
				try
				{
					return Wrapper.of(future.get(aMaxWaitTime, TimeUnit.MILLISECONDS)) ;
				}
				catch (InterruptedException | ExecutionException | TimeoutException e)
				{
					return Wrapper.ofNull() ;
				}
				finally
				{
					future.cancel(true) ;
				}
			}
			catch(RejectedExecutionException e)
			{
				JCommon.sleep(100);
				continue ;
			}
		}
		return Wrapper.ofNull() ;
	}
	
	/**
	 * 
	 * @param aRunner
	 * @param aThreadName
	 */
	public static void execInSelfThread(Runnable aRunner , String aThreadName)
	{
		execInSelfThread(aRunner, aThreadName , Thread.NORM_PRIORITY , true);
	}
	
	public static void execInSelfThread(Runnable aRunner , String aThreadName , String aGroupName)
	{
		execInSelfThread(aRunner, aThreadName , aGroupName , Thread.NORM_PRIORITY , true);
	}
	
	/**
	 * 
	 * @param aRunner				
	 * @param aThreadName			线程名称
	 * @param aPriority				线程的执行优先级
	 * @param aDaemon				是否是守护线程
	 */
	public static void execInSelfThread(Runnable aRunner , String aThreadName , int aPriority , boolean aDaemon)
	{
		execInSelfThread(aRunner, aThreadName, null , aPriority, aDaemon) ;
	}
	
	public static void execInSelfThread(Runnable aRunner , String aThreadName , String aGroupName , int aPriority , boolean aDaemon)
	{
		Thread thread = new Thread(getThreadGroup(JCommon.defaultIfEmpty(aGroupName , "任务独享线程")) , CRun.wrap(aRunner), aThreadName) ;
		thread.setPriority(aPriority);
		if(aDaemon)
			thread.setDaemon(aDaemon);
		thread.start();
	}
	
	public static void exec(Method aMethod , Object aSource , Object...aArgs)
	{
		getCachedExecutor().execute(new MethodRunner(aMethod, aSource, aArgs));
	}
	
	static class NamedRun implements CRun
	{
		String mName ;
		boolean mFirst = true ;
		Runnable mTask ;
		
		public NamedRun(String aName , Runnable aTask)
		{
			mName = aName ;
			mTask = aTask ;
		}
		
		@Override
		public void run()
		{
			if(isClosing())
				return ;
			if(mFirst)
			{
				mFirst = false ;
				try
				{
					Thread.currentThread().setName(mName);
				}
				catch(Throwable aE)
				{}
			}
			mTask.run(); 
		}
	}
	
	
	/**
	 * 此方法适用于处理过程耗时，被处理对象并不是很多的情形。
	 * 例如导出一个数据库用户的表成CSV格式，所有将要被导出的表就是被处理对象，处理者就是单个表的导出执行者
	 * 
	 * @param aGoodIt  				被处理对象的迭代器
	 * @param aConsumeAction		被处理对象和处理者指派，执行处理
	 * @param aWaitForFinish		当前线程是否等所有被处理对象都处理完。当false时，迭代器迭代完，把任务安排出去，就返回了，任务未必都处理完
	 * @param aConsumers			处理者，每一次处理都有且只有一个处理着参与。因此处理者的数量就是最大并发数
	 */
	@SuppressWarnings("unchecked")
	public static <T , E> void consume(Iterator<E> aGoodIt , BiConsumer<T, E> aConsumeAction 
			, boolean aWaitForFinish , T... aConsumers)
	{
		Assert.notEmpty(aConsumers , "不能没有消费者");
		Pool<T> consumerPool = new Pool<>(aConsumers) ;
		while(aGoodIt.hasNext())
		{
			E good = aGoodIt.next() ;
			T consumer = consumerPool.get(Integer.MAX_VALUE) ;
			exec(()->{
				aConsumeAction.accept(consumer, good);
				consumerPool.release(consumer);
			}) ;
		}
		if(aWaitForFinish)
		{
			while(consumerPool.getUsingSize()>0)
				JCommon.sleep(100) ;
		}
	}
	
	/**
	 * 此方法用在这样的情形：		<br>
	 * 某个方法因为外界因素，在某些时候可能被短时间高频度地被触发调用，但此方法并不需要如此频繁，允许在一定的延时内被调用并保持最新即可。
	 * 用定时器的话，从总体上来看，大部分的调用时无效的。如果只是单纯采用记时，做延时判定的话，可能又丢掉了最近一次变化调用
	 * @param aToken
	 * @param aDelayTimeInMillSec			单位毫秒
	 * @param aRun
	 */
	public static void smoothExec(String aToken , int aDelayTimeInMillSec , Runnable aRun)
	{
		Assert.isNotTrue(sClosed , "CommonExecutor已经关闭") ;
		aToken = aToken.intern() ;
		synchronized (aToken)
		{
			SmoothExecTask task = sSmoothExecTaskMap.get(aToken) ;
			if(task == null)
			{
				task = new SmoothExecTask(aToken, aRun) ;
				sSmoothExecTaskMap.put(aToken , task) ;
				exec(task, aDelayTimeInMillSec , TimeUnit.MILLISECONDS);
			}
			else
				task.setRun(aRun) ;
		}
	}
	
	private static class SmoothExecTask implements CRun
	{
		String mToken ;
		Runnable mRun ;
		
		public SmoothExecTask(String aToken , Runnable aRun)
		{
			mToken = aToken ;
			mRun = aRun ;
		}
		
		public void setRun(Runnable aRun)
		{
			mRun = aRun;
		}
		
		@Override
		public void run()
		{
			if(isClosing())
				return ;
			synchronized (mToken)
			{
				sSmoothExecTaskMap.remove(mToken) ;			//肯定不为null
			}
			Runnable run = mRun ;
			if(run != null)
				mRun.run();
		}
	}
	
	
	/**
	 * 和smoothExec相似，差别只是第一次执行有点不同。throttleExec如果是第一次执行，会立马执行；而smoothExec是延时指定时间执行
	 * @param aToken
	 * @param aDelayTimeInMillSec
	 * @param aRun
	 */
	public static void throttleExec(String aToken , int aDelayTimeInMillSec , Runnable aRun)
	{
		aToken = aToken.intern() ;
		synchronized (aToken)
		{
			SmoothExecTask task = sSmoothExecTaskMap.get(aToken) ;
			if(task == null)
			{
				if(aRun != null)
				{
					aRun.run();
					task = new SmoothExecTask(aToken, null) ;
					sSmoothExecTaskMap.put(aToken , task) ;
					exec(task, aDelayTimeInMillSec , TimeUnit.MILLISECONDS);
				}
			}
			else
				task.setRun(aRun) ;
		}
	}
	
	static class ContextRun implements CRun
	{
		Map<String , Object> mThreadCtx ;
		Runnable mRun ;
		
		public ContextRun(Map<String, Object> aThreadCtx , Runnable aRun)
		{
			mThreadCtx = aThreadCtx ;
			mRun = aRun ;
		}

		@Override
		public void run()
		{
			if(isClosing())
				return ;
			AppContext.injectThreadContext(mThreadCtx) ;
			try
			{
				mRun.run();
			}
			finally
			{
				AppContext.removeInjectedThreadContext();
			}
		}
		
	}
	
	public static boolean isClosed()
	{
		return sClosed ;
	}
	
	public static void close()
	{
		if(!sClosed)
		{
			sClosed = true ;
			if(sCachedExecutor != null)
				sCachedExecutor.shutdown();
			if(sDaemonScheduledExecutor != null)
				sDaemonScheduledExecutor.shutdown(); 
			if(sScheduledExecutor != null)
				sScheduledExecutor.shutdown() ;
		}
	}
	
	public static void safeRun(final Runnable aRun)
	{
		if(aRun == null)
			return ;
		try
		{
			aRun.run();
		}
		catch(Throwable e)
		{
			Log.error(ExceptionAssist.getClearMessage(CommonExecutor.class , e , 1)) ;
		}
	}
}
