package team.sailboat.commons.fan.excep;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionAssist
{
	/**
	 * 取得最原始的异常
	 * @param aE
	 * @return
	 */
	public static Throwable getRootException(Throwable aE)
	{
		while(aE.getCause() != null)
			aE = aE.getCause() ;
		return aE ;
	}
	
	/**
	 * 取得最原始的异常消息
	 * @param aE
	 * @return
	 */
	public static String getRootMessage(Throwable aE)
	{
		return getRootException(aE).getMessage() ;
	}
	
	/**
	 * 取得异常堆栈
	 * @param aE
	 * @return
	 */
	public static String getStackTrace(Throwable aE)
	{
		StringWriter strWriter = new StringWriter() ;
		aE.printStackTrace(new PrintWriter(strWriter));
		return strWriter.toString() ; 
	}
	
	public static String getLocation(Class<?> aClass , Throwable e)
	{
		return getLocation(aClass, e, 0) ;
	}
	
	public static String getLocation(Class<?> aClass , Throwable e , int aUpper)
	{
		String name = aClass.getName() ;
		String methodName = null ;
		int lineNum = 0 ;
		int u = -1 ;
		for(StackTraceElement trace : e.getStackTrace())
		{
			if(name.equals(trace.getClassName()))
			{
				u = 0 ;
			}
			if(u >= aUpper)
			{
				methodName = trace.getMethodName() ;
				lineNum = trace.getLineNumber() ;
				break ;
			}
		}
		if(methodName == null)
			return null ;
		return String.format("%s(行%s，方法%s)", name , lineNum , methodName) ;
	}
	
	/**
	 * 格式：位置：%1$s ; 根消息类型：%2$s ; 根消息：%3$s
	 * @param aClass
	 * @param aE
	 * @return
	 */
	public static String getClearMessage(Class<?> aClass , Throwable aE)
	{
		return getClearMessage(aClass, aE, 0) ;
	}
	
	public static String getClearMessage(Class<?> aClass , Throwable aE , int aUpper)
	{
		return String.format("位置：%1$s ; 根消息类型：%2$s ; 根消息：%3$s", getLocation(aClass, aE , aUpper) 
				, getRootException(aE).getClass().getName() , getRootMessage(aE)) ;
	}
	
	/**
	 * 格式：位置：%1$s ; 根消息类型：%2$s ; 根消息：%3$s ; 消息：%4$s
	 * @param aClass
	 * @param aE
	 * @param aExtendMsg
	 * @return
	 */
	public static String getClearMessage(Class<?> aClass , Throwable aE , String aExtendMsg)
	{
		return getClearMessage(aClass, aE, aExtendMsg, 0) ;
	}
	
	public static String getClearMessage(Class<?> aClass , Throwable aE , String aExtendMsg
			, int aUpper)
	{
		return String.format("位置：%1$s ; 根消息类型：%2$s ; 根消息：%3$s ; 消息：%4$s", getLocation(aClass, aE , aUpper) 
				, getRootException(aE).getClass().getName() , getRootMessage(aE) , aExtendMsg) ;
	}
	
	public static void impossible(Throwable e)
	{
		if(e instanceof RuntimeException)
			throw (RuntimeException)e ;
		throw new IllegalStateException(e) ;
	}
}
