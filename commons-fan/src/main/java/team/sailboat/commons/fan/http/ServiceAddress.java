package team.sailboat.commons.fan.http;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;

public class ServiceAddress implements ToJSONObject , Cloneable
{
	String mHost ;
	int mPort ;
	
	public ServiceAddress()
	{
	}
	
	public ServiceAddress(String aHost, int aPort)
	{
		super();
		mHost = aHost;
		mPort = aPort;
	}
	
	public String getHost()
	{
		return mHost;
	}
	public void setHost(String aHost)
	{
		mHost = aHost;
	}
	
	public int getPort()
	{
		return mPort;
	}
	public void setPort(int aPort)
	{
		mPort = aPort;
	}
	
	@Override
	public int hashCode()
	{
		return (mHost+":"+mPort).hashCode() ;
	}
	
	@Override
	public boolean equals(Object aObj)
	{
		if(aObj == this)
			return true ;
		if(aObj == null)
			return false ;
		if(aObj instanceof ServiceAddress)
		{
			return JCommon.equals(mHost, ((ServiceAddress)aObj).mHost)
					&& mPort == ((ServiceAddress)aObj).mPort ;
		}
		return false ;
	}
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return aJSONObj.put("host" , mHost)
				.put("port", mPort) 
				;
	}
	
	@Override
	public String toString()
	{
		return mHost+":"+mPort ;
	}
	
	@Override
	public ServiceAddress clone()
	{
		return new ServiceAddress(mHost, mPort) ;
	}
	
	public static ServiceAddress build(JSONObject aJo)
	{
		return new ServiceAddress(aJo.optString("host") , aJo.optInteger("port")) ;
	}
	
	public static <T extends Collection<ServiceAddress>> T build(JSONArray aJa , T aServerAddrs)
	{
		if(aJa == null || aJa.size() == 0)
			return aServerAddrs ; 
		final int len = aJa.size() ;
		for(int i=0 ; i<len ; i++)
		{
			aServerAddrs.add(build(aJa.optJSONObject(i))) ;
		}
		return aServerAddrs ;
	}
	
	public static List<ServiceAddress> parse(String aServiceAddrs , int aDefaultPort)
	{
		if(XString.isEmpty(aServiceAddrs))
			return Collections.emptyList() ;
		List<ServiceAddress> addrList = XC.arrayList() ;
		for(String addr : aServiceAddrs.split(","))
		{
			String[] segs = addr.split(":") ;
			if(segs.length == 0)
				addrList.add(new ServiceAddress(segs[0], aDefaultPort)) ;
			else
				addrList.add(new ServiceAddress(segs[0], Integer.parseInt(segs[1]))) ;
		}
		return addrList ;
	}
}
