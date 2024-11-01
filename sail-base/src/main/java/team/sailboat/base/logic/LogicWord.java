package team.sailboat.base.logic;

public enum LogicWord
{
	AND("且") ,
	OR("或") ;
	
	String mDisplayName ;
	
	private LogicWord(String aDisplayName)
	{
		mDisplayName = aDisplayName ;
	}
	
	public String getDisplayName()
	{
		return mDisplayName;
	}
	
}
