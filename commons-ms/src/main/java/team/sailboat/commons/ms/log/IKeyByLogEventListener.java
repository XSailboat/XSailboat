package team.sailboat.commons.ms.log;

import ch.qos.logback.classic.spi.ILoggingEvent;

public interface IKeyByLogEventListener
{
	void accept(String aAppenderName , String aKey , ILoggingEvent aEvent) ;
}
