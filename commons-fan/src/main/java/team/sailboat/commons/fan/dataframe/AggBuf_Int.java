package team.sailboat.commons.fan.dataframe;

public class AggBuf_Int implements AggregatorBuffer<Integer>
{
	
	int mValue = 0 ;
	
	public AggBuf_Int()
	{
	}
	
	public void increase()
	{
		mValue++ ;
	}

	@Override
	public Integer terminate()
	{
		return mValue ;
	}

}
