package team.sailboat.ms.crane;

/**
 * 应用常量表
 *
 * @author yyl
 * @since 2024年10月11日
 */
public interface AppConsts
{
	
	public static final String sAppName = "SailMSCrane" ;
	
	/**
	 * 应用的中文名
	 */
	public static final String sAppCnName = "平台安装工具" ;
	
	public static final String sAppDesc = "平台安装工具。"  ;
	
	public static final String sFN_AppConfig = "config.ini" ;
	
	public static final String sLogFileName = "service.log" ;
	
	public static final String sAppVer = "1.0.0" ;
	
	public static final String sAppDirName = sAppName ;
	
	public static final String sSysLogName = "System" ;
	
	public static final String sAccessLogName = "AccessLog" ;
	
	public static final String sDefaultProcedureCatalog = "未分类" ;
	
	/**
	 * 程序运行状态数据存储文件
	 */
	public static final String sFN_RunData = "runData.rcd" ;
	
	/**
	 * 特定主机的规划信息同步状态。		<br />
	 * 已经改变了，没有同步到相应主机
	 */
	public static final String sHostProfile_SyncStatus_changed = "changed" ;
	
	/**
	 * 特定主机的规划信息同步状态。		<br />
	 * 已同步
	 */
	public static final String sHostProfile_SyncStatus_sync = "sync" ;
}