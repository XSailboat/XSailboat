package team.sailboat.commons.ms;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import team.sailboat.commons.fan.app.App;
import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.log.Debug;
import team.sailboat.commons.fan.log.Log;
import team.sailboat.commons.fan.sys.XNet;
import team.sailboat.commons.fan.text.XString;

public class MSApp extends App
{
	
	final Logger mLogger = LoggerFactory.getLogger(MSApp.class) ;
	
	String mHttpPort ;
	String mHttpsPort ;
	
	String mContextPath ;
	
	public static MSApp instance()
	{
		if(sInstance == null)
		{
			sInstance = new MSApp() ;
		}
		return (MSApp)sInstance ;
	}
	
	public MSApp()
	{
		super() ;
	}
	
	protected void _ms_init()
	{
		if(!XC.contains(mAppArgs, "-x_console"))
		{
			System.setProperty("logLevel.stdout" , "ERROR") ;
		}

		File cfgFile = mAppPaths.getMainConfigFile() ;
		Assert.isTrue(cfgFile.exists() , "不存在配置文件:"+cfgFile.getAbsolutePath());
		
		PropertiesEx propEx = null ;
		try
		{
			propEx = PropertiesEx.loadFromFile(cfgFile, AppContext.sDefaultEncoding.name()) ;
		}
		catch (IOException e)
		{
			WrapException.wrapThrow(e) ;
		}
		AppContext.set(ACKeys_Common.sServiceCfg, propEx);

		String port = "80" ;
		int i = XC.indexOf(mAppArgs, "-ms_port") ;
		if(i != -1 && i+1<mAppArgs.length)
			port = mAppArgs[i+1] ;
		else
			port = JCommon.defaultIfNull(propEx.getString("http.prot") , "80") ;
		if("0".equals(port))
		{
			port = Integer.toString(XNet.getAvailablePort()) ;
			JCommon.cout("指定端口为0，采用随机端口!");
		}
		mHttpPort = port ;
		System.setProperty("http-port", port) ;
		JCommon.cout("微服务的http端口：%s" , port) ;
		
		i = XC.indexOf(mAppArgs, "-ms_sport") ;
		if(i != -1 && i+1<mAppArgs.length)
			mHttpsPort = JCommon.defaultIfEmpty(mAppArgs[i+1] , null) ;
		else
			mHttpsPort = JCommon.defaultIfEmpty(propEx.getString("https.port") , null) ;
			
		if(mHttpsPort != null)
		{
			mLogger.info("配置了Https端口[{}]，认为启用了Https。" , mHttpsPort) ;
			System.setProperty("server-port", mHttpsPort) ;
		}
		else
			System.setProperty("server-port" , mHttpPort) ;
		
		
		System.setProperty("app.config.path", mAppPaths.getMainConfigFile().getAbsolutePath()) ;
		System.setProperty("app.dir.config", mAppPaths.getConfigDir().getAbsolutePath()) ;
		System.setProperty("app.dir.data", mAppPaths.getDataDir().getAbsolutePath()) ;
		System.setProperty("app.dir.temp", mAppPaths.getTempDir().getAbsolutePath()) ;
		System.setProperty("app.dir.log", mAppPaths.getLogDir().getAbsolutePath()) ;
		System.setProperty("java.io.tmpdir", mAppPaths.getTempDir().getAbsolutePath()) ;
		
		if(XC.indexOf(mAppArgs , "-x_debug") != -1)
		{
			Log.setPrintOnConsole(true);
			Debug.sDebug = true ;
			System.setProperty("logLevel", "DEBUG") ;
		}
		else
			System.setProperty("logLevel", "INFO") ;
		if(System.getProperty("logLevel.stdout") == null)
			System.setProperty("logLevel.stdout" , System.getProperty("logLevel")) ;
	}
	
	@Override
	public MSApp withApplicationArgs(String... aArgs)
	{
		return (MSApp) super.withApplicationArgs(aArgs) ;
	}
	
	public MSApp withIdentifier(String aName , String aVer , String aDesc)
	{
		return (MSApp) withIdentifier(aName, aVer, aDesc, "MicroService") ;
	}
	
	public void setContextPath(String aContextPath)
	{
		mContextPath = aContextPath ;
	}
	
	public String getContextPath()
	{
		return mContextPath;
	}
	
	public String toRealPath(String aPath)
	{
		if(XString.isEmpty(mContextPath) || aPath.startsWith(mContextPath))
			return aPath ;
		else
			return new StringBuilder(mContextPath).append(aPath.startsWith("/")?"":"/").append(aPath)
					.toString() ;
	}
	
	public String toCodePath(String aPath)
	{
		if(XString.isEmpty(mContextPath) || !aPath.startsWith(mContextPath))
			return aPath ;
		else
			return aPath.substring(mContextPath.length()) ;
	}
	
	@Override
	public App s0_init(Runnable aPerformer)
	{
		super.s0_init(aPerformer);
		_ms_init() ;
		return this ;
	}
	
	public String getHttpPort()
	{
		return mHttpPort ;
	}
	
	public String getHttpsPort()
	{
		return mHttpsPort;
	}
	
	public static String realPath(String aPath)
	{
		return instance().toRealPath(aPath) ;
	}
	
	public static String codePath(String aPath)
	{
		return instance().toCodePath(aPath) ;
	}
}
