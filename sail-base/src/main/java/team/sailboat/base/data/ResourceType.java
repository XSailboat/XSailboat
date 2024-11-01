package team.sailboat.base.data;

public enum ResourceType
{
	Dataset("数据集") ,
	Chart("图表") ,
	Dashboard("仪表板") ,
	BigScreen("大屏")
	;
	
	String mDisplayName ;
	
	private ResourceType(String aDisplayName)
	{
		mDisplayName = aDisplayName ;
	}
	
	public String getDisplayName()
	{
		return mDisplayName;
	}
}
