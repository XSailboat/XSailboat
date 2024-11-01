package team.sailboat.commons.fan.dataframe;

import team.sailboat.commons.fan.lang.XClassUtil;

public class CountAggExp extends AggExp<AggBuf_Int>
{

	public CountAggExp(ScalarExp aExp)
	{
		super(aExp, aExp.getName() , XClassUtil.sCSN_Integer);
	}

	@Override
	public AggBuf_Int newBuffer()
	{
		return new AggBuf_Int() ; 
	}

	@Override
	protected AggBuf_Int element(AggBuf_Int aBuf, Object aS)
	{
		aBuf.increase();
		return aBuf ;
	}
}
