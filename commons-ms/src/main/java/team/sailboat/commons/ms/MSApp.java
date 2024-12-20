package team.sailboat.commons.ms;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import team.sailboat.commons.fan.app.App;
import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.http.URLBuilder;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.log.Debug;
import team.sailboat.commons.fan.log.Log;
import team.sailboat.commons.fan.sys.XNet;
import team.sailboat.commons.fan.text.XString;

/**
 * 
 * 微服务App
 *
 * @author yyl
 * @since 2024年12月7日
 */
public class MSApp extends App
{
	
	final Logger mLogger = LoggerFactory.getLogger(MSApp.class) ;
	
	String mHttpPort ;
	String mHttpsPort ;
	
	String mContextPath ;
	
	/**
	 * 服务地址ip
	 */
	String mServiceIp ;
	
	/**
	 * 微服务的服务地址			<br />
	 * 如果启用了https，那么它就是https服务地址，如果没有启用https，那么它就是http服务端口
	 */
	String mServiceUri ;
	
	/**
	 * http服务地址
	 */
	String mHttpServiceUri ;
	
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
		
		i = XC.indexOf(mAppArgs, "-service_ip") ;
		if(i != -1 && i+1<mAppArgs.length)
			mServiceIp = JCommon.defaultIfEmpty(mAppArgs[i+1] , null) ;
		if(XString.isEmpty(mServiceIp))
		{
			try
			{
				mServiceIp = XNet.getPreferedIpv4() ;
			}
			catch (SocketException e)
			{
				WrapException.wrapThrow(e) ;
			}
		}
		_rebuildServiceUri() ;
	}
	
	void _rebuildServiceUri()
	{
		mServiceUri = URLBuilder.create().protocol(isSecure()?"https":"http")
				.host(mServiceIp)
				.port(Integer.parseInt(getServerPort()))
				.path(mContextPath)
				.toString() ;
		if(isSecure())
		{
			if(mHttpPort != null)
				mHttpServiceUri = URLBuilder.create().protocol("http")
						.host(mServiceIp)
						.port(Integer.parseInt(mHttpPort))
						.path(mContextPath)
						.toString() ;
		}
		else
			mHttpServiceUri = mServiceUri ;
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
		_rebuildServiceUri();
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
	
	/**
	 * 
	 * 排除了contextPath之后的本地API的path部分
	 * 
	 * @param aPath
	 * @return
	 */
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
	
	/**
	 * 取得服务地址
	 * @return
	 */
	public String getServiceUri()
	{
		return mServiceUri ;
	}
	
	/**
	 * 
	 * 取得https服务地址<br />
	 * 如果没有启用https，返回null
	 * 
	 * @return
	 */
	public String getHttpsServiceUri()
	{
		return isSecure()?mServiceUri:null ;
	}
	
	/**
	 * http服务地址。		<br />
	 * 如果没有启用http服务，那么返回null
	 * @return
	 */
	public String getHttpServiceUri()
	{
		return mHttpServiceUri ;
	}
	
	/**
	 * 
	 * 是否启用了https			<br />
	 * 如果https服务端口不为null，就认为启用了https
	 * 
	 * @return
	 */
	public boolean isSecure()
	{
		return mHttpsPort != null ;
	}
	
	/**
	 * 取得服务端口			<br />
	 * 如果启用了https，将返回https服务端口，如果没有启用https，将返回http服务端口
	 * 
	 * @return
	 */
	public String getServerPort()
	{
		return System.getProperty("server-port") ;
	}
	
	/**
	 * 取得http服务端口。		
	 * 
	 * @return		如果没有启用Http服务，将返回null
	 */
	public String getHttpPort()
	{
		return mHttpPort ;
	}
	
	/**
	 * 
	 * 取得https服务端口。
	 * 
	 * @return		如果没有启用Http服务，将返回null
	 */
	public String getHttpsPort()
	{
		return mHttpsPort;
	}
	
	public static String realPath(String aPath)
	{
		return instance().toRealPath(aPath) ;
	}
	
	/**
	 * 
	 * 排除了contextPath之后的本地API的path部分
	 * 
	 * @param aPath
	 * @return
	 */
	public static String codePath(String aPath)
	{
		return instance().toCodePath(aPath) ;
	}
}
