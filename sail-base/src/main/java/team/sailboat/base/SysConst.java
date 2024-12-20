package team.sailboat.base;

import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.text.XString;

/**
 * 系统常量表
 *
 * @author yyl
 * @since 2024年12月19日
 */
public interface SysConst
{
	/**
	 * 系统应用版本。全局统一
	 */
	public static final String sAppVer = "0.0.2-beta";
	
	/**
	 * Spring应用的额外配置文件名
	 */
	public static final String sFN_SpringAppExtraConfig = "application-extra.yaml" ;
	
	/**
	 * System.getProperty()的键<br />
	 * 用来设置表明当前应该归属和使用哪个环境：dev（开发环境）、prod（生产环境）、test（测试环境）。<br/>
	 */
	public static final String sPK_SysEnv = "sys_env" ;
	
	/**
	 * 系统环境			<br />
	 * 开发环境(dev)
	 */
	public static final String sPKV_SysEnv_dev = "dev" ;
	
	/**
	 * 系统环境			<br />
	 * 生产环境(prod)
	 */
	public static final String sPKV_SysEnv_prod = "prod" ;
	
	/**
	 * 系统环境			<br />
	 * 测试环境(test)
	 */
	public static final String sPKV_SysEnv_test = "test" ;
	
	public static final String sCompany = "威海欣智信息科技有限公司" ;
	
	/**
	 * 服务类型是提供http(s)接口的微服务
	 */
	public static final String sServiceType_MS_HTTP_API = "MS-HTTP-API" ;
	
	/**
	 * 服务类型是SailboatWeb应用--认证中心
	 */
	public static final String sServiceType_SailboatWeb_AuthCenter = "SailboatWeb-AuthCenter" ;
	
	/**
	 * 服务类型是SailboatWeb应用--平台模块
	 */
	public static final String sServiceType_SailboatWeb_Module = "SailboatWeb-Module" ;
	
	public static final String sAppName_PyTask = "PyTask" ;
	
	public static final String sAppName_SailMSExecEngine = "SailMSExecEngine" ;
	
	public static final String sAppName_XTMS = "XTMS" ;
	
	public static final String sAppName_SailMSPivot = "SailMSPivot" ;
	
	public static final String sAppName_SailMSWorks = "SailMSWorks" ;
	
	/**
	 * 认证中心
	 */
	public static final String sAppName_SailAC = "SailAC" ;
	
	/**
	 * xz是“协作”的缩写
	 */
	public static final String sSysCode = "xz" ;
	
	public static final String sZK_PathPtn_SysRoot = "/"+sSysCode+"/{}" ;
	
	/**
	 * Sailboat Web模块注册的ZK路径
	 */
	public static final String sZK_SysPath_SailboatWebModules = "/registry-site/SailboatWeb/module" ;
	
	/**
	 * 某个Sailboat Web模注册的ZK路径模版
	 */
	public static final String sZK_SysPathPtn_SailboatWebModuleOne = "/registry-site/SailboatWeb/module/{}" ;
	
	public static final String sZK_SysPath_RegistrySite = "/registry-site" ;
	
	public static final String sZK_SysPath_WebApp =  "/registry-site/WebApp" ;
	
	public static final String sZK_SysPathPtn_WebApp =  "/registry-site/WebApp/{}" ;
	
	/**
	 * 活跃的WebApp实例，得下面有临时节点存在才可以。
	 */
	public static final String sZK_SysPathPtn_ActiveWebApp =  "/registry-site/WebApp/{}/active" ;
	
	public static final String sZK_SysPath_ApiService =  "/registry-site/ApiService" ;
	
	public static final String sZK_SysPathPtn_ApiService =  "/registry-site/ApiService/{}" ;
	
	public static final String sZK_SysPathPtn_http =  "/registry-site/ApiService/{}/http" ;
	
	public static final String sZK_SysPathPtn_https =  "/registry-site/ApiService/{}/https" ;
	
	public static final String sZK_SysPathPtn_ApiService_special = "/registry-site/ApiService/{}/{}" ;
	
	public static final String sZK_Path_SysCommon = "/"+sSysCode+"/common" ;
	
	public static final String sZK_Path_Kafka = "/kafka" ;
	
	/**
	 * 
	 */
	public static final String sZK_Path_KafkaBrokerIds = sZK_Path_Kafka + "/brokers/ids" ;
	
	/**
	 * 集群的ip白名单
	 */
	public static final String sZK_SysPath_cluster_ipWhiteList = "/cluster/ip_white_list" ;
	
	public static final String sZK_PathPtn_Auth_usersVersion = sZK_PathPtn_SysRoot+"/auth/users-version" ;
	
	/**
	 * 第一个参数是WorkEnv，第2个参数是"集群名称.计算管道id"
	 */
	public static final String sZK_SysPathPtn_CPipeJob = "/flink/{}/debug/{}" ;
	
	/**
	 * 任务的rcd信息记录路径
	 */
	public static final String sZK_SysPath_XTaskRecord = "/xtask/rcd" ;
	
	/**
	 * 系统使用的hadoop集群名称
	 */
	public static final String sZK_CommonPath_hadoop = sZK_Path_SysCommon+"/hadoop" ;
	
	public static final String sZK_CommonPath_hdfs_http = sZK_CommonPath_hadoop+"/hdfs/http" ;
	
	public static final String sZK_CommonPath_yarn_web = sZK_CommonPath_hadoop+"/yarn/web" ;
	
	public static final String sZK_CommonPath_tez_url = sZK_CommonPath_hadoop+"/tez/url" ;
	
	public static final String sZK_CommonPath_hive = sZK_CommonPath_hadoop + "/hive" ;
	
	public static final String sZK_CommonPath_hosts = sZK_Path_SysCommon + "/hosts" ;
	
	public static String sZK_SysPath_Register_XTMS = XString.msgFmt(sZK_SysPathPtn_ApiService , sAppName_XTMS) ;
	
	public static String sZK_SysPathPtn_Job = sZK_SysPath_Register_XTMS +"/{}/{}/{}" ;
	
	/**
	 * 和XTMS的连接状态
	 */
	public static String sZK_SysPathPtn_JobConnStatus = sZK_SysPath_Register_XTMS +"/{}/{}/{}/conn_status" ;
	/**
	 * 任务的动态配置
	 * /{workEnv}/{taskType}/{任务名}/dynamic_conf
	 */
	public static String sZK_SysPathPtn_JobDynamicConf = sZK_SysPath_Register_XTMS +"/{}/{}/{}/dynamic_conf" ;
	
	/**
	 * JobServer的公共配置
	 */
	public static String sZK_SysPath_JobServerCommonConf = sZK_SysPath_Register_XTMS +"/JobServer/CommonConf" ;
	
	
	/**
	 * 数据集成部分的源数据浏览功能
	 */
	public static final String sAppName_SailMSDataLens = "SailMSDataLens" ;
	
	/**
	 * 消息中心
	 */
	public static final String sAppName_SailMSMsg = "SailMSMsg" ;
	
	/**
	 * 数据资产目录
	 */
	public static final String sAppName_SailMSDataCatalog = "SailMSDataCatalog1" ;
	
	/**
	 * 数据建模
	 */
	public static final String sAppName_SailMSDataModel = "SailMSDataModel" ;
	
	/**
	 * 应用引擎
	 */
	public static final String sAppName_SailMSSAE = "SailMSSAE" ;
	
	/**
	 * 报表工具
	 */
	public static final String sAppName_SailMSReport = "SailMSReport" ;
	
	/**
	 * 智能助理
	 */
	public static final String sAppName_SailMSAi = "SailMSAi" ;
	
	public static final String sAppName_SailPyAi = "SailPyAi" ;
	
	public static final String sAppName_SailMSGateway = "SailMSGateway" ;
	
	/**
	 * 指标管理系统后台服务
	 */
	public static final String sAppName_SailMSIndicator = "SailMSIndicator" ;
	
	public static final String sAppName_SailMSDataService = "SailMSDataService" ;
	
	/**
	 * 大数据平台最基础核心服务，不依赖Hadoop生态		<br />
	 * 依赖关系数据库和Zookeeper
	 */
	public static final String sAppName_SailMSCore = "SailMSCore" ;
	
	/**
	 * Object Storage Service
	 */
	public static final String sAppName_SailMSOSS = "SailMSOSS" ;
	
	/**
	 * 应用/系统进行的操作
	 */
	public static final String sUserId_sys = "__sys__" ;
	
	public static PropertiesEx getApiServiceProperties()
	{
		PropertiesEx prop = new PropertiesEx() ;
		prop.put("company" , sCompany) ;
		prop.put("produce" , sServiceType_MS_HTTP_API) ;
		return prop ;
	}
	
	public static PropertiesEx getSailboatWebModuleProperties()
	{
		PropertiesEx prop = new PropertiesEx() ;
		prop.put("company" , sCompany) ;
		prop.put("produce" , sServiceType_SailboatWeb_Module) ;
		return prop ;
	}
	
	public static final String sKafka_TN_log_app = "_log_app" ;
	
	/**
	 * 数据建模中的数据和模式编辑日志
	 */
	public static final String sKafka_TN_log_dm_data_edit = "_log_dm_data_edit" ;
	
	/**
	 * 日志反演任务的日志
	 */
	public static final String sKafka_TN_Log_dm_deducer = "_log_dm_deducer" ;
	
	/**
	 * 智链服务的过程日志
	 */
	public static final String sKafka_TN_log_ai_chain = "_log_ai_chain" ;
	/**
	 * Flink的运行日志
	 */
	public static final String sKafka_TN_log_flink = "_log_flink" ;
	
	/**
	 * 平台运维消息主题
	 */
	public static final String sMsgTopic_sys_opt = "_msg_sys_opt" ;
	
	/**
	 * 标签路径：/智能聊天/知识库/应用场景
	 */
	public static final String sDL_KbApplyScene = "/智能聊天/知识库/应用场景" ;
	
}
