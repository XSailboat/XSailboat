package team.sailboat.ms.base.utils;

import java.io.IOException;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import team.sailboat.base.IZKSysProxy;
import team.sailboat.base.SysConst;
import team.sailboat.base.ZKSysProxy;
import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.sys.IPList;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.ms.ACKeys_Common;
import team.sailboat.commons.ms.access.IPRestrictFilter;

public class IPRestrictHelper
{
	static final Logger sLogger = LoggerFactory.getLogger(IPRestrictHelper.class) ;
	
	public static void enable() throws Exception
	{
		ConfigurableApplicationContext ctx = (ConfigurableApplicationContext)AppContext.get(ACKeys_Common.sSpringAppContext) ;
		Assert.notNull(ctx , "无法从AppContext中取得Spring上下文！") ;
		IPRestrictFilter filter = ctx.getBean(IPRestrictFilter.class) ;
		Assert.notNull(filter , "无法从Spring上下文中取得IPRestrictFilter！") ;
		IZKSysProxy zkSysProxy = ZKSysProxy.getSysDefault() ;
		String str = zkSysProxy.getNodeData_Str(SysConst.sZK_SysPath_cluster_ipWhiteList) ;
		setIPList(str, filter) ;
		zkSysProxy.watchNode(SysConst.sZK_SysPath_cluster_ipWhiteList , new Watcher() {

			@Override
			public void process(WatchedEvent aEvent)
			{
				try
				{
					String str = zkSysProxy.getNodeData_Str(SysConst.sZK_SysPath_cluster_ipWhiteList) ;
					setIPList(str, filter) ;
				}
				catch (Exception e)
				{
					sLogger.error(ExceptionAssist.getClearMessage(getClass(), e)) ;
				}
			}
		}, true) ;
	}
	
	static void setIPList(String aIpListStr , IPRestrictFilter aFilter) throws IOException
	{
		if(XString.isEmpty(aIpListStr))
		{
			aFilter.setIPList(null) ;
			sLogger.info("已将IP白名单置空，不限制访问IP。");
		}
		else
		{
			sLogger.info("已更新IP白名单");
			aFilter.setIPList(IPList.load(aIpListStr)) ;
		}
	}
}
