package team.sailboat.bd.base.beanch;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import team.sailboat.base.util.IWSDBeanIdHelper;
import team.sailboat.bd.base.ZBDException;
import team.sailboat.bd.base.model.ContextOutputParam;
import team.sailboat.bd.base.model.IFlowNode;
import team.sailboat.bd.base.model.IFlowValve;
import team.sailboat.bd.base.model.IWSNodeSite;
import team.sailboat.bd.base.model.ParamBinding;
import team.sailboat.bd.base.model.ParamIOContext;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.Assert;

public class FlowDiscovery implements IFlowDiscovery
{
	
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	final IWSRepoSite mWsRepoSite ;
	
//	final ThreadLocal<GetterContext> mTL_getterCtx = new ThreadLocal<>() ;
	
	public FlowDiscovery(IWSRepoSite aWsRepoSite)
	{
		mWsRepoSite = aWsRepoSite ;
	}
	
	IWSNodeSite getNodeSite(String aNodeId) throws ZBDException
	{
		String wsId = IWSDBeanIdHelper.getWsIdFromDBeanId(aNodeId) ;
		IWSRepo wsRepo = mWsRepoSite.checkWorkspaceRepo(wsId) ;
		return wsRepo.getWsNodeSite() ;
	}
	
	@Override
	public IFlowNode getFlowNode(String aNodeId) throws ZBDException
	{
		return getNodeSite(aNodeId)
				.getFlowNode(aNodeId) ;
	}
	
	@Override
	public IFlowValve getFlowValve(String aValveId) throws ZBDException
	{
		return getNodeSite(aValveId)
				.getFlowValve(aValveId) ; 
	}
	
	@Override
	public List<IFlowValve> getFlowValvesOfNode(String aNodeId) throws ZBDException
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
	 * 取得指定节点的所有前置节点
	 * @param aNode
	 * @return
	 * @throws ZBDException 
	 */
	@Override
	public List<IFlowNode> getPrecursors(IFlowNode aNode) throws ZBDException
	{
		Collection<String> precursorIds = getPrecursorIds(aNode) ;
		if(precursorIds.isEmpty())
			return Collections.emptyList() ;
		List<IFlowNode> precursors = XC.arrayList() ;
		IWSNodeSite nodeSite = null ;
		String wsId = null ;
		for(String precursorId : precursorIds)
		{
			String wsId_0 = IWSDBeanIdHelper.getWsIdFromDBeanId(precursorId) ;
			if(!wsId_0.equals(wsId))
			{
				nodeSite = mWsRepoSite.checkWorkspaceRepo(wsId_0).getWsNodeSite() ;
				wsId = wsId_0 ;
			}
			IFlowNode node = nodeSite.getFlowNode(precursorId) ;
			if(node != null)
				precursors.add(node) ;
		}
		return precursors ;
	}
	
	@Override
	public Collection<String> getPrecursorIds(IFlowNode aNode) throws ZBDException
	{
		Collection<ParamBinding> valveIds = aNode.getInputs() ;
		if(XC.isEmpty(valveIds))
			return Collections.emptyList() ;
		Set<String> precursorIds = XC.linkedHashSet() ;
		IWSNodeSite nodeSite = null ;
		String wsId = null ;
		for(ParamBinding binding : valveIds)
		{
			String valveId = binding.getRef() ;
			String wsId_0 = IWSDBeanIdHelper.getWsIdFromDBeanId(valveId) ;
			if(!wsId_0.equals(wsId))
			{
				nodeSite = mWsRepoSite.checkWorkspaceRepo(wsId_0).getWsNodeSite() ;
				wsId = wsId_0 ;
			}
			IFlowValve flowValve = nodeSite.getFlowValve(valveId) ;
			if(flowValve != null)
			{
				precursorIds.add(flowValve.getSourceNodeId()) ;
			}
		}
		return precursorIds ;
		
	}
	
	@Override
	public List<? extends IFlowNode> getFollowers(IFlowNode aNode) throws ZBDException
	{
		Collection<String> followerIds = getFollowerIds(aNode) ;
		if(followerIds.isEmpty())
			return Collections.emptyList() ;
		List<IFlowNode> followers = XC.arrayList() ;
		IWSNodeSite nodeSite = null ;
		String wsId = null ;
		for(String followerId : followerIds)
		{
			String wsId_0 = IWSDBeanIdHelper.getWsIdFromDBeanId(followerId) ;
			if(!wsId_0.equals(wsId))
			{
				nodeSite = mWsRepoSite.checkWorkspaceRepo(wsId_0).getWsNodeSite() ;
				wsId = wsId_0 ;
			}
			IFlowNode node = nodeSite.getFlowNode(followerId) ;
			if(node != null)
				followers.add(node) ;
		}
		return followers ;
	}
	
	@Override
	public Collection<String> getFollowerIds(IFlowNode aNode) throws ZBDException
	{
		Collection<ParamBinding> valveIds = aNode.getOutputs() ;
		if(XC.isEmpty(valveIds))
			return Collections.emptyList() ;
		Set<String> followerIds = XC.linkedHashSet() ;
		IWSNodeSite nodeSite = null ;
		String wsId = null ;
		for(ParamBinding binding : valveIds)
		{
			String valveId = binding.getRef() ;
			String wsId_0 = IWSDBeanIdHelper.getWsIdFromDBeanId(valveId) ;
			if(!wsId_0.equals(wsId))
			{
				nodeSite = mWsRepoSite.checkWorkspaceRepo(wsId_0).getWsNodeSite() ;
				wsId = wsId_0 ;
			}
			IFlowValve flowValve = nodeSite.getFlowValve(valveId) ;
			if(flowValve != null)
			{
				followerIds.addAll(flowValve.getTargetNodeIds()) ;
			}
		}
		return followerIds ;
	}

	@Override
	public String getWorkspaceName(String aWsId)
	{
		return mWsRepoSite.getWorkspaceNameById(aWsId) ;
	}
	
	@Override
	public String getWorkspaceId(String aWsName)
	{
		return mWsRepoSite.getWorkspaceIdByName(aWsName) ;
	}
	
	@Override
	public IFlowNode getFlowNodeOfValve(String aValveId) throws ZBDException
	{
		// 1.取得阀
		IWSNodeSite nodeSite = getNodeSite(aValveId) ; 
		IFlowValve valve = nodeSite.getFlowValve(aValveId) ;
		Assert.notNull(valve , "不存在指定id为%s的阀", aValveId) ;
		String nodeId = valve.getSourceNodeId() ;
		return nodeSite.getFlowNode(nodeId) ;
	}
	
	@Override
	public ContextOutputParam getContextOutputParam(String aValveId, String aName) throws ZBDException
	{
		// 1.通过阀id取得以此阀为输出的节点，可能多个
		IFlowNode node = getFlowNodeOfValve(aValveId) ;
		ParamIOContext paramIOCtx = node.getParamIOContext() ;
		if(paramIOCtx != null)
		{
			ContextOutputParam outParam = paramIOCtx.getOutputParam(aName) ;
			if(outParam != null)
				return outParam ;
		}
		return null;
	}
	
//	GetterContext threadContext()
//	{
//		GetterContext ctx = mTL_getterCtx.get() ;
//		if(ctx == null)
//		{
//			ctx = new GetterContext() ;
//			mTL_getterCtx.set(ctx) ;
//		}
//		return ctx ;
//	}
	
	
//	/**
//	 * 非线程安全
//	 *
//	 * @author yyl
//	 * @since 2021年6月18日
//	 */
//	class GetterContext
//	{
//		String mWsId = null ;
//		WeakReference<IWSRepo> mWf_WsRepo = null ;
//		
//		
//		public IWSNodeSite getNodeSite(String aDBeanId)
//		{
//			String wsId_0 = IWSDBeanIdHelper.getWsIdFromDBeanId(aDBeanId) ;
//			if(!wsId_0.equals(mWsId))
//			{
//				try
//				{
//					mWf_WsRepo = new WeakReference<IWSRepo>(mWsRepoSite.checkWorkspaceRepo(wsId_0)) ;
//				}
//				catch (ZBDException e)
//				{
//					WrapException.wrapThrow(e) ;
//					return null ;			//dead code
//				}
//				mWsId = wsId_0 ;
//			}
//			return mWsNodeSite ;
//		}
//	}

}
