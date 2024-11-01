package team.sailboat.base.sql.model;

public enum BDataType
{
	Unknow("未知") ,
	Numeric("数值") ,
	Boolean("布尔") ,
	String("字符串") ,
	Datetime("时间") ;
	
	String mDisplayName ;
	
	private BDataType(String aDisplayName)
	{
		mDisplayName = aDisplayName ;
	}
}
