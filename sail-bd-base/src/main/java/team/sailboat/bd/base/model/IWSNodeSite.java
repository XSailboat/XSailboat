package team.sailboat.bd.base.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import team.sailboat.bd.base.ZBDException;
import team.sailboat.bd.base.beanch.IWSRepo;
import team.sailboat.bd.base.proxy.INodeResourceProxy;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;

public interface IWSNodeSite
{
	IWSRepo getWsRepo() ;
	
	IFlowValve getRootValve() ;
	
	IFlowValve getFlowValve(String aValveId) throws ZBDException ;
	
	IFlowNode getRootNode() ;
	
	IFlowNode getFlowNode(String aNodeId) throws ZBDException ;
	
	/**
	 * 已经按自然序升序排列
	 * @param aNode
	 * @return
	 * @throws ZBDException 
	 */
	default String[] getPrecursorIds(IFlowNode aNode) throws ZBDException
	{
		ParamBinding[] preValveRefs = aNode.getInputArray() ;
		if(XC.isEmpty(preValveRefs))
			return JCommon.sEmptyStringArray ;
		
		Set<String> nodeIds = XC.treeSet() ;
		for(ParamBinding preValveRef : preValveRefs)
		{
			IFlowValve valve = getFlowValve(preValveRef.getRef()) ;
			if(valve != null)
			{
				nodeIds.add(valve.getSourceNodeId()) ;
			}
		}
		return nodeIds.toArray(JCommon.sEmptyStringArray) ;
	}
	
	/**
	 * 已经按自然序升序排列
	 * @param aNode
	 * @return
	 * @throws ZBDException 
	 */
	default String[] getFollowerIds(IFlowNode aNode) throws ZBDException
	{
		ParamBinding[] sufValveRefs = aNode.getOutputArray() ;
		if(XC.isEmpty(sufValveRefs))
			return JCommon.sEmptyStringArray ;
		
		Set<String> nodeIds = XC.treeSet() ;
		for(ParamBinding sufValveRef : sufValveRefs)
		{
			IFlowValve valve = getFlowValve(sufValveRef.getRef()) ;
			if(valve != null)
			{
				nodeIds.addAll(valve.getTargetNodeIds()) ;
			}
		}
		return nodeIds.toArray(JCommon.sEmptyStringArray) ;
	}
	
	default IFlowNode getFlowNodeOfValve(String aValveId) throws ZBDException
	{
		// 1.取得阀
		IFlowValve valve = getFlowValve(aValveId) ;
		Assert.notNull(valve , "不存在指定id为%s的阀", aValveId) ;
		String nodeId = valve.getSourceNodeId() ;
		return getFlowNode(nodeId) ;
	}
	
	default List<IFlowValve> getFlowValvesOfNode(String aNodeId) throws ZBDException
	{
		IFlowNode node = getFlowNode(aNodeId) ;
		Assert.notNull(node, "不存在id为%s的节点" , aNodeId) ;
		Collection<ParamBinding> bindings = node.getOutputs() ;
		if(XC.isNotEmpty(bindings))
		{
			List<IFlowValve> valveList = XC.arrayList() ;
			for(ParamBinding binding : bindings)
			{
				valveList.add(getFlowValve(binding.getRef())) ;
			}
			return valveList ;
		}
		return Collections.emptyList() ;
	}
	
	/**
	 * 判定指定的节点id或者阀id是否是属于当前工作空间
	 * @param aNodeOrValveId
	 * @return
	 */
	boolean isCurrentWorkspace(String aNodeOrValveId) ;
	
	INodeResourceProxy getResourceProxy() ;
}
