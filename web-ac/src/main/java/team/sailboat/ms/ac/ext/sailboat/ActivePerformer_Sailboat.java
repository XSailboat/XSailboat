package team.sailboat.ms.ac.ext.sailboat;

import java.io.IOException;

import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import team.sailboat.base.ClusterMode;
import team.sailboat.base.IZKSysProxy;
import team.sailboat.base.ZKSysProxy;
import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.ms.MSApp;
import team.sailboat.ms.ac.AppConsts;
import team.sailboat.ms.ac.ext.IActivePerformer;

/**
 * 
 * 在Sailbaot环境下的启动换成时执行的激活器
 *
 * @author yyl
 * @since 2024年12月7日
 */
public class ActivePerformer_Sailboat implements IActivePerformer
{
	
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	@Autowired
	AppConfig_Sailboat mAppConf ;
	
	public ActivePerformer_Sailboat()
	{
	}
	
	public void run()
	{
		try
		{
			ZKSysProxy.setSysDefault(mAppConf.getZookeeperQuorum()) ;
		}
		catch (IOException | KeeperException | InterruptedException e)
		{
			WrapException.wrapThrow(e) ;
		}
		IZKSysProxy zkProxy = ZKSysProxy.getSysDefault() ;
		try
		{
			PropertiesEx prop = new PropertiesEx() ;
			prop.put("company" , AppConsts.sCompany) ;
			prop.put("displayName" , AppConsts.sAppCnName) ;
			prop.put("description", AppConsts.sAppDesc) ;
			
			zkProxy.registerWebApp(AppConsts.sAppName , prop
					, ClusterMode.MasterSlave
					, MSApp.instance().getServiceUri()) ;
			mLogger.info("{}已经向ZK注册Web应用：{}" , AppConsts.sAppName , MSApp.instance().getServiceUri()) ;
		}
		catch (Exception e)
		{
			mLogger.error("注册Web应用过程中出现异常，系统将退出！！！异常消息："+ExceptionAssist.getStackTrace(e)) ;
			System.exit(0);
		}
	}
}
