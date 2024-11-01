package team.sailboat.base.def;

public interface IApis_SailMSPivot
{
	public static final String sPOST_DeployCPipe = "/flink/cpipe/dev/_deploy" ;
	
	/**
	 * 取得计算管道及其节点的版本信息
	 */
	public static final String sGET_CPipeVersions = "/cpipe/one/version/all" ;
	
	/**
	 * 提交计算管道
	 */
	public static final String sPOST_CommitCPipe = "/cpipe/one/_commit" ;
	
	/**
	 * 取得多个任务实例的最新执行报告
	 */
	public static final String sPOST_GetTaskInstExecReports = "/taskInst/execReport/many/_get" ;
}
