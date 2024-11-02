package team.sailboat.bd.base.bean;

import java.util.Collection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import team.sailboat.bd.base.model.DispatchConfiguration;
import team.sailboat.bd.base.model.FlowNodeParam;
import team.sailboat.bd.base.model.IFlowNode;
import team.sailboat.bd.base.model.dag.InstGenWay;
import team.sailboat.commons.fan.time.XTime;
import team.sailboat.commons.ms.infc.UserSupport;

@Schema(description = "节点的性质")
@Data
@EqualsAndHashCode(callSuper = true)
public class NodeProperties extends UserSupport
{
	@Schema(description = "描述")
	String description ;
	
	@Schema(description = "参数表")
	Collection<FlowNodeParam> params ;
	
	@Schema(description = "实例生成方式")
	InstGenWay instGenWay ;
	
	@Schema(description = "是否空跑")
	Boolean runWithNoLoad ;
	
	@Schema(description = "生效时间下限，格式yyyy-MM-dd")
	String validTimeSpaceLower ;

	@Schema(description = "生效时间上限，格式yyyy-MM-dd")
	String validTimeSpaceUpper ;
	
	@Schema(description = "调度计划")
	String schedule ;
	
	@Schema(description = "超时时间，0表示系统缺省，大于0的时候，单位是小时")
	int timeout ;
	
	@Schema(description = "运行时间限制，单位分钟，缺省10分钟")
	int runTimeLimit ;
	
	public boolean isRunWithNoLoad(boolean aDefaultValue)
	{
		return runWithNoLoad == null?aDefaultValue:runWithNoLoad.booleanValue() ;
	}
	
	public static NodeProperties of(IFlowNode aFlowNode)
	{
		if(aFlowNode == null)
			return null ;
		NodeProperties prop = new NodeProperties() ;
		prop.setDescription(aFlowNode.getDescription()) ;
		DispatchConfiguration conf =  aFlowNode.getDispatchConfiguration() ;
		prop.setInstGenWay(conf.getInstGenWay()) ;
		prop.setRunWithNoLoad(conf.isRunWithNoLoad()) ;
		prop.setSchedule(conf.getSchedule()) ;
		prop.setTimeout(conf.getTimeout()) ;
		prop.setValidTimeSpaceLower(XTime.format$yyyyMMdd(conf.getValidTimeSpaceLower())) ;
		prop.setValidTimeSpaceUpper(XTime.format$yyyyMMdd(conf.getValidTimeSpaceUpper())) ;
		prop.setParams(aFlowNode.getParams()) ;
		prop.setRunTimeLimit(conf.getRunTimeLimit()) ;
		prop.setCreateUserId(aFlowNode.getCreateUserId()) ;
		prop.setLastEditUserId(aFlowNode.getLastEditUserId()) ;
		return prop ;
	}
}
