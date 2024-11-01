package team.sailboat.commons.fan.app;

import java.io.File;

import team.sailboat.commons.fan.file.FileType;

public interface IPathSupport extends IAppPaths
{
	
	IPathSupport wholeIn(String aBranch) ;
	
	IPathSupport dataIn(String aBranch) ;
	
	IPathSupport configIn(String aBranch) ;
	
	IPathSupport logIn(String aBranch) ;
	
	IPathSupport tempIn(String aBranch) ;
	
	/**
	 * 
	 * @param aFileName
	 * @param aFileType
	 * <ul>	
	 * 		<li>0表示不存在时不需要创建，返回一个不存在的文件</li>
	 *      <li>IXFileDescription.sRegFile，如果不存在就创建常规文件</li>
	 *      <li>IXFileDescription.sDirectory，如果不存在就创建目录</li>
	 * 	</ul>
	 * @return
	 * @throws Y_Exception
	 */
	File getConfigFile(String aFileName , FileType aFileType) ;
	
	/**
	 * @param aFileName
	 * @param aFileType
	 *  <ul>	
	 * 		<li>0表示不存在时不需要创建，返回一个不存在的文件</li>
	 *      <li>IXFileDescription.sRegFile，如果不存在就创建常规文件</li>
	 *      <li>IXFileDescription.sDirectory，如果不存在就创建目录</li>
	 * 	</ul>
	 * @return
	 */
	File getDataFile(String aFileName , FileType aFileType) ;
	
	/**
	 * 
	 * @param aFileName
	 * @param aFileType
	 *  <ul>	
	 * 		<li>0表示不存在时不需要创建，返回一个不存在的文件</li>
	 *      <li>IXFileDescription.sRegFile，如果不存在就创建常规文件</li>
	 *      <li>IXFileDescription.sDirectory，如果不存在就创建目录</li>
	 * 	</ul>
	 * @return
	 * @throws Y_Exception
	 */
	File getLogFile(String aFileName , FileType aFileType) ;
}
