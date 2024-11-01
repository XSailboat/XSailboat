package team.sailboat.commons.fan.file;

import java.io.File;
import java.io.FileFilter;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.Assert;

public class FileExtNameFilter implements FileFilter
{
	
	String[] mExtNames ;
	/**
	 * 扩展名不用带"."，如“xml”,"svg"
	 * @param aExtNames
	 */
	public FileExtNameFilter(String...aExtNames)
	{
		Assert.notEmpty(aExtNames);
		mExtNames = XC.extract(aExtNames , (extName)->extName.startsWith(".")?extName.toLowerCase():"."+extName.toLowerCase() , String.class) ;
	}

	@Override
	public boolean accept(File aPathname)
	{
		String name = aPathname.getName().toLowerCase() ;
		for(String extName : mExtNames)
		{
			if(name.endsWith(extName))
				return true ;
		}
		return false ;
	}

}
