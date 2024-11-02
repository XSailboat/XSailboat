package team.sailboat.bd.base.model;

import java.io.IOException;

import team.sailboat.bd.base.ZBDException;

public interface IGraphEditor
{
	
	/**
	 * 删除节点
	 * @param aNodeId
	 * @param aDeleteUserId
	 * @throws IOException
	 * @throws ZBDException
	 */
	void deleteNode(String aNodeId , String aDeleteUserId) throws ZBDException ;
	
	/**
	 * 使指定节点孤立
	 * @param aNode
	 * @throws ZBDException
	 */
	void isolatedNode(IFlowNode aNode) throws ZBDException ;
	
	/**
	 * 断开两个点之间的关联关系
	 * @param aPrecursor		不能为null
	 * @param aFollower			不能为null
	 */
	void disconnectNodes(IFlowNode aPrecursor , IFlowNode aFollower) throws ZBDException ;
	
	void disconnectNodes(String aPrecursorId , String aFollowerId) throws ZBDException ;
	
	boolean disconnectNodes(IFlowValve aValve , String... aFollowerIds) throws ZBDException ;
}
