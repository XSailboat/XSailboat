package team.sailboat.commons.fan.log;

import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.fan.time.XTime;

public class ConsoleLogListener implements ILogListener
{
	
	public static final ConsoleLogListener sInstance = new ConsoleLogListener() ;

	private ConsoleLogListener()
	{
	}
	
	@Override
	public void log(int aType, String aMsg)
	{
		System.out.println(format(aType, aMsg)) ;
	}

	protected String format(int aType, String aMsg)
	{
		switch(aType)
		{
		case sInfo:
			return XString.splice(XTime.current$yyyyMMddHHmmss()," [" , Thread.currentThread().getName() 
					, "] INFO  LOG -" , aMsg) ;
		case sError:
			return XString.splice(XTime.current$yyyyMMddHHmmss()," [" , Thread.currentThread().getName() 
					, "] ERROR  LOG -" , aMsg) ;
		case sDebug:
			return XString.splice(XTime.current$yyyyMMddHHmmss()," [" , Thread.currentThread().getName() 
					, "] DEBUG  LOG -" , aMsg) ;
		case sWarn:
			return XString.splice(XTime.current$yyyyMMddHHmmss()," [" , Thread.currentThread().getName() 
					, "] WARN  LOG -" , aMsg) ;
		default:
			throw new IllegalArgumentException("未知的日志级别：" +aType) ;
		}
	}
}
