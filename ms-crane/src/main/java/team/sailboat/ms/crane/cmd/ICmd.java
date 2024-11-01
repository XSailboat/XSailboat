package team.sailboat.ms.crane.cmd;

import team.sailboat.commons.fan.infc.EConsumer;

/**
 * 命令
 *
 * @author yyl
 * @since 2024年10月19日
 */
public interface ICmd extends EConsumer<String[] , Exception>
{
	default CmdEnv getEnv()
	{
		return LocalCmds.sEnv ;
	}
}
