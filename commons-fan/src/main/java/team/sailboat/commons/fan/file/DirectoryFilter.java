package team.sailboat.commons.fan.file;

import java.io.File;
import java.io.FileFilter;

public class DirectoryFilter implements FileFilter
{

	@Override
	public boolean accept(File aPathname)
	{
		return aPathname.isDirectory() ;
	}

}
