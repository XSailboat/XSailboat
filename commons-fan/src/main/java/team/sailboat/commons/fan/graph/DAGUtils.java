package team.sailboat.commons.fan.graph;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;
import java.util.function.Predicate;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.infc.EConsumer;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.struct.XInt;
import team.sailboat.commons.fan.text.XString;

public class DAGUtils
{
	
	public static <T extends IDirectedGraphNode> boolean isAGraph(T[] aNodes)
	{
		return isAGraph(Arrays.asList(aNodes)) ;
	}
	
	public static <T extends IDirectedGraphNode> boolean isAGraph(Collection<T> aNodes)
	{
		return isAGraph(aNodes ,IDirectedGraphNode::getId) ;
	}
	
	/**
	 * 是否是一张有向无环图
	 * @param aNodes
	 * @return
	 */
	public static <T extends IDirectedGraphNode> boolean isAGraph(Collection<T> aNodes
			, Function<T, String> aIdGetter)
	{
		if(XC.isEmpty(aNodes))
			return false ;
		if(aNodes.size() == 1)
			return true ;
		// 首先它是一张图。判定方法是每一个节点必需有前置节点或者后置节点在节点id的集合内
		Map<String, ? extends IDirectedGraphNode> map = XC.hashMap(aNodes, aIdGetter , true) ;
		Map<String, XInt>  ioMeasMap = XC.hashMap() ;
		List<String> inMeas_0 = XC.arrayList() ;
		for(T node : aNodes)
		{
			Collection<String> ids = node.getPrecursorIds() ;
			XInt inMeas = new XInt(0) ;
			if(XC.isNotEmpty(ids))
			{
				for(String id : ids)
				{
					if(map.containsKey(id))
						inMeas.incrementAndGet() ;
				}
			}
			ids = node.getFollowerIds() ;
			XInt outMeas = new XInt(0) ;
			if(XC.isNotEmpty(ids))
			{
				for(String id : ids)
				{
					if(map.containsKey(id))
					{
						outMeas.incrementAndGet() ;
					}
				}
			}
			if(inMeas.i == 0 && outMeas.i == 0)
				return false ;
			String id = aIdGetter.apply(node) ;
			ioMeasMap.put(id , inMeas) ;
			if(inMeas.i == 0)
				inMeas_0.add(id) ;
		}
		// 判定有没有环
		while(!ioMeasMap.isEmpty())
		{
			if(inMeas_0.isEmpty())
			{
				// 存在环
				return false ;
			}
			String[] ioMeas_0_ids = inMeas_0.toArray(JCommon.sEmptyStringArray) ;
			inMeas_0.clear();
			for(String id : ioMeas_0_ids)
			{
				IDirectedGraphNode node = map.get(id) ;
				Collection<String> followerIds = node.getFollowerIds() ;
				ioMeasMap.remove(id) ;
				if(XC.isNotEmpty(followerIds))
				{
					// 这些节点的入度都减去1
					for(String followerId : followerIds)
					{
						XInt inMeas = ioMeasMap.get(followerId) ;
						if(inMeas != null && inMeas.decrementAndGet() == 0)
						{
							inMeas_0.add(followerId) ;
						}
					}
				}
			}
		}
		return true ;
	}
	
	/**
	 * 是否是一张有向无环图
	 * @param aNodes
	 * @return
	 */
	public static <T extends IDirectedGraphNode> String isAGraph0(Collection<T> aNodes
			, Function<T, String> aIdGetter)
	{
		if(XC.isEmpty(aNodes))
			return "不是一个合法的有向无环图图！因为没有节点。" ;
		if(aNodes.size() == 1)
			return null ;
		// 首先它是一张图。判定方法是每一个节点必需有前置节点或者后置节点在节点id的集合内
		Map<String, ? extends IDirectedGraphNode> map = XC.hashMap(aNodes, aIdGetter , true) ;
		Map<String, XInt>  ioMeasMap = XC.hashMap() ;
		List<String> inMeas_0 = XC.arrayList() ;
		for(T node : aNodes)
		{
			Collection<String> ids = node.getPrecursorIds() ;
			XInt inMeas = new XInt(0) ;
			if(XC.isNotEmpty(ids))
			{
				for(String id : ids)
				{
					if(map.containsKey(id))
						inMeas.incrementAndGet() ;
				}
			}
			ids = node.getFollowerIds() ;
			XInt outMeas = new XInt(0) ;
			if(XC.isNotEmpty(ids))
			{
				for(String id : ids)
				{
					if(map.containsKey(id))
					{
						outMeas.incrementAndGet() ;
					}
				}
			}
			if(inMeas.i == 0 && outMeas.i == 0)
				return XString.msgFmt("不是一个合法的有向无环图！因为节点[{}]的入度和出度都是0，它是一个孤立的节点。" , node.toString()) ;
			String id = aIdGetter.apply(node) ;
			ioMeasMap.put(id , inMeas) ;
			if(inMeas.i == 0)
				inMeas_0.add(id) ;
		}
		// 判定有没有环
		while(!ioMeasMap.isEmpty())
		{
			if(inMeas_0.isEmpty())
			{
				// 存在环
				return "不是一个合法的有向无环图！因为图中存在环。" ;
			}
			String[] ioMeas_0_ids = inMeas_0.toArray(JCommon.sEmptyStringArray) ;
			inMeas_0.clear();
			for(String id : ioMeas_0_ids)
			{
				IDirectedGraphNode node = map.get(id) ;
				Collection<String> followerIds = node.getFollowerIds() ;
				if(ioMeasMap.get(id).get() == 0)
					ioMeasMap.remove(id) ;
				if(XC.isNotEmpty(followerIds))
				{
					// 这些节点的入度都减去1
					for(String followerId : followerIds)
					{
						XInt inMeas = ioMeasMap.get(followerId) ;
						if(inMeas != null && inMeas.decrementAndGet() == 0)
						{
							inMeas_0.add(followerId) ;
						}
					}
				}
			}
		}
		return null ;
	}
	
	/**
	 * 有序遍历有向无环图
	 * @param aNodesJo
	 * @throws Exception 
	 */
	public static void orderlyVisit(JSONObject aNodesJo , EConsumer<JSONObject , Exception> aNodeConsumer) throws Exception
	{
		if(aNodesJo == null || aNodesJo.isEmpty())
			return ;
		Map<String , XInt> nodeInDegreeMap = XC.hashMap() ;
		List<String> zeroInDegreeNodeIds = XC.linkedList() ;
		for(String nodeId : aNodesJo.keySet())
		{
			JSONObject jo = aNodesJo.optJSONObject(nodeId) ;
			JSONArray inNodeIds = jo.optJSONArray("precursorIds") ;
			int degree = inNodeIds == null?0:inNodeIds.size() ;
			if(degree == 0)
				zeroInDegreeNodeIds.add(nodeId) ;
			else
				nodeInDegreeMap.put(nodeId , new XInt(degree)) ;
		}
		while(!zeroInDegreeNodeIds.isEmpty())
		{
			String nodeId = zeroInDegreeNodeIds.remove(0) ;
			JSONObject nodeJo = aNodesJo.optJSONObject(nodeId) ;
			aNodeConsumer.accept(nodeJo) ;
			JSONArray followerIdsJa = nodeJo.optJSONArray("followerIds") ;
			if(followerIdsJa == null || followerIdsJa.isEmpty())
				continue ;
			final int len = followerIdsJa.size() ;
			for(int i=0 ; i<len ; i++)
			{
				nodeId = followerIdsJa.optString(i) ;
				XInt degree = nodeInDegreeMap.get(nodeId) ;
				if(degree != null)
				{
					if(degree.decrementAndGet() == 0)
					{
						zeroInDegreeNodeIds.add(nodeId) ;
						nodeInDegreeMap.remove(nodeId) ;
						nodeJo = aNodesJo.optJSONObject(nodeId) ;
					}
				}
			}
		}
		Assert.isEmpty(nodeInDegreeMap, "有%1$d个节点因不满足DAG图的规则而无法遍历！这些节点的id是：%2$s", nodeInDegreeMap.size()
				, XString.toString(",", nodeInDegreeMap.keySet()));
	}
	
	/**
	 * 
	 * @param aGraph
	 * @param aStartNodeId
	 * @param aEndNodeId
	 * @param aPathVisitor		返回false，则即刻终止遍历，不再遍历更多。返回的节点路径包含起始节点，不包含结束节点
	 */
	public static void forEachPath(DAGraph aGraph , String aStartNodeId , String aEndNodeId
			, Predicate<List<? extends IDirectedGraphNode>> aPathVisitor)
	{
		IDirectedGraphNode start = aGraph.getNode(aStartNodeId) ;
		Stack<IDirectedGraphNode> path = new Stack<>() ;
		path.add(start) ;
		deepthVisit(aGraph , start.getFollowerIds() , aEndNodeId , path , aPathVisitor) ;
	}
	
	static boolean deepthVisit(DAGraph aGraph , Collection<String> aNodeIds , String aEndNodeId 
			, Stack<IDirectedGraphNode> aPath , Predicate<List<? extends IDirectedGraphNode>> aVisitor)
	{
		if(XC.isEmpty(aNodeIds))
			return true ;
		for(String nodeId : aNodeIds)
		{
			if(nodeId.equals(aEndNodeId))
			{
				boolean ctn = aVisitor.test(aPath) ;
				if(!ctn)
					return false ;
			}
			else
			{
				IDirectedGraphNode node = aGraph.getNode(nodeId) ;
				Assert.notNull(node , "无效的节点id：%s" , nodeId) ;
				aPath.add(node) ;
				boolean ctn = deepthVisit(aGraph, node.getFollowerIds() , aEndNodeId, aPath, aVisitor) ;
				aPath.pop() ;
				if(!ctn)
					return false ;
			}
		}
		return true ;
	}
}
