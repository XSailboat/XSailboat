package team.sailboat.commons.fan.dtool;

public class SqlParam
{

	Object mValue ;
	int mDataType ;
	
	public SqlParam()
	{
	}
	
	public SqlParam(Object aValue , int aDataType)
	{
		mValue = aValue ;
		mDataType = aDataType ;
	}
	
	public Object getValue()
	{
		return mValue;
	}
	
	public int getDataType()
	{
		return mDataType;
	}

}
