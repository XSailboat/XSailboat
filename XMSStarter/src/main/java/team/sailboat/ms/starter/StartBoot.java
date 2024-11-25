package team.sailboat.ms.starter;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.fest.reflect.core.Reflection;

import team.sailboat.commons.fan.app.AppArgs;
import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.file.FileExtNameFilter;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.file.JarFileName;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.lang.YClassLoader;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.fan.sys.JEnvKit;
import team.sailboat.commons.fan.text.XString;

public class StartBoot
{
	
	static final String sIK_include_jars = "include_jars" ;
	static final String sIK_exclude_jars = "exclude_jars" ;
	static final String sIK_include_dirs = "include_dirs" ;
	
	static File sAppDir ;
	static File sProductDir ;
	
	public void run(String[] args) throws Exception
	{		
		YClassLoader classLoader = JCommon.getYClassLoader() ;
		Thread.currentThread().setContextClassLoader(classLoader);
		if(!(ClassLoader.getSystemClassLoader() instanceof YClassLoader))
		{
			Reflection.staticField("scl").ofType(ClassLoader.class).in(ClassLoader.class).set(classLoader);
		}
		
		AppArgs appArg = new AppArgs(args) ;
		String[] projects = appArg.getStringArray("dev_proj") ;
		if(XC.isNotEmpty(projects))
		{
			String projDirStr = appArg.get("proj_dir") ;
			File dir = null ;
			if(XString.isEmpty(projDirStr))
			{
				File file = new File(StartBoot.class.getClassLoader().getResource("").getFile()) ;
				if(file.getAbsolutePath().endsWith(File.separator+"bin"))
					dir = file.getParentFile().getParentFile() ;
			}
			else
				dir = new File(projDirStr) ;
			if(dir != null)
			{ 
				for(String project : projects)
				{
					File projDir = new File(dir , project) ;
					if(projDir.exists())
						classLoader.addBundle(projDir) ;
					else
					{
						projDir = new File(dir.getParentFile() , project) ;
						if(projDir.exists())
							classLoader.addBundle(projDir) ;	
					}
				}
			}
		}
		
		String iniFileName = appArg.get("x_ini") ;
		Assert.notEmpty(iniFileName , "没有通过x_ini参数指定程序的初始化配置") ;
		File file = locateFile("$bin/ms_jars/"+iniFileName) ;
		Assert.isTrue(file.exists() , "指定的初始化配置文件[%s]不存在" , file.getAbsolutePath());
		PropertiesEx propEx = PropertiesEx.loadFromFile(file , AppContext.sDefaultEncoding.name()) ;
		String[] includeJars = propEx.getStringArray(sIK_include_jars) ;
		if(XC.isNotEmpty(includeJars))
		{
			for(String path : includeJars)
			{
				if(path.contains("*"))
				{
					File[] files = getUnrepeatJarFiles(path) ;
					if(XC.isNotEmpty(files))
					{
						for(File file_0 : files)
							classLoader.addBundle(file_0) ;
					}
 				}
				else
					classLoader.addBundle(locateFile(path)) ;
			}
		}
		String[] excludeJars = propEx.getStringArray(sIK_exclude_jars) ;
		Set<String> excludeFilePaths = new HashSet<>() ;
		if(XC.isNotEmpty(excludeJars))
		{
			for(String path : excludeJars)
			{
				if(path.contains("*"))
				{
					File[] files = getJarFiles(path) ;
					if(XC.isNotEmpty(files))
					{
						for(File file_0 : files)
							excludeFilePaths.add(file_0.getAbsolutePath()) ;
					}
				}
				else
					excludeFilePaths.add(locateFile(path).getAbsolutePath()) ;
			}
		}
		String[] includeDirs = propEx.getStringArray(sIK_include_dirs) ;
		if(XC.isNotEmpty(includeDirs))
		{
			for(String path : includeDirs)
			{
				File[] jarFiles = locateDir(path).listFiles(new FileExtNameFilter("jar" , "jar.ln")) ;
				if(XC.isNotEmpty(jarFiles))
				{
					for(File jarFile : jarFiles)
					{
						if(excludeFilePaths.contains(jarFile.getAbsolutePath()))
							continue ;
						if(jarFile.getName().toLowerCase().endsWith(".jar.ln"))
						{
							String path_1 = StreamAssist.load(jarFile, "UTF-8").toString() ;
							try
							{
								jarFile = locateFile(path_1 , jarFile.getParent()) ;
							}
							catch(IllegalArgumentException e)
							{
								System.err.println("文件路径："+jarFile.getAbsolutePath());
								throw e ;
							}
							if(jarFile.exists())
								classLoader.addBundle(jarFile) ;
						}
						else
							classLoader.addBundle(jarFile) ;
					}
				}
			}
		}
		
		String launcherClassName = propEx.getString("launcher_class") ;
		Assert.notEmpty(launcherClassName , "没有指定启动类launcher_class") ;
		
		Class<?> clazz = classLoader.loadClass(launcherClassName) ;
		Assert.notNull(clazz , "无法加载启动类：%s" , launcherClassName) ;
		IMSLauncher launcher = (IMSLauncher) clazz.getConstructor().newInstance() ; 
		List<String> argList = XC.arrayList(args) ;
		boolean changed = false ;
		for(Entry<Object, Object> entry : propEx.entrySet())
		{
			switch(entry.getKey().toString())
			{
			case sIK_exclude_jars:
			case sIK_include_dirs:
			case sIK_include_jars:
				break ;
			default:
				changed = true ;
				argList.add("-"+entry.getKey().toString()) ;
				String v = entry.getValue().toString() ;
				if(XString.isNotEmpty(v))
					argList.add(v) ;
			}
		}
//		classLoader.loadClass("javax.annotation.Nonnull") ;
		launcher.start(changed?argList.toArray(JCommon.sEmptyStringArray):args);
	}

	public static void main(String[] args)
	{
		try
		{
			String runDir = JEnvKit.getRunDir() ;
			sAppDir = new File(runDir) ;
			if(runDir.endsWith("bin"))
				sAppDir = sAppDir.getParentFile() ;
			
			sProductDir = sAppDir.getParentFile() ;
			
			new StartBoot().run(args) ;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	static File[] getJarFiles(String aPath)
	{
		aPath = FileUtils.toCommonPath(aPath) ;
		String dirPath = XString.substringLeft(aPath, '/', false, 0) ;
		Assert.isNotTrue(dirPath.contains("*") , "不支持除jar文件名之外包含*") ;
		String fileNamePattern = XString.substringRight(aPath, '/', false, 0).replace("*", ".*") ;
		Pattern ptn = Pattern.compile(fileNamePattern) ;
		File dir = locateDir(dirPath) ;
		return dir.listFiles((file_0)->ptn.matcher(file_0.getName()).matches()) ;
	}
	
	static File locateDir(String aPath)
	{
		return locateDir(aPath, null) ;
	}
	
	static File locateDir(String aPath , String aBaseDir)
	{
		File file = _locateFile(aPath , aBaseDir) ;
		if(file.exists())
			Assert.isTrue(file.isDirectory()) ;
		return file ;
	}
	
	static File locateFile(String aPath)
	{
		return locateFile(aPath, null) ;
	}
	
	static File locateFile(String aPath , String aBaseDir)
	{
		File file = _locateFile(aPath , aBaseDir) ;
		if(file.exists())
			Assert.isTrue(file.isFile() , "文件[%s]不是一个常规文件！" , file.getPath()) ;
		return file ;
	}
	
	static File _locateFile(String aPath , String aBaseDir)
	{
		if(aPath.startsWith("$XZHOME/"))
			return new File(sProductDir , XString.substringRight(aPath, '/', true, 0)) ;
		else if(aPath.startsWith("$"))
			return new File(sAppDir , aPath.substring(1)) ;
		else if(aBaseDir != null)
			return new File(FileUtils.getPath(aBaseDir , aPath)) ;
		else
			return new File(aPath) ;
	}
	
	static File[] getUnrepeatJarFiles(String aPath)
	{
		File[] files = getJarFiles(aPath) ;
		if(files != null)
		{
			if(files.length == 1)
				return files ;
			else
				return JarFileName.getNewestJars(files) ;
		}
		return null ;
	}

}
