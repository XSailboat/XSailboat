package team.sailboat.commons.fan.dataframe;

public class AggBuf_Double implements AggregatorBuffer<Double>
{
	
	double mValue = 0 ;
	
	public AggBuf_Double()
	{
	}
	
	public void add(double aVal)
	{
		mValue += aVal ;
	}

	@Override
	public Double terminate()
	{
		return mValue ;
	}

}
