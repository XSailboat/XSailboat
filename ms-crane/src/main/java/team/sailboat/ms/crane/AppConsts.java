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
	
	/**
	 * 代理安装程序上的接口。执行命令
	 */
	public static final String sApi_ExecCommands_POST = "/core/exec/commands" ;
	
	/**
	 * 上传文件到SailPyInstaller
	 */
	public static final String sApi_UploadFile_POST = "/core/upload" ;
	
	public static final String sDefaultProcedureCatalog = "未分类" ;
}