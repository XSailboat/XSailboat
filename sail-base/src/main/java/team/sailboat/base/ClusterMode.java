package team.sailboat.base;

public enum ClusterMode
{
	/**
	 * 联邦模式
	 */
	Federation("联邦模式") ,
	/**
	 * 主辅模式
	 */
	MasterSlave("主辅模式")
	;
	
	final String mDisplayName ;
	
	private ClusterMode(String aDisplayName)
	{
		mDisplayName = aDisplayName ;
	}
	
	public String getDisplayName()
	{
		return mDisplayName;
	}
}
