package team.sailboat.bd.base.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import team.sailboat.base.util.IWSDBeanIdHelper;
import team.sailboat.bd.base.ZBDException;
import team.sailboat.bd.base.beanch.IFlowDiscovery;
import team.sailboat.bd.base.model.IFlowNode;
import team.sailboat.bd.base.model.IFlowValve;
import team.sailboat.bd.base.model.ParamBinding;
import team.sailboat.bd.base.model.ParamBindingSource;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.text.XString;

@Schema(description = "节点输出")
@Data
public class NodeOutput
{
	static Logger sLogger = LoggerFactory.getLogger(NodeOutput.class) ;
	
	@Schema(description = "阀id")
	String valveId ;
	
	@Schema(description = "输出名称")
	String outputName ;
	 
	@Schema(description = "下游节点")
	List<NodeCorner> followers ;
	
	@Schema(description = "参数来源")
	String paramBindingSource ;
	
	public static ArrayList<NodeOutput> ofList(Collection<ParamBinding> aBindings 
			, IFlowDiscovery aDiscovery) throws ZBDException
	{
		ArrayList<NodeOutput> outputs = XC.arrayList() ;
		for(ParamBinding binding : aBindings)
		{
			outputs.add(of(binding, aDiscovery)) ;
		}
		return outputs ;
	}
	
	public static NodeOutput of(ParamBinding aBinding
			, IFlowDiscovery aDiscovery )throws ZBDException
	{
		String valveId = aBinding.getRef() ;
		String subId = valveId.substring(valveId.indexOf('#')+1) ;
		String wsId = IWSDBeanIdHelper.getWsIdFromDBeanId(valveId) ;
		String wsName = aDiscovery.getWorkspaceName(wsId) ;
		NodeOutput output = new NodeOutput() ;
		output.setValveId(valveId) ;
		output.setOutputName(wsName+"."+subId) ;
		output.setParamBindingSource(aBinding.getSource().name()) ;
		IFlowValve valve = aDiscovery.getFlowValve(valveId) ;
		Assert.notNull(valve , "找不到阀：%s" , valveId) ;
		Set<String> targetIds = valve.getTargetNodeIds() ;
		if(XC.isNotEmpty(targetIds))
		{
			List<NodeCorner> followers = XC.arrayList() ;
			List<String> invalidNodeIds = null ;
			for(String followerId : targetIds)
			{
				IFlowNode node = aDiscovery.getFlowNode(followerId) ;
				if(node == null)
				{
					sLogger.warn("无法找到后继节点[{}]，将它从阀中移除！" , followerId) ;
					if(invalidNodeIds == null)
						invalidNodeIds = XC.arrayList() ;
					invalidNodeIds.add(followerId) ;
					continue ;
				}
				followers.add(NodeCorner.of(node)) ;
			}
			if(invalidNodeIds != null)
			{
				for(String nodeId : invalidNodeIds)
					valve.removeTargetNodeId(nodeId) ;
			}
			output.setFollowers(followers); 
		}
		return output ;
	}
	
	public void checkAndSetValveId(IFlowDiscovery aDiscovery)
	{
		if(XString.isEmpty(valveId) && XString.isNotEmpty(outputName))
		{
			int i = outputName.indexOf('.') ;
			Assert.isTrue(i > 0 , "输出名[%s]不合法" , outputName) ;
			String wsName = outputName.substring(0, i) ;
			String wsId = aDiscovery.getWorkspaceId(wsName) ;
			Assert.notEmpty(wsId, "不存在名为 %s 的工作空间", wsName) ;
			valveId = wsId+"#"+outputName.substring(i+1) ;
		}
	}
	
	public static void checkAndSetValveId(IFlowDiscovery aDiscovery , Collection<NodeOutput> aNodeOutput)
	{
		if(XC.isNotEmpty(aNodeOutput))
		{
			for(NodeOutput nodeOutput : aNodeOutput)
			{
				nodeOutput.checkAndSetValveId(aDiscovery) ;
			}
		}
	}
	
	public static List<ParamBinding> asList(Collection<NodeOutput> aOutputs)
	{
		return XC.extractAsArrayList(aOutputs , NodeOutput::as) ;
	}
	
	public static ParamBinding as(NodeOutput aOutput)
	{
		String valveId = aOutput.getValveId() ;
		Assert.notEmpty(valveId , "输出[%s]没有设置valveId" , aOutput.getOutputName()) ;
		return new ParamBinding(valveId , ParamBindingSource.valueOf(aOutput.getParamBindingSource())) ;
	}
}
