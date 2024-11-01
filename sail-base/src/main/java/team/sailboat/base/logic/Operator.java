package team.sailboat.base.logic;

public enum Operator
{
	IN("属于") ,
	NOT_IN("不属于") ,
	CONTAINS("包含") ,
	NOT_CONTAINS("不包含") ,
	IS_NULL("为NULL") ,
	NOT_NULL("非NULL") ,
	EQUALS("相等") ,
	NOT_EQUALS("不相等") ,
	EMPTY("为空") ,
	NOT_EMPTY("非空") ,
	IN_RANGE("范围") ,
	STARTS_WITH("开头是") ,
	NOT_STARTS_WITH("开头不是") ,
	ENDS_WITH("结尾是") ,
	NOT_ENDS_WITH("结尾不是") ,
	;
	
	String mDisplayName ;
	
	private Operator(String aDisplayName)
	{
		mDisplayName = aDisplayName ;
	}
	
	public String getDisplayName()
	{
		return mDisplayName;
	}
}
