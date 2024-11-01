package team.sailboat.commons.ms.util;

import org.slf4j.Logger;

import jakarta.servlet.http.HttpServletRequest;
import team.sailboat.commons.fan.http.IdentityTrace;
import team.sailboat.commons.fan.text.XString;

public class LogKit
{
	public static void info(Logger aLogger , HttpServletRequest aReq , String aMsg , Object...aArgs)
	{
    	aLogger.info(XString.msgFmt("来自[{}]的用户{}-->" , aReq.getRemoteAddr() , IdentityTrace.get(aReq))
    			+ XString.msgFmt(aMsg, aArgs)) ;
	}
	
	public static void warn(Logger aLogger , HttpServletRequest aReq , String aMsg , Object...aArgs)
	{
    	aLogger.warn(XString.msgFmt("来自[{}]的用户{}-->" , aReq.getRemoteAddr() , IdentityTrace.get(aReq))
    			+ XString.msgFmt(aMsg, aArgs)) ;
	}
	
	public static void error(Logger aLogger , HttpServletRequest aReq , String aMsg , Object...aArgs)
	{
    	aLogger.error(XString.msgFmt("来自[{}]的用户{}-->" , aReq.getRemoteAddr() , IdentityTrace.get(aReq))
    			+ XString.msgFmt(aMsg, aArgs)) ;
	}
}
