package team.sailboat.commons.fan.sys;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.fan.text.RegexUtils;
import team.sailboat.commons.fan.text.XString;

/**
 * 格式：				<br />
 * 1. 在ip地址中，*替代1到3位数字，例如：192.168.1.*，192。168.1.12*
 * 2. 地址区间：192.168.0.100-192.168.0.120
 * 3. 指定IP：192.168.0.120
 *
 * @author yyl
 * @since 2024年8月24日
 */
public class IPList implements Predicate<String>
{
	final Set<String> mIPSet = new LinkedHashSet<>() ;
	
	Pattern[] mIPPtns ;
	
	final Map<String , Pattern> mPtnMap = new LinkedHashMap<>() ; 
	
	IPRange[] mIPRanges ;
	
	boolean mEmpty = true ;
	
	public boolean isEmpty()
	{
		return mEmpty;
	}
	
	@Override
	public boolean test(String aIP)
	{
		if(mIPSet.contains(aIP))
			return true ;
		if(mIPPtns != null && mIPPtns.length>0)
		{
			int i = mIPPtns.length ;
			while(i-->0)
			{
				if(mIPPtns[i].matcher(aIP).matches())
					return true ;
			}
		}
		IPRange[] ranges = mIPRanges ;
		if(ranges != null && ranges.length>0)
		{
			int i = ranges.length ;
			String[] segs = aIP.split("\\.") ;
			if(segs.length == 4)
			{
				int[] segInts = new int[4] ;
				for(int j=0 ; j<4 ; j++)
				{
					segInts[j] = Integer.parseInt(segs[j]) ;
					if(segInts[i]<0 || segInts[i]>255)
						return false ;
				}
				while(i-->0)
				{
					if(ranges[i].test(segInts))
						return true ;
				}
			}
		}
		return false;
	}
	
	public boolean add(String aIP)
	{
		if(add_0(aIP))
		{
			mEmpty = false ;
			return true ;
		}
		return false ;
	}
	
	boolean add_0(String aIP)
	{
		if(aIP.contains("*"))
		{
			if(mPtnMap.get(aIP) == null)
			{
				mPtnMap.put(aIP , Pattern.compile(aIP.replace("*", "\\d{1,3}"))) ;
				return true ;
			}
			return false ;
		}
		else
		{
			IPRange range = IPRange.parse(aIP) ;
			if(range != null)
			{
				IPRange[] ranges = mIPRanges ;
				if(ranges != null && ranges.length>0)
				{
					if(XC.indexOf(ranges, range) != -1)
						return false ;
					else
					{
						mIPRanges = XC.merge(ranges, range) ;
						return true ;
					}
				}
				else
				{
					mIPRanges = new IPRange[] {range} ;
					return true ;
				}
			}
			else if(RegexUtils.checkIPv4(aIP))
			{
				mIPSet.add(aIP) ;
				return true ;
			}
			return false ;
		}
	}
	
	public static IPList load(String aText) throws IOException
	{
		IPList ipList = new IPList() ;
		String[] lines = aText.split("\n") ;
		if(XC.isNotEmpty(lines))
		{
			for(String line : lines)
			{
				line = line.trim() ;
				if(!line.isEmpty())
					ipList.add(line) ;
			}
		}
		return ipList ;
	}
	
	public static IPList load(File aFile) throws IOException
	{
		List<String> lines = StreamAssist.loadLines(aFile, "UTF-8") ;
		IPList ipList = new IPList() ;
		if(XC.isNotEmpty(lines))
		{
			for(String line : lines)
			{
				line = line.trim() ;
				if(!line.isEmpty())
					ipList.add(line) ;
			}
		}
		return ipList ;
	}
	
	public static void store(IPList aIPList , File aFile) throws IOException
	{
		if(aIPList == null)
			return ;
		try(BufferedWriter writer = FileUtils.openBufferedWriter(aFile, "UTF-8"))
		{
			if(!aIPList.mIPSet.isEmpty())
			{
				StreamAssist.appendLines(writer , aIPList.mIPSet.toArray(JCommon.sEmptyStringArray)) ;
			}
			if(!aIPList.mPtnMap.isEmpty())
			{
				StreamAssist.appendLines(writer, aIPList.mPtnMap.keySet().toArray(JCommon.sEmptyStringArray));
			}
			IPRange[] ranges = aIPList.mIPRanges ;
			if(ranges != null && ranges.length>0)
			{
				for(int i=0 ; i<ranges.length ; i++)
				{
					writer.append(ranges[i].mSourceText)
							.append(XString.sLineSeparator) ;
				}
			}
		}
	}

	static class IPRange implements Predicate<int[]>
	{
		String mSourceText ;
		/**
		 * 固定的段数
		 */
		int mFixedSegNum ;
		
		int mStartRange ;
		int mEndRange ;
		
		@Override
		public boolean test(int[] aIPSegs)
		{
			int x = 0 ;
			for(int i=mFixedSegNum ; i<4;i++)
				x = x<<8 + aIPSegs[i] ;
			
			return x>=mStartRange && x<=mEndRange ;
		}
		
		static IPRange parse(String aText)
		{
			int i = aText.indexOf('-') ;
			if(i<0)
				return null ;
			String ip1 = aText.substring(0 , i).trim() ;
			String ip2 = aText.substring(i+1).trim() ;
			String[] ip1_segs = ip1.split("\\.") ;
			if(ip1_segs.length != 4)
				return null ;
			String[] ip2_segs = ip2.split("\\.") ;
			if(ip2_segs.length != 4)
				return null ;
			IPRange range = new IPRange() ;
			range.mSourceText = XString.deflate(aText) ;
			for(i=0 ; i<4 ; i++)
			{
				if(!ip1_segs[i].equals(ip2_segs[2]))
				{
					range.mFixedSegNum = i ;
					int x1 = 0 ;
					int x2 = 0 ;
					for(int j=i ; j<4 ; j++)
					{
						x1 = x1<<8 + Integer.parseInt(ip1_segs[j]) ;
						x2 = x2<<8 + Integer.parseInt(ip2_segs[i]) ;
					}
					if(x1 == x2)
						return null ;
					if(x1<x2)
					{
						range.mStartRange = x1 ;
						range.mEndRange = x2 ;
					}
					else
					{
						range.mStartRange = x2 ;
						range.mEndRange = x1 ;
					}
					return range ;
				}
			}
			return null ;
		}
	}
}
