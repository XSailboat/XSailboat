package team.sailboat.commons.fan.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import team.sailboat.commons.fan.cli.CommandLine;
import team.sailboat.commons.fan.cli.DefaultParser;
import team.sailboat.commons.fan.cli.Options;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.fan.sys.JEnvKit;

public class Zip
{
	static boolean sQuite = true ;
	static boolean sIgnoreRootDir = false ;
	static boolean sBuildDir = true ;
	static boolean sRecursion = true ;

	public static void main(String[] args) throws FileNotFoundException, IOException
	{
		Options opts = new Options() ;
		opts.addOption("q", false , "不显示指令执行过程") ;
		opts.addOption("b", true, "工作目录") ;
		opts.addOption("O", "不建立指定的目录") ;
		try
		{
			CommandLine cmdLine =  new DefaultParser().parse(opts, args) ;
			sQuite = cmdLine.hasOption("q") ;
			String workDirStr = cmdLine.getOptionValue("b") ;
			sIgnoreRootDir = cmdLine.hasOption("O") ;
			
			String[] args_0 = cmdLine.getArgs() ;
			if(XC.count(args_0)<2)
			{
				System.err.println("命令格式错误！zip [-b <工作目录>][-q] <输出文件> <输入文件1> <输入文件2> ...") ;
				return ;
			}
			if(workDirStr == null)
				workDirStr = JEnvKit.getRunDir() ;

			String outputFileStr = args_0[0] ;
			File outputFile = FileUtils.isAbsolutePath(outputFileStr)?new File(outputFileStr):new File(workDirStr , outputFileStr) ;
			try(ZipOutputStream outs = new ZipOutputStream(FileUtils.openBufferedOStream(outputFile)))
			{
				for(int i=1 ; i<args_0.length ; i++)
				{
					String inputFileStr = args_0[i] ;
					if(FileUtils.isAbsolutePath(inputFileStr))
					{
						File inputFile = new File(inputFileStr) ;
						if(inputFile.exists())
						{
							if(inputFile.isDirectory())
							{
								if(!sIgnoreRootDir && sBuildDir)
								{
									String path = inputFile.getName()+"/" ;
									info("添加目录【%1$s】至【%2$s】" , inputFileStr , path);
									outs.putNextEntry(new ZipEntry(path));
								}
								if(sRecursion)
									recursionCompress(outs, inputFile, sIgnoreRootDir?inputFile.getAbsolutePath():inputFile.getParentFile().getAbsolutePath() , true);
							}
							else
							{
								String path = inputFile.getName() ;
								info("添加文件【%1$s】至【%2$s】" , inputFileStr , path);
								ZipEntry zentry = new ZipEntry(path) ;
								zentry.setSize(inputFile.length()) ;
								zentry.setLastModifiedTime(FileTime.fromMillis(inputFile.lastModified())) ;
								outs.putNextEntry(zentry) ;
								StreamAssist.transfer_cn(new FileInputStream(inputFile) , outs) ;
							}
						}
						else
							warn("绝对路径文件不存在："+inputFileStr);
					}
					else
					{
						File inputFile = new File(workDirStr , inputFileStr) ;
						if(inputFile.exists())
						{
							String path = FileUtils.toCommonPath(inputFileStr) ;
							if(inputFile.isDirectory())
							{
								if(!sIgnoreRootDir && sBuildDir)
								{
									if(!path.endsWith("/"))
										path += "/" ;
									info("添加目录【%1$s】至【%2$s】" , inputFileStr , path);
									outs.putNextEntry(new ZipEntry(path)) ;
								}
								if(sRecursion)
									recursionCompress(outs, inputFile, sIgnoreRootDir?inputFile.getAbsolutePath():inputFile.getParentFile().getAbsolutePath() , false);
							}
							else
							{
								info("添加文件【%1$s】至【%2$s】" , inputFileStr , path);
								ZipEntry zentry = new ZipEntry(path) ;
								zentry.setSize(inputFile.length()) ;
								zentry.setLastModifiedTime(FileTime.fromMillis(inputFile.lastModified())) ;
								outs.putNextEntry(zentry) ;
								StreamAssist.transfer_cn(new FileInputStream(inputFile) , outs) ;
							}
						}
						else
							warn("相对路径文件不存在："+inputFileStr);
					}
				}
			}
		}
		catch (team.sailboat.commons.fan.cli.ParseException e)
		{
			e.printStackTrace();
		}
	}
	
	static void warn(String aMsg , Object...aArgs)
	{
		System.out.println("[警告]："+(XC.isEmpty(aArgs)?aMsg:String.format(aMsg, aArgs)));
	}
	
	static void info(String aMsg , Object...aArgs)
	{
		if(!sQuite)
			System.out.println("[消息]："+(XC.isEmpty(aArgs)?aMsg:String.format(aMsg, aArgs)));
	}
	
	static void recursionCompress(ZipOutputStream aZouts , File aDirectory , String aBaseDir , boolean aAbsolute) throws IOException
	{
		File[] files = aDirectory.listFiles() ;
		if(XC.isEmpty(files))
			return ;
		int len = aBaseDir.length() ;
		for(File file : files)
		{
			String path = file.getAbsolutePath() ;
			Assert.isTrue(path.startsWith(aBaseDir)) ;
			path = FileUtils.toCommonPath(path.substring(aBaseDir.endsWith("/")||aBaseDir.endsWith("\\")?len:len+1)) ;
			if(file.isDirectory())
			{
				if(sBuildDir)
				{
					if(!path.endsWith("/"))
						path += "/" ;
					info("添加目录【%1$s】至【%2$s】" , aAbsolute?file.getAbsolutePath():path , path);
					aZouts.putNextEntry(new ZipEntry(path)) ;
				}
				recursionCompress(aZouts, file, aBaseDir, aAbsolute);
			}
			else
			{
				ZipEntry zentry = new ZipEntry(path) ;
				zentry.setSize(file.length());
				info("添加文件【%1$s】至【%2$s】,长度【%3$d】：" , aAbsolute?file.getAbsolutePath():path , path
						, file.length());
				zentry.setLastModifiedTime(FileTime.fromMillis(file.lastModified())) ;
				aZouts.putNextEntry(zentry) ;
				StreamAssist.transfer_cn(new FileInputStream(file) , aZouts) ;
			}
		}
	}

}
