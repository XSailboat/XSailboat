package team.sailboat.commons.ms.log;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.qos.logback.classic.spi.ILoggingEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;
import team.sailboat.commons.fan.time.XTime;

@Tag(name="日志记录行")
public class LogRecord implements ToJSONObject
{
	
	public static final String sLN_error = "错误" ;
	public static final String sLN_info = "消息" ;
	public static final String sLN_warn = "警告" ;
	public static final String sLN_debug = "调试" ;
	
	Date mTime;
	String mMessage = "";
	String mLevelName ;
	int mIndent;
	long mSeq ;
	
	public LogRecord()
	{
	}
	
	public LogRecord(long aSeq)
	{
		mSeq = aSeq ;
	}
	
	@Schema(description = "时间戳")
	public Date getTime()
	{
		return mTime;
	}
	public void setTime(long aTime)
	{
		mTime = new Date(aTime) ;
	}
	public void setTime(Date aTime)
	{
		mTime = aTime ;
	}
	
	@Schema(description = "日志消息")
	public String getMessage()
	{
		return mMessage;
	}
	public void setMessage(String aMessage)
	{
		mMessage = aMessage;
	}
	
	@Schema(description = "日志级别名称")
	public String getLevelName()
	{
		return mLevelName ;
	}
	public void setLevelName(String aLevelName)
	{
		mLevelName = aLevelName ;
	}
	

	@Schema(description = "缩进数量")
	public int getIndent()
	{
		return mIndent;
	}
	public void setIndent(int aIndent)
	{
		mIndent = aIndent;
	}
	
	@Schema(description = "序号")
	public long getSeq()
	{
		return mSeq;
	}
	public void setSeq(long aSeq)
	{
		mSeq = aSeq;
	}

	
	@Override
	public String toString()
	{
		StringBuilder strBld = new StringBuilder() ;
		
		strBld.append('[').append(XTime.format$yyMMddHHmmss(mTime))
			.append(",") // .append(getLevelName(mLevel))
			.append("] ") ;
		if(mIndent>0)
		{
			for(int i=0 ; i<mIndent ; i++)
				strBld.append('\t') ;
		}
		strBld.append(mMessage) ;
		return strBld.toString() ;
	}

	@Schema(hidden = true)
	@JsonIgnore
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return aJSONObj.put("seq", mSeq)
				.put("indent", mIndent)
				.put("levelName" , mLevelName)
				.put("time", XTime.format$yyyyMMddHHmmssSSS(mTime, ""))
				.put("message" , mMessage) ;
	}
	
	public static LogRecord create(long aIndex , ILoggingEvent aLogRcd)
	{
		LogRecord logRcd = new LogRecord(aIndex) ;
		switch(aLogRcd.getLevel().levelInt)
		{
		case ch.qos.logback.classic.Level.INFO_INT :
			logRcd.setLevelName(sLN_info) ;
			break ;
		case ch.qos.logback.classic.Level.ERROR_INT:
			logRcd.setLevelName(sLN_error);
			break ;
		case ch.qos.logback.classic.Level.WARN_INT:
			logRcd.setLevelName(sLN_warn) ;
			break ;
		case ch.qos.logback.classic.Level.DEBUG_INT:
			logRcd.setLevelName(sLN_debug);
			break ;
		default:
		}
		
		logRcd.setMessage(aLogRcd.getFormattedMessage());
		logRcd.setTime(aLogRcd.getTimeStamp());
		return logRcd ;
	}
}
