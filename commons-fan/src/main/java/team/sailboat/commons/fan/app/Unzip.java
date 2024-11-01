package team.sailboat.commons.fan.app;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipInputStream;

import team.sailboat.commons.fan.cli.CommandLine;
import team.sailboat.commons.fan.cli.DefaultParser;
import team.sailboat.commons.fan.cli.Options;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.serial.ZlibUtil;

public class Unzip
{

	public static void main(String[] args)
	{
		Options opts = new Options() ;
		opts.addOption("zfile", true , "需要解压的zip文件") ;
		opts.addOption("entryDir", true , "zip文件中的需要解压的目录") ;
		opts.addOption("startIndex" , true , "需要解压出来的符合解压条件的文件开始序号") ;
		opts.addOption("amount", true, "此次解压出来的文件数量") ;
		opts.addOption("encoding", true , "zip文件中的信息编码") ;
		try
		{
			CommandLine cmdLine =  new DefaultParser().parse(opts, args) ;
			String zfilePath = cmdLine.checkOptionValue("zfile") ;
			String entryDir = cmdLine.checkOptionValue("entryDir") ;
			int startIndex = cmdLine.checkOptionValue_Int("startIndex") ;
			int amount = cmdLine.checkOptionValue_Int("amount") ;
			String encoding = cmdLine.getOptionValue("encoding") ;
			File zfile = new File(zfilePath) ;
			Assert.isTrue(zfile.exists() , "指定的文件%s不存在" , zfile.getAbsolutePath()) ;
			
			
			try(InputStream ins = FileUtils.openBufferedInStream(zfile))
			{
				ZipInputStream zins = null ;
				if(encoding == null)
					zins = new ZipInputStream(ins) ;
				else
					zins = new ZipInputStream(ins, Charset.forName(encoding)) ;
				ZlibUtil.uncompress(zins, zfile.getParentFile()
						, (zentry)->!zentry.isDirectory() && zentry.getName().startsWith(entryDir)
				, startIndex, amount);
			}
		}
		catch (team.sailboat.commons.fan.cli.ParseException|IOException e)
		{
			e.printStackTrace();
		}
	}

}
