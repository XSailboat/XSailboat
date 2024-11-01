package team.sailboat.commons.fan.log;

import java.util.function.Supplier;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.infc.ICondition;
import team.sailboat.commons.fan.text.XString;

public class Log implements ILogLevel
{
	
	static ILogListener[] sLsns ;
	
	public static void setPrintOnConsole(boolean aPrintOnConsole)
	{
		if(sPrintOnConsole != aPrintOnConsole)
		{
			synchronized (Log.class)
			{
				if(sPrintOnConsole != aPrintOnConsole)
				{
					sPrintOnConsole = aPrintOnConsole ;
					if(sPrintOnConsole)
						addListener(ConsoleLogListener.sInstance) ;
					else
						removeListener(ConsoleLogListener.sInstance) ;
				}
			}
		}
	}
	
	private static boolean sPrintOnConsole = false ;
	
	public synchronized static void addListener(ILogListener aLsn)
	{
		sLsns = XC.merge(sLsns, aLsn) ;
	}
	
	public synchronized static void removeListener(ILogListener aLsn)
	{
		sLsns = XC.remove(sLsns, aLsn) ;
	}
	
	private static void log(int aType , String aMsg)
	{
		if(aMsg != null)
		{
			final ILogListener[] lsns = sLsns ;
			if(lsns != null && lsns.length>0)
			{
				for(int i=0 ; i<lsns.length ; i++)
					lsns[i].log(aType, aMsg);
			}
		}
	}
	
	public static void info(String aMsgFmt , Object...aArgs)
	{
		log(sInfo , XString.msgFmt(aMsgFmt , aArgs));
	}
	
	public static <T> void info(Supplier<T> aSupplier)
	{
		Object result = aSupplier.get() ;
		if(result != null)
			log(sInfo, result.toString()) ;
	}
	
	public static void error(String aMsg)
	{
		log(sError, aMsg);
	}
	
	public static void error(String aMsg , Object...aArgs)
	{
		error(XString.msgFmt(aMsg, aArgs)) ;
	}
	
	public static void error(Class<?> aClass , Throwable e)
	{
		log(sError, ExceptionAssist.getClearMessage(aClass, e)) ;
	}
	
	public static void error(Class<?> aClass , Throwable e , String aAddMsg)
	{
		log(sError, ExceptionAssist.getClearMessage(aClass, e, aAddMsg)) ;
	}
	
	public static void warn(String aMsgFmt , Object...aArgs)
	{
		log(sWarn, XString.msgFmt(aMsgFmt, aArgs)) ;
	}
	
	public static void warn(Class<?> aClass , Throwable e)
	{
		log(sError, ExceptionAssist.getClearMessage(aClass, e)) ;
	}
	
	public static <T> void warn(Supplier<T> aSupplier)
	{
		T val = aSupplier.get() ;
		if(val != null)
			log(sWarn, val.toString()) ;
	}
	
	public static void debug(Object aMsg)
	{
		if(aMsg != null)
			log(sDebug, aMsg.toString()) ;
	}
	
	public static void debug(String aMsg , Object...aArgs)
	{
		log(sDebug, XString.msgFmt(aMsg, aArgs)) ;
	}
	
	public static <T> void debug(Supplier<T> aSupplier)
	{
		T val = aSupplier.get() ;
		if(val != null)
			log(sDebug, val.toString());
	}
	
	public static <T> void debug(ICondition aCnd , String aMsg)
	{
		if(aCnd.test())
		{
			log(sDebug, aMsg);
		}
	}
}
