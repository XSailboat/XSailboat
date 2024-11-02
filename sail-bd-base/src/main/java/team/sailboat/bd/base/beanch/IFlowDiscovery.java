package team.sailboat.bd.base.beanch;

import java.util.Collection;
import java.util.List;

import team.sailboat.bd.base.ZBDException;
import team.sailboat.bd.base.model.ContextOutputParam;
import team.sailboat.bd.base.model.IFlowNode;
import team.sailboat.bd.base.model.IFlowValve;

public interface IFlowDiscovery
{
	/**
	 * 取得指定节点的前置节点
	 * @param aNode
	 * @return
	 */
	List<? extends IFlowNode> getPrecursors(IFlowNode aNode) throws ZBDException ;
	
	/**
	 * 取得指定节点的前置节点的id
	 * @param aNode
	 * @return
	 */
	Collection<String> getPrecursorIds(IFlowNode aNode) throws ZBDException ;
	
	/**
	 * 取得指定节点的后置节点
	 * @param aNode
	 * @return
	 */
	List<? extends IFlowNode> getFollowers(IFlowNode aNode) throws ZBDException ;
	
	/**
	 * 取得指定节点的后置节点的id
	 * @return
	 */
	Collection<String> getFollowerIds(IFlowNode aNode) throws ZBDException ;
	
	/**
	 * 可以跨工作空间
	 * @param aNodeId
	 * @return
	 */
	IFlowNode getFlowNode(String aNodeId) throws ZBDException ;
	
	/**
	 * 可以跨工作空间
	 * @param aValveId
	 * @return
	 */
	IFlowValve getFlowValve(String aValveId) throws ZBDException ;
	
	/**
	 * 取得指定节点的所有输出阀
	 * @param aNodeId
	 * @return
	 */
	List<IFlowValve> getFlowValvesOfNode(String aNodeId) throws ZBDException ;
	
	/**
	 * 通过工作空间id取得工作空间名称
	 * @param aWsId
	 * @return
	 */
	String getWorkspaceName(String aWsId) ;
	
	/**
	 * 通过工作空间名称获取工作空间id
	 * @param aWsName
	 * @return
	 */
	String getWorkspaceId(String aWsName) ;
	
	/**
	 * 通过阀id和参数名，找到上下文输出参数的定义
	 * @param aValveId
	 * @param aName
	 * @return
	 */
	ContextOutputParam getContextOutputParam(String aValveId , String aName) throws ZBDException ;
	
	/**
	 * 取得以指定阀所属的节点
	 * @param aValveId
	 * @return
	 */
	IFlowNode getFlowNodeOfValve(String aValveId) throws ZBDException ;
}
