package team.sailboat.commons.fan.dataframe;

import team.sailboat.commons.fan.json.JSONArray;

public abstract class AggExp<T extends AggregatorBuffer<?>> extends NameExpBase
{
	
	
	
	ScalarExp mSubExp ;
	
	T mBuf ;
	
	public AggExp(ScalarExp aExp , String aName , String aDataType)
	{
		super(aName , aDataType) ;
		mSubExp = aExp ;
	}
	

	public abstract T newBuffer() ;
	
	public void setCurrentBuffer(T aBuf)
	{
		mBuf = aBuf ;
	}
	
	@Override
	public T eval(JSONArray aRow)
	{
		return element(mBuf, mSubExp.eval(aRow)) ;
	}
	
	protected abstract T element(T aBuf , Object aS) ;

}
