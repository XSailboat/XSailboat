package team.sailboat.commons.fan.event;


public interface ITListener
{
	/**
	 * 处理事件
	 * @param aEvent
	 */
	void handle(TEvent aEvent) ;
	/**
	 * 是否关注此事件
	 * @param aEvent
	 * @return
	 */
	boolean isCare(TEvent aEvent) ;
}
