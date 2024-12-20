package team.sailboat.ms.crane.cmd;

import java.util.Map;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.lang.Assert;

/**
 * 本地命令集
 *
 * @author yyl
 * @since 2024年10月19日
 */
public class LocalCmds
{
	
	static final CmdEnv sEnv = new CmdEnv() ;
	
	static final Map<String , Class<? extends ICmd>> sCmdMap = XC.hashMap(
			"xc_upload" , Xc_upload.class
			, "xc_conf_iptable" , Xc_conf_iptable.class
			, "xc1_to_zk" , Xc1_to_zk.class
			) ;
	
	/**
	 * 取得命令执行环境
	 * @return
	 */
	public static CmdEnv getEnv()
	{
		return sEnv ;
	}
	
	/**
	 * 取得指定名称的命令对象
	 * @param aCmdName
	 * @return
	 */
	public static ICmd getCmd(String aCmdName)
	{
		Class<? extends ICmd> cmdClass = sCmdMap.get(aCmdName) ;
		Assert.notNull(cmdClass , "无效的本地命令：%s" , aCmdName) ;
		try
		{
			return cmdClass.getConstructor().newInstance();
		}
		catch (Exception e)
		{
			WrapException.wrapThrow(e );
		}
		return null ;			// dead code
	}
	
	/**
	 * 
	 * 本地命令集是否支持
	 * 
	 * @param aCmd
	 * @return
	 */
	public static boolean support(String aCmd)
	{
		return sCmdMap.get(aCmd) != null ;
	}
	
	/**
	 * 是否是本地执行一次的命令
	 * @param aCmd
	 * @return
	 */
	public static boolean isLocalOne(String aCmd)
	{
		return aCmd != null && aCmd.startsWith("xc1_") ;
	}
}
