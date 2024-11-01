package team.sailboat.commons.fan.event;

public interface IExceptionHandler
{
	/**
	 * 是否需要异步处理
	 * @return
	 */
	boolean isAsynchronous() ;
	
	void handle(Throwable aE) ;
	
	/**
	 * 提示消息
	 * @param aMsg
	 * @param aType		取值为IMessageType
	 */
	void prompt(String aMsg , int aType) ;
	
	/**
	 * 记录消息
	 * @param aMsg
	 * @param aType
	 */
	void log(String aMsg , int aType) ;
	
	void setEnabled(boolean aEnabled) ;
	
	boolean isEnabled() ;
}
