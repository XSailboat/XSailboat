package team.sailboat.commons.fan.event;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.event.IXEListener.BreakException;

public class XListenerAssist implements IXEventNotifier
{
	static MassDispatchThread sThread ;
	IXListener[] mLsns ;
	IXListener[] mLastLsns ;
	int mHandle = -1 ;
	
	public void addListener(IXListener aLsn)
	{
		if(aLsn == null)
			return ;
		if(mLsns == null)
			mLsns = new IXListener[]{aLsn} ;
		else if(!XC.contains(mLsns , aLsn))
			mLsns = XC.merge(mLsns , aLsn) ;
	}
	
	public void addLastListener(IXListener aLsn)
	{
		if(aLsn == null)
			return ;
		if(mLastLsns == null)
			mLastLsns = new IXListener[]{aLsn} ;
		else if(!XC.contains(mLastLsns , aLsn))
			mLastLsns = XC.merge(mLastLsns , aLsn) ;
	}
	
	public boolean hasListener(IXListener aLsn)
	{
		return (mLsns != null && XC.contains(mLsns, aLsn))
				|| (mLastLsns != null && XC.contains(mLastLsns, aLsn))
				;
	}
	
	@Override
	public void notifyLsns(XEvent aEvent)
	{
		mLsns = notifyLsns(aEvent, mLsns) ;
		mLastLsns = notifyLsns(aEvent, mLastLsns) ;
	}
	
	protected IXListener[] notifyLsns(XEvent aEvent , IXListener[] aLsns)
	{
		if(aLsns != null)
		{
			boolean dirty = false ;
			for(IXListener lsn : aLsns)
			{
				if(lsn instanceof XComplexLsn)
				{
					if(((XComplexLsn)lsn).isDisposed())
					{
						dirty = true ;
						continue ;
					}
					if(!((XComplexLsn)lsn).isEnabled())
						continue ;
				}
				lsn.handle(aEvent) ;
			}
			if(dirty)
				aLsns = cleanDisposed(aLsns) ;
		}
		return aLsns ;
	}
	
	IXListener[] cleanDisposed(IXListener[] aLsns)
	{
		if(aLsns != null)
		{
			boolean dirty = false ;
			for(int i=0 ; i<aLsns.length ; i++)
			{
				if(aLsns[i] instanceof XComplexLsn)
				{
					if(((XComplexLsn)aLsns[i]).isDisposed())
					{
						aLsns[i] = null ;
						dirty = true ;
					}
				}
			}
			if(dirty)
				aLsns = XC.cleanNull(aLsns) ;
		}
		return aLsns ;
	}
	
	public boolean hasListeners()
	{
		return ((mLsns != null && mLsns.length > 1) 
				|| (mLastLsns != null && mLastLsns.length > 1)) ;
	}
	
	/**
	 * 异步回调注册的监听器
	 * @param aEvent
	 */
	public void asyncNotifyLsns(XEvent aEvent)
	{
		if(!hasListeners())
			return ;
		if(sThread == null)
		{
			sThread = new MassDispatchThread("XListenerAssist的异步通知回调线程") ;
			sThread.start() ;
		}
		if(mHandle == -1)
		{
			mHandle = sThread.getHandle() ;
			sThread.addCallback(mHandle, new AsyncNotifier()) ;
		}
		sThread.push(mHandle, aEvent) ;
	}
	
	@Override
	public void notifyLsns(Exception aE) throws BreakException
	{
		notifyLsns(aE, mLsns) ;
		notifyLsns(aE , mLastLsns) ;
	}
	
	protected void notifyLsns(Exception aE , IXListener[] aLsns) throws BreakException
	{
		if(aLsns != null)
		{
			for(IXListener lsn : aLsns)
				if(lsn instanceof IXEListener)
					((IXEListener) lsn).dealException(aE) ;
		}
	}
	
	public void removeLsn(IXListener aLsn)
	{
		if(mLsns != null)
			mLsns = XC.remove(mLsns , aLsn) ;
		if(mLastLsns != null)
			mLastLsns = XC.remove(mLastLsns , aLsn) ;
	}
	
	public IXListener[] getLsns()
	{
		if(mLsns == null || mLsns.length == 0)
			return mLastLsns ;
		else if(mLastLsns == null || mLastLsns.length == 0)
			return mLsns ;
		else
			return XC.merge(mLsns, mLastLsns) ;
	}
	
	public void removeAllLsns()
	{
		mLsns = null ;
		mLastLsns = null ;
	}
	
	class AsyncNotifier implements IXListener
	{
		@Override
		public void handle(XEvent aEvent)
		{
			if(aEvent.getTag() == mHandle)
				notifyLsns((XEvent)aEvent.getSource()) ;
		}
		
	}
}
