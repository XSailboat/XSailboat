package team.sailboat.commons.fan.app;

import java.io.File;
import java.io.IOException;

import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.file.TempFileManager;

public interface IAppPaths
{
	public static String sAppCategory_MicroService = "MicroService" ;
	
	public static String sAppCategory_XTask = "XTask" ;
	
	File getConfigDir() ;
	
	default File getConfigFile(String aFileName)
	{
		return new File(getConfigDir() , aFileName) ;
	}
	
	File getDataDir() ;
	
	default File getDataFile(String aFileName)
	{
		return new File(getDataDir() , aFileName) ;
	}
	
	File getLogDir() ;
	
	default File getLogFile(String aFileName)
	{
		return new File(getLogDir() , aFileName) ;
	}
	
	File getTempDir() ;
	
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
