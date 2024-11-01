package team.sailboat.commons.ms.log;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class MyLineSeparatorConverter extends ClassicConverter {

    public String convert(ILoggingEvent event)
    {
        return "\n" ; 
    }

}

