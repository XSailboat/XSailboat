package team.sailboat.bd.base.model;

import java.util.Set;

import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;

public interface IFlowValve extends ToJSONObject
{
	String getId() ;
	
	/**
	 * 以此阀作为输出的节点
	 * @param aNodeId
	 */
	boolean setSourceNodeId(String aNodeId) ;
	/**
	 * 以此阀作为输出的节点id
	 */
	String getSourceNodeId() ;
	
	void setTargetNodeIds(String...aNodeIds) ;
	
	void addTargetNodeId(String aNodeId) ;
	
	boolean removeTargetNodeId(String aNodeId) ;
	/**
	 * 返回结果不为null
	 * @return
	 */
	Set<String> getTargetNodeIds() ;
	
	ParamSource getSource() ;
	void setSource(ParamSource aSource) ;
	
	public static void connect(IFlowValve aPreValve , IFlowNode aFollower , ParamBindingSource aSource)
	{
		aPreValve.addTargetNodeId(aFollower.getId()) ;
		aFollower.addInput(aPreValve.getId() , aSource) ;
	}
	
	public static boolean disconnect(IFlowValve aPreValve , IFlowNode aFollower)
	{
		boolean changed = aPreValve.removeTargetNodeId(aFollower.getId()) ;
		changed |= aFollower.removeInput(aPreValve.getId()) ;
		return changed ;
	}
	
	@Override
	default JSONObject setTo(JSONObject aJSONObj)
	{
		return aJSONObj.put("id", getId())
			.put("sourceNodeId" , getSourceNodeId())
			.put("targetNodeIds", new JSONArray(getTargetNodeIds()))
			.put("source", getSource().name()) ;
	}
	
	public static void update(IFlowValve aValve , JSONObject aJo)
	{
		aValve.setSourceNodeId(aJo.optString("sourceNodeId")) ;
		aValve.setTargetNodeIds(aJo.optJSONArray("targetNodeIds").toStringArray()) ;
		aValve.setSource(ParamSource.valueOf(aJo.optString("source"))) ;
	}
}
