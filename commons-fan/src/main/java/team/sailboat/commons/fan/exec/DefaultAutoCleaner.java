package team.sailboat.commons.fan.exec;

import team.sailboat.commons.fan.infc.ICondition;

/**
 * 得持有，防止在它相关的资源没回收之前被回收
 *
 * @author yyl
 * @since 2018年10月24日
 */
public class DefaultAutoCleaner extends AutoCleaner
{
	
	Runnable mCleaner ;
	ICondition mDisposedCnd ;
	
	/**
	 * 
	 * @param aPeriod  		 单位，秒
	 */
	public DefaultAutoCleaner(int aPeriodInSeconds , Runnable aCleaner)
	{
		super(aPeriodInSeconds) ;
		mCleaner = aCleaner ;
	}
	
	public DefaultAutoCleaner(int aPeriodInSeconds , Runnable aCleaner , ICondition aDisposedCnd)
	{
		super(aPeriodInSeconds) ;
		mCleaner = aCleaner ;
		mDisposedCnd = aDisposedCnd ;
	}
	
	@Override
	protected  void doClean()
	{
		mCleaner.run();
	}
	
	@Override
	public boolean isDisposed()
	{
		if(!mDisposed)
		{
			if(mDisposedCnd != null && mDisposedCnd.test())
				mDisposed = true ;
		}
		return mDisposed ;
	}
}
