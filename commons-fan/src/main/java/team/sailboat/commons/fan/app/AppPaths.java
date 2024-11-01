package team.sailboat.commons.fan.app;

import java.io.File;

import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.sys.JEnvKit;
import team.sailboat.commons.fan.text.XString;

public class AppPaths implements IAppPaths
{
	String mAppCategory = sAppCategory_MicroService ;
	
	String mAppDirName ;
	String mMainConfigFileName ;
	String mMainLogFileName ;
	
	File mRootDir ;
	File mConfigDir ;
	File mCommonConfigDir ;
	File mTempDir ;
	File mDataDir ;
	File mLogDir ;
	File mBinDir ;
	File mMainConfigFile ;
	File mMainLogFile ;
	
	public AppPaths()
	{
	}
	
	void setAppCategory(String aAppCategory)
	{
		if(JCommon.unequals(mAppCategory, aAppCategory))
		{
			mAppCategory = aAppCategory;
			mRootDir = null ;
			mConfigDir = null ;
			mCommonConfigDir = null ;
			mTempDir = null ;
			mDataDir = null ;
			mLogDir = null ;
			mMainConfigFile = null ;
			mMainLogFile = null ;
		}
	}
	
	public File getRootDir()
	{
		if(mRootDir == null)
		{
			mRootDir = new File(JEnvKit.getRunDir()) ;
			if("bin".equals(mRootDir.getName()))
				mRootDir = mRootDir.getParentFile() ;
		}
		return mRootDir ;
	}
	
	public File getCommonConfigFile(String aFileName)
	{
		return new File(getCommonConfigDir() , aFileName) ;
	}
	
	public String getAppDirName()
	{
		if(mAppDirName == null)
		{
			AppPathConfig pathCfg = (AppPathConfig)AppContext.get(ACKeys.sAppPathConfig) ;
			Assert.notNull(pathCfg , "应用目录名没有设置") ;
			mAppDirName = pathCfg.getAppDirName() ;
		}
		return mAppDirName ;
	}
	
	public String getMainConfigFileName()
	{
		if(mMainConfigFileName == null)
		{
			AppPathConfig pathCfg = (AppPathConfig)AppContext.get(ACKeys.sAppPathConfig) ;
			Assert.notNull(pathCfg , "应用目录名没有设置") ;
			mMainConfigFileName = pathCfg.getMainConfigFileName() ;
		}
		return mMainConfigFileName ;
	}
	
	public String getMainLogFileName()
	{
		if(mMainLogFileName == null)
		{
			AppPathConfig pathCfg = (AppPathConfig)AppContext.get(ACKeys.sAppPathConfig) ;
			Assert.notNull(pathCfg , "应用目录名没有设置") ;
			mMainLogFileName = pathCfg.getMainLogFileName() ;
		}
		return mMainLogFileName ;
	}
	
	@Override
	public File getConfigDir()
	{
		if(mConfigDir == null)
		{
			mConfigDir = new File(getRootDir() , XString.msgFmt("config/{}/{}" , mAppCategory , getAppDirName())) ;
		}
		return mConfigDir ;
	}
	
	public File getCommonConfigDir()
	{
		if(mCommonConfigDir == null)
		{
			mCommonConfigDir = new File(getRootDir() , XString.msgFmt("config/{}/common" , mAppCategory)) ;
		}
		return mCommonConfigDir ;
	}
	
	public File getTempDir()
	{
		if(mTempDir == null)
		{
			mTempDir = new File(getRootDir() , XString.msgFmt("temp/{}/{}" , mAppCategory , getAppDirName())) ;
			mTempDir.mkdirs() ;
		}
		return mTempDir ;
	}
	
	public File getDataDir()
	{
		if(mDataDir == null)
			mDataDir = new File(getRootDir() , XString.msgFmt("data/{}/{}" , mAppCategory , getAppDirName())) ;
		return mDataDir ;
	}
	
	public File getMainConfigFile()
	{
		if(mMainConfigFile == null)
			mMainConfigFile = getConfigFile(getMainConfigFileName()) ;
		return mMainConfigFile ;
	}
	public void setMainConfigFile(File aMainConfigFile)
	{
		mMainConfigFile = aMainConfigFile;
	}
	
	public File getLogDir()
	{
		if(mLogDir == null)
			mLogDir = new File(getRootDir() , XString.msgFmt("log/{}/{}" , mAppCategory , getAppDirName())) ;
		return mLogDir ;
	}
	
	public File getBinDir()
	{
		if(mBinDir == null)
			mBinDir = new File(getRootDir() , "bin") ;
		return mBinDir ;
	}
	
	public File getMainLogFile()
	{
		if(mMainLogFile == null)
			mMainLogFile = new File(getLogDir(), "log/") ;
		return mMainLogFile ;
	}
	
}
