package team.sailboat.bd.base.def;

import java.util.Set;

import team.sailboat.commons.fan.collection.XC;

/**
 * 流程节点的变量的类型
 *
 * @author yyl
 * @since 2023年5月20日
 */
public class FlowNodeSysParams
{
	/**
	 * 业务日期
	 */
	public static final String sBizdate = "sys_bizdate" ;
	
	/**
	 * 流程id
	 */
	public static final String sFlowId = "sys_flowid" ;
	
	/**
	 * 节点id
	 */
	public static final String sNodeId = "sys_nodeid" ;
	
	/**
	 * 任务实例id
	 */
	public static final String sTaskId = "sys_taskid" ;
	
	static final Set<String> sParams = XC.linkedHashSet(sBizdate
			, sFlowId
			, sNodeId
			, sTaskId) ;
	
	public static boolean isSysParam(String aParamName)
	{
		return sParams.contains(aParamName) ;
	}
}
