package team.sailboat.ms.crane.bench;

import java.util.Date;
import java.util.List;

import team.sailboat.commons.fan.cli.CommandLineParser;
import team.sailboat.commons.fan.event.IXListener;
import team.sailboat.commons.fan.exec.ITask;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.fan.text.XStringReader;
import team.sailboat.commons.fan.time.XTime;
import team.sailboat.commons.ms.log.LogPool;

/**
 * 操作的执行者
 *
 * @author yyl
 * @since 2024年9月14日
 */
public interface IOperator extends ITask
{
	/**
	 * 是否已经执行完成。无论是成功，还是失败
	 * @return
	 */
	boolean isFinished() ;
	
	/**
	 * 添加执行完成监听器
	 * @param aLsn
	 */
	void addFinishListener(IXListener aLsn) ;
	
	public static void logInfo(LogPool aLogPool , String aHost , String aMsg , Object...aArgs)
	{
		String log = null ;
		if(aHost == null)
			log = XString.msgFmt("{} 消息 -{}", XTime.format$HHmmssSSS(new Date()) 
					, XString.msgFmt(aMsg , aArgs)) ;
		else
			log = XString.msgFmt("{} 消息[{}] -{}", XTime.format$HHmmssSSS(new Date()) , aHost 
					, XString.msgFmt(aMsg , aArgs)) ;
		aLogPool.add(log) ;
	}
	
	public static void logError(LogPool aLogPool , String aHost , String aMsg , Object...aArgs)
	{
		String log = null ;
		if(aHost == null)
			log = XString.msgFmt("{} 错误 -{}", XTime.format$HHmmssSSS(new Date()) , XString.msgFmt(aMsg , aArgs)) ;
		else
			log = XString.msgFmt("{} 错误[{}] -{}", XTime.format$HHmmssSSS(new Date()) , aHost 
					, XString.msgFmt(aMsg , aArgs)) ;
		aLogPool.add(log) ;
	}
	
	/**
	 * 
	 * 是不是需要本地执行的命令
	 * 
	 * @param aCommand
	 * @return
	 */
	@SuppressWarnings("resource")
	static String getCmdName(String aCommand)
	{
		return new XStringReader(aCommand).readNextUnblank() ;
	}
	
	/**
	 * 
	 * 取得命令参数
	 * 
	 * @param aCommand
	 * @return
	 */
	static String[] getCmdArgs(String aCommand)
	{
		List<String> segs = CommandLineParser.splitLine(aCommand) ;
		return segs.subList(1, segs.size()).toArray(JCommon.sEmptyStringArray) ;
	}
}
