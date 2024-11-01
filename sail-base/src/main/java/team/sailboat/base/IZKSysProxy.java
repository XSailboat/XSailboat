package team.sailboat.base;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;

public interface IZKSysProxy extends IZKProxy
{
	void registerWebModule(String aModuleName , String aDisplayName  , String aServiceAddr
			, String aPagePath , String aIconPath , double aOrder) throws Exception ;

	
	JSONArray getRegisteredWebModules() throws Exception ;
	
	JSONObject getRegisteredWebModule(String aName) throws Exception ;
			
	/**
	 * 注册HTTP API服务
	 * @param aServiceName
	 * @param aAddrs
	 * @throws Exception
	 */
	default void registerHttpService(String aServiceName , Properties aInfo
			, ClusterMode aMode , String aAddrs) throws Exception
	{
		registerService(aServiceName , aInfo , aMode , "http" , aAddrs) ;
	}
	
	@Deprecated
	default void registerHttpService(String aServiceName  , String aAddrs , Properties aInfo
			, ClusterMode aMode) throws Exception
	{
		registerService(aServiceName , aInfo , aMode , "http" , aAddrs) ;
	}
	
	default void registerHttpAndHttpsService(String aServiceName , Properties aInfo
			, ClusterMode aMode , String aHttpAddrs , String aHttpsAddrs) throws Exception
	{
		registerServices(aServiceName , aInfo , aMode , XC.hashMap("http" , aHttpAddrs
				, "https" , aHttpsAddrs)) ;
	}
	
	/**
	 * 
	 * @param aServiceName
	 * @param aInfo
	 * @param aMode
	 * @param aAddrs		键是协议，值是地址
	 * @throws Exception
	 */
	default void registerServices(String aServiceName , Properties aInfo
			, ClusterMode aMode , Map<String , String> aAddrs) throws Exception
	{
		for(Entry<String , String> entry : aAddrs.entrySet())
			registerService(aServiceName, aInfo, aMode, entry.getKey(), entry.getValue()) ;
	}
	
	/**
	 * 
	 * @param aServiceName
	 * @param aInfo
	 * @param aMode
	 * @param aProtocol
	 * @param aAddrs
	 * @throws Exception
	 */
	void registerService(String aServiceName , Properties aInfo , ClusterMode aMode , String aProtocol , String aAddrs) throws Exception ;
	
	String getRegisteredHttpService(String aServiceName) throws Exception ;
	
	/**
	 * 取得已经注册服务的简要信息，包括服务名(name)、厂家(company)、描述（description）
	 * @param aProduce 		提供此种类型服务的服务
	 * @return
	 * @throws Exception
	 */
	JSONArray getRegisteredServiceBriefs(String aProduce) throws Exception ;
	
	/**
	 * 取得hadoop集群名称，也是hdfs的集群名
	 * @return
	 */
	String getHadoopClusterName() throws Exception ;
	
	/**
	 * 取得所有平台主机信息
	 * @return
	 */
	JSONObject getHosts() throws Exception ;
}
