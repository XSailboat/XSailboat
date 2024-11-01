package team.sailboat.ms.crane.bench;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import team.sailboat.commons.fan.event.IXListener;
import team.sailboat.commons.fan.event.XEvent;
import team.sailboat.commons.fan.event.XListenerAssist;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.exec.RunStatus;
import team.sailboat.commons.fan.struct.Tuples;
import team.sailboat.commons.ms.log.LogPool;
import team.sailboat.ms.crane.bean.HostProfile;

/**
 * 操作手，与指定主机相关的命令
 *
 * @author yyl
 * @since 2024年9月14日
 */
public abstract class Operator_RemoteHost implements IOperator
{
	protected final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	protected RunStatus mRunStatus = RunStatus.norun ;
	private final LogPool mLogPool ;
	protected final HostProfile mHostProfile ;
	protected final String mOperationName ;
	/**
	 * 执行成功监听管理器
	 */
	protected final XListenerAssist mFinishLsnAssist = new XListenerAssist() ;
	
	public Operator_RemoteHost(LogPool aLogPool , HostProfile aHostProfile
			, String aOperationName)
	{
		mLogPool = aLogPool ;
		mHostProfile = aHostProfile ;
		mOperationName = aOperationName ;
	}
	
	@Override
	public void addFinishListener(IXListener aLsn)
	{
		mFinishLsnAssist.addLastListener(aLsn) ;
	}
	
	protected void logInfo(String aMsg , Object...aArgs)
	{
		IOperator.logInfo(mLogPool, mHostProfile.getName(), aMsg, aArgs) ;
	}
	
	protected void logError(String aMsg , Object...aArgs)
	{
		IOperator.logError(mLogPool, mHostProfile.getName(), aMsg, aArgs) ;
	}

	@Override
	public RunStatus getStatus()
	{
		return mRunStatus ;
	}
	
	@Override
	public boolean isFinished()
	{
		return mRunStatus == RunStatus.success
				|| mRunStatus == RunStatus.failure ;
	}

	@Override
	public final void run()
	{
		mRunStatus = RunStatus.running ;
		try
		{
			mRunStatus = doCmds()?RunStatus.success:RunStatus.failure ;
		}
		catch(Exception e)
		{
			logError(ExceptionAssist.getRootMessage(e));
			mRunStatus = RunStatus.failure ;
		}
		finally
		{
			mFinishLsnAssist.notifyLsns(new XEvent(Tuples.of(mHostProfile.getName() , mRunStatus), 0)) ;
		}
		
	}
	
	/**
	 * 
	 * @return	执行成功返回true，执行失败返回false。错误消息在日志中记录
	 */
	protected abstract boolean doCmds() ;

}
