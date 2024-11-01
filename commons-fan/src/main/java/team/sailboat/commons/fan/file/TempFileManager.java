package team.sailboat.commons.fan.file;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.util.regex.Pattern;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.fan.sys.JEnvKit;

public class TempFileManager implements Closeable
{
	private static final Object sMutex = new Object() ;
	static TempFileManager sInstance ;
	static final Pattern sPoolDirPtn = Pattern.compile("Pool_\\d+") ;
	static final String sLockFileName = "live.lock" ;
	
	public static TempFileManager getInstance()
	{
		if(sInstance == null)
		{
			synchronized(sMutex)
			{
				if(sInstance == null)
				{
					try
					{
						sInstance = new TempFileManager() ;
					}
					catch (IOException e)
					{
						throw new IllegalStateException(e) ;
					}
				}
			}
		}
		return sInstance ;
	}
	
	File mTempDir ;
	int mPid ;
	FileOutputStream mFouts ;
	
	protected TempFileManager() throws IOException
	{
		this(getDefaultTempRootDir()) ;
 	}
	
	public TempFileManager(File aTmpRootDir) throws IOException
	{
		clearDeadTempDir(aTmpRootDir);
		mPid = JEnvKit.getPID() ;
		mTempDir = new File(aTmpRootDir , "Pool_"+Integer.toString(mPid)) ;
		Assert.isNotTrue(mTempDir.exists(), "已经存在缓存池目录[%s]" , mTempDir.getPath());
		mTempDir.mkdirs() ;
		mFouts = new FileOutputStream(new File(mTempDir , sLockFileName)) ;
		mFouts.getChannel().tryLock() ;
	}
	
	private static File getDefaultTempRootDir()
	{
		return FileUtils.getOsTempDir() ;
	}
	
	private void clearDeadTempDir(File aTempRootDir)
	{
		File[] files = aTempRootDir.listFiles() ;
		if(XC.isEmpty(files))
			return ;
		for(File dir : files)
		{
			if(!dir.isDirectory())
				continue ;
			if(!sPoolDirPtn.matcher(dir.getName()).matches())
				continue ;
			File file = new File(dir , sLockFileName) ;
			if(!file.exists() || !file.isFile())
				continue ;
			try(FileOutputStream fouts = new FileOutputStream(file))
			{
				FileLock lock = fouts.getChannel().tryLock() ; 
				if(lock == null)
					continue ;
				lock.release();
			}
			catch (IOException e)
			{
				continue ;
			}
			FileUtils.deleteFile(dir);
		}
	}
	
	@Override
	public void close()
	{
		StreamAssist.close(mFouts) ;
	}
	
	@Override
	protected void finalize() throws Throwable
	{
		close();
//		super.finalize();
	}
	
	public File createTempFile() throws IOException
	{
		return File.createTempFile("XTFrame" , null , mTempDir) ;
	}
	
	public File createTempFile(String aPrefix) throws IOException
	{
		return File.createTempFile(aPrefix, null, mTempDir) ;
	}
	
	public File createTempFileWithFileName(String aFileName)
	{
		return new File(mTempDir , aFileName) ;
	}
	
	public File createTempFile(String aPrefix , String aSuffix) throws IOException
	{
		return File.createTempFile(aPrefix, aSuffix, mTempDir) ;
	}
}
