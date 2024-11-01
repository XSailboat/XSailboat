package team.sailboat.commons.ms.util;

import java.net.SocketException;

import org.slf4j.Logger;

import jakarta.servlet.http.HttpServletRequest;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.http.IdentityTrace;
import team.sailboat.commons.fan.sys.XNet;
import team.sailboat.commons.fan.text.RegexUtils;
import team.sailboat.commons.fan.text.XString;

public class Guard
{
	/**
	 * 
	 * @param aReq
	 * @param aLogger
	 * @param aOperDesc			操作描述
	 */
	public static IdentityTrace checkIdentityTrace(HttpServletRequest aReq , Logger aLogger
			, String aOperDesc , Object...aArgs)
	{
		IdentityTrace trace = IdentityTrace.get(aReq) ;
		if(XString.isEmpty(trace.getUserName()))
			throw new IllegalArgumentException("此操作要求必需有合法的审计信息！必需填写用户名。") ;
		if(!RegexUtils.checkIPv4(trace.getRootIP()))
			throw new IllegalArgumentException("此操作要求必需有合法的审计信息！rootIP："+trace.getRootIP()) ;
		aLogger.info(trace+" => " + XString.msgFmt(aOperDesc , aArgs));
		return trace ;
	}
	
	public static void checkLANIdentityTrace(HttpServletRequest aReq , String aWord , Logger aLogger
			, String aOperDesc , Object...aArgs)
	{
		IdentityTrace trace = IdentityTrace.get(aReq) ;
		try
		{
			if("whosyourdady".equals(aWord) && XNet.isSameWithLocalIp(trace.getRootIP() , 24))
			{
				aLogger.info(trace+"（通过ip及口令验证） => " + XString.msgFmt(aOperDesc , aArgs));
			}
			else
			{
				aLogger.warn("已拒绝！"+trace+"（通过ip及口令验证） => " + XString.msgFmt(aOperDesc , aArgs));
				throw new IllegalArgumentException("口令或ip验证不通过！") ;
			}
		}
		catch (SocketException e)
		{
			aLogger.warn(ExceptionAssist.getStackTrace(e)) ;
			throw new IllegalArgumentException("服务器内部异常！") ;
		}
	}
}
