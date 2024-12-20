package team.sailboat.ms.ac.utils;

import java.util.Map;

import team.sailboat.commons.fan.collection.CircularArrayList;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.time.XTime;
import team.sailboat.commons.fan.time.XTimeUnit;

public class LoginFailStore
{
	public static final int sTimeGapMi = 30 ;
	static final long sTimeGapMs =  XTimeUnit.MINUTE.toMillis(sTimeGapMi) ;
	
	final Map<String , CircularArrayList<Long>> mFailLoginMap = XC.autoCleanHashMap_idle(sTimeGapMi) ;
	final int mFailTimesLimit ;
	
	public LoginFailStore(int aFailTimesLimit)
	{
		mFailTimesLimit = aFailTimesLimit ;
	}
	
	public int getFailTimesLimit()
	{
		return mFailTimesLimit ;
	}
	
	public void clearLoginFail(String aIp)
	{
		mFailLoginMap.remove(aIp) ;
	}
	
	public int recordLoginFail(String aIp)
	{
		CircularArrayList<Long> fails = mFailLoginMap.get(aIp) ;
		if(fails == null)
		{
			fails = new CircularArrayList<Long>(mFailTimesLimit) ;
			fails.add(System.currentTimeMillis()) ;
			mFailLoginMap.put(aIp, fails) ;
			return mFailTimesLimit - 1 ;
		}
		fails.add(System.currentTimeMillis()) ;
		return getRemainRetryTimes(fails) ;
		
	}
	
	int getRemainRetryTimes(CircularArrayList<Long> aFails)
	{
		int i=0 ;
		for(Long time : aFails)
		{
			if(!XTime.pass(time, sTimeGapMs))
			{
				break ;
			}
			i++ ;
		}
		return mFailTimesLimit - (aFails.size() - i) ;
	}
	
	public boolean isLoginFailTooMore(String aIp)
	{
		CircularArrayList<Long> fails= mFailLoginMap.get(aIp) ;
		if(fails == null || !fails.isFull())
			return false ;
		long time = fails.getFirst() ;
		return !XTime.pass(time , sTimeGapMs) ;
	}
}
