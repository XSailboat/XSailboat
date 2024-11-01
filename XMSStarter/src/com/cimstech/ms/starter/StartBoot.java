package com.cimstech.ms.starter;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.fest.reflect.core.Reflection;

import com.cimstech.xfront.common.AppArgs;
import com.cimstech.xfront.common.AppContext;
import com.cimstech.xfront.common.FileUtils;
import com.cimstech.xfront.common.JCommon;
import com.cimstech.xfront.common.YClassLoader;
import com.cimstech.xfront.common.collection.PropertiesEx;
import com.cimstech.xfront.common.collection.XArray;
import com.cimstech.xfront.common.collection.XCollections;
import com.cimstech.xfront.common.file.FileExtNameFilter;
import com.cimstech.xfront.common.file.JarFileName;
import com.cimstech.xfront.common.log.Assert;
import com.cimstech.xfront.common.serial.StreamAssist;
import com.cimstech.xfront.common.sys.JEnvKit;
import com.cimstech.xfront.common.text.XString;

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
		Reflection.staticField("scl").ofType(ClassLoader.class).in(ClassLoader.class).set(classLoader);
		
		AppArgs appArg = new AppArgs(args) ;
		String[] projects = appArg.getStringArray("dev_proj") ;
		if(XArray.isNotEmpty(projects))
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
		if(XArray.isNotEmpty(includeJars))
		{
			for(String path : includeJars)
			{
				if(path.contains("*"))
				{
					File[] files = getUnrepeatJarFiles(path) ;
					if(XArray.isNotEmpty(files))
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
		if(XArray.isNotEmpty(excludeJars))
		{
			for(String path : excludeJars)
			{
				if(path.contains("*"))
				{
					File[] files = getJarFiles(path) ;
					if(XArray.isNotEmpty(files))
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
		if(XArray.isNotEmpty(includeDirs))
		{
			for(String path : includeDirs)
			{
				File[] jarFiles = locateDir(path).listFiles(new FileExtNameFilter("jar" , "jar.ln")) ;
				if(XArray.isNotEmpty(jarFiles))
				{
					for(File jarFile : jarFiles)
					{
						if(excludeFilePaths.contains(jarFile.getAbsolutePath()))
							continue ;
						if(jarFile.getName().toLowerCase().endsWith(".jar.ln"))
						{
							String path_1 = StreamAssist.load(jarFile, "UTF-8").toString() ;
							jarFile = new File(FileUtils.getPath(jarFile.getParent(), path_1)) ;
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
		IMSLauncher launcher = (IMSLauncher) clazz.newInstance() ; 
		List<String> argList = XCollections.asArrayList(args) ;
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
		launcher.start(changed?argList.toArray(XArray.sEmptyStringArray):args);
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
		File file = _locateFile(aPath) ;
		if(file.exists())
			Assert.isTrue(file.isDirectory()) ;
		return file ;
	}
	
	static File locateFile(String aPath)
	{
		File file = _locateFile(aPath) ;
		if(file.exists())
			Assert.isTrue(file.isFile()) ;
		return file ;
	}
	
	static File _locateFile(String aPath)
	{
		if(aPath.startsWith("$XZHOME/"))
			return new File(sProductDir , XString.substringRight(aPath, '/', true, 0)) ;
		else if(aPath.startsWith("$"))
			return new File(sAppDir , aPath.substring(1)) ;
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
