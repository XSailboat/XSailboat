package team.sailboat.ms.ac;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import lombok.Data;
import lombok.EqualsAndHashCode;
import team.sailboat.base.bean.AppConfCommon;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.ms.MSApp;
import team.sailboat.commons.ms.custom.PropertiesExSourceFactory;

/**
 * 
 * 应用来自配置文件的配置信息
 *
 * @author yyl
 * @since 2024年10月9日
 */
@Data
@EqualsAndHashCode(callSuper = true)
@PropertySource(value={"file:${app.config.common.path}" , "file:${app.config.path}"} , ignoreResourceNotFound=false
		, encoding="utf-8" , name="app-config" , factory = PropertiesExSourceFactory.class)
public class AppConfig extends AppConfCommon
{
	
	@Value("${component.login.ding.enabled}")
	String dingLoginEnable ;
	
	@Value("${component.login.ding.appkey}")
	String dingAppKey ;
	
	@Value("${component.login.ding.appsecret}")
	String dingAppSecret ;
	
	@Value("${component.login.ding.callback.url}")
	String dingCodeCallbackUrl ;
	
	String handledDingCodeCallbackUrl ;
	
	@Value( "${credential.renewal.days:90}")
	Integer credentialRenewalDays ;
	
	@Value( "${login.retry.times:5}")
	Integer loginRetryTimes ;
	
	public boolean isDingLoginEnable()
	{
		return !"false".equalsIgnoreCase(dingLoginEnable) ;
	}
	
	public String getDingCodeCallbackUrl()
	{
		if(handledDingCodeCallbackUrl == null)
		{
			if(XString.isNotEmpty(dingCodeCallbackUrl))
			{
				if(dingCodeCallbackUrl.startsWith("http:"))
					handledDingCodeCallbackUrl = XString.msgFmt(dingCodeCallbackUrl, MSApp.instance().getHttpPort()) ;
				else if(dingCodeCallbackUrl.startsWith("https:"))
					handledDingCodeCallbackUrl = XString.msgFmt(dingCodeCallbackUrl, MSApp.instance().getHttpsPort()) ;		
			}
		}
		return handledDingCodeCallbackUrl ;
	}
}
