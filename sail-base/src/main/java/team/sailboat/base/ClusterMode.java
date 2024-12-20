package team.sailboat.base;

/**
 * 集群模式
 *  
 * @author yyl
 * @since 2024年12月6日
 */
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
	
	/**
	 * 获取集群模式的显示名
	 * 
	 * @return	显示名
	 */
	public String getDisplayName()
	{
		return mDisplayName;
	}
}
