package team.sailboat.ms.crane.cmd;

import java.util.Map;
import java.util.TreeSet;

import team.sailboat.commons.fan.cli.CommandLine;
import team.sailboat.commons.fan.cli.DefaultParser;
import team.sailboat.commons.fan.cli.Options;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.http.Request;
import team.sailboat.commons.fan.json.JOptions;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.ms.crane.IApis_PyInstaller;
import team.sailboat.ms.crane.bean.HostProfile;
import team.sailboat.ms.crane.bean.Module;

/**
 * 程序包上传		<br />
 * 
 * 命令格式：	xc_conf_iptable hostnames
 *
 * 了解更多，阅读<a href = "https://www.yuque.com/okgogogooo/dh03hh/kzerf381o7s33lrg#ntTZs">《工具定义的命令清单》</a>
 * 
 * @author yyl
 * @since 2024年10月19日
 */
public class Xc_conf_iptable extends RestCmd
{
	
	static final Options sOpts = new Options() ;
	static
	{
		sOpts.addRequiredOption("d" , null , true , "目标存储目录") ;
	}
	
	
	@Override
	public void accept(String[] aArgs) throws Exception
	{
		CommandLine cmdLine = new DefaultParser().parse(sOpts, aArgs) ;
		String hostNamesStr = cmdLine.getArgList().get(0) ;
		String[] hostNames = hostNamesStr.split(",") ;
		
		// 构建防火墙配置信息
		JSONObject hostsJo = new JSONObject() ;
		JSONObject appsJo = new JSONObject() ;
		JSONObject confJo = JSONObject.one()
				.put("hosts" , hostsJo)
				.put("apps", appsJo)
				;
		
		Map<String , Module> moduleMap = XC.hashMap(getEnv().getAllModuleSupplier().get() , Module::getName , true) ;
		for(HostProfile hostProfile : getEnv().getAllHostSupplier().get())
		{
			hostsJo.put(hostProfile.getName() , hostProfile.getIp()) ;
			TreeSet<String> moduleNames = hostProfile.getDeployModuleNames() ;
			if(moduleNames != null)
			{
				for(String moduleName : moduleNames)
				{
					JSONObject appJo = appsJo.optJSONObject(moduleName , JOptions.sNewAndInjectIfNotExists) ;
					JSONArray hostsJa = appJo.optJSONArray("hosts" , JOptions.sNewAndInjectIfNotExists) ;
					hostsJa.putIfAbsent(hostProfile.getName()) ;
					
					JSONArray portsJa = appJo.optJSONArray("ports" , JOptions.sNewAndInjectIfNotExists) ;
					portsJa.putAnyIfAbsent(moduleMap.get(moduleName).getPorts()) ;
				}
			}
		}
		
		for(String hostName :hostNames)
		{
			mLogger.logInfo("向 %s 上传集群规划配置...", hostName) ;
			mClient.ask(Request.POST().path(IApis_PyInstaller.sPOST_UploadIptableConf)
					.setJsonEntity(confJo)) ;
			mLogger.logInfo("在 %s 上应用集群规划，配置防火墙..." , hostName) ;
			String resultMsg = mClient.askForString(Request.POST().path(IApis_PyInstaller.sPOST_ExecIpTableConf)) ;
			if(XString.isNotEmpty(resultMsg))
				throw new IllegalStateException(resultMsg) ;		// 抛出异常，让外面的命令执行器捕捉到执行失败的信息
		}
	}

}
