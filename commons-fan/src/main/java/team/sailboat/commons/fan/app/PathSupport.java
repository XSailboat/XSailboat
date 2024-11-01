package team.sailboat.commons.fan.app;

import java.io.File;
import java.io.IOException;

import team.sailboat.commons.fan.file.FileType;

public class PathSupport implements IPathSupport
{
	File mConfigDir ;
	File mDataDir ;
	File mLogDir ;
	File mTempDir ;
	
	public PathSupport(IAppPaths aAppPaths)
	{
		mConfigDir = aAppPaths.getConfigDir() ;
		mDataDir = aAppPaths.getDataDir() ;
		mLogDir = aAppPaths.getLogDir() ;
		mTempDir = aAppPaths.getTempDir() ;
	}
	
	public PathSupport(File aCfgDir , File aDataDir , File aLogDir , File aTempDir)
	{
		mConfigDir = aCfgDir ;
		mDataDir = aDataDir ;
		mLogDir = aLogDir ;
		mTempDir = aTempDir ;
	}
	
	public PathSupport(IPathSupport aParentPvd , String aBranch)
	{
		mConfigDir = new File(aParentPvd.getConfigDir() , aBranch) ;
		mDataDir = new File(aParentPvd.getDataDir() , aBranch) ;
		mLogDir = new File(aParentPvd.getLogDir() , aBranch) ;
		mTempDir = new File(aParentPvd.getTempDir() , aBranch) ;
	}
	
	@Override
	public PathSupport wholeIn(String aBranch)
	{
		return new PathSupport(this, aBranch) ;
	}
	
	@Override
	public IPathSupport dataIn(String aBranch)
	{
		PathSupport clone = clone() ;
		clone.mDataDir = new File(clone.mDataDir , aBranch) ;
		return clone ;
	}
	
	@Override
	public IPathSupport configIn(String aBranch)
	{
		PathSupport clone = clone() ;
		clone.mConfigDir = new File(clone.mConfigDir , aBranch) ;
		return clone ;
	}
	
	@Override
	public IPathSupport logIn(String aBranch)
	{
		PathSupport clone = clone() ;
		clone.mLogDir = new File(clone.mLogDir , aBranch) ;
		return clone ;
	}
	
	@Override
	public IPathSupport tempIn(String aBranch)
	{
		PathSupport clone = clone() ;
		clone.mTempDir = new File(clone.mTempDir , aBranch) ;
		return clone ;
	}
	
	@Override
	public File getConfigFile(String aFileName, FileType aFileType)
	{
		return getFile(mConfigDir, aFileName, aFileType) ;
	}
	
	@Override
	public File getDataFile(String aFileName, FileType aFileType)
	{
		return getFile(mDataDir, aFileName, aFileType) ;
	}

	@Override
	public File getLogDir()
	{
		return mLogDir ;
	}
	
	@Override
	public File getLogFile(String aFileName , FileType aFileType)
	{
		return getFile(mLogDir, aFileName, aFileType) ;
	}
	
	@Override
	public File getTempFile(String aFileName)
	{
		return new File(mTempDir, aFileName) ;
	}
	
	@Override
	public File getTempDir()
	{
		return mTempDir ;
	}

	public PathSupport clone()
	{
		return new PathSupport(mConfigDir, mDataDir, mLogDir , mTempDir) ;
	}
	
	static File getFile(File aParent , String aFileName , FileType aFileType)
	{
		File file = new File(aParent , aFileName) ;
		if(!file.exists() && aFileType != null)
		{
			switch(aFileType)
			{
			case RegFile:
				File pfile = file.getParentFile() ;
				try
				{
					if(!pfile.exists())
						pfile.mkdirs() ;
					file.createNewFile() ;
				}
				catch (SecurityException e)
				{
					throw new IllegalStateException("创建文件["+file.getAbsolutePath()+"]失败，不具备必需的权限") ;
				}
				catch (IOException e)
				{
					throw new IllegalStateException("创建文件["+file.getAbsolutePath()+"]失败,异常消息："+e.getMessage()) ;
				}
				break ;
			case Directory:
				file.mkdirs() ;
			default:
				throw new IllegalArgumentException("不支持的FileType："+aFileType) ;
			}
				
		}
		return file ;
	}

	@Override
	public File getConfigDir()
	{
		return mConfigDir ;
	}

	@Override
	public File getDataDir()
	{
		return mDataDir ;
	}
}
