package team.sailboat.bd.base.model;

import team.sailboat.bd.base.beanch.IFlowDiscovery;
import team.sailboat.bd.base.beanch.IWSRepo;
import team.sailboat.bd.base.proxy.INodeResourceProxy;
import team.sailboat.commons.fan.dpa.DRepository;

public abstract class WSNodeSiteBase implements IWSNodeSite
{
	protected final IWSRepo mParent ;
	protected final String mWsId ;
	protected final String mIdPrefix ;
	
	protected IFlowDiscovery mDiscovery ;
	protected INodeResourceProxy mResourceProxy ;
	protected DRepository mRepo ;
	
	public WSNodeSiteBase(IWSRepo aParent , IFlowDiscovery aFlowDiscovery 
			, INodeResourceProxy aResourceProxy)
	{
		mParent = aParent ;
		mWsId = mParent.getWorkspaceId() ;
		mIdPrefix = mWsId + "#" ;
		mDiscovery = aFlowDiscovery ;
		mResourceProxy = aResourceProxy ;
		mRepo = mParent.getWsRepository() ;
	}
	
	@Override
	public IWSRepo getWsRepo()
	{
		return mParent ;
	}
	
	public IFlowDiscovery getDiscovery()
	{
		return mDiscovery;
	}
	
	@Override
	public boolean isCurrentWorkspace(String aNodeOrValveId)
	{
		return aNodeOrValveId.startsWith(mIdPrefix) ;
	}
	
	@Override
	public INodeResourceProxy getResourceProxy()
	{
		return mResourceProxy ;
	}
}
