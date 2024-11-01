package team.sailboat.commons.ms.log;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;

@Tag(name="日志记录行")
public class LogMsg implements ToJSONObject
{
	String mMessage = "";
	long mSeq ;
	
	public LogMsg()
	{
	}
	
	public LogMsg(long aSeq , String aMessage)
	{
		mSeq = aSeq ;
		mMessage = aMessage ;
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
		return mMessage ;
	}

	@Schema(hidden = true)
	@JsonIgnore
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return aJSONObj.put("seq", mSeq)
				.put("message" , mMessage) ;
	}
	
	public static LogMsg create(long aIndex , String aLogMsg)
	{
		return new LogMsg(aIndex , aLogMsg) ;
	}
}
