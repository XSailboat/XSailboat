package team.sailboat.commons.fan.event;

import team.sailboat.commons.fan.collection.XC;


public class TEventSite
{
	static TEventSite sInstance ;
	
	public static TEventSite getDefault()
	{
		if(sInstance == null)
			sInstance = new TEventSite() ;
		return sInstance ;
	}
	
	ITListener[] mLsns ;
	
	public TEventSite()
	{
		
	}
	
	/**
	 * 发布事件
	 * @param aEvent
	 */
	public void publish(TEvent aEvent)
	{
		if(mLsns != null)
		{
			for(ITListener lsn : mLsns)
			{
				try
				{
					lsn.handle(aEvent) ;
				}
				catch(Exception e)
				{
					e.printStackTrace(); 
				}
			}
		}
	}
	
	public void addListener(ITListener aLsn)
	{
		if(aLsn == null)
			return ;
		if(mLsns == null)
			mLsns = new ITListener[]{aLsn} ;
		else if(!XC.contains(mLsns , aLsn))
			mLsns = XC.merge(mLsns , aLsn) ;
	}
	
	public void removeListener(ITListener aLsn)
	{
		if(mLsns != null)
			mLsns = XC.remove(mLsns , aLsn) ;
	}
	
}
