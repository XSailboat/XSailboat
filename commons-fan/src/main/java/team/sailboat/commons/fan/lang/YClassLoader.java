package team.sailboat.commons.fan.lang;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.jar.Manifest;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.file.FileExtNameFilter;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.file.JarFileName;
import team.sailboat.commons.fan.log.Log;

/**
 * 
 *
 * @author yyl
 * @since 2018年8月22日
 */
public class YClassLoader extends URLClassLoader
{
	
	protected final Map<String, String> mSymbolicName_JarPathMap = new HashMap<>() ;
	
	/**
	 * 存的是已经添加到mBundle的symbolicName
	 */
	protected final Map<String , Manifest> mActiveBundles = new HashMap<>() ;
	
	public YClassLoader(ClassLoader aParent)
	{
		super("YClassLoader" , new URL[0] , aParent) ;
	}
	
	public void addContextDir(File aPluginDir)
	{
		addContextDir(aPluginDir, false);
	}
	
	public void addContextDir(File aPluginDir , boolean aActiveJar , String...aExcludeJarFileNames)
	{
		for(File jarFile : aPluginDir.listFiles(new FileExtNameFilter("jar")))
		{
			if(XC.contains(aExcludeJarFileNames , jarFile.getName()))
			{
				Log.info("jar文件[%s]按要求被排除", jarFile.getAbsolutePath()) ;
				continue ;
			}
			addJar(jarFile, aActiveJar) ;
		}
	}
	
	protected boolean addDirectory(File aDirectory , boolean aActive)
	{
		String symbolicName = aDirectory.getName() ; ;
		String path = mSymbolicName_JarPathMap.get(symbolicName) ;
		if(path == null)
		{
			File dir = new File(aDirectory , "target/classes") ;
			if(!dir.exists())
			{
				dir = new File(aDirectory , "bin") ;
				if(!dir.exists())
					return false ;
			}
			
			mSymbolicName_JarPathMap.put(symbolicName , dir.getAbsolutePath()) ;
			if(aActive)
			{
				activeDirectory(dir, symbolicName);
			}
			return true ;
		}
		return false ;
	}
	
	protected void activeDirectory(File aDir , String aSymbolicName)
	{
		Manifest manifest = null ;
		try
		{
			addURL(aDir.toURI().toURL()) ; 
			File manifestFile = null ;
			if(aDir.getAbsolutePath().endsWith(File.separator+"bin"))
				manifestFile = new File(aDir.getParentFile() , "META-INF/MANIFEST.MF") ;
			else
			{
				//target/classes目录，这种目录的MF文件会自动生成，无法配置，所以转而到工程跟目录下面去找
				if(aDir.getAbsolutePath().endsWith(File.separator+"target"+File.separator+"classes"))
				{
					File dir = aDir.getParentFile().getParentFile() ;
					manifestFile = new File(dir , "META-INF/MANIFEST.MF") ;
					if(!manifestFile.exists())
						manifestFile = null ;
				}
				if(manifestFile == null)
					manifestFile = new File(aDir , "META-INF/MANIFEST.MF") ;
			}
			try(InputStream ins = FileUtils.openBufferedInStream(manifestFile))
			{
				manifest = new Manifest(ins) ;
			}
			catch (Exception e)
			{
				throw new IllegalStateException(e) ;
			}
		}
		catch (MalformedURLException e)
		{
			throw new IllegalStateException(e) ;
		}
		mActiveBundles.put(aSymbolicName , manifest) ;
	}
	
	protected boolean addJar(File aJarFile , boolean aActiveJar)
	{
		String symbolicName = JarFileName.getSymbolicName(aJarFile.getName()) ;
		String path = mSymbolicName_JarPathMap.get(symbolicName) ;
		
		if(path == null)
		{
			mSymbolicName_JarPathMap.put(symbolicName , aJarFile.getAbsolutePath()) ;
			if(aActiveJar)
				activeJar(aJarFile, symbolicName);
			return true ;
		}
		return false ;
	}
	
	protected void activeJar(File aJarFile , String aSymbolicName)
	{
		Manifest manifest = null ;
		try
		{
			URL url = aJarFile.toURI().toURL() ; 
			addURL(url) ;
			try(InputStream ins = URI.create("jar:" +  url.toString()+"!/META-INF/MANIFEST.MF")
					.toURL()
					.openStream())
			{
				manifest = new Manifest(ins) ;
			}
			catch(FileNotFoundException e)
			{}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		catch (MalformedURLException e)
		{
			throw new IllegalStateException(e) ;
		}
		mActiveBundles.put(aSymbolicName , manifest) ;
	}
	
	public void forEachManifest(BiConsumer<String, Manifest> aConsumer)
	{
		mActiveBundles.forEach(aConsumer) ;
	}
	
	/**
	 * 如果aVer0比aVer1大，返回大于0			<br>
	 * 如果aVer0与aVer1相等，返回0			<br>
	 * 如果aVer0比aVer1小，返回小于0			<br>
	 * 不为null的比null的大
	 * @param aVer0
	 * @param aVer1
	 * @return
	 */
	protected int compareVersion(int[] aVer0 , int[] aVer1)
	{
		if(aVer0 != null)
		{
			if(aVer1 == null)
				return 1 ;
			else
			{
				for(int i=0 ; i<3 ; i++)
				{
					int d = aVer0[i]-aVer1[i] ;
					if(d != 0)
						return d ;
				}
				return 0 ;
			}
		}
		else
			return aVer1 == null?0:-1 ;
	}
	
	/**
	 * 
	 * @param aSymbolicName
	 * @return
	 */
	public synchronized boolean addBundle(Object aBundle)
	{
		if(aBundle instanceof String)
		{
			if(mActiveBundles.containsKey((String)aBundle))
				return false ;
			String jarPath = mSymbolicName_JarPathMap.get((String)aBundle) ;
			if(jarPath == null)
				return false ;
			File file = new File(jarPath) ;
			if(!file.exists())
				return false ;
			activeJar(file , (String)aBundle) ;
			return true ;
		}
		else if(aBundle instanceof File)
		{
			File file = (File)aBundle ;
			if(file.exists())
			{
				if(file.isFile() && file.getName().endsWith(".jar"))
					return addJar((File)aBundle, true);
				else if(file.isDirectory())
				{
					//开发的时候会是目录结构
					return addDirectory(file, true) ;
				}
			}
		}
		return false ;
	}	
	
	public Class<?> loadClass0(String aClassName)
	{
		try
		{
			return super.loadClass(aClassName) ;
		}
		catch (ClassNotFoundException e)
		{
			return null ;
		}
	}
	
	void appendToClassPathForInstrumentation(String aPath)
	{
		if(aPath.endsWith(".jar"))
			addJar(new File(aPath) , true) ;
		else
			throw new IllegalStateException("未支持的claspath："+aPath) ;
    }
}
