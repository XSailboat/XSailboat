package team.sailboat.bd.base.model;

public enum SubfieldType
{
	OLAFlow("离线分析流程") ,
	RTAPipe("实时计算管道")
	;
	
	String mDisplayName ;
	
	private SubfieldType(String aDisplayName)
	{
		mDisplayName = aDisplayName ;
	}
	
	public String getDisplayName()
	{
		return mDisplayName;
	}
}
