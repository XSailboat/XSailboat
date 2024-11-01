package team.sailboat.commons.fan.file;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileNameFilter implements FileFilter
{
	public static final int sFile = 1 ;
	public static final int sDirectory = 2 ;
	public static final int sAll = sFile|sDirectory ;
	
	static Pattern sRegPtn ;
	
	Pattern mPattern ; 
	
	int mType = sAll ;

	public FileNameFilter(Pattern aPattern)
	{
		mPattern = aPattern ;
 	}
	
	/**
	 * 
	 * @param aPattern
	 * @param aType			FileNameFilter.sFile，FileNameFilter.sDirectory，FileNameFilter.sAll
	 */
	public FileNameFilter(Pattern aPattern , int aType)
	{
		this(aPattern) ;
		mType = aType ;
	}
	
	@Override
	public boolean accept(File aPathname)
	{
		if(mType == sFile)
			return aPathname.isFile() && (mPattern==null || mPattern.matcher(aPathname.getName()).matches()) ;
		else if(mType == sDirectory)
			return aPathname.isDirectory() && (mPattern==null || mPattern.matcher(aPathname.getName()).matches()) ;
		return (mPattern==null || mPattern.matcher(aPathname.getName()).matches()) ;
	}

	public static Pattern parsePlainPattern(String aText)
	{
		if(sRegPtn == null)
			sRegPtn = Pattern.compile("(i?)reg\\((.+)\\)") ;
		Matcher matcher = sRegPtn.matcher(aText) ;
		if(matcher.matches())
		{
			if(matcher.group(1).isEmpty())
				return Pattern.compile(matcher.group(2)) ;
			else
				return Pattern.compile(matcher.group(2) , Pattern.CASE_INSENSITIVE) ;
		}
		else
			return Pattern.compile(aText.replace(".", "\\.").replace("$", "\\$").replace("*", ".*")) ;
	}
}
