package team.sailboat.commons.fan.struct;

public class IntObject<T>
{
	int mP ;
	
	T mObject ;
	
	public IntObject()
	{
	}
	
	public IntObject(int aPValue , T aObject)
	{
		mP = aPValue ;
		mObject = aObject ;
	}

	public int getP()
	{
		return mP;
	}

	public void setP(int aP)
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
}
