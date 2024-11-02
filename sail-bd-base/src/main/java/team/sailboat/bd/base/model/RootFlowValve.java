package team.sailboat.bd.base.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONObject;

public class RootFlowValve implements IFlowValve
{

	final String mId ;
	
	final String mSourceNodeId ;
	
	final LinkedHashSet<String> mTargetNodeIds = XC.linkedHashSet() ;
	
	final ParamSource mSource = ParamSource.auto ;
	
	public RootFlowValve(String aWsId)
	{
		mId = aWsId+"#root_out" ;
		mSourceNodeId = aWsId+"#root" ;
	}
	
	@Override
	public String getId()
	{
		return mId ;
	}

	@Override
	public boolean setSourceNodeId(String aNodeId)
	{
		throw new IllegalStateException("工作空间根节点的阀，此方法不支持!") ;
	}

	@Override
	public String getSourceNodeId()
	{
		return mSourceNodeId ;
	}

	@Override
	public void addTargetNodeId(String aNodeId)
	{
		mTargetNodeIds.add(aNodeId) ;
	}
	
	@Override
	public void setTargetNodeIds(String... aNodeIds)
	{
		boolean empty_new = XC.isEmpty(aNodeIds) ;
		if(empty_new)
		{
			mTargetNodeIds.clear();
		}
		else
		{
			mTargetNodeIds.clear();
			XC.addAll(mTargetNodeIds, aNodeIds) ;
		}
	}

	@Override
	public boolean removeTargetNodeId(String aNodeId)
	{
		return mTargetNodeIds.remove(aNodeId) ;
	}

	@Override
	public Set<String> getTargetNodeIds()
	{
		return mTargetNodeIds == null ? Collections.emptySet() : mTargetNodeIds ;
	}

	@Override
	public ParamSource getSource()
	{
		return mSource ;
	}

	@Override
	public void setSource(ParamSource aSource)
	{
		throw new IllegalStateException("工作空间根节点的阀，此方法不支持!") ;
	}
	
	public static boolean isValveOfWsRoot(String aValveId)
	{
		return aValveId != null && aValveId.endsWith("#root_out") ;
	}
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return null;
	}
}
