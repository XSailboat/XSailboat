package team.sailboat.bd.base;

import java.util.concurrent.TimeUnit;

import team.sailboat.base.SysConst;
import team.sailboat.bd.base.hbase.HBaseUtils;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;

/**
 * 工程常量表
 *
 * @author yyl
 * @since 2021年3月12日
 */
public interface BdConst extends SysConst
{	
	public static final String sNodeType_ZBDVirtual = "ZBDVirtual" ;
	
	public static final String sNodeType_ZBDPython = "ZBDPython" ;
	
	public static final String sNodeType_ZBDSql = "ZBDSql" ;
	
	public static final String sNodeType_ZBDRoot = "ZBDRoot" ;
	
	/**
	 * 数据集成节点
	 */
	public static final String sNodeType_ZBDDi = "ZBDDi" ;
	
	public static final String sAV_NodeTypes = sNodeType_ZBDSql+","+sNodeType_ZBDVirtual+","+sNodeType_ZBDPython+","+sNodeType_ZBDDi ;
	
	public static final JSONArray sNodeTypes_Ja = new JSONArray()
			.put(new JSONObject().put("name", sNodeType_ZBDSql).put("displayName", "ZBDSql节点"))
			.put(new JSONObject().put("name", sNodeType_ZBDPython).put("displayName", "ZBDPython节点"))
			.put(new JSONObject().put("name", sNodeType_ZBDDi).put("displayName", "数据集成节点"))
			.put(new JSONObject().put("name", sNodeType_ZBDVirtual).put("displayName", "虚拟节点")) ;
	
	public static final String sAV_InstGenWay = "Tp1,RightNow" ;
	
	public static final String sRole_Dev = "开发" ;
	public static final String sRole_Test = "测试" ;
	public static final String sRole_Ops = "运维" ;
	public static final String sRole_Admin = "空间管理员" ;
	public static final String sRole_Guest = "访客" ;
	
	public static final String[] sRoles = new String[] {sRole_Admin , sRole_Dev , sRole_Test , sRole_Ops , sRole_Guest} ;
	
	/*==================================================================
	 |
	 | HBase表名定义以及表中列簇TTL定义
	 | 
	 ===================================================================*/
	
	public static final String sHBase_TN_container_taskInstExec = "container_taskInstExec" ; 
	public static final int sHBase_TTL_container_taskInstExec__baseInfo = (int)TimeUnit.DAYS.toSeconds(7) ; 
	
	public static final String sHBase_TN_container_log = "container_log" ;
	public static final int sHBase_TTL_container_log__logInfo = (int)TimeUnit.DAYS.toSeconds(30) ;
	
	/**
	 * 源数据浏览模块的sql执行日志
	 */
	public static final String sHBase_TN_datalens_sql_exec_log = "datalens_sql_exec_log" ;
	public static final int sHBase_TTL_datalens_sql_exec_log = (int)TimeUnit.DAYS.toSeconds(30) ;
	
	public static final String sHBase_TN_zbdtask_log = "zbdtask_log" ;
	public static final int sHBase_TTL_zbdtask_log__logInfo = (int)TimeUnit.DAYS.toSeconds(30) ;
	
	public static final String sHBase_TN_app_log = "app_log" ;
	public static final int sHBase_TTL_app_log__logInfo = (int)TimeUnit.DAYS.toSeconds(30) ;
	
	public static final String sHBase_TN_app_log_apps = "app_log_apps" ;
	
	public static final String sHBase_TN_flink_log = "flink_log" ;
	public static final int sHBase_TTL_flink_log__logInfo = (int)TimeUnit.DAYS.toSeconds(30) ;
	
	public static final String sHBase_TN_ai_chain_log = "ai_chain_log" ;
	public static final int sHBase_TTL_ai_chain_log__logInfo = (int)TimeUnit.DAYS.toSeconds(30) ;
	
	
	public static final String sHBase_TN_zbdtask_plan_inst = "zbdtask_plan_inst" ;
	public static final int sHBase_TTL_zbdtask_plan_inst__baseInfo = (int)TimeUnit.DAYS.toSeconds(30) ;
	/**
	 * 任务实例表应该在ms-pivot中创建
	 */
	public static final String sHBase_TN_zbdtask_inst = "zbdtask_inst" ;
	public static final int sHBase_TTL_zbdtask_inst__baseInfo = (int)TimeUnit.DAYS.toSeconds(30) ;
//	/**
//	 * 任务实例执行摘要,此表在ms-am中创建
//	 */
//	public static final String sHBase_TN_zbdtask_inst_exec = "zbdtask_inst_exec" ;
//	public static final int sHBase_TTL_zbdtask_inst_exec__baseInfo = (int)TimeUnit.DAYS.toSeconds(30) ;
	
	/**
	 * 应用的活动记录
	 */
	public static final String sHBase_TN_app_activity_record = "app_activity_record" ;
	public static final int sHBase_TTL_app_activity_record__baseInfo = (int)TimeUnit.DAYS.toSeconds(30) ; 
	
	/*==================================================================
	 |
	 | HBase 表的列簇名定义
	 | 
	 ===================================================================*/
	
	public static final String sHBase_FN_baseInfo = "baseInfo" ; 
	public static final byte[] sHBase_FNB_baseInfo = HBaseUtils.toBytes(sHBase_FN_baseInfo) ; 
	
	public static final String sHBase_FN_logInfo = "logInfo" ;
	public static final byte[] sHBase_FNB_logInfo = HBaseUtils.toBytes(sHBase_FN_logInfo) ;
 	
	public static final String sHBase_FN_dynamicInfo = "dynamicInfo" ;
	public static final byte[] sHBase_FNB_dynamicInfo = HBaseUtils.toBytes(sHBase_FN_dynamicInfo) ;
	
	public static final String sHBase_FN_extendInfo = "extendInfo" ;
	public static final byte[] sHBase_FNB_extendInfo = HBaseUtils.toBytes(sHBase_FN_extendInfo) ;
	
	public static final String sHBase_FN_file = "file" ;
	public static final byte[] sHBase_FNB_file = HBaseUtils.toBytes(sHBase_FN_file) ;
	
	/*==================================================================
	 |
	 | HBase 表的列簇名定义
	 | 
	 ===================================================================*/
	public static final String sHBase_TVK_description = "__description" ;
	
	public static final String sHBase_TVK_colDataType = "__colDataType#" ;
	
	/*==================================================================
	 |
	 | HBase 表的列名定义
	 | 
	 ===================================================================*/
	
	public static final String sCN_nodeId = "nodeId" ;
	
	public static final String sCN_containerId = "containerId" ;
	
//	public static final String sCN_runStartTime = "runStartTime" ;
	
//	public static final String sCN_runEndTime = "runEndTime" ;
	
	public static final String sCN_precursorIds = "precursorIds" ;
	
	public static final String sCN_followerIds = "followerIds" ;
	
	public static final String sHBase_CN_bizdate = "bizdate" ;
	public static final byte[] sHBase_CNB_bizdate = HBaseUtils.toBytes(sHBase_CN_bizdate) ;
	
	public static final String sHBase_CN_id = "id" ;
	public static final byte[] sHBase_CNB_id = HBaseUtils.toBytes(sHBase_CN_id) ;
	
	public static final String sHBase_CN_publishItemId = "publishItemId" ;
	public static final byte[] sHBase_CNB_publishItemId = HBaseUtils.toBytes(sHBase_CN_publishItemId) ;
	
	public static final String sHBase_CN_editorId = "editorId" ;
	
	public static final String sHBase_CN_version = "version" ;
	
	public static final String sHBase_CN_startTime = "startTime" ;
	public static final byte[] sHBase_CNB_startTime = HBaseUtils.toBytes(sHBase_CN_startTime) ;
	
	public static final String sHBase_CN_stopTime = "stopTime" ;
	public static final byte[] sHBase_CNB_stopTime = HBaseUtils.toBytes(sHBase_CN_stopTime) ;
	
	public static final String sHBase_CN_beginRunTime = "beginRunTime" ;
	public static final byte[] sHBase_CNB_beginRunTime = HBaseUtils.toBytes(sHBase_CN_beginRunTime) ;
	
	public static final String sHBase_CN_status = "status" ;
	public static final byte[] sHBase_CNB_status = HBaseUtils.toBytes(sHBase_CN_status) ;
	
	public static final String sHBase_CN_code = "code" ;
	public static final byte[] sHBase_CNB_code = HBaseUtils.toBytes(sHBase_CN_code) ;
	
	public static final String sHBase_CN_result = "result" ;
	public static final byte[] sHBase_CNB_result = HBaseUtils.toBytes(sHBase_CN_result) ;
	
	public static final String sHBase_CN_instSource = "instSource" ;
	public static final byte[] sHBase_CNB_instSource = HBaseUtils.toBytes(sHBase_CN_instSource) ;
	
	public static final String sHBase_CN_blocked = "blocked" ;
	public static final byte[] sHBase_CNB_blocked = HBaseUtils.toBytes(sHBase_CN_blocked) ;
	
	public static final String sHBase_CN_taskInstExecId = "taskInstExecId" ;
	public static final byte[] sHBase_CNB_taskInstExecId = HBaseUtils.toBytes(sHBase_CN_taskInstExecId) ;
	
	public static final String sHBase_CN_body = "body" ;
	public static final byte[] sHBase_CNB_body = HBaseUtils.toBytes(sHBase_CN_body) ;
	
	public static final String sHBase_CN_dbName = "dbName" ;
	
	public static final String sHBase_CN_source = "source" ;
	
	public static final String sHBase_CN_sourceFileName = "sourceFileName" ;
	
	public static final String sHBase_CN_userId = "userId" ;
	public static final byte[] sHBase_CNB_userId = HBaseUtils.toBytes(sHBase_CN_userId) ;
	
	public static final String sHBase_CN_wsName = "wsName" ;
	public static final byte[] sHBase_CNB_wsName = HBaseUtils.toBytes(sHBase_CN_wsName) ;
	
	public static final String sHBase_CN_nodeAmount = "nodeAmount" ;
	public static final byte[] sHBase_CNB_nodeAmount = HBaseUtils.toBytes(sHBase_CN_nodeAmount) ;
	
	public static final String sHBase_CN_valveAmount = "valveAmount" ;
	public static final byte[] sHBase_CNB_valveAmount = HBaseUtils.toBytes(sHBase_CN_valveAmount) ;
	
	public static final String sHBase_CN_nodes = "nodes" ;
	public static final byte[] sHBase_CNB_nodes = HBaseUtils.toBytes(sHBase_CN_nodes) ;
	
	public static final String sHBase_CN_node = "node" ;
	public static final byte[] sHBase_CNB_node = HBaseUtils.toBytes(sHBase_CN_node) ;
	
	public static final String sHBase_CN_nodeId = "nodeId" ;
	public static final byte[] sHBase_CNB_nodeId = HBaseUtils.toBytes(sHBase_CN_nodeId) ;
	
	public static final String sHBase_CN_timestamp = "timestamp" ;
	public static final byte[] sHBase_CNB_timestamp = HBaseUtils.toBytes(sHBase_CN_timestamp) ;
	
	public static final String sHBase_CN_amount = "amount" ;
	
	public static final String sHBase_CN_data = "data" ;
	public static final byte[] sHBase_CNB_data = HBaseUtils.toBytes(sHBase_CN_data) ;
	
	/**
	 * 启动次数
	 */
	public static final String sHBase_CN_startTimes = "startTimes" ;
	public static final byte[] sHBase_CNB_startTimes = HBaseUtils.toBytes(sHBase_CN_startTimes) ;
	
	public static final String sHBase_CN_host = "host" ;
	public static final byte[] sHBase_CNB_host = HBaseUtils.toBytes(sHBase_CN_host)  ;
	
	public static final String sHBase_CN_sysEnv = "sysEnv" ;
	public static final byte[] sHBase_CNB_sysEnv = HBaseUtils.toBytes(sHBase_CN_sysEnv) ;
	
	public static final String sHBase_CN_appName = "appName" ;
	public static final byte[] sHBase_CNB_appName = HBaseUtils.toBytes(sHBase_CN_appName) ;
	
	public static final String sHBase_CN_name = "name" ;
	public static final byte[] sHBase_CNB_name = HBaseUtils.toBytes(sHBase_CN_name) ;
	
	public static final String sHBase_CN_flowId = "flowId" ;
	public static final byte[] sHBase_CNB_flowId = HBaseUtils.toBytes(sHBase_CN_flowId) ;
	
	public static final String sHBase_CN_description = "description" ;
	public static final byte[] sHBase_CNB_description = HBaseUtils.toBytes(sHBase_CN_description) ;
	
	public static final String sHBase_CN_content = "content" ;
	public static final byte[] sHBase_CNB_content = HBaseUtils.toBytes(sHBase_CN_content) ;
	
	public static final String sHBase_CN_type = "type" ;
	public static final byte[] sHBase_CNB_type = HBaseUtils.toBytes(sHBase_CN_type) ;
	
	public static final String sHBase_CN_configuration = "configuration" ;
	public static final byte[] sHBase_CNB_configuration = HBaseUtils.toBytes(sHBase_CN_configuration) ;
	
	public static final String sHBase_CN_input = "input" ;
	public static final byte[] sHBase_CNB_input = HBaseUtils.toBytes(sHBase_CN_input) ;
	
	public static final String sHBase_CN_output = "output" ;
	public static final byte[] sHBase_CNB_output = HBaseUtils.toBytes(sHBase_CN_output) ;
	
	public static final String sHBase_CN_createTime = "createTime" ;
	public static final byte[] sHBase_CNB_createTime = HBaseUtils.toBytes(sHBase_CN_createTime) ;
	
	public static final String sHBase_CN_lastEditTime = "lastEditTime" ;
	public static final byte[] sHBase_CNB_lastEditTime = HBaseUtils.toBytes(sHBase_CN_lastEditTime) ;
	
	public static final String sHBase_CN_commitTime = "commitTime" ;
	public static final byte[] sHBase_CNB_commitTime = HBaseUtils.toBytes(sHBase_CN_commitTime) ;
	
	public static final String sHBase_CN_commitUserId = "commitUserId" ;
	public static final byte[] sHBase_CNB_commitUserId = HBaseUtils.toBytes(sHBase_CN_commitUserId) ;
	
	public static final String sHBase_CN_commitType = "commitType" ;
	public static final byte[] sHBase_CNB_commitType = HBaseUtils.toBytes(sHBase_CN_commitType) ;
	
	public static final String sHBase_CN_context = "context" ;
	public static final byte[] sHBase_CNB_context = HBaseUtils.toBytes(sHBase_CN_context) ;
	
	public static final String sHBase_CN_arguments = "arguments" ;
	public static final byte[] sHBase_CNB_arguments = HBaseUtils.toBytes(sHBase_CN_arguments) ;
	
	/**
	 * 阀
	 */
	public static final String sHBase_CN_valves = "valves" ;
	public static final byte[] sHBase_CNB_valves = HBaseUtils.toBytes(sHBase_CN_valves) ;
	
	public static final String sHBase_CN_value = "value" ;
	public static final byte[] sHBase_CNB_value = HBaseUtils.toBytes(sHBase_CN_value) ;
	
	public static final String sHBase_CN_progress = "progress" ;
	public static final byte[] sHBase_CNB_progress = HBaseUtils.toBytes(sHBase_CN_progress) ;
	
	public static final String sHBase_CN_runStatus = "runStatus" ;
	public static final byte[] sHBase_CNB_runStatus = HBaseUtils.toBytes(sHBase_CN_runStatus) ;
	
	public static final String sHBase_CN_precursorIds = "precursorIds" ;
	public static final byte[] sHBase_CNB_precursorIds = HBaseUtils.toBytes(sHBase_CN_precursorIds) ;
	
	public static final String sHBase_CN_followerIds = "followerIds" ;
	public static final byte[] sHBase_CNB_followerIds = HBaseUtils.toBytes(sHBase_CN_followerIds) ;
	
	public static final String sHBase_CN_runWithNoLoad = "runWithNoLoad" ;
	public static final byte[] sHBase_CNB_runWithNoLoad = HBaseUtils.toBytes(sHBase_CN_runWithNoLoad) ;
	
	public static final String sHBase_CN_startScheduleTime = "startScheduleTime" ;
	public static final byte[] sHBase_CNB_startScheduleTime = HBaseUtils.toBytes(sHBase_CN_startScheduleTime) ;
	
	public static final String sHBase_CN_schedule = "schedule" ;
	public static final byte[] sHBase_CNB_schedule = HBaseUtils.toBytes(sHBase_CN_schedule) ;
	
	public static final String sHBase_CN_timeout = "timeout" ;
	public static final byte[] sHBase_CNB_timeout = HBaseUtils.toBytes(sHBase_CN_timeout) ;
	
	public static final String sHBase_CN_nodeType = "nodeType" ;
	public static final byte[] sHBase_CNB_nodeType = HBaseUtils.toBytes(sHBase_CN_nodeType) ;
	
	/*==================================================================
	 |
	 | 文件路径定义
	 | 
	 ===================================================================*/
	
	public static final String sFS_Path_pivot = "/user/zbd/pivot" ;
	
	public static final String sFS_Path_works = "/user/zbd/works" ;
	
	public static final String sFS_Path_pytask = sFS_Path_pivot + "/_engine_/pytask" ;
	
	/**
	 * 检查点保存路径，第一个参数是工作空间名称，第2个参数是“资源队列#label”
	 */
	public static final String sFS_PathPtn_CheckPoints_prod = sFS_Path_pivot + "/{}/flink/checkpoints/{}" ;
	
	public static final String sFS_PathPtn_CheckPoints_dev = sFS_Path_works + "/{}/flink/checkpoints/{}" ;
	
	/**
	 * 保存点保存路径，第一个参数是工作空间名称，第2个参数是“资源队列#label”
	 */
	public static final String sFS_PathPtn_SavePoints_prod = sFS_Path_pivot + "/{}/flink/savepoints/{}" ;
	public static final String sFS_PathPtn_SavePoints_dev = sFS_Path_works + "/{}/flink/savepoints/{}" ;
	
	public static final String sFS_PathPtn_works_res = sFS_Path_works + "/{}/prePublish/resources" ;
	
	public static final String sFS_PathPtn_works_jars = sFS_Path_works + "/{}/prePublish/jars" ;
	
	public static final String sFS_PathPtn_pivot_res = sFS_Path_pivot + "/{}/resources" ;
	
	public static final String sFS_PathPtn_pivot_jars = sFS_Path_pivot + "/{}/jars" ;
	
	public static final String sFS_Path_exec_engine = sFS_Path_pivot + "/_engine_/sailboat" ; 
	
	public static final String sFS_Name_resources = "resources" ;
	
	public static final String sFS_Path_flink_product = "/user/flink/app" ;
	
	public static final String sFS_Path_flink_extJars = "/user/flink/ext_jars" ;
	
	public static final String sFS_Path_datamodel = "/user/zbd/datamodel" ;
	
	/**
	 * 里面存储数据建模反演出的各个版本的数据
	 */
	public static final String sFS_Path_dm_store = sFS_Path_datamodel + "/store" ;
	
	/**
	 * 应用引擎 目录
	 */
	public static final String sFS_Path_SAE = "/user/zbd/sae" ;
	
	/**
	 * 应用引擎的 应用仓库
	 */
	public static final String sFS_Path_SAE_AppStore = sFS_Path_SAE + "/appstore" ;
	
	/**
	 * 数据源附件地址：workenv/dsId
	 */
	public static final String sFS_PathPtn_DS_Attachments = "/user/zbd/di/ds_attachments/{}/{}" ;
	
	/*==================================================================
	 |
	 | 工作空间的关系数据库(MySql)表
	 | 
	 ===================================================================*/
//	public static final String sRDB_TN_WS_FlowValve = "flow_valve" ;
//	public static final String sRDB_TN_WS_FlowNode = "flow_node" ;
	
	/*==================================================================
	 |
	 | Hadoop配置文件的常量
	 | 
	 ===================================================================*/
	public static final String sYARN_WebAddr1 = "yarn.resourcemanager.webapp.address.rm1" ;
	
	public static final String sYARN_WebAddr2 = "yarn.resourcemanager.webapp.address.rm2" ;
}
