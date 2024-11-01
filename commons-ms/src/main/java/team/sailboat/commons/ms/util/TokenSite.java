package team.sailboat.commons.ms.util;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import team.sailboat.commons.fan.collection.AutoCleanHashMap;

public class TokenSite
{
	String mName ;
	
	AutoCleanHashMap<String, Secret> mSecretMap ;
	
	final int mTokenValidTimeInMinutes ;
	
	public TokenSite(String aName, int aTokenValidTimeInMinutes)
	{
		mName = aName ;
		mTokenValidTimeInMinutes = aTokenValidTimeInMinutes ;
		mSecretMap = AutoCleanHashMap.withExpired_Created(mTokenValidTimeInMinutes) ;
	}
	
	public String getName()
	{
		return mName;
	}
	
	public String genTokenFor(String aIp , int aTimes)
	{
		String token = UUID.randomUUID().toString() +"#"+ Math.random()*10000 ;
		mSecretMap.put(aIp, new Secret(aIp , aTimes , token)) ;
		return token ;
	}
	
	public boolean useToken(String aIp , String aToken)
	{
		Secret secret = mSecretMap.get(aIp) ;
		if(secret == null)
			return false ;
		if(!secret.useOnce())
		{
			mSecretMap.remove(aIp) ;
			return false ;
		}
		return true ;
	}
	
	static class Secret
	{
		String mIp ;
		int mUseTimesLimit ;
		AtomicInteger mUseTimesRemain;
		String mToken ;
		
		public Secret(String aIp , int aUseTimeLimit , String aToken)
		{
			mIp = aIp ;
			mUseTimesLimit = aUseTimeLimit ;
			mUseTimesRemain = new AtomicInteger(mUseTimesLimit) ;
			mToken = aToken ;
		}
		
		public boolean useOnce()
		{
			return mUseTimesRemain.decrementAndGet() >= 0 ;
		}
		
	}
}
