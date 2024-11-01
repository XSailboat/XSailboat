package team.sailboat.commons.fan.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.text.XString;

public class DAGraph implements ToJSONObject
{
	boolean dirty = false ;
	
	final LinkedHashSet<String> rootNodes = XC.linkedHashSet() ;
	
	final Map<String , DAGNode> nodeMap = XC.hashMap() ;
	
	public synchronized void forEachNode(Consumer<DAGNode> aConsumer)
	{
		nodeMap.values().forEach(aConsumer) ;
	}
	
	public synchronized void setNodes(Collection<DAGNode> aNodes)
	{
		nodeMap.clear(); 
		rootNodes.clear();
		if(XC.isNotEmpty(aNodes))
		{
			for(DAGNode node : aNodes)
			{
				Assert.notEmpty(node.getId() , "图节点的id不能为null！") ;
				nodeMap.put(node.getId() , node) ;
				if(XC.isEmpty(node.getPrecursorIds()))
					rootNodes.add(node.getId()) ;
			}
			dirty = true ;
		}
	}
	
	public synchronized void addNodes(DAGNode... aNodes)
	{
		if(XC.isNotEmpty(aNodes))
		{
			for(DAGNode node : aNodes)
			{
				Assert.notEmpty(node.getId() , "图节点的id不能为null！") ;
				nodeMap.put(node.getId(), node) ;
			}
			dirty = true ;
		}
	}
	
	public synchronized void addNodes(Collection<DAGNode> aNodes)
	{
		if(XC.isNotEmpty(aNodes))
		{
			for(DAGNode node : aNodes)
			{
				Assert.notEmpty(node.getId() , "图节点的id不能为null！") ;
				nodeMap.put(node.getId(), node) ;
			}
			dirty = true ;
		}
	}
	
	public synchronized boolean deleteNode(String aId)
	{
		DAGNode node = nodeMap.get(aId) ;
		if(node != null)
		{
			deleteEdgesRelatedToNode(node) ;
			nodeMap.remove(aId) ;
			rootNodes.remove(aId) ;
			return true ;
		}
		return false ;
	}
	
	
	/**
	 * 重复删除边不会有影响
	 * @param aPrecursorId
	 * @param aFollowerId
	 */
	public synchronized boolean deleteEdge(String aPrecursorId , String aFollowerId)
	{
		DAGNode precursor = nodeMap.get(aPrecursorId) ;
		boolean changed = false ;
		if(precursor != null)
			changed = precursor.removeFollower(aFollowerId) ;
		
		DAGNode follower = nodeMap.get(aFollowerId) ;
		if(follower != null)
			changed |= follower.removePrecursor(aPrecursorId) ;
		return changed ;
	}
	
	void deleteEdgesRelatedToNode(DAGNode aNode)
	{
		deleteEdgesWithPrecursors(aNode);
		rootNodes.add(aNode.getId()) ;
		deleteEdgesWithFollowers(aNode) ;
	}
	
	void deleteEdgesWithPrecursors(DAGNode aNode)
	{
		Set<String> precursorIds = aNode.getPrecursorIds() ;
		String nodeId = aNode.getId() ;
		if(XC.isNotEmpty(precursorIds))
		{
			for(String id : precursorIds)
			{
				DAGNode node = nodeMap.get(id) ;
				if(node != null)
				{
					node.removeFollower(nodeId) ;
				}
			}
			precursorIds.clear();
		}
	}
	
	void deleteEdgesWithFollowers(DAGNode aNode)
	{
		Set<String> followerIds = aNode.getFollowerIds() ;
		String nodeId = aNode.getId() ;
		if(XC.isNotEmpty(followerIds))
		{
			for(String id : followerIds)
			{
				DAGNode node = nodeMap.get(id) ;
				if(node != null)
				{
					node.removePrecursor(nodeId) ;
					if(XC.isEmpty(node.getPrecursorIds()))
						rootNodes.add(id) ;
				}
			}
			followerIds.clear();
		}
	}
	
	/**
	 * 是否有前置节点
	 * @param aNodeId
	 * @return
	 */
	public boolean hasPrecursor(String aNodeId)
	{
		DAGNode node = nodeMap.get(aNodeId) ;
		Assert.notNull(node , "无效的节点id：%s", aNodeId) ;
		return XC.isNotEmpty(node.getPrecursorIds()) ;
	}
	
	public boolean hasFollower(String aNodeId)
	{
		DAGNode node = nodeMap.get(aNodeId) ;
		Assert.notNull(node , "无效的节点id：%s", aNodeId) ;
		return XC.isNotEmpty(node.getFollowerIds()) ;
	}
	
	/**
	 * 将指定的两个点用线连接 		<br />
	 * 重复添加不会有影响
	 * @param aPrecursorId
	 * @param aFollowerId
	 */
	public synchronized boolean addEdge(String aPrecursorId , String aFollowerId)
	{
		DAGNode precursor = nodeMap.get(aPrecursorId) ;
		Assert.notNull(precursor , "不存在id为%s的节点！" , aPrecursorId) ;
		DAGNode follower = nodeMap.get(aFollowerId) ;
		Assert.notNull(follower , "不存在id为%s的节点！" , aFollowerId) ;
		boolean changed = precursor.addFollower(aFollowerId); 
		changed |= follower.addPrecursor(aPrecursorId) ;
		rootNodes.remove(aFollowerId) ;
		return changed ;
	}
	
	public synchronized void clean()
	{
		if(dirty)
		{
			for(DAGNode node : nodeMap.values())
			{
				String nodeId = node.getId() ; 
				
				LinkedHashSet<String> nodeIds = node.getPrecursorIds() ;
				if(XC.isNotEmpty(nodeIds))
				{
					Iterator<String> it = nodeIds.iterator() ;
					while(it.hasNext())
					{
						String nodeId_0 = it.next() ;
						if(XString.isEmpty(nodeId_0) || nodeId.equals(nodeId_0))
							it.remove(); 
						DAGNode node_0 = nodeMap.get(nodeId_0) ;
						if(node_0 == null)
							it.remove(); 
						node_0.addFollower(nodeId) ;
					}
					if(nodeIds.isEmpty())
						rootNodes.add(nodeId) ;
					else
						rootNodes.remove(nodeId) ;
				}
				else
					rootNodes.add(nodeId) ;
				
				nodeIds = node.getFollowerIds() ;
				if(XC.isNotEmpty(nodeIds))
				{
					Iterator<String> it = nodeIds.iterator() ;
					while(it.hasNext())
					{
						String nodeId_0 = it.next() ;
						if(XString.isEmpty(nodeId_0) || nodeId.equals(nodeId_0))
							it.remove(); 
						DAGNode node_0 = nodeMap.get(nodeId_0) ;
						if(node_0 == null)
							it.remove(); 
						node_0.addPrecursor(nodeId) ;
					}
				}
			}
			dirty = false ;
		}
	}
	
	public synchronized List<DAGNode> getRootNodes()
	{
		if(dirty)
		{
			clean() ;
		}
		if(rootNodes.isEmpty())
			return Collections.emptyList() ;
		List<DAGNode> nodeList = XC.arrayList() ;
		for(String nodeId : rootNodes)
		{
			nodeList.add(nodeMap.get(nodeId)) ;
		}
		return nodeList ;
	}
	
	public DAGNode getNode(String aNodeId)
	{
		return nodeMap.get(aNodeId) ;
	}
	
	public List<DAGNode> getNodes(Collection<String> aNodeIds)
	{
		List<DAGNode> nodeList = XC.arrayList() ;
		for(String nodeId : aNodeIds)
		{
			DAGNode node = nodeMap.get(nodeId) ;
			if(node != null)
				nodeList.add(node) ;
		}
		return nodeList ;
	}
	
	public List<DAGNode> getFollowers(DAGNode aNode)
	{
		return getFollowers(aNode, null) ;
	}
	
	public List<DAGNode> getFollwers(String aNodeId)
	{
		DAGNode node = nodeMap.get(aNodeId) ;
		return node == null?Collections.emptyList():getFollowers(node) ;
	}
	
	public List<DAGNode> getFollowers(DAGNode aNode , Predicate<DAGNode> aFilter)
	{
		Collection<String> ids = aNode.getFollowerIds() ;
		if(XC.isNotEmpty(ids))
		{
			List<DAGNode> nodeList = XC.arrayList() ;
			for(String id : ids)
			{
				DAGNode node = nodeMap.get(id) ;
				if(node != null && (aFilter == null || aFilter.test(node)))
				{
					nodeList.add(node) ;
				}
			}
			return nodeList ;
		}
		return Collections.emptyList() ;
	} 
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		JSONObject nodesJo = new JSONObject() ;
		forEachNode((node)->{
			nodesJo.put(node.getId() ,node.toJSONObject()) ;
		}) ;
		return aJSONObj.put("nodes" , nodesJo)
			.put("rootIds" , XC.extractAsArrayList(getRootNodes() , DAGNode::getId)) ;
	}
}
