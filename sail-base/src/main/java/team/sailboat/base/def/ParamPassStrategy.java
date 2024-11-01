package team.sailboat.base.def;

public enum ParamPassStrategy
{
	PassThrough("入参透传" , 1) ,
	MapAndFilter("入参映射（过滤未知参数）" , 2) ,
	MapAndPass("入参映射（透传未知参数）" , 3) ,
	;
	String mDisplayName ;
	int mValue ;
	
	private ParamPassStrategy(String aDisplayName , int aValue)
	{
		mDisplayName = aDisplayName ;
		mValue = aValue ;
	}
	
	public String getDisplayName()
	{
		return mDisplayName;
	}
	
	public int getValue()
	{
		return mValue;
	}
}
