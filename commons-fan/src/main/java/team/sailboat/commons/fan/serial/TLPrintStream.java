package team.sailboat.commons.fan.serial;

import java.io.OutputStream;
import java.io.PrintStream;

import team.sailboat.commons.fan.event.IXListener;
import team.sailboat.commons.fan.event.XEvent;
import team.sailboat.commons.fan.event.XListenerAssist;

public class TLPrintStream  extends PrintStream
{
	
	final ThreadLocal<XListenerAssist> mTL_LsnAssist = new ThreadLocal<XListenerAssist>() ;
	final ThreadLocal<Boolean> mTL_door = new ThreadLocal<Boolean>() ;
	

	public TLPrintStream(OutputStream aOut)
	{
		super(aOut);
	}
	
	public IXListener addMessageListener(IXListener aLsn)
	{
		XListenerAssist lsnAssist = mTL_LsnAssist.get() ;
		if(lsnAssist == null)
		{
			lsnAssist = new XListenerAssist() ;
			mTL_LsnAssist.set(lsnAssist) ;
		}
		lsnAssist.addListener(aLsn) ;
		return aLsn ;
	}
	
	public void removeMessageListener(IXListener aLsn)
	{
		XListenerAssist lsnAssist = mTL_LsnAssist.get() ;
		if(lsnAssist != null)
		{
			lsnAssist.removeLsn(aLsn) ;
		}
	}
	
	@Override
	public void print(String aS)
	{
		if(!Boolean.TRUE.equals(mTL_door.get()))
		{
			// 防止无限递归
			mTL_door.set(Boolean.TRUE) ;
			try
			{
				XListenerAssist lsnAssist = mTL_LsnAssist.get() ;
				if(lsnAssist != null)
				{
					lsnAssist.notifyLsns(new XEvent(Thread.currentThread() , 0 , aS));
				}
			}
			finally
			{
				mTL_door.set(Boolean.FALSE) ;
			}
		}
		super.print(aS) ;
	}
	
	public static TLPrintStream wrapSysOut()
	{
		PrintStream ps = System.out ;
		if(ps == null)
			return null ;
		if(ps instanceof TLPrintStream)
			return (TLPrintStream)ps ;
		
		TLPrintStream tlps = new TLPrintStream(System.out) ;
		System.setOut(tlps) ;
		return tlps ;
	}
	
	public static void removeSysOutListener(IXListener aLsn)
	{
		if(aLsn == null)
			return ;
		PrintStream ps = System.out ;
		if(ps == null)
			return ;
		if(ps instanceof TLPrintStream)
		{
			((TLPrintStream)ps).removeMessageListener(aLsn)  ;
		}
	}
}