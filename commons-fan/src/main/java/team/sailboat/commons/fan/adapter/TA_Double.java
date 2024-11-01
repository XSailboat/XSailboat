package team.sailboat.commons.fan.adapter;

import team.sailboat.commons.fan.lang.XClassUtil;

public class TA_Double implements ITypeAdapter<Double>
{
	Double mDefault ; 
	
	public TA_Double()
	{
	}
	
	public TA_Double(double aDefault)
	{
		mDefault = aDefault ;
	}

	@Override
	public Double apply(Object aT)
	{
		if(aT == null)
			return mDefault ;
		return XClassUtil.assetDouble(aT) ;
	}
	
	@Override
	public Class<Double> getType()
	{
		return Double.class ;
	}

}
