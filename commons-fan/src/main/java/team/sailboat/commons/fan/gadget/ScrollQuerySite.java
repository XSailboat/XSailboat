package team.sailboat.commons.fan.gadget;

import team.sailboat.commons.fan.collection.AutoCleanHashMap;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;

public class ScrollQuerySite
{
	static ScrollQuerySite sInstance ;
	
	public static ScrollQuerySite getInstance()
	{
		if(sInstance == null)
			sInstance = new ScrollQuerySite() ;
		
		return sInstance ;
	}
	
	final AutoCleanHashMap<String , IScrollQuery<?>> mResMap = AutoCleanHashMap.withExpired_Idle(1, true) ;
	
	private ScrollQuerySite()
	{
	}
	
	public JSONObject scrollNext(String aHandle , int aMaxSize)
	{
		IScrollQuery<?> sq =  mResMap.remove(aHandle) ;
		Assert.notNull(sq, "查询句柄[%s]无效，可能已经过期，过期时间2分钟!" , aHandle) ;
		return sq.scrollNext(aMaxSize) ;
	}
	
	public void cacheScrollQuery(IScrollQuery<?> aScrollQuery)
	{
		if(aScrollQuery.getHandle() != null)
			mResMap.put(aScrollQuery.getHandle() , aScrollQuery) ;
	}
	
	
}
