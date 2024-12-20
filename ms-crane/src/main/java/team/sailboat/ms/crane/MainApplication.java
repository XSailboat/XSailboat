package team.sailboat.ms.crane;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import jakarta.annotation.PostConstruct;
import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.app.AppPathConfig;
import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.statestore.IRunData;
import team.sailboat.commons.fan.statestore.RunData_Properties;
import team.sailboat.commons.ms.ACKeys_Common;
import team.sailboat.commons.ms.EnableMSCommon;
import team.sailboat.commons.ms.MSApp;
import team.sailboat.commons.ms.controller.CommonController;
import team.sailboat.commons.ms.crypto.RSAKeyPairMaker4JS;

/**
 * 
 * SailMSCrane的入口类
 *
 * @author yyl
 * @since 2024年11月6日
 */
@EnableMSCommon
@Import(CommonController.class)
@SpringBootApplication
public class MainApplication
{
	
	@Autowired
	AppConfig mAppConf ;
	
	@PostConstruct
	void _init() throws IOException
	{
		PropertiesEx propEx = PropertiesEx.loadFromFile(MSApp.instance().getAppPaths().getMainConfigFile()) ;
		PropertiesEx newPropEx = new PropertiesEx() ;
		int startPos = "sys_params.".length() ;
		for(String propName : propEx.stringPropertyNames())
		{
			if(propName.startsWith("sys_params."))
				newPropEx.setProperty(propName.substring(startPos) , propEx.getProperty(propName)) ;
		}
		mAppConf.setSys_params(newPropEx) ;
	}
	
	@Bean
	RSAKeyPairMaker4JS _rsaMaker() throws NoSuchAlgorithmException
	{
		return RSAKeyPairMaker4JS.getDefault() ;
	}
	
	@Bean
	IRunData _runData() throws IOException
	{
		return new RunData_Properties(MSApp.instance().getAppPaths().getDataFile(AppConsts.sFN_RunData)) ;
	}
	
	public static void main(String[] aArgs)
	{
		MSApp.instance().withApplicationArgs(aArgs)
			.withIdentifier(AppConsts.sAppName, AppConsts.sAppVer, AppConsts.sAppDesc)
			.withActivePerformer(()->{
				
			})
			.withStopPerformer(()->{
				
			})
			.s0_init(()->{
				AppContext.set(ACKeys_Common.sAppPathConfig , new AppPathConfig(AppConsts.sAppDirName)) ;
				AppContext.set(ACKeys_Common.sControllerPackages, new String[] {
	                    "com.cimstech.sailboat.ms.datamodel.controller",
	                    "com.cimstech.ms.common.controller"
	            });
			})
			.s1_start(()->{
				SpringApplication.run(MainApplication.class, aArgs) ;
			})
			.s3_waiting() ;
	}

}
