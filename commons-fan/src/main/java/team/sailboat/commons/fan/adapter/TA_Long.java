package team.sailboat.commons.fan.adapter;

import team.sailboat.commons.fan.lang.XClassUtil;

public class TA_Long implements ITypeAdapter<Long>
{
	
	Long mDefault ;
	
	public TA_Long()
	{
	}
	
	public TA_Long(long aDefault)
	{
		mDefault = aDefault ;
	}

	@Override
	public Long apply(Object aT)
	{
		if(aT == null)
			return mDefault ;
		return XClassUtil.assetLong(aT) ;
	}
	
	@Override
	public Class<Long> getType()
	{
		return Long.class ;
	}

}
