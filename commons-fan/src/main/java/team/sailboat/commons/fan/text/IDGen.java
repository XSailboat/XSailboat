package team.sailboat.commons.fan.text;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class IDGen
{
	static String sSeed  ;
	static String sSeed5 ;
	
	static final Map<String, AtomicInteger> sCountMap = new HashMap<String, AtomicInteger>() ;
	
	/**
	 * 已经按照ascii码表排序
	 */
	final static char[] sDigits = {
			'0' , '1' , '2' , '3' , '4' , '5' ,
			'6' , '7' , '8' , '9' , 
			'=' ,
			'A' , 'B' ,
			'C' , 'D' , 'E' , 'F' , 'G' , 'H' ,
			'I' , 'J' , 'K' , 'L' , 'M' , 'N' ,
			'O' , 'P' , 'Q' , 'R' , 'S' , 'T' ,
			'U' , 'V' , 'W' , 'X' , 'Y' , 'Z' ,
			'_' ,
			'a' , 'b' , 
			'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
			'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
			'o' , 'p' , 'q' , 'r' , 's' , 't' ,
			'u' , 'v' , 'w' , 'x' , 'y' , 'z'
		    } ;
	
	public static void init()
	{
		if(sSeed == null)
		{
			sSeed = toUnsignedString0(System.currentTimeMillis()/60_000 , 6 , -1)
					+ sDigits[(int)(Math.random()*sDigits.length)] ;
			sSeed5 = toUnsignedString0(System.currentTimeMillis()/60_000 , 5 , -1)
					+ sDigits[(int)(Math.random()*Math.pow(2, 5))] ;
		}
	}
	
	static {
		init() ;
	}
	
	public static String toUnsignedString(long val, int aWidth)
	{
		return toUnsignedString0(val, 6, aWidth) ;
	}
	
	static String toUnsignedString0(long val, int shift , int aWidth)
	{
        int mag = Long.SIZE - Long.numberOfLeadingZeros(val);
        int chars = aWidth <= 0? Math.max(((mag + (shift - 1)) / shift), 1) : aWidth ;
        char[] buf = new char[chars];
        if(aWidth >0 )
        	Arrays.fill(buf, sDigits[0]) ;
        formatUnsignedLong(val, shift, buf, 0, chars);
        return new String(buf);
    }
	
	static int formatUnsignedLong(long val, int shift, char[] aBuf, int aOffset, int len) {
        int charPos = len;
        int radix = 1 << shift;
        int mask = radix - 1;
        do {
            aBuf[aOffset + --charPos] = sDigits[((int) val) & mask];
            val >>>= shift;
        } while (val != 0 && charPos > 0);

        return charPos;
    }
	
	public static String newID(String aCategory)
	{
		return newID(aCategory , -1) ;
	}
	
	/**
	 * 当width == 3时，262144
	 * 当width == 4时,一个category和seed的组合，能标识16777216个对象，当width == 5时，它能标识10亿个以上对象
	 * @param aCategory					可以为null
	 * @param aWidth					增长位的宽度
	 * @return
	 */
	public static String newID(String aCategory , int aWidth)
	{
		return newID(aCategory, aWidth, false) ;
	}
	
	/**
	 * 当width == 5时，它能标识3千3百万个对象，当with=6能标识10亿个以上对象
	 * @param aCategory
	 * @param aWidth
	 * @return
	 */
	public static String newID_ignoreCase(String aCategory , int aWidth)
	{
		return newID_ignoreCase(aCategory, aWidth, false) ;
	}
	
	/**
	 * 当width == 3时，262144
	 * 当width == 4时,一个category和seed的组合，能标识16777216个对象，当width == 5时，它能标识10亿个以上对象
	 * @param aCategory			可以为null
	 * @param aWidth
	 * @param aNoSeed
	 * @return
	 */
	public static String newID(String aCategory , int aWidth , boolean aNoSeed)
	{
		AtomicInteger count = sCountMap.get(aCategory) ;
		if(count == null)
		{
			synchronized (sCountMap)
			{
				count = sCountMap.get(aCategory) ;
				if(count == null)
					count = new AtomicInteger(0) ;
				sCountMap.put(aCategory, count) ;
			}
		}
		String suffix = toUnsignedString0(count.incrementAndGet() , 6 , aWidth) ;
		return aNoSeed?suffix:sSeed+ suffix ;
	}
	
	public static String newID_ignoreCase(String aCategory , int aWidth , boolean aNoSeed)
	{
		AtomicInteger count = sCountMap.get(aCategory) ;
		if(count == null)
		{
			synchronized (sCountMap)
			{
				count = sCountMap.get(aCategory) ;
				if(count == null)
					count = new AtomicInteger(0) ;
				sCountMap.put(aCategory, count) ;
			}
		}
		String suffix = toUnsignedString0(count.incrementAndGet() , 5 , aWidth) ;
		return aNoSeed?suffix:sSeed5+ suffix ;
	}
	
	/**
	 * 
	 * @param aCategory		可以为null
	 * @param aPrefix
	 * @return
	 */
	public static String newID(String aCategory , String aPrefix)
	{
		return XString.isEmpty(aPrefix)?newID(aCategory):(aPrefix+newID(aCategory)) ;
	}
	
	public static String newID(String aCategory , String aPrefix , int aWidth)
	{
		return XString.isEmpty(aPrefix)?newID(aCategory , aWidth):(aPrefix+newID(aCategory , aWidth)) ;
	}
	
	public static String newID_ignoreCase(String aCategory , String aPrefix , int aWidth)
	{
		return XString.isEmpty(aPrefix)?newID_ignoreCase(aCategory , aWidth)
				:(aPrefix+newID_ignoreCase(aCategory , aWidth)) ;
	}
}
