package team.sailboat.commons.fan.adapter;

import team.sailboat.commons.fan.lang.XClassUtil;

public class TA_Boolean implements ITypeAdapter<Boolean>
{
	Boolean mDefault ;
	
	public TA_Boolean()
	{
	}
	
	public TA_Boolean(boolean aDefault)
	{
		mDefault = aDefault ;
	}

	@Override
	public Boolean apply(Object aT)
	{
		if(aT == null)
			return null ;
		return XClassUtil.assetBoolean(aT) ;
	}

	@Override
	public Class<Boolean> getType()
	{
		return Boolean.class ;
	}

}