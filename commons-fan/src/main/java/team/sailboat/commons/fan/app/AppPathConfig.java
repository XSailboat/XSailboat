package team.sailboat.commons.fan.app;

public class AppPathConfig
{
	String mAppDirName ;
	String mMainConfigFileName = "config.ini" ;
	String mMainLogFileName = "service.log" ;
	
	public AppPathConfig(String aAppDirName , String aMainConfigFileName , String aMainLogFileName)
	{
		mAppDirName = aAppDirName ;
		mMainConfigFileName = aMainConfigFileName ;
		mMainLogFileName = aMainLogFileName ;
	}
	
	public AppPathConfig(String aAppDirName)
	{
		mAppDirName = aAppDirName ;
	}
	
	public String getAppDirName()
	{
		return mAppDirName;
	}
	
	public String getMainConfigFileName()
	{
		return mMainConfigFileName;
	}
	
	public String getMainLogFileName()
	{
		return mMainLogFileName;
	}
	
}
