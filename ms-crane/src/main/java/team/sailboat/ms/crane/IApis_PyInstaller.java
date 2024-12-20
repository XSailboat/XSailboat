package team.sailboat.ms.crane;

/**
 * 
 * SailPyInstaller的路径
 *
 * @author yyl
 * @since 2024年11月16日
 */
public interface IApis_PyInstaller
{
	/**
	 * 
	 * 上传防火墙配置。全局规划信息
	 */
	public static final String sPOST_UploadIptableConf = "/iptables/conf/one/_createOrUpdate" ;
	
	/**
	 * 文件形式上传防火墙配置。全局规划信息
	 */
	public static final String sPOST_UploadIptableConfFile = "/iptables/conf/file/one/_createOrUpdate" ;
	
	/**
	 * 执行/应用当前SailPyInstaller上的防火墙配置。
	 */
	public static final String sPOST_ExecIpTableConf = "/iptables/conf/_exec" ;
	
	/**
	 * 代理安装程序上的接口。执行命令
	 */
	public static final String sPOST_ExecCommand = "/core/command/many/_exec" ;
	
	/**
	 * 创建或更新主机的规划配置信息
	 */
	public static final String sPOST_CreateOrUpdateHostProfile = "/core/hostProfile/one/_createOrUpdate" ;
	
	/**
	 * 上传文件到SailPyInstaller
	 */
	public static final String sPOST_UploadFile = "/file/one" ;
	
	/**
	 * 验证系统的用户名明码
	 */
	public static final String sPOST_ValidateUserAndPswd = "/user/password/_validate" ;
	
	/**
	 * 取得一个动态RSA密钥对的公钥部分
	 */
	public static final String sGET_RSAPublicKey = "/user/rsa/public_key" ;
}
