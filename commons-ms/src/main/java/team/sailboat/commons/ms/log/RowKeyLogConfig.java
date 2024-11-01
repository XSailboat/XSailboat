package team.sailboat.commons.ms.log;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import team.sailboat.commons.fan.app.App;
import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.sys.JEnvKit;
import team.sailboat.commons.fan.sys.XNet;
import team.sailboat.commons.fan.text.IDGen;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.fan.time.XTime;

/**
 * 格式：appName_timestamp_host_pid_gid
 *
 * @author yyl
 * @since 2021年4月22日
 */
public class RowKeyLogConfig extends ClassicConverter
{
	String mAppName ;
	String mHostName ;
	String mPid ;
	String mSysEnv ;
	
	public RowKeyLogConfig()
	{
		mHostName = XNet.getHostName() ;
		mPid = Integer.toString(JEnvKit.getPID()) ;
		IDGen.init();
	}

	@Override
	public String convert(ILoggingEvent aEvent)
	{
		String appName = mAppName ;
		if(appName == null)
		{
			mAppName = App.instance().getName() ;
			if(mAppName == null)
				mAppName = AppContext.getAppName() ;
			if(mAppName != null)
			{
				appName = mAppName ;
			}
			else
			{
				JCommon.cerr(XTime.currentPlain_yyyyMMddHHmmssSSS()+ "：appName未设置，appName暂用UNKNOW") ;
				appName = "UNKNOW" ;
			}
		}
		String sysEnv = mSysEnv ;
		if(sysEnv == null)
		{
			mSysEnv = JEnvKit.getSysEnv() ;
			if(XString.isNotEmpty(mSysEnv))
				sysEnv = mSysEnv ;
			else
			{
				JCommon.cerr("没有设置sys_env，缺省认为是生产环境prod");
				sysEnv = "prod" ;
			}
		}
		return new StringBuilder().append(sysEnv)
				.append('_').append(appName)
				.append('_').append(aEvent.getTimeStamp())
				.append('_').append(mHostName)
				.append('_').append(mPid)
				.append('#').append(IDGen.newID("SysLog" , 5 , false))
				.toString() ;
	}

}
