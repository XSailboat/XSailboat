package team.sailboat.bd.base.def;

/**
 * 流程节点的变量的类型
 *
 * @author yyl
 * @since 2023年5月20日
 */
public enum FlowNodeParamType
{
	SystemParam("系统参数") ,
	ContextParam("上下文参数") ,
	CustomParam("自定义参数")
	;
	
	String mDisplayName ;
	
	private FlowNodeParamType(String aDisplayName)
	{
		mDisplayName = aDisplayName ;
	}
	
	public String getDisplayName()
	{
		return mDisplayName;
	}
}
