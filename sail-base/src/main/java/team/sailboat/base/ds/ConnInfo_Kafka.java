package team.sailboat.base.ds;

import java.util.LinkedHashSet;

import io.swagger.v3.oas.annotations.media.Schema;
import team.sailboat.base.def.DataSourceType;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.http.ServiceAddress;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;


@Schema(name="ConnInfo_Kafka" , description="Kafka的集群地址")
public class ConnInfo_Kafka extends ConnInfo
{
	
	LinkedHashSet<ServiceAddress> mServiceAddrs ;
	
	public ConnInfo_Kafka()
	{
		super("ConnInfo_Kafka") ;
	}
	
	@Schema(description = "服务地址")
	public LinkedHashSet<ServiceAddress> getServiceAddrs()
	{
		return mServiceAddrs;
	}
	public void setServiceAddrs(LinkedHashSet<ServiceAddress> aServerAddrs)
	{
		mServiceAddrs = aServerAddrs;
	}
	
	@Override
	public void checkForCreate()
	{
		Assert.notEmpty(mServiceAddrs , "至少指定一个服务地址！") ;
		for(ServiceAddress addr : mServiceAddrs)
		{
			// 1. hostName不能为空不能为空
			Assert.notEmpty(addr.getHost() , "主机名不能为空") ;
			// 2. port
			Assert.isTrue(addr.getPort() > 0 , "必需指定端口");
		}
	}
	
	@Override
	public String getConnURI(DataSourceType aType)
	{
		return XString.toString(",", mServiceAddrs) ;
	}
	
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return super.setTo(aJSONObj)
				.put("serviceAddrs" , new JSONArray(mServiceAddrs))
				;
	}
	
	@Override
	protected boolean update(ConnInfo aConnInfo , boolean aPartially)
	{
		boolean changed = super.update(aConnInfo, aPartially) ;
		if(aConnInfo instanceof ConnInfo_Kafka)
		{
			ConnInfo_Kafka connInfo = (ConnInfo_Kafka)aConnInfo ;
			boolean resourceChanged = false ;
			if((!aPartially || connInfo.mServiceAddrs != null) && !JCommon.equals(connInfo.mServiceAddrs ,mServiceAddrs))
			{
				LinkedHashSet<ServiceAddress> addrs = XC.linkedHashSet() ;
				XC.deepClone(addrs , connInfo.mServiceAddrs) ;
				mServiceAddrs = addrs ;
				changed = true ;
				resourceChanged = true ;
			}
			if(resourceChanged)
				closeResource() ;
		}
		return changed ;
	}
	
	@Override
	public boolean equals(Object aObj)
	{
		if(aObj == null || !aObj.getClass().equals(getClass()))
			return false ;
		ConnInfo_Kafka connInfo = (ConnInfo_Kafka)aObj ;
		return JCommon.equals(mServiceAddrs, connInfo.mServiceAddrs)
				 ;
	}
	
	@Override
	public ConnInfo_Kafka clone()
	{
		ConnInfo_Kafka clone = new ConnInfo_Kafka() ;
		clone.copyFrom(this) ;
		return clone ;
	}
	
	public static ConnInfo_Kafka parse(String aConnInfo)
	{
		JSONObject jo = new JSONObject(aConnInfo) ;
		ConnInfo_Kafka connInfo = new ConnInfo_Kafka() ;
		connInfo.setDescription(jo.optString("description")) ;
		connInfo.setServiceAddrs(ServiceAddress.build(jo.optJSONArray("serviceAddrs") , XC.linkedHashSet())) ;
		return connInfo ;
	}

	
}
