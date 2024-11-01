package team.sailboat.commons.ms.log;

import java.util.function.Consumer;

import ch.qos.logback.classic.spi.ILoggingEvent;
import team.sailboat.commons.fan.collection.IMultiMap;
import team.sailboat.commons.fan.collection.SizeIter;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.text.XString;

public class KeyByLogEventSite
{
	final static _LogEventListener sAllocateLsn = new _LogEventListener() ;
	
	static IKeyByLogEventListener[] sLsns = new IKeyByLogEventListener[] { sAllocateLsn } ;

	
	public static void addLogListener(IKeyByLogEventListener aLsn)
	{
		if(aLsn != null)
			sLsns = XC.merge(sLsns, aLsn) ;
	}
	
	public static void removeLogListener(IKeyByLogEventListener aLsn)
	{
		sLsns = XC.remove(sLsns, aLsn) ;
	}
	
	public static void addLogListener(String aAppenderName , String aKey , Consumer<ILoggingEvent> aConsumer)
	{
		sAllocateLsn.addConsumer(aAppenderName, aKey, aConsumer) ;
	}
	
	public static void consume(String aAppenderName , String aKey , ILoggingEvent aEvent)
	{
		IKeyByLogEventListener[] lsns = sLsns ;
		for(IKeyByLogEventListener lsn : lsns)
		{
			lsn.accept(aAppenderName, aKey, aEvent) ;
		}
	}
	
	static class _LogEventListener implements IKeyByLogEventListener
	{
		
		final IMultiMap<String, Consumer<ILoggingEvent>> mConsumerMap = XC.multiMap() ;

		
		public void addConsumer(String aAppenderName , String aKey , Consumer<ILoggingEvent> aConsumer)
		{
			mConsumerMap.put(XString.splice(aAppenderName , aKey) , aConsumer) ;
		}
		
		@Override
		public void accept(String aAppenderName, String aKey, ILoggingEvent aEvent)
		{
			SizeIter<Consumer<ILoggingEvent>> it = mConsumerMap.get(XString.splice(aAppenderName , aKey)) ;
			if(it != null)
			{
				for(Consumer<ILoggingEvent> consumer : it)
				{
					consumer.accept(aEvent) ;
				}
			}
		}
		
	}
}
