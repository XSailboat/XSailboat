package team.sailboat.commons.fan.res;

import team.sailboat.commons.fan.lang.Assert;

public class ResourceBean<T>
{
	long mGetTime = 0 ;
	long mCreateTime = 0 ;
	long mReleaseTime = 0 ;
	boolean mDiscard ;
	
	T mResource ;
	
	public ResourceBean(T aResource)
	{
		Assert.notNull(aResource , "用以构造ResourceBean的资源为null") ;
		mResource = aResource ;
		mCreateTime = System.currentTimeMillis() ;
	}
}
