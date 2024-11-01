package team.sailboat.commons.fan.file;

import java.io.File;
import java.util.Comparator;

import team.sailboat.commons.fan.text.ChineseComparator;

public class FileModifiedTimeComparator implements Comparator<File>
{

	ChineseComparator mComparator = new ChineseComparator() ;
	
	@Override
	public int compare(File aO1, File aO2)
	{
		if(aO1.isFile() && aO2.isFile())
		{
			long delta = aO1.lastModified()-aO2.lastModified() ;
			int rc = delta>0?1:(delta==0?0:-1) ;
			if(rc == 0)
				return mComparator.compare(aO1.getName(), aO2.getName()) ;
			else
				return rc ;
		}
		else if(aO1.isDirectory())
		{
			if(aO2.isFile())
				return -1 ;
			else
				return mComparator.compare(aO1.getName(), aO2.getName()) ;
		}
		else
		{
			return 1 ;
		}
	}

}
