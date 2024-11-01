package team.sailboat.ms.crane;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.app.AppPathConfig;
import team.sailboat.commons.ms.ACKeys_Common;
import team.sailboat.commons.ms.EnableMSCommon;
import team.sailboat.commons.ms.MSApp;

@EnableMSCommon
@SpringBootApplication
public class MainApplication
{
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
