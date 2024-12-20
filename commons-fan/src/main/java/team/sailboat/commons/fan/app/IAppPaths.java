package team.sailboat.commons.fan.app;

import java.io.File;
import java.io.IOException;

import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.file.TempFileManager;

/**
 * 
 * 我们定义的标准产品目录/程序运行目录结构
 *
 * @author yyl
 * @since 2024年12月6日
 */
public interface IAppPaths
{
	public static String sAppCategory_MicroService = "MicroService" ;
	
	public static String sAppCategory_XTask = "XTask" ;
	
	/**
     * 获取当前App的配置文件的目录。
     * 
     * @return 配置文件的目录（File类型）
     */
	File getConfigDir() ;
	
	/**
     * 获取当前App配置文件目录下的指定名称（或相对路径）的文件或目录文件
     * 
     * @param aFileName 文件名或相对路径
     * @return 配置文件或目录（File类型）
     */
	default File getConfigFile(String aFileName)
	{
		return new File(getConfigDir() , aFileName) ;
	}
	
	/**
     * 获取当前App的数据文件的目录。
     * 
     * @return 数据文件的目录（File类型）
     */
	File getDataDir() ;
	
	/**
	 * 
	 * 获取当前App的数据文件目录下的指定名称（或相对路径）的文件或目录文件
	 * 
	 * @param aFileName 文件名或相对路径
     * @return 数据文件或目录
	 */
	default File getDataFile(String aFileName)
	{
		return new File(getDataDir() , aFileName) ;
	}
	
	/**
     * 获取当前App的日志文件的目录。
     * 
     * @return 日志文件的目录（File类型）
     */
	File getLogDir() ;
	
	/**
     * 根据文件名获取日志文件的全路径。
     * 
     * @param aFileName 文件名或相对路径
     * @return 日志文件或目录
     */
	default File getLogFile(String aFileName)
	{
		return new File(getLogDir() , aFileName) ;
	}
	
	/**
     * 获取临时文件的目录。
     * 
     * @return 临时文件的目录（File类型）
     */
	File getTempDir() ;
	
	/**
     * 根据文件名获取临时文件的全路径，并尝试使用TempFileManager来管理临时文件。
     * 如果TempFileManager实例不存在，则创建一个新的实例并设置到AppContext中。
     * 
     * @param aFileName 文件名或相对路径
     * @return 日志文件或目录
     */
	default File getTempFile(String aFileName)
	{
		final String  key = "TempFileManager_"+getTempDir().getName()  ;
		TempFileManager tfm = AppContext.get(key , TempFileManager.class) ;
		if(tfm == null)
		{
			try
			{
				tfm = new TempFileManager(getTempDir()) ;
				AppContext.set(key, tfm) ;
			}
			catch (IOException e)
			{
				WrapException.wrapThrow(e) ;
			}
			
		}
		return tfm.createTempFileWithFileName(aFileName) ;
	}
}
