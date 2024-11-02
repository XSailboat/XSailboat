package team.sailboat.bd.base.log;

import java.io.Closeable;

import team.sailboat.bd.base.infc.IRunDelegation;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.fan.time.XTime;

public interface ILogCommitter extends Closeable , IRunDelegation
{
	void start() ;
	
	void submitLogs(String aExecId , JSONArray aLogsJa) ;
	
	void submitLogs(String aExecId , String aPrefix , String... aLogs) ;
	
	String getName() ;
	
	default void info(String aExecId , String aMsg , Object...aArgs)
	{
		submitLogs(aExecId , null , new StringBuilder()
				.append(XTime.current$yyyyMMddHHmmssSSS())
				.append(" INFO  ")
				.append(getName())
				.append(" -")
				.append(XString.msgFmt(aMsg, aArgs)).toString()) ;
	}
	
	default void error(String aExecId , String aMsg , Object...aArgs)
	{
		submitLogs(aExecId , null , new StringBuilder()
				.append(XTime.current$yyyyMMddHHmmssSSS())
				.append(" ERROR  ")
				.append(getName())
				.append(" -")
				.append(XString.msgFmt(aMsg, aArgs)).toString()) ;
	}
	
	default void warn(String aExecId , String aMsg , Object...aArgs)
	{
		submitLogs(aExecId , null , new StringBuilder()
				.append(XTime.current$yyyyMMddHHmmssSSS())
				.append(" WARN  ")
				.append(getName())
				.append(" -")
				.append(XString.msgFmt(aMsg, aArgs)).toString()) ;
	}
}
