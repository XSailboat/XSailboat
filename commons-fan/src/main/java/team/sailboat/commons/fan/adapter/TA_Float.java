package team.sailboat.commons.fan.adapter;

import team.sailboat.commons.fan.lang.XClassUtil;

public class TA_Float implements ITypeAdapter<Float>
{

	@Override
	public Float apply(Object aT)
	{
		if(aT instanceof Float)
			return (Float) aT ;
		return XClassUtil.assetFloat(aT) ;
	}
	
	@Override
	public Class<Float> getType()
	{
		return Float.class ;
	}

}