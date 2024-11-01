package team.sailboat.base.def;


/**
 * 配置项来源
 *
 * @author yyl
 * @since 2023年5月22日
 */
public enum ConfigItemSource
{
	ManualAdd("手动添加") ,
	AutoParse("自动解析") ;
	
	String displayName ;
	
	private ConfigItemSource(String aDisplayName)
	{
		displayName = aDisplayName ;
	}
	
	public String getDisplayName()
	{
		return displayName;
	}
}
