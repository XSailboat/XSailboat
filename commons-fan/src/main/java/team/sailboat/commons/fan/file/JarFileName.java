package team.sailboat.commons.fan.file;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import team.sailboat.commons.fan.collection.XC;

public class JarFileName
{
	static Pattern sJarNamePtn = Pattern.compile("(\\S+)[_\\-](\\d{1,2})\\.(\\d{1,2})\\.(\\d{1,2})(?:\\.v(\\d{14}|\\d{12}))?\\.jar") ;
	
	String mSymbolicName ;
	int[] mVersions ;
	Date mExportTime ;
	
	public JarFileName(String aJarFileName)
	{
		Matcher matcher = sJarNamePtn.matcher(aJarFileName) ;
		if(matcher.matches())
		{
			mSymbolicName =	matcher.group(1) ;
			mVersions = new int[] {Integer.parseInt(matcher.group(2))
					, Integer.parseInt(matcher.group(3))
					, Integer.parseInt(matcher.group(4))} ;
			String dateTime = matcher.group(5) ;
			if(dateTime != null)
			{
				try
				{
					switch(dateTime.length())
					{
					case 14:
						mExportTime = new SimpleDateFormat("yyyyMMddHHmmss").parse(dateTime) ;
						break ;
					case 12:
						mExportTime = new SimpleDateFormat("yyyyMMddHHmm").parse(dateTime) ;
						break ;
					default:
						throw new IllegalStateException("未预料到的时间格式："+dateTime) ;
					}
				}
				catch (ParseException e)
				{
					throw new IllegalStateException(e) ;
				}
			}
		}
		else
		{
			mSymbolicName = FileUtils.getCleanName(aJarFileName) ;
			mVersions = new int[] {0 , 0 , 0} ;
		}
	}

	public String getSymbolicName()
	{
		return mSymbolicName;
	}

	public void setSymbolicName(String aSymbolicName)
	{
		mSymbolicName = aSymbolicName;
	}

	public int[] getVersions()
	{
		return mVersions;
	}

	public void setVersions(int[] aVersions)
	{
		mVersions = aVersions;
	}

	public Date getExportTime()
	{
		return mExportTime;
	}

	public void setExportTime(Date aExportTime)
	{
		mExportTime = aExportTime;
	}
	
	public static String getSymbolicName(String aJarFileName)
	{
		return new JarFileName(aJarFileName).getSymbolicName() ;
	}
	
	/**
	 * 如果jar有同名，不同版本的情况，取其中版本最新的那个jar
	 * @param aFiles
	 * @return
	 */
	public static File[] getNewestJars(File...aFiles)
	{
		Map<String , Object> symbolicMap = new HashMap<>() ;
		Map<String , JarFileName> fileName_JFNMap = new HashMap<>() ;
		boolean repeat = false ;
		for(File file : aFiles)
		{
			JarFileName jfn = new JarFileName(file.getName()) ;
			fileName_JFNMap.put(file.getName(), jfn) ;
			Object obj = symbolicMap.put(jfn.getSymbolicName() , file) ;
			if(obj != null)
			{
				repeat = true ;
				if(obj instanceof File)
				{
					symbolicMap.put(jfn.getSymbolicName(), XC.arrayList((File)obj , file)) ;
				}
				else if(obj instanceof List)
				{
					((List)obj).add(file) ;
					symbolicMap.put(jfn.getSymbolicName() , obj) ;
				}
			}
		}
		if(!repeat)
			return aFiles ;
		File[] files = new File[symbolicMap.size()] ;
		
		//要降序排列
		Comparator<File> comp = (file_1 , file_2)->{
			JarFileName jfn_1 = fileName_JFNMap.get(file_1.getName()) ;
			JarFileName jfn_2 = fileName_JFNMap.get(file_2.getName()) ;
			int d = jfn_2.mVersions[0]-jfn_1.mVersions[0] ;
			if(d != 0)
				return d ;
			d = jfn_2.mVersions[1]-jfn_1.mVersions[1] ;
			if(d != 0)
				return d ;
			d = jfn_2.mVersions[2]-jfn_1.mVersions[2] ;
			if(d != 0)
				return d ;
			if(jfn_2.mExportTime != null)
			{
				if(jfn_1.mExportTime != null)
					return jfn_2.mExportTime.after(jfn_1.mExportTime)?1:-1 ;
				else
					return -1 ;
			}
			else if(jfn_1.mExportTime != null)
				return 1 ;
			else
				return 0 ;
		} ;
		
		int i=0 ;
		for(Entry<String , Object> entry : symbolicMap.entrySet())
		{
			if(entry.getValue() instanceof File)
				files[i] = (File)entry.getValue() ;
			else
			{
				List<File> fileList = (List<File>)entry.getValue() ;
				Collections.sort(fileList, comp) ;
				files[i] = fileList.get(0) ;
			}
			i++ ;
		}
		return files ;
	}
}
