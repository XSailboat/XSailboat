package team.sailboat.commons.fan.struct;

public class LongObject<T> implements Cloneable
{
	long mP ;
	
	T mObject ;
	
	public LongObject()
	{
	}
	
	public LongObject(long aPValue , T aObject)
	{
		mP = aPValue ;
		mObject = aObject ;
	}

	public long getP()
	{
		return mP;
	}

	public void setP(long aP)
	{
		mP = aP;
	}

	public T getObject()
	{
		return mObject;
	}

	public void setObject(T aObject)
	{
		mObject = aObject;
	}
	
	@Override
	public LongObject<T> clone()
	{
		return new LongObject<>(mP, mObject) ;
	}
}
