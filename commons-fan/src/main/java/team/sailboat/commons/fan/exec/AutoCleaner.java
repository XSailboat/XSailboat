package team.sailboat.commons.fan.exec;

import java.io.IOException;

import team.sailboat.commons.fan.infc.IAutoCleanable;
import team.sailboat.commons.fan.infc.YRunnable;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.time.XTime;

public abstract class AutoCleaner implements IAutoCleanable
{
	static final Object sMutext = new Object() ;
	static WeakTasksRun sRunner ;
	
	YRunnable mPeriodRun = null ;
	
	boolean mDisposed ;
	
	/**
	 * 
	 * @param aPeriod  		 单位，秒
	 */
	public AutoCleaner(int aPeriod)
	{
		if(sRunner == null)
		{
			synchronized (sMutext)
			{
				if(sRunner == null)
				{
					sRunner = new WeakTasksRun() ;
					CommonExecutor.execInSelfThread(new LoopRunner(sRunner, 1000) 
							, "AutoCleaner清理", Thread.NORM_PRIORITY, true);
				}
			}
		}
		mPeriodRun = new PeriodRunner(aPeriod*1000) ;
		sRunner.addTask(mPeriodRun);
	}
	
	public boolean isDisposed()
	{
		return mDisposed ;
	}
	
	public void dispose()
	{
		mDisposed = true ;
	}
	
	protected abstract void doClean() ;
	
	/**
	 * 保证超过周期时间再执行
	 *
	 * @author yyl
	 * @since 2018年9月19日
	 */
	class PeriodRunner implements YRunnable
	{
		int mPeriod ;
		long mLastTime ;

		/**
		 * 
		 * @param aPeriod 单位毫秒
		 */
		public PeriodRunner(int aPeriod)
		{
			mPeriod = aPeriod ;
			mLastTime = System.currentTimeMillis() ;
		}
		
		@Override
		public void run()
		{
			if(XTime.pass(mLastTime, mPeriod))
			{
				doClean();
				mLastTime = System.currentTimeMillis() ;
			}
		}
		
		@Override
		public boolean isClosed()
		{
			return isDisposed() ;
		}

		@Override
		public void close() throws IOException
		{
			mDisposed = true ;
		}
		
	}
	
	/**
	 * 防止执行频率过高
	 *
	 * @author yyl
	 * @since 2018年9月19日
	 */
	static class LoopRunner implements Runnable
	{

		long mLastRunTime ;
		boolean mInterrupted = false ;
		Runnable mRunner ;
		int mMinTimeGap = 1000 ;
		
		/**
		 * 
		 * @param aRunner
		 * @param aMinTimeGap		单位毫秒
		 */
		public LoopRunner(Runnable aRunner , int aMinTimeGap)
		{
			mRunner = aRunner ;
			mMinTimeGap = aMinTimeGap ;
		}
		
		public void interrupt()
		{
			mInterrupted = true ;
		}
		
		@Override
		public void run()
		{
			while(!mInterrupted)
			{
				if(XTime.pass(mLastRunTime, mMinTimeGap))
				{
					mLastRunTime = System.currentTimeMillis() ;
					mRunner.run();
				}
				else
					JCommon.sleep(mMinTimeGap) ;
			}
		}
		
	}
}
