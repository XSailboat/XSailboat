package team.sailboat.commons.fan.dataframe;

import team.sailboat.commons.fan.lang.XClassUtil;

public class SumAggExp extends AggExp<AggBuf_Double>
{

	public SumAggExp(ScalarExp aExp, String aName)
	{
		super(aExp, aName , XClassUtil.sCSN_Integer);
	}

	@Override
	public AggBuf_Double newBuffer()
	{
		return new AggBuf_Double() ; 
	}

	@Override
	protected AggBuf_Double element(AggBuf_Double aBuf, Object aS)
	{
		aBuf.add(XClassUtil.toDouble(aS, 0));
		return aBuf ;
	}
}
