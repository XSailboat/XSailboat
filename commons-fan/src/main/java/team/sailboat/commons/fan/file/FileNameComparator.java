package team.sailboat.commons.fan.file;

import java.io.File;
import java.util.Comparator;

import team.sailboat.commons.fan.text.ChineseComparator;

/**
 *
 * <strong>功能：</strong>
 * <p style="text-indent:2em">
 * 文件名升序排列
 *
 * @author yyl
 * @since 2016年11月29日
 */
public class FileNameComparator implements Comparator<File>
{

	ChineseComparator mComparator = new ChineseComparator() ;
	
	@Override
	public int compare(File aO1, File aO2)
	{
		return mComparator.compare(aO1.getName() , aO2.getName()) ;
	}

}
