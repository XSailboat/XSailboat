package team.sailboat.ms.crane.cmd;

import java.io.File;

import team.sailboat.commons.fan.cli.CommandLine;
import team.sailboat.commons.fan.cli.DefaultParser;
import team.sailboat.commons.fan.cli.Options;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.http.EntityPart;
import team.sailboat.commons.fan.http.MediaType;
import team.sailboat.commons.fan.http.Request;
import team.sailboat.ms.crane.AppConsts;

/**
 * 程序包上传		<br />
 * 
 * 命令格式：	upload -d path 程序包
 *
 * 了解更多，阅读<a href = "https://www.yuque.com/okgogogooo/dh03hh/kzerf381o7s33lrg#ntTZs">《工具定义的命令清单》</a>
 * 
 * @author yyl
 * @since 2024年10月19日
 */
public class Xc_upload extends RestCmd
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
		String appPkgName = cmdLine.getArgList().get(0) ;
		File appPkgFile = getEnv().getAppPkgFileGetter().apply(appPkgName) ;
		mClient.ask(Request.POST().path(AppConsts.sApi_UploadFile_POST)
				.queryParam("path" , cmdLine.getOptionValue('d'))
				.setMultiPartEntity(EntityPart.build(appPkgFile.getName() , FileUtils.openBufferedInStream(appPkgFile)
						, MediaType.APPLICATION_OCTET_STREAM_VALUE))) ;
	}

}
