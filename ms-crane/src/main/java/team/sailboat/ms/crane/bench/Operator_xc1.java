package team.sailboat.ms.crane.bench;

import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.ms.log.LogPool;
import team.sailboat.ms.crane.cmd.ICmd;
import team.sailboat.ms.crane.cmd.LocalCmds;

/**
 * 
 * 本地一次执行的命令
 *
 * @author yyl
 * @since 2024年10月23日
 */
public class Operator_xc1 extends Operator_LocalEnv
{
	String mRealCmd ;
	
	public Operator_xc1(LogPool aLogPool , String aOperationName , String aRealCmd)
	{
		super(aLogPool, aOperationName) ;
		mRealCmd = aRealCmd ;
	}

	@Override
	protected boolean doCmds()
	{
		String cmdName = IOperator.getCmdName(mRealCmd) ;
		Assert.isTrue(LocalCmds.isLocalOne(cmdName) , "命令[%s]不是本地一次执行(LocalOne)的命令！" , mRealCmd) ;
		
		ICmd cmd = LocalCmds.getCmd(cmdName) ;
		Assert.notNull("不支持的本地命令：%s", cmdName) ;
		try
		{
			cmd.accept(IOperator.getCmdArgs(mRealCmd)) ;
			logInfo("命令执行成功。命令：{}" , mRealCmd) ;
			return true ;
		}
		catch (Exception e)
		{
			logError("命令执行失败。命令：{} 。原因：{}",mRealCmd , ExceptionAssist.getRootMessage(e)) ;
			mLogger.error(ExceptionAssist.getStackTrace(e)) ;
			return false ;			// dead code
		}
	}
	
}
