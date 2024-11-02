package team.sailboat.bd.base.bean;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import team.sailboat.bd.base.model.IFlowNode;
import team.sailboat.bd.base.model.NodeType;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.time.XTime;

@Data
@Schema(description = "")
public class NodeProfile
{
	@Schema(description = "节点id")
	String id ;
	
	@Schema(description = "节点类型")
	NodeType type ;
	
	@Schema(description = "节点名称")
	String name ;
	
	@Schema(description = "节点所属流程id")
	String flowId ;
	
	@Schema(description = "节点的属性")
	NodeProperties properties ;
	
	@Schema(description = "节点的输入输出参数")
	NodeIOParams ioParams ;
	
	public static boolean injectTo(NodeProfile aSource , IFlowNode aTarget
			, boolean aPartially)
	{
		boolean changed = false ;
		if(!aPartially || aSource.getName() != null)
			changed |= aTarget.setName(aSource.getName()) ;
		
		if(!aPartially || aSource.getType() != null)
		{
			changed |= aTarget.setType(aSource.getType()) ;
		}
		if(!aPartially || aSource.getType() != null)
			changed |= aTarget.setFlowId(aSource.getFlowId()) ;
		
		NodeProperties props = aSource.getProperties() ;
		if(props != null)
		{
			if(!aPartially || props.getDescription() != null)
				changed |= aTarget.setDescription(props.getDescription()) ;
			if(!aPartially || props.getInstGenWay() != null)
				changed |= aTarget.setInstGenWay(props.getInstGenWay()) ;
			if(!aPartially && props.getRunWithNoLoad()  != null)
				changed |= aTarget.setRunWithNoLoad(props.getRunWithNoLoad()) ;
			
			if(!aPartially && XC.isNotEmpty(props.getParams()))
				changed |= aTarget.setParams(props.getParams()) ;
			
			if(!aPartially || props.getSchedule() != null)
				changed |= aTarget.setSchedule(props.getSchedule()) ;
			
			if(!aPartially || props.getValidTimeSpaceLower() != null)
				changed |= aTarget.setValidTimeSpaceLower(XTime.parse$yyyyMMdd_0(props.getValidTimeSpaceLower())) ;
			if(!aPartially || props.getValidTimeSpaceUpper() != null)
				changed |= aTarget.setValidTimeSpaceUpper(XTime.parse$yyyyMMdd_0(props.getValidTimeSpaceUpper())) ;
		}
		
		// 单向挂接，忽略阀的输出
		NodeIOParams ioParams = aSource.getIoParams() ;
		if(ioParams != null)
		{
			List<NodeInput> inputs = ioParams.getInputs() ;
			if(!aPartially || inputs != null)
			{
				changed |= aTarget.setInputs(XC.extractAsArrayList(inputs, NodeInput::as)) ;
			}
			// 上下文输入参数
			List<NodeInputCtxParam> inputParams = ioParams.getInputContextParams() ;
			if(!aPartially || inputParams != null)
			{
				changed |= aTarget.getParamIOContext().setInputParams(NodeInputCtxParam.asList(inputParams)) ;
			}
			List<NodeOutputCtxParam> outputParams = ioParams.getOutputContextParams() ;
			if(!aPartially || outputParams != null)
			{
				changed |= aTarget.getParamIOContext().setOutputParam(NodeOutputCtxParam.asList(outputParams , aSource.getId()));
			}
		}
		return changed ;
	}
	
	public static void checkFielsForCreate(NodeProfile aNode)
	{
		throw new IllegalStateException("待实现") ;
	}
}
