package team.sailboat.commons.fan.log;

import java.util.Map.Entry;
import java.util.Properties;

public class Debug
{

	/**
	 * 当前是否是调试状态
	 */
	public static boolean sDebug = false ;
	
	static ThreadLocal<Long> sClock = new ThreadLocal<Long>() ;
	
	/**
	 * 
	 */
	public static void clockOn()
	{
		if(sDebug)
			sClock.set(System.currentTimeMillis()) ;
	}
	
	public static void clockOn(String aMsg , Object...aArgs)
	{
		if(sDebug)
		{
			Log.debug(String.format(aMsg, aArgs));
			sClock.set(System.currentTimeMillis()) ;
		}
	}
	
	public static void clockOff(String aTip , Object...aArgs)
	{
		if(!sDebug)
			return ;
		long end = System.currentTimeMillis() ;
		Long start = sClock.get() ;
		if(start != null)
		{
			sClock.remove() ;
			StringBuilder strBld = new StringBuilder() ;
			strBld.append("耗时：").append(end-start).append("毫秒。") ;
			if(aTip != null)
				strBld.append(String.format(aTip, aArgs)) ;
			Log.debug(strBld.toString()) ;
		}
	}
	
	public static void cout(Object aMsg)
	{
		if(sDebug)
			Log.debug(aMsg.toString()) ;
	}
	
	public static void cout(String aMsg , Object...aArgs)
	{
		if(sDebug)
			Log.debug(String.format(aMsg, aArgs)) ;
	}
	
	public static void coutProperties()
	{
		if(!sDebug)
			return ;
		Properties props = System.getProperties() ;
		Log.debug("--Properties--------------------------------") ;
		for(Entry<Object, Object> entry : props.entrySet())
			Log.debug(entry.getKey()+"="+entry.getValue());
	}
}
