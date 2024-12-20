package team.sailboat.ms.crane.cmd;

import java.io.File;

import team.sailboat.base.IZKProxy;
import team.sailboat.base.ZKProxy;
import team.sailboat.commons.fan.app.App;
import team.sailboat.commons.fan.cli.CommandLine;
import team.sailboat.commons.fan.cli.DefaultParser;
import team.sailboat.commons.fan.cli.Options;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.ms.jackson.JacksonUtils;

/**
 * 将数据导入到Zookeeper中		<br />
 * 
 * 命令格式：	xc1_to_zk -s zk-quorum conf-file
 * 
 * 了解更多，阅读<a href = "https://www.yuque.com/okgogogooo/dh03hh/kzerf381o7s33lrg#eKt2H">《工具定义的命令清单》</a>
 *
 * @author yyl
 * @since 2024年10月19日
 */
public class Xc1_to_zk extends LocalEnvCmd
{
	
	static final Options sOpts = new Options() ;
	static
	{
		sOpts.addRequiredOption("s" , null , true , "Zookeeper服务地址") ;
	}
	
	
	@Override
	public void accept(String[] aArgs) throws Exception
	{
		CommandLine cmdLine = new DefaultParser().parse(sOpts, aArgs) ;
		String quorum = cmdLine.getOptionValue('s') ;
		String fileName = cmdLine.getArgList().get(0) ;
		// 文件名的参考起始路径是应用的配置根目录，即$config/MicroService/SailMSCrane
		File configFile = App.instance().getAppPaths().getConfigFile(fileName) ;
		JSONObject confJo = JacksonUtils.asJSONObjectFromYaml(configFile) ;
		
		// 连接Zookeeper
		IZKProxy zkProxy = ZKProxy.get(quorum) ;
		// base_node
		String baseNodePath = confJo.optString("base_node") ;
		Assert.notEmpty(baseNodePath , "配置文件[%1$s]中的base_node配置项为空！" , configFile.getAbsolutePath()) ;
		zkProxy.ensureExists(baseNodePath) ;
		JSONObject treeJo = confJo.optJSONObject("tree") ;
		if(treeJo != null)
		{
			createChildren(zkProxy , configFile.getParentFile() , treeJo, baseNodePath) ;
		}
	}
	
	void createChildren(IZKProxy aZkProxy , File aDir , JSONObject aChildJo , String aParentPath)
	{
		aChildJo.forEach((key , val)->{
			try
			{
				if(val == null || val instanceof String)
				{
					String valStr = (String)val ;
					if(valStr != null && valStr.length() > 1 && valStr.charAt(0) == '@')
					{
						// 引用文件中的内容
						valStr = StreamAssist.load(new File(aDir, valStr.substring(1)) , "UTF-8")
								.toString() ;
					}
					if("..".equals(key))
					{
						aZkProxy.setNodeData(aParentPath, valStr) ;
					}
					else
					{
						String path = FileUtils.getPath(aParentPath , key) ;
						aZkProxy.ensureExists(path , valStr) ;
					}
				}
				else if(val instanceof JSONObject)
				{
					String path = FileUtils.getPath(aParentPath , key) ;
					aZkProxy.ensureExists(path , (String)null) ;
					createChildren(aZkProxy, aDir, (JSONObject)val , path) ;
				}
				return ;
			}
			catch (Exception e)
			{
				WrapException.wrapThrow(e) ;
			}
			throw new IllegalStateException("配置项[%1$s]的取值不能是类型%2$s".formatted(key , val.getClass().getName())) ;
		}) ;
	}

}
