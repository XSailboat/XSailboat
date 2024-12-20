package team.sailboat.ms.crane.cmd;

import team.sailboat.ms.crane.bench.ICmdExecLogger;

/**
 * 
 * Cmd基础类
 *
 * @author yyl
 * @since 2024年11月15日
 */
public abstract class Cmd implements ICmd
{
	protected ICmdExecLogger mLogger ;
	
	@Override
	public void setCmdExecLogger(ICmdExecLogger aLogger)
	{
		mLogger = aLogger ;
	}
}
