package team.sailboat.commons.fan.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.event.IXListener;
import team.sailboat.commons.fan.event.XEvent;
import team.sailboat.commons.fan.event.XListenerAssist;
import team.sailboat.commons.fan.event.IXEListener.BreakException;
import team.sailboat.commons.fan.serial.StreamAssist;

public class EventSource implements Runnable
{	
	public static final int sEventTag_end = -1 ;
	
	public static final int sEventTag_data = 1 ;
	
	Reader mReader ;
	
	final Map<String , XListenerAssist> mLsnAssistMap = XC.hashMap() ;
	
	public EventSource(InputStream aIns)
	{
		mReader = new InputStreamReader(aIns, AppContext.sUTF8) ;
	}
	
	public void addEventListener(String aTag , IXListener aLsn)
	{
		XListenerAssist lsnAssist = mLsnAssistMap.get(aTag) ;
		if(lsnAssist == null)
		{
			lsnAssist = new XListenerAssist() ;
			mLsnAssistMap.put(aTag, lsnAssist) ;
		}
		lsnAssist.addLastListener(aLsn) ;
	}
	
	protected boolean isEventStart(StringBuilder aStrBld , int aTagIndex)
	{
		if(aTagIndex == 0)
			return true ;
		if(aStrBld.charAt(aTagIndex-1) == '\n')
		{
			int ch = aStrBld.charAt(aTagIndex - 2) ;
			if(ch == '\n' )
				return true ;
			else if(ch == '\r')
			{
				return aStrBld.charAt(aTagIndex - 3) == '\n' ;
			}
		}
		return false ;
	}
	
	protected void notifyStreamEnd()
	{
		XEvent endEvent = new XEvent(null, sEventTag_end , "end") ;
		for(XListenerAssist lsnAssist : mLsnAssistMap.values())
		{
			lsnAssist.notifyLsns(endEvent) ;
		}
	}
	
	protected void notifyStreamException(Exception e)
	{
		for(XListenerAssist lsnAssist : mLsnAssistMap.values())
		{
			try
			{
				lsnAssist.notifyLsns(e) ;
			}
			catch (BreakException e1)
			{
				
			}
		}
	}
	
	@Override
	public void run()
	{
		char[] buf = new char[1024] ;
		int len = 0 ;
		StringBuilder strBld = new StringBuilder() ;
		int eventTagEndPos = 0 ;
		String tag = null ;			// 当前的数据标签
		int contentPos = 0 ;
		try
		{
			while((len = mReader.read(buf)) != -1)
			{
				if(len>0)
				{
					strBld.append(buf , 0 , len) ;
					int i = 0 ;
					while( (i = strBld.indexOf("event: " , eventTagEndPos)) != -1)
					{
						if(isEventStart(strBld, i))
						{
							if(tag != null)
							{
								if("fault".equals(tag))
								{
									notifyStreamException(new IllegalStateException(strBld.substring(contentPos , i))) ;
								}
								else
								{
									// 把前面的输出出去
									XListenerAssist assist = mLsnAssistMap.get(tag) ;
									if(assist != null)
									{
										assist.notifyLsns(new XEvent(strBld.substring(contentPos , i) , sEventTag_data
												, tag)) ; 
									}
								}
							}
							if(i > 0)
							{
								// 把前面的删掉
								strBld.delete(0, i) ;
							}
							eventTagEndPos = 7 ;
							// 提取新tag
							tag = null ;
							int tagEnd = strBld.indexOf("\n" , eventTagEndPos) ;
							if(tagEnd != -1)
							{
								tag = strBld.substring(eventTagEndPos , tagEnd).trim() ;
								contentPos = strBld.indexOf("data: " , tagEnd) ;
								if(contentPos != -1)
									contentPos += 6 ;
								if("end".equals(tag))
								{
									notifyStreamEnd(); 
								}
								
							}
						}
						else
						{
							// 当做正文内容
							eventTagEndPos = i + 7 ;
						}
					}
				}
			}
		}
		catch (IOException e)
		{
			notifyStreamException(e) ;
		}
		finally
		{
			StreamAssist.close(mReader) ;
		}
	}
	
	
}
