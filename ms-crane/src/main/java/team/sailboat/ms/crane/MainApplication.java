package team.sailboat.ms.crane;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;
import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.app.AppPathConfig;
import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.ms.ACKeys_Common;
import team.sailboat.commons.ms.EnableMSCommon;
import team.sailboat.commons.ms.MSApp;

@EnableMSCommon
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
				AppContext.set(ACKeys_Common.sServicePackages, new String[] {
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
