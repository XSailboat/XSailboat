package team.sailboat.commons.fan.event;

import team.sailboat.commons.fan.event.IXEListener.BreakException;

/**
 * 事件通知器
 *
 * @author yyl
 * @version 1.0 
 * @since 2014-6-4
 */
public interface IXEventNotifier
{
	public void notifyLsns(XEvent aEvent) ;
	public void notifyLsns(Exception aE) throws BreakException ;
}
