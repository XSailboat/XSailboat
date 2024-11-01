package team.sailboat.commons.ms.log;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.ms.ACKeys_Common;

/**
 * 格式：taskId#[毫秒时间][3位序号]
 *
 * @author yyl
 * @since 2021年4月22日
 */
public class TaskLogRowKeyConfig extends ClassicConverter
{
	
	final ThreadLocal<long[]> mLastSnap = new ThreadLocal<long[]>() ; 
	
	public TaskLogRowKeyConfig()
	{
	}


	@Override
	public String convert(ILoggingEvent aEvent)
	{
		String taskId = AppContext.getThreadLocal(ACKeys_Common.sTaskId , String.class) ;
		long[] last = mLastSnap.get() ;
		if(last == null)
		{
			last = new long[] {aEvent.getTimeStamp() , 0} ;
			mLastSnap.set(last) ;
		}
		else if(last[0] == aEvent.getTimeStamp())
			last[1]++ ;
		else
		{
			last[0] = aEvent.getTimeStamp() ;
			last[1] = 0 ;
		}
			
		return (taskId != null? taskId:"0000") +"#"+String.format("%1$d%2$03d", last[0] , last[1]) ;
	}

}
