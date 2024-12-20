package team.sailboat.ms.crane.bench;

public interface ICmdExecLogger
{
	
	/**
	 * 
	 * 记录命令执行相关的消息类型的日志
	 * 
	 * @param aMsg
	 * @param aArgs
	 */
	void logInfo(String aMsg , Object...aArgs) ;
	
	/**
	 * 
	 * 记录命令执行相关的错误类型的日志
	 * 
	 * @param aMsg
	 * @param aArgs
	 */
	void logError(String aMsg , Object...aArgs) ;
}
