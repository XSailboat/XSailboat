package team.sailboat.base.util;

import java.io.StringReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import team.sailboat.base.HAZKInfoProtos;
import team.sailboat.base.IZKProxy;
import team.sailboat.base.IZKSysProxy;
import team.sailboat.base.SysConst;
import team.sailboat.base.ZKProxy;
import team.sailboat.base.ZKSysProxy;
import team.sailboat.base.HAZKInfoProtos.ActiveNodeInfo;
import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.text.XString;

public class HadoopUtils
{
	static final Logger sLogger = LoggerFactory.getLogger(HadoopUtils.class) ;
	
	public static String getActiveNameNode() throws Exception
	{
		IZKSysProxy sysProxy = ZKSysProxy.getSysDefault() ;
		String props = sysProxy.getNodeData_Str(SysConst.sZK_CommonPath_hadoop) ;
		PropertiesEx propEx = new PropertiesEx() ;
		propEx.load(new StringReader(props));
		String cluster = propEx.getProperty("cluster") ;
		
		IZKProxy proxy = ZKProxy.get(ZKSysProxy.getDefaultQuorum()) ;
		if(XString.isEmpty(cluster))
		{
			String clusterPath = proxy.getAnyOneChildPath("/hadoop-ha") ;
			Assert.notEmpty(clusterPath , "ZK中的路径“/hadoop-ha”下没有注册集群") ;
			cluster = FileUtils.getFileName(clusterPath) ;
			sysProxy.setNodeData(SysConst.sZK_CommonPath_hadoop , cluster) ;
			sLogger.warn("在ZK的系统目录下没有设置系统使用的hadoop集群名称({})，将其设置为{}" , SysConst.sZK_CommonPath_hadoop , cluster) ;
		}
		String path = XString.msgFmt("/hadoop-ha/{}/ActiveStandbyElectorLock", cluster) ;
		
		
		ActiveNodeInfo activeNodeInfo = HAZKInfoProtos.ActiveNodeInfo.parseFrom(proxy.getNodeData(path)) ;
		return activeNodeInfo.getHostname() ;
	}
	
	public static String getActiveNameNodeAddr() throws UnknownHostException, Exception
	{
		InetAddress addr = Inet4Address.getByName(getActiveNameNode()) ;
		return addr.getHostAddress() ;
	}
}
