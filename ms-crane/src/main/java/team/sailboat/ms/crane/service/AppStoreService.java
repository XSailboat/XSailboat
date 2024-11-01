package team.sailboat.ms.crane.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import team.sailboat.commons.fan.app.AppPaths;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.ms.MSApp;
import team.sailboat.ms.crane.bean.AppPkg;
import team.sailboat.ms.crane.cmd.LocalCmds;

@Service
public class AppStoreService
{
	/**
	 * 程序包的放置目录
	 */
	File mAppPkgDir ;
	
	@PostConstruct
	void _init()
	{
		AppPaths appPaths = MSApp.instance().getAppPaths() ;
		mAppPkgDir = new File(appPaths.getDataDir() , "appStore") ;
		LocalCmds.getEnv().setAppPkgFileGetter(this::getAppPkgFile) ;
	}
	
	/**
	 * 取得程序包放置目录下的所有应用软件包
	 * @return
	 */
	public List<AppPkg> getAppPkgs()
	{
		if(!mAppPkgDir.exists())
			return Collections.emptyList() ;
		List<AppPkg> pkgList = XC.arrayList() ;
		for(File file : mAppPkgDir.listFiles())
		{
			if(file.isFile())
			{
				pkgList.add(AppPkg.builder()
						.name(file.getName())
						.fileLen(file.length())
						.updateTime(new Date(file.lastModified()))
						.build()) ;
			}
		}
		return pkgList ;
	}
	
	/**
	 * 取得指定名称的应用软件包
	 * @param aName	应用软件包名
	 * @return
	 */
	public AppPkg getAppPkgByName(String aName)
	{
		File file = new File(mAppPkgDir , aName) ;
		Assert.isTrue(file.exists() , "程序包[%s]不存在！" , aName) ;
		return AppPkg.builder()
				.name(file.getName())
				.fileLen(file.length())
				.updateTime(new Date(file.lastModified()))
				.build() ;
	}
	
	/**
	 * 取得程序包文件
	 * @param aName
	 * @return
	 */
	public File getAppPkgFile(String aName)
	{
		return new File(mAppPkgDir , aName) ;
	}
	
	/**
	 * 上传一个程序包
	 * @param aIns		软件包的数据流，用完关闭
	 * @param aName		应用软件包名
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public void uploadAppPkg(InputStream aIns , String aName) throws Exception
	{
		File file = new File(mAppPkgDir , aName) ;
		StreamAssist.transfer_cc(aIns, FileUtils.openOStream(file));
	}
	
	public void deleteAppPkg(String aName)
	{
		File file = new File(mAppPkgDir , aName) ;
		if(file.exists())
			file.delete() ;
	}
}
