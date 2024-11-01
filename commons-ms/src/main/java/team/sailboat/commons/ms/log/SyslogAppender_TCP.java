package team.sailboat.commons.ms.log;

import java.net.SocketException;
import java.net.UnknownHostException;

import ch.qos.logback.classic.net.SyslogAppender;
import ch.qos.logback.core.net.SyslogOutputStream;
import team.sailboat.commons.fan.app.AppContext;

public class SyslogAppender_TCP extends SyslogAppender
{
	public SyslogAppender_TCP()
	{
		super() ;
		setCharset(AppContext.sUTF8);
	}
	
	@Override
	public SyslogOutputStream createOutputStream() throws SocketException, UnknownHostException
	{
		return new SyslogOutputStream_TCP(getSyslogHost(), getPort());
	}
}
