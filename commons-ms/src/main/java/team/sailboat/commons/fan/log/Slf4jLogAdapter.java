package team.sailboat.commons.fan.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import team.sailboat.commons.fan.log.ILogListener;
import team.sailboat.commons.fan.log.Log;

public class Slf4jLogAdapter implements ILogListener
{
	final Logger mLogger = LoggerFactory.getLogger(Log.class) ;

	@Override
	public void log(int aType, String aMsg)
	{
		switch(aType)
		{
		case Log.sInfo:
			mLogger.info(aMsg) ;
			break ;
		case Log.sWarn:
			mLogger.warn(aMsg) ;
			break ;
		case Log.sError:
			mLogger.error(aMsg) ;
			break ;
		case Log.sDebug:
			mLogger.debug(aMsg) ;
		}
	}

}
