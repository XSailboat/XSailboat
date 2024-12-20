package team.sailboat.ms.ac;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.alibaba.druid.pool.DruidDataSource;

import jakarta.annotation.PostConstruct;
import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.app.AppPathConfig;
import team.sailboat.commons.fan.collection.AutoCleanHashMap;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dpa.DRepository;
import team.sailboat.commons.fan.dtool.DBHelper;
import team.sailboat.commons.fan.dtool.DBType;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.ms.ACKeys_Common;
import team.sailboat.commons.ms.EnableMSCommon;
import team.sailboat.commons.ms.MSApp;
import team.sailboat.commons.ms.ac.EnableApiGuard;
import team.sailboat.commons.ms.crypto.RSAKeyPairMaker4JS;
import team.sailboat.commons.ms.security.EnableExtendSEL;
import team.sailboat.commons.web.ac.EnableResId;
import team.sailboat.login.extend.ding.EnableDingLogin;
import team.sailboat.ms.ac.ext.IActivePerformer;
import team.sailboat.ms.ac.server.ResourceManageServer;
import team.sailboat.ms.ac.utils.LoginFailStore;

/**
 * 
 * 微服务入口
 *
 * @author yyl
 * @since 2024年10月9日
 */
@EnableExtendSEL
@EnableResId
@EnableWebSecurity
@EnableMSCommon
@EnableDingLogin
@EnableApiGuard
@SpringBootApplication
@Import(SoftwareEnvSelector.class)
public class MainApplication
{
	static MainApplication sInstance;

	final Logger mLogger = LoggerFactory.getLogger(MainApplication.class);
	
	DRepository mRepo ;
	
	@Value("${server.servlet.context-path}")
	String mContextPath ;

	@Autowired
	AppConfig mAppConfig;
	
	@Autowired
	IActivePerformer mActivePerformer ;

	public MainApplication()
	{
		sInstance = this;
	}
	
	@PostConstruct
	void _init()
	{
		MSApp.instance().setContextPath(mContextPath) ;
		// 检查Data目录下有没有验证图片
		File secuImagesDir = MSApp.instance().getAppPaths().getDataFile(AppConsts.sFN_secu_images) ;
		Assert.isTrue(secuImagesDir.exists() && XC.isNotEmpty(secuImagesDir.listFiles())
				, "验证图片目录为空！") ;
	}
	
	@Bean("sysDB")
    DataSource _sysDataSource()
    {
    	DruidDataSource ds = new DruidDataSource() ;
    	String connUrl = mAppConfig.getSysRdbConnUrl() ; 
    	ds.setUrl(connUrl) ;
    	ds.setUsername(mAppConfig.getSysRdbConnKey()) ;
    	ds.setPassword(mAppConfig.getSysRdbConnSecret()) ;
    	mLogger.info("sysRDB的URL:{}" , connUrl);
    	mLogger.info("sysRDB的用户:{}" , mAppConfig.getSysRdbConnKey());
    	ds.setDefaultAutoCommit(false) ;
    	DBType dbType = DBHelper.getDBType(connUrl) ;
    	try
		{
    		ds.setDriverClassName(DBHelper.loadJDBC(dbType).getName()) ;
		}
		catch (ClassNotFoundException e)
		{
			throw new IllegalStateException(e) ;
		}
    	return ds ;
    }
	 
	@Bean("sysRepo")
    DRepository _sysRepository(@Autowired @Qualifier("sysDB") DataSource aDS) throws SQLException
    {
		if(mRepo == null)
			mRepo = DRepository.of("系统数据库", aDS) ;
		return mRepo ;
    }
	 
	@Bean
	ResourceManageServer _resourceManageServer(@Autowired @Qualifier("sysRepo") DRepository aRepo) throws SQLException
	{
		ResourceManageServer resMngServer = new ResourceManageServer(aRepo) ;
		resMngServer.init() ;
		return resMngServer ;
	}
	
	@Bean
	PasswordEncoder _passwordEncoder()
	{
		return new BCryptPasswordEncoder() ;
	}
	
	@Bean
	RSAKeyPairMaker4JS _rsaMaker() throws NoSuchAlgorithmException
	{
		return RSAKeyPairMaker4JS.getDefault() ;
	}
	
	@Bean("resetPasswdUsernames")
	Map<String , String> _resetPasswdUsernames()
	{
		return AutoCleanHashMap.withExpired_Created(1) ;
	}
	
	@Bean
	LoginFailStore _loginFailStore()
	{
		return new LoginFailStore(mAppConfig.getLoginRetryTimes()) ;
	}

	public void stop()
	{
		System.exit(0);
	}

	public static void main(String[] aArgs)
	{
		MSApp.instance()
				.withApplicationArgs(aArgs)
				.withIdentifier(AppConsts.sAppName, AppConsts.sAppVer, AppConsts.sAppDesc)
				.withActivePerformer(() -> {
					// API
					AppApis.syncApis(MainApplication.sInstance.mRepo , AppApis.getApis()) ;
					MainApplication.sInstance.mActivePerformer.run() ;
				})
				.withStopPerformer(() -> {
				})
				.s0_init(() -> {
					AppContext.set(ACKeys_Common.sAppPathConfig, new AppPathConfig(AppConsts.sAppDirName));
					AppContext.set(ACKeys_Common.sControllerPackages,
							new String[] {
									"team.sailboat.ms.ac.controller" ,
									"team.sailboat.ms.ac.foreign.controller"
							});
				})
				.s1_start(() -> {
					String commonConfPath = MSApp.instance().getAppPaths().getCommonConfigFile("sailboat-common.ini").getAbsolutePath() ;
					System.setProperty("app.config.common.path" , commonConfPath) ;
					
					SpringApplication.run(MainApplication.class, aArgs);
				})
				.active()
				.s3_waiting();
	}
}
