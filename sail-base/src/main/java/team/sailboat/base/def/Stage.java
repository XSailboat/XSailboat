package team.sailboat.base.def;

public enum Stage
{
	offline("下线") ,
	test("测试") ,
	release("在线")
	;
	
	String mDisplayName ;
	
	private Stage(String aDisplayName)
	{
		mDisplayName = aDisplayName ;
	}
	
	public String getDisplayName()
	{
		return mDisplayName;
	}
}
