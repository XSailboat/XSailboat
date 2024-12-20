package team.sailboat.base;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.text.XString;

/**
 * 
 * 系统的Zookeeper注册/数据相关的操作代理
 *
 * @author yyl
 * @since 2024年12月7日
 */
public interface IZKSysProxy extends IZKProxy
{
	
	/**
	 * 
	 * 注册一个Web应用
	 * 
	 * @param aAppName
	 * @param aProp
	 * @param aMode			注册模式
	 * @param aServiceUri	服务uri
	 * @throws Exception
	 */
	void registerWebApp(String aAppName
			, PropertiesEx aProp
			, ClusterMode aMode
			, String aServiceUri) throws Exception ;
	
	/**
	 * 
	 * 取得指定名称的注册为WebApp的信息
	 * 
	 * @param aName
	 * @return
	 * @throws Exception
	 */
	JSONObject getRegisteredWebApp(String aName) throws Exception ;
	
	/**
	 * 
	 * 注册大数据平台的Web模块				<br />
	 * 这是要在大数据平台的总体菜单中出现的
	 * 
	 * @param aModuleName			模块名
	 * @param aDisplayName			模块显示名
	 * @param aServiceAddr			服务地址
	 * @param aPagePath				页面路径
	 * @param aIconPath				图表路径
	 * @param aOrder				排序序号
	 * @throws Exception
	 */
	void registerSailboatWebModule(String aModuleName , String aDisplayName  , String aServiceAddr
			, String aPagePath , String aIconPath , double aOrder) throws Exception ;

	/**
	 * 
	 * 取得所有注册为大数据平台web模块的信息
	 * 
	 * @return
	 * @throws Exception
	 */
	JSONArray getRegisteredSailboatWebModules() throws Exception ;
	
	/**
	 * 
	 * 取得指定名称的注册为大数据平台Web模块的信息
	 * 
	 * @param aName
	 * @return
	 * @throws Exception
	 */
	JSONObject getRegisteredSailboatWebModule(String aName) throws Exception ;
	
	default void registerApiServices(String aServiceName , Properties aInfo
			, ClusterMode aMode , String aHttpAddrs , String aHttpsAddrs) throws Exception
	{
		registerApiServices(aServiceName , aInfo , aMode , XC.hashMapIf((k, v)->XString.isNotEmpty((String)v)
				, "http" , aHttpAddrs
				, "https" , aHttpsAddrs)) ;
	}
	
	
	/**
	 * 
	 * @param aServiceName
	 * @param aInfo
	 * @param aMode
	 * @param aProtocal				协议
	 * @param aAddrs				指定协议的服务地址，不带协议。只有”主机:port"，多个之间用“,”分隔
	 * @throws Exception
	 */
	void _registerApiService(String aServiceName , Properties aInfo
			, ClusterMode aMode , String aProtocal , String aAddrs) throws Exception ;
	
	/**
	 * 
	 * @param aServiceName
	 * @param aInfo
	 * @param aMode
	 * @param aAddrs		键是协议，值是"地址:端口"
	 * @throws Exception
	 */
	default void registerApiServices(String aServiceName , Properties aInfo
			, ClusterMode aMode , Map<String , String> aAddrs) throws Exception
	{
		for(Entry<String , String> entry : aAddrs.entrySet())
			_registerApiService(aServiceName, aInfo, aMode, entry.getKey(), entry.getValue()) ;
	}
	
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
