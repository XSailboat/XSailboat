package team.sailboat.bd.base.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.IntPredicate;

import team.sailboat.bd.base.ZBDException;
import team.sailboat.bd.base.model.IFlowNode;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.infc.EFunction;
import team.sailboat.commons.fan.infc.EPredicate;
import team.sailboat.commons.fan.infc.IterateOpCode;
import team.sailboat.commons.fan.infc.IteratorPredicate;

public class DAGVisitor
{
	
	/**
	 * 广度优先遍历			<br />
	 * 当aSceopJudge == null 时，它是优先到达的		<br>
	 * 当aScopeJudge != null 时，它是最后到达的		<br>
	 * @param aNodeList		这些节点会被访问
	 * @param aPred				
	 * @param aScopeJudge		如果非空，且aFirstArrive为false，则必需精确描述遍历范围，且指定的开始遍历节点是此范围的根，否则可能无法正常遍历
	 * @param aFirstArrive		true表示首先到达，否则得当范围内的所有节点都遍历过之后，才能访问这个节点。当aScopeJudge==null时，aFirstArrive只能为true
	 * @throws ZBDException 
	 */
	public static void breadthFirstVisit(List<? extends IFlowNode> aNodeList , IteratorPredicate<IFlowNode> aPred 
			, EPredicate<String , ZBDException> aScopeJudge , boolean aFirstArrive , IntPredicate aFloorPred
			, EFunction<IFlowNode , Collection<? extends IFlowNode> , ZBDException> aSpade
			, EFunction<IFlowNode , Collection<String> , ZBDException> aInvSpade) throws ZBDException
	{
		if(aScopeJudge == null)
			aFirstArrive = true ;
		List<IFlowNode> nodeList = XC.arrayList(aNodeList) ;
		if(aFloorPred != null)
			nodeList.add(null) ;								//作为层与层之间的分界线
		// 值为1表示已经添加到nodeList，但还没有visit；为2表示已经visited
		Map<String , Integer> visitedIdMap = XC.hashMap() ;
		int floor = 1 ;
		IFlowNode floorStart = null ;
		for(int i=0 ; i<nodeList.size() ; i++)
		{
			IFlowNode node = nodeList.get(i) ;
			if(node == null)
			{
				if(aFloorPred != null)
				{
					floorStart = null ;
					if(!aFloorPred.test(floor++))
						return ;
				}
				continue ;
			}
			visitedIdMap.put(node.getId() , 2) ;
			switch(aPred.visit(node))
			{
			case IterateOpCode.sBreak:    			//不要再深入挖掘
				continue ;
			case IterateOpCode.sInterrupted:		//中止整个遍历
				return  ;
			case IterateOpCode.sContinue :
				break ;
			default :
				throw new IllegalStateException("不合法的状态值") ;
			}
			if(node == floorStart)
			{
				nodeList.add(null) ;				//作为层与层之间的分界线
			}
			Collection<? extends IFlowNode> nodes = aSpade.apply(node) ; 
			if(nodes != null)
			{
				for(IFlowNode node_t : nodes)
				{
					//不在范围内，不用遍历
					if(aScopeJudge != null && !aScopeJudge.test(node_t.getId())
							|| visitedIdMap.containsKey(node_t.getId()))
						continue ;
					//看看前置节点是否都已经遍历完
					boolean allVisited = true ;
					if(!aFirstArrive)
					{
						for(String nodeId : aInvSpade.apply(node_t))
						{
							if(aScopeJudge.test(nodeId) && visitedIdMap.getOrDefault(nodeId , 0) != 2)
							{
								allVisited = false ;
								break ;
							}
						}
					}
					if(allVisited && !visitedIdMap.containsKey(node_t.getId()))
					{
						nodeList.add(node_t) ;
						visitedIdMap.put(node_t.getId(), 1) ;
						if(aFloorPred != null && floorStart == null)
							floorStart = node_t ;
					}
				}
			}
			// 被访问过的元素太多了，清理一下
			if(i>=1000)
			{
				nodeList.subList(0, i).clear();
				i = 0 ;
			}
		}
	}	
	
}
