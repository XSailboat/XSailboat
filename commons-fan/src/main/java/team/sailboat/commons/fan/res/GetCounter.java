package team.sailboat.commons.fan.res;

public class GetCounter<T>
{
	T mResource ;
	int mCount = 1 ;
	
	public GetCounter(T aResource)
	{
		mResource = aResource ;
	}
	
	public GetCounter<T> add()
	{
		mCount++ ;
		return this ;
	}
	
	public GetCounter<T> reduce()
	{
		mCount-- ;
		return this ;
	}
}
