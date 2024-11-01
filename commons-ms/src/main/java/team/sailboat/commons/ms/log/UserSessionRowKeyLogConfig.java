package team.sailboat.commons.ms.log;

import java.util.Map;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.sys.JEnvKit;
import team.sailboat.commons.fan.text.IDGen;
import team.sailboat.commons.fan.text.XString;

/**
 * 格式：sysEnv_userId_sessionId_timestamp#gid
 *
 * @author yyl
 * @since 2021年4月22日
 */
public class UserSessionRowKeyLogConfig extends ClassicConverter
{
	String mAppName ;
	String mSysEnv ;
	
	public UserSessionRowKeyLogConfig()
	{
		IDGen.init();
	}

	@Override
	public String convert(ILoggingEvent aEvent)
	{
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
		Map<String, String> mdc = aEvent.getMDCPropertyMap() ;
		return new StringBuilder().append(sysEnv)
				.append('_').append(mdc.getOrDefault("userId" , "UnknowUser"))
				.append('_').append(mdc.getOrDefault("sessionId" , "UnknowSession"))
				.append('_').append(aEvent.getTimeStamp())
				.append('#').append(IDGen.newID("UserSession" , 5 , false))
				.toString() ;
	}

}
