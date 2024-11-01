package team.sailboat.base.util;

import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.text.XString;

public interface IWSDBeanIdHelper
{
	public static String getIdPrefix(String aWsId)
	{
		return aWsId + "#" ;
	}
	
	public static String getWsIdFromDBeanId(String aId)
	{
		int i = aId.indexOf('#') ;
		Assert.isTrue(i>0 , "无法从id[%s]中提取工作空间的id" , aId) ;
		return aId.substring(0 , i) ;
	}
	/**
	 * id形式上是否合法，是否能解析出wsId
	 * @param aId
	 * @return
	 */
	public static boolean isLegal(String aId)
	{
		if(XString.isEmpty(aId))
			return false ;
		if(aId.startsWith("ws"))
			return aId.indexOf('#') != -1 ;
		return false ;
	}
	
	public static String getDefaultValveId(String aFlowNodeId)
	{
		return aFlowNodeId+"_out" ;
	}
	
}
