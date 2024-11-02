package team.sailboat.bd.base.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import team.sailboat.base.util.IWSDBeanIdHelper;
import team.sailboat.bd.base.ZBDException;
import team.sailboat.bd.base.beanch.IFlowDiscovery;
import team.sailboat.bd.base.model.IFlowNode;
import team.sailboat.bd.base.model.ParamBinding;
import team.sailboat.bd.base.model.ParamBindingSource;
import team.sailboat.commons.fan.collection.XC;

@Schema(description = "本节点输入/父节点输出")
@Data
public class NodeInput
{
	static final Logger sLogger = LoggerFactory.getLogger(NodeInput.class) ; 
	
	@Schema(description = "阀的id")
	String valveId ;
	
	@Schema(description = "父节点输出名")
	String parentOutputName ;
	
	@Schema(description = "父节点")
	NodeCorner parent ;
	
	@Schema(description = "参数来源")
	String paramBindingSource ;
	
	
	public static ArrayList<NodeInput> ofList(Collection<ParamBinding> aBindings 
			, IFlowDiscovery aDiscovery) throws ZBDException
	{
		ArrayList<NodeInput> inputs = XC.arrayList() ;
		for(ParamBinding binding : aBindings)
		{
			NodeInput nodeInput = of(binding, aDiscovery) ;
			if(nodeInput == null)
			{
				sLogger.warn("找不到输入阀[{}]所属节点！" , binding.getRef()) ;
				continue ;
			}
			inputs.add(nodeInput) ;
		}
		return inputs ;
	}
	
	public static NodeInput of(ParamBinding aBinding , IFlowDiscovery aDiscovery)
			throws ZBDException
	{
		String valveId = aBinding.getRef() ;
		IFlowNode node = aDiscovery.getFlowNodeOfValve(valveId);
		if(node == null)
			return null ;
		String subId = valveId.substring(valveId.indexOf('#')+1) ; 
		String wsId = IWSDBeanIdHelper.getWsIdFromDBeanId(valveId) ;
		String wsName = aDiscovery.getWorkspaceName(wsId) ;
		NodeInput input = new NodeInput() ;
		input.setValveId(valveId) ;
		input.setParentOutputName(wsName+"."+subId) ;
		input.setParamBindingSource(aBinding.getSource().name()) ;
		input.setParent(NodeCorner.of(node)) ;
		return input ;
	}
	
	public static List<ParamBinding> asList(Collection<NodeInput> aInputs)
	{
		return XC.extractAsArrayList(aInputs, NodeInput::as) ;
	}
	
	public static ParamBinding as(NodeInput aInput)
	{
		return new ParamBinding(aInput.getValveId() , ParamBindingSource.valueOf(aInput.getParamBindingSource())) ;
	}
}
