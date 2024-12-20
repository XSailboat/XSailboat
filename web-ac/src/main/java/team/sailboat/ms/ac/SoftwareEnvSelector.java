package team.sailboat.ms.ac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.ms.MSApp;
import team.sailboat.ms.ac.ext.ActivePerformer;
import team.sailboat.ms.ac.ext.sailboat.ActivePerformer_Sailboat;
import team.sailboat.ms.ac.ext.sailboat.AppConfig_Sailboat;

/**
 * 运行环境选择
 *
 * @author yyl
 * @since 2024年10月10日
 */
public class SoftwareEnvSelector implements ImportSelector
{
	
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;

	@Override
	public String[] selectImports(AnnotationMetadata aImportingClassMetadata)
	{
		String[] args = MSApp.instance().getStartArgs() ;
		int i= XC.indexOf(args, "-software_env") ;
		if(i == -1)
		{
			mLogger.info("没有设置软件环境(software_env)，软件将以最小功能特性运行！");
			return new String[] {AppConfig.class.getName()
					, ActivePerformer.class.getName()} ;
		}
		String env = args[i+1] ;
		if("sailboat".equals(env))
		{
			mLogger.info("软件环境(software_env)被设置为{}" , env);
			return new String[] {AppConfig_Sailboat.class.getName()
					, ActivePerformer_Sailboat.class.getName()} ;
		}
		else
			throw new IllegalArgumentException("未知的software_env值："+env) ;
	}

}
