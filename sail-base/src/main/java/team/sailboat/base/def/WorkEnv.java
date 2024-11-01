package team.sailboat.base.def;

public enum WorkEnv
{
	dev("开发环境"),
	prod("生产环境") ;
	
	String mDisplayName ;
	
	private WorkEnv(String aDisplayName)
	{
		mDisplayName = aDisplayName ;
	}
	
	public String getDisplayName()
	{
		return mDisplayName;
	}
}
