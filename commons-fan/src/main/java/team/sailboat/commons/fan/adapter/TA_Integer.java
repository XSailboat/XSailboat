package team.sailboat.commons.fan.adapter;

import team.sailboat.commons.fan.lang.XClassUtil;

public class TA_Integer implements ITypeAdapter<Integer>
{
	Integer mDefault ;
	
	public TA_Integer()
	{
	}
	
	public TA_Integer(int aDefault)
	{
		mDefault = aDefault ;
	}

	@Override
	public Integer apply(Object aT)
	{
		if(aT == null)
			return mDefault ;
		return XClassUtil.assetInteger(aT) ;
	}

	@Override
	public Class<Integer> getType()
	{
		return Integer.class ;
	}
}
