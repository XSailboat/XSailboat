package team.sailboat.bd.base.bean;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 调度配置
 *
 * @author yyl
 * @since 2021年6月17日
 */
@Schema(description = "节点的调度参数配置")
public class ADispatchConfiguration
{
	
	@Schema(description = "节点实例的生成方式" , allowableValues = {"Tp1","RightNow"})
	String instGenWay ;
	
	@Schema(description = "是否空跑")
	boolean runWithNoLoad = false ;
	
	@Schema(description = "生效时间下限")
	String validTimeSpaceLower ;

	@Schema(description = "生效时间上限")
	String validTimeSpaceUpper ;
	
	@Schema(description = "调度计划")
	String schedule ;
	
	@Schema(description = "超时时间，0表示系统缺省，大于0的时候，单位是小时")
	int timeout ;
}
