package team.sailboat.commons.fan.graph;

import java.util.LinkedHashSet;
import java.util.Map;

import lombok.Data;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.text.XString;

@Data
public class DAGNode implements IDirectedGraphNode , ToJSONObject
{
	String id ;
	
	double x ;
	
	double y ;
	
	String name ;
	
	String nodeType ;
	
	Map<String , Object> extendAttrs ;
	
	LinkedHashSet<String> precursorIds ;

	LinkedHashSet<String> followerIds ;
	
	public DAGNode()
	{}
	
	public DAGNode(String aId)
	{
		id = aId ;
	}
	
	public DAGNode(String aId , String aName)
	{
		id = aId ;
		name = aName ;
	}
	
	/**
	 * 只用于Jackson反序列化
	 * @param aIds
	 */
	@Deprecated
	public void setPrecursorIds(LinkedHashSet<String> aIds)
	{
		precursorIds = aIds ;
	}
	
	/**
	 * 
	 * @return		返回结果不为null
	 */
	public String[] getPrecursorIdsAsArray()
	{
		return precursorIds == null?JCommon.sEmptyStringArray
				: precursorIds.toArray(JCommon.sEmptyStringArray) ;
	}
	
	public String[] getFollowerIdsAsArray()
	{
		return followerIds == null?JCommon.sEmptyStringArray
				: followerIds.toArray(JCommon.sEmptyStringArray) ;
	}
	
	/**
	 * 只用于Jackson反序列化
	 * @param aIds
	 */
	@Deprecated
	public void setFollowerIds(LinkedHashSet<String> aIds)
	{
		followerIds = aIds ;
	}
	
	boolean addFollower(String aFollowerId)
	{
		if(followerIds == null)
			followerIds = XC.linkedHashSet() ;
		return followerIds.add(aFollowerId) ;
	}
	
	boolean addPrecursor(String aPrecursorId)
	{
		if(precursorIds == null)
			precursorIds = XC.linkedHashSet() ;
		return precursorIds.add(aPrecursorId) ;
	}
	
	boolean removeFollower(String aFollwerId)
	{
		if(followerIds != null)
		{
			return followerIds.remove(aFollwerId) ;
		}
		return false ;
	}
	
	boolean removePrecursor(String aPrecursorId)
	{
		if(precursorIds != null)
			return precursorIds.remove(aPrecursorId) ;
		return false ;
	}
	
	public Object getExtendAttr(String aAttrName)
	{
		return extendAttrs != null?extendAttrs.get(aAttrName):null ;
	}
	
	public Integer getExtendAttrAsInt(String aAttrName)
	{
		return XClassUtil.toInteger(getExtendAttr(aAttrName)) ;
	}
	
	public void setExtendAttr(String aAttrName , Object aValue)
	{
		if(extendAttrs == null)
			extendAttrs = XC.hashMap() ;
		extendAttrs.put(aAttrName, aValue) ;
	}
	
	public boolean hasPrecursor()
	{
		return XC.isNotEmpty(precursorIds) ;
	}
	
	public void setPosition(double aX , double aY)
	{
		x = aX ;
		y = aY ;
	}
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		aJSONObj.put("id" , id)
				.put("name" , name)
				.putIf(XString.isNotEmpty(nodeType) , "nodeType" , nodeType)
				.put("x", x)
				.put("y", y)
				.putIf(XC.isNotEmpty(precursorIds) , "precursorIds" , new JSONArray(precursorIds))
				.putIf(XC.isNotEmpty(followerIds) , "followerIds" , new JSONArray(followerIds))
				;
		if(XC.isNotEmpty(extendAttrs))
		{
			extendAttrs.forEach(aJSONObj::put) ;
		}
		return aJSONObj ;
	}
}
