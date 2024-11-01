package team.sailboat.commons.fan.text;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gnu.trove.set.TCharSet;
import gnu.trove.set.hash.TCharHashSet;
import team.sailboat.commons.fan.collection.ArrayIterator;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.math.XMath;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.fan.struct.Ints;

public class XString
{
	/**
	 * 角度单位：°（度）
	 */
	public static final char sAngle_Degrees = '°' ;
	/**
	 * 角度单位：′（分）
	 */
	public static final char sAngle_Minutes = '′' ;
	/**
	 * 角度单位：″（秒）
	 */
	public static final char sAngle_Seconds = '″' ;
	
	static Pattern sPtn_GeoCood = Pattern.compile("(\\d{1,3})[°](\\d{1,2})[′']([\\d\\.]+)[″\"]([NWES])") ;
	
	/**
	 * RD是RomanDigit的缩写
	 */
	public static final char sRDC_1 = 'Ⅰ' ;
	public static final char sRDC_2 = 'Ⅱ' ;
	public static final char sRDC_3 = 'Ⅲ' ;
	public static final char sRDC_4 = 'Ⅳ' ;
	public static final char sRDC_5 = 'Ⅴ' ;
	public static final char sRDC_6 = 'Ⅵ' ;
	public static final char sRDC_7 = 'Ⅶ' ;
	public static final char sRDC_8 = 'Ⅷ' ;
	public static final char sRDC_9 = 'Ⅸ' ;
	public static final char sRDC_10 = 'Ⅹ' ;
	
	public static final String sRDS_1 = "Ⅰ" ;
	public static final String sRDS_2 = "Ⅱ" ;
	public static final String sRDS_3 = "Ⅲ" ;
	public static final String sRDS_4 = "Ⅳ" ;
	public static final String sRDS_5 = "Ⅴ" ;
	public static final String sRDS_6 = "Ⅵ" ;
	public static final String sRDS_7 = "Ⅶ" ;
	public static final String sRDS_8 = "Ⅷ" ;
	public static final String sRDS_9 = "Ⅸ" ;
	public static final String sRDS_10 = "Ⅹ" ;
	
	public static final String sLineSeparator = System.getProperty("line.separator") ;
	
	/**
     * <p>The maximum size to which the padding constant(s) can expand.</p>
     */
    private static final int PAD_LIMIT = 8192;
	
    /**
     * 空字符串
     */
	public static final String sEmpty = "";
	/**
	 * 空格
	 */
	public static final String SPACE = " ";
	/**
	 * 
	 * @param aVal
	 * @param aPrecision  精度,大于等于0
	 * @param aAdjust	如果为true，但小数点后面的末尾数字是0，是否省略，如果为false，那么小数点后位数不足，将用0补齐
	 * @return			格式化后的字符串
	 */
	public static String format(float aVal , int aPrecision , boolean aAdjust)
	{
		
		String str = String.format("%."+aPrecision+"f", aVal) ;
		if(aAdjust)
		{
			int i = -1 ;
			if((i=str.indexOf('.'))>=0)
			{
				int k = str.length() -1 ;
				for(; k>i ; k--)
				{
					if(str.charAt(k) != '0') break ;
				}
				if(k<str.length()-1)
				{
					if(str.charAt(k) == '.') 
						return str.substring(0, k) ;
					return str.substring(0, k+1) ;
				}
			}
			return str ;
		}else return str ;
		
	}
	
	public static String format(float aVal)
	{
		String text = Float.toString(aVal) ;
		int i = text.length()-1 ;
		for( ;i>=0;i--)
		{
			if(text.charAt(i) != '0')
				break ;
		}
		return text.substring(0, text.charAt(i)=='.'?i:i+1) ;
	}
	
	public static String format(double aVal)
	{
		String text = Double.toString(aVal) ;
		int i = text.length()-1 ;
		for( ;i>=0;i--)
		{
			if(text.charAt(i) != '0')
				break ;
		}
		return text.substring(0, text.charAt(i)=='.'?i:i+1) ;
	}
	
	public static String format(double aVal , int aPrecision , boolean aAdjust)
	{
		String str = String.format("%."+aPrecision+"f", aVal) ;
		if(aAdjust)
		{
			int i = -1 ;
			if((i=str.indexOf('.'))>=0)
			{
				int k = str.length() -1 ;
				for(; k>i ; k--)
				{
					if(str.charAt(k) != '0') break ;
				}
				if(k<str.length()-1)
				{
					if(str.charAt(k) == '.') 
						return str.substring(0, k) ;
					return str.substring(0, k+1) ;
				}
			}
			return str ;
		}else return str ;
		
	}
	
	/**
	 * 键盘能直接输入的简单字符
	 * @param aCh
	 * @return
	 */
	public static boolean isSimpleChar(int aCh)
	{
		return aCh>=32 && aCh<=126 ;
	}
	
	/**
	 * 小于等于空格（32）的字符，包括制表符和所有控制字符
	 * @param aCh
	 * @return
	 */
	public static boolean isBlank(int aCh)
	{
		return aCh <= ' ' ;
	}
	
	/**
	 * a到z或者A到Z
	 * @param aCh
	 * @return
	 */
	public static boolean isLetter(char aCh)
	{
		return aCh>='a' && aCh<='z' || (aCh>='A' && aCh<='Z') ;
	}
	
	/**
	 * 0-9
	 * @param aCh
	 * @return
	 */
	public static boolean isDigit(char aCh)
	{
		return aCh>='0' && aCh<='9' ;
	}
	
	/**
	 * [a-zA-Z] 或者_ 或者$
	 * @param aCh
	 * @return
	 */
	public static boolean isPlainChar(char aCh)
	{
		return isLetter(aCh) || aCh == '_' || aCh == '$' ;
	}
	
	/**
	 * Java变量可以用的合法字符
	 * @param aCh
	 * @return
	 */
	public static boolean isJavaChar(char aCh)
	{
		return isPlainChar(aCh) || isDigit(aCh) ;
	}
	
	public static String toHex(byte aByte)
	{
		char[] buf = new char[]{'0' , '0'};
		int charPos = 2;
		int radix = 1 << 4;
		int mask = radix - 1;
		do {
		    buf[--charPos] = sDigits[aByte & mask];
		    aByte >>>= 4;
		} while (aByte != 0  && charPos>0);
		return new String(buf, 0, 2) ;
	}
	
	public static String toHex(byte[] aBytes , int aStart , int aTo , boolean aLowerCase)
	{
		char[] digits = aLowerCase?sDigitsLowerCaseFirst:sDigits ;
		if(aBytes != null && aBytes.length>0)
		{
			int start = Math.max(0, aStart) ;
			int to = Math.min(aBytes.length , aTo) ;
			if(to<=start)
				return null ;
			int len = to-start ;
			char[] buf = new char[len*2] ;
			Arrays.fill(buf, '0') ;
			final int radix = 1 << 4;
			for(int i=start ; i<to ; i++)
			{
				int charPos = (i-start)*2+2;
				int mask = radix - 1;
				byte b = aBytes[i] ;
				int k = 2 ;
				do {
				    buf[--charPos] = digits[b & mask];
				    b >>>= 4;
				} while (b != 0 && k-->0);
			}
			return new String(buf) ;
		}
		return null ;
	}
	
	public static String toHex(byte[] aBytes , boolean aLowerCase)
	{
		char[] digits = aLowerCase?sDigitsLowerCaseFirst:sDigits ;
		if(aBytes != null && aBytes.length>0)
		{
			char[] buf = new char[aBytes.length*2] ;
			Arrays.fill(buf, '0') ;
			int start = 0 ;
			for(byte b : aBytes)
			{
				int charPos = start+2;
				int radix = 1 << 4;
				int mask = radix - 1;
				do {
				    buf[--charPos] = digits[b & mask];
				    b >>>= 4;
				} while (b != 0  && charPos>start);
				start += 2 ;
			}
			return new String(buf) ;
		}
		return null ;
	}
	
	public static String toHex(byte[] aBytes)
	{
		return toHex(aBytes, false) ;
	}
	
	/**
	 * 十六进制字符串转成byte[]数组
	 * @param aText
	 * @return
	 */
	public static byte[] toBytesOfHex(String aText)
	{
		if(aText == null || "".equals(aText = XString.trim(aText)))
			return JCommon.sEmptyByteArray ;
		final int len = aText.length() ;
		Assert.isTrue(len%2 == 0 , "十六进制字符串的长度是%d ，不能被2整除" , len);
		
		byte[] bytes = new byte[len/2];
		for (int i = 0; i < len;)
		{
			bytes[i/2] = (byte)(digitOfHex(aText.charAt(i))<<4 | digitOfHex(aText.charAt(i+1))) ;
			i+=2 ;
		}
		return bytes;
	}
	
	public static byte[] toBytesOfBin(String aText)
	{
		if(aText == null || "".equals(aText = XString.trim(aText)))
			return JCommon.sEmptyByteArray ;
		final int len = aText.length() ;
		Assert.isTrue(len%8 == 0 , "十六进制字符串的长度是%d ，不能被8整除" , len);
		
		byte[] bytes = new byte[len/8];
		int k = 0 ;
		for (int i = 0; i < len;)
		{
			byte b = 0 ;
			for(int j=0 ; j<8 ; j++,i++)
				b = (byte) (b<<1 | (aText.charAt(i) - '0')) ;
			bytes[k++] = b ;
		}
		return bytes;
	}
	
	static int digitOfHex(char aCh)
	{
		if(aCh>='0' && aCh<='9')
			return aCh - '0' ;
		else if(aCh>='A' && aCh<='F')
			return aCh - 55 ;			//A=55
		else if(aCh>='a' && aCh<='f')
			return aCh - 87 ; 			//a==97
		else
			throw new IllegalStateException("不是十六进制的字符："+aCh) ; 
	}
	
	public static String toHex(byte[] aBytes , String aJoin)
	{
		if(XString.isEmpty(aJoin))
			return toHex(aBytes) ;
		if(aBytes != null && aBytes.length>0)
		{ 
			char[] joinChars = aJoin.toCharArray() ;
			char[] buf = new char[aBytes.length*2+joinChars.length*(aBytes.length-1)] ;
			Arrays.fill(buf, '0') ;
			int start = 0 ;
			final int radix = 1 << 4;
			final int mask = radix - 1;
			for(byte b : aBytes)
			{
				int charPos = start+2;
				do {
				    buf[--charPos] = sDigits[b & mask]; 
				    b >>>= 4;
				} while (b != 0  && charPos>start);
				start += 2 ;
				if(start<buf.length)
				{
					for(int i=0 ; i<joinChars.length ; i++,start++)
						buf[start] = joinChars[i] ;
				}
			}
			return new String(buf) ;
		}
		return null ;
	}
	
	public static String toHex(byte[] aBytes , String aJoin , int aStart , int aTo)
	{
		if(XString.isEmpty(aJoin))
			return toHex(aBytes) ;
		if(aBytes != null && aBytes.length>0)
		{ 
			char[] joinChars = aJoin.toCharArray() ;
			int len = aTo-aStart ;
			char[] buf = new char[len*2+joinChars.length*(len-1)] ;
			Arrays.fill(buf, '0') ;
			int start = 0 ;
			final int radix = 1 << 4;
			final int mask = radix - 1;
			for(int j=aStart ; j<aTo ; j++)
			{
				byte b = aBytes[j] ;
				int charPos = start+2;
				do {
				    buf[--charPos] = sDigits[b & mask]; 
				    b >>>= 4;
				} while (b != 0  && charPos>start);
				start += 2 ;
				if(start<buf.length)
				{
					for(int i=0 ; i<joinChars.length ; i++,start++)
						buf[start] = joinChars[i] ;
				}
			}
			return new String(buf) ;
		}
		return null ;
	}
	
	public static String toString(String aJoin , String aWrap , String...aStrs)
	{
		if(XC.isNotEmpty(aStrs))
		{
			StringBuilder strBld = new StringBuilder() ;
			for(int i=0 ; i<aStrs.length ; i++)
			{
				if(i>0)
					strBld.append(aJoin) ;
				strBld.append(aWrap).append(aStrs[i]).append(aWrap) ;
			}
			return strBld.toString() ;
		}
		return "" ;
	}
	
	public static String toString(String aJoin , String aWrap , Iterable<? extends Object> aStrs)
	{
		if(aStrs == null)
			return null ;
		StringBuilder strBld = new StringBuilder() ;
		boolean first = true ;
		for(Object str : aStrs)
		{
			if(first)
				first = false ;
			else
				strBld.append(aJoin) ;
			strBld.append(aWrap).append(str).append(aWrap) ;
		}
		return strBld.toString() ;
	}
	
	public static String toString(String aJoin , String aWrap , int aFrom , int aTo , Object...aStrs)
	{
		if(XC.isNotEmpty(aStrs))
		{
			StringBuilder strBld = new StringBuilder() ;
			aTo = Math.min(aTo, aStrs.length) ;
			for(int i=aFrom ; i<aTo ; i++)
			{
				if(i>aFrom)
					strBld.append(aJoin) ;
				strBld.append(aWrap).append(aStrs[i]).append(aWrap) ;
			}
			return strBld.toString() ;
		}
		return "" ;
	}
	
	public static String toString(String aJoin , Object[] aObjs , int aMaxAmount)
	{
		if(aMaxAmount<=0)
			aMaxAmount = Integer.MAX_VALUE ;
		if(XC.isNotEmpty(aObjs))
		{
			StringBuilder strBld = new StringBuilder() ;
			for(int i=0 ; i<aObjs.length ; i++)
			{
				if(i>0)
					strBld.append(aJoin) ;
				if(i>=aMaxAmount)
				{
					strBld.append(String.format("...(共%1$d项，剩%2$d项)" , aObjs.length , aObjs.length-i-1)) ;
					break ;
				}
				if(aObjs[i] != null)
					strBld.append(aObjs[i].toString()) ;
				else
					strBld.append("<NULL>") ;
			}
			return strBld.toString() ;
		}
		return "" ;
	}
	
	public static String toString(String aJoin , Object[] aObjs)
	{
		return toString(aJoin, aObjs, -1) ;
	}
	
	public static String toString(String aJoin , Object[] aObjs , int aStartIndex , int aEndIndex)
	{
		if(XC.isNotEmpty(aObjs))
		{
			StringBuilder strBld = new StringBuilder() ;
			int endIndex = Math.min(aObjs.length , aEndIndex) ;
			for(int i=aStartIndex ; i<endIndex ; i++)
			{
				if(i != aStartIndex)
					strBld.append(aJoin) ;
				if(aObjs[i] != null)
					strBld.append(aObjs[i].toString()) ;
				else
					strBld.append("<NULL>") ;
			}
			return strBld.toString() ;
		}
		return "" ;
	}
	
	public static String toString(String aJoin , Iterable<?> aObjs)
	{
		if(aObjs != null)
		{
			StringBuilder strBld = new StringBuilder() ;
			boolean first = true ;
			for(Object obj : aObjs)
			{
				if(first)
					first = false ;
				else
					strBld.append(aJoin) ;
				if(obj != null)
					strBld.append(obj.toString()) ;
				else
					strBld.append("<NULL>") ;
			}
			return strBld.toString() ;
		}
		return "" ;
	}
	
	public static <T>  String toString(String aJoin , List<T> aObjs , int aStartIndex , int aEndIndex 
			, Function<T , ?> aFunc , boolean aIgnoreNull)
	{
		if(aObjs != null)
		{
			StringBuilder strBld = new StringBuilder() ;
			for(T obj : aObjs.subList(aStartIndex, Math.min(aEndIndex, aObjs.size())))
			{
				if(strBld.length() > 0)
					strBld.append(aJoin) ;
				Object val = aFunc != null?aFunc.apply(obj):obj ;
				if(val != null)
					strBld.append(val) ;
				else if(!aIgnoreNull)
					strBld.append("<NULL>") ;
			}
			return strBld.toString() ;
		}
		return "" ;
	}
	
	/**
	 * 如果aFunc处理得到的结果是null，这个结果会被忽略，不会被拼接到结果字符串
	 * @param aJoin
	 * @param aObjs
	 * @param aFunc
	 * @return
	 */
	public static <T> String toString(String aJoin , Iterable<T> aObjs , Function<T, String> aFunc)
	{
		if(aObjs != null)
		{
			StringBuilder strBld = new StringBuilder() ;
			boolean first = true ;
			for(T obj : aObjs)
			{
				String str = aFunc.apply(obj) ;
				if(str == null)
					continue ;
				if(first)
					first = false ;
				else
					strBld.append(aJoin) ;
				strBld.append(str) ;
			}
			return strBld.toString() ;
		}
		return "" ;
	}
	
	public static <T> String toString_Pred(String aJoin , Iterable<T> aObjs , Predicate<T> aPred)
	{
		if(aObjs != null)
		{
			StringBuilder strBld = new StringBuilder() ;
			boolean first = true ;
			for(T obj : aObjs)
			{
				if(!aPred.test(obj))
					continue ;
				if(first)
					first = false ;
				else
					strBld.append(aJoin) ;
				strBld.append(obj) ;
			}
			return strBld.toString() ;
		}
		return "" ;
	}
	
	public static String toString(String aJoin , String aWrap , Object[] aObjs)
	{
		return toString(aJoin, aWrap, aWrap, true, aObjs) ;
	}
	
	public static String toString(String aJoin , String aPrefix , String aSuffix , Object[] aObjs)
	{
		return toString(aJoin, aPrefix, aSuffix, true, aObjs) ;
	}
		
	public static String toString(String aJoin , String aPrefix , String aSuffix , boolean aIgnoreNull , Object[] aObjs)
	{
		if(XC.isNotEmpty(aObjs))
		{
			return toString(aJoin, aPrefix , aSuffix , aIgnoreNull , new ArrayIterator<>(aObjs)) ;
		}
		return "" ;
	}
	
	public static String toString(String aJoin , String aPrefix , String aSuffix , boolean aIgnoreNull , Iterable<?> aObjs)
	{
		if(aObjs != null)
		{
			StringBuilder strBld = new StringBuilder() ;
			boolean first = true ;
			for(Object obj : aObjs)
			{
				if(obj != null || !aIgnoreNull)
				{
					if(!first)
						strBld.append(aJoin) ;
					else
						first = false ;
					if(aPrefix != null)
						strBld.append(aPrefix) ;
					strBld.append(JCommon.toString(obj)) ;
					if(aSuffix != null)
						strBld.append(aSuffix) ;
				}
			}
			return strBld.toString() ;
		}
		return "" ;
	}
	
	/**
	 * 
	 * @param aJoin
	 * @param aPnts
	 * @param aStartIndex
	 * @param aEndIndex  				不包含
	 * @return
	 */
	public static String toString(String aJoin , double[] aObjs , int aStartIndex , int aEndIndex)
	{
		if(aObjs == null)
			return null ;
		StringBuilder strBld = new StringBuilder() ;
		int end = Math.min(aObjs.length, aEndIndex) ;
		for(int i=aStartIndex ; i<end ; i++)
		{
			if(i>0) strBld.append(aJoin) ;
			strBld.append(format(aObjs[i])) ;
		}
		return strBld.toString() ;
	}
	
	public static String toString(String aJoin , byte[] aObjs , int aStartIndex , int aEndIndex)
	{
		if(aObjs == null)
			return null ;
		StringBuilder strBld = new StringBuilder() ;
		int end = Math.min(aObjs.length, aEndIndex) ;
		for(int i=aStartIndex ; i<end ; i++)
		{
			if(i>0) strBld.append(aJoin) ;
			strBld.append(toHex(aObjs[i])) ;
		}
		return strBld.toString() ;
	}
	
	public static String toString(String aJoin , double...aObjs)
	{
		if(aObjs == null)
			return null ;
		StringBuilder strBld = new StringBuilder() ;
		for(int i=0 ; i<aObjs.length ; i++)
		{
			if(i>0) strBld.append(aJoin) ;
			strBld.append(format(aObjs[i])) ;
		}
		return strBld.toString() ;
	}
	
	public static String toString(String aJoin , float...aObjs)
	{
		if(aObjs == null)
			return null ;
		StringBuilder strBld = new StringBuilder() ;
		for(int i=0 ; i<aObjs.length ; i++)
		{
			if(i>0) strBld.append(aJoin) ;
			strBld.append(format(aObjs[i])) ;
		}
		return strBld.toString() ;
	}
	
	public static String toString(String aJoin , int...aObjs)
	{
		if(aObjs == null)
			return null ;
		StringBuilder strBld = new StringBuilder() ;
		for(int i=0 ; i<aObjs.length ; i++)
		{
			if(i>0) strBld.append(aJoin) ;
			strBld.append(format(aObjs[i])) ;
		}
		return strBld.toString() ;
	}
	
	public static String toString(String aJoin , long...aObjs)
	{
		if(aObjs == null)
			return null ;
		StringBuilder strBld = new StringBuilder() ;
		for(int i=0 ; i<aObjs.length ; i++)
		{
			if(i>0) strBld.append(aJoin) ;
			strBld.append(format(aObjs[i])) ;
		}
		return strBld.toString() ;
	}
	
	/**
	 * 转成百分比的形式，带百分号(%)
	 * @param aVal
	 * @param aPrecision
	 * @param aAdjust
	 * @return
	 */
	public static String toPercents(double aVal , int aPrecision , boolean aAdjust)
	{
		return format(aVal*100, aPrecision, aAdjust)+"%" ;
	}
	
	public static boolean isChinese(char aCh)
	{
		return 0x4E00<=aCh && aCh<=0x9FBF ;
	}
	
	public static boolean isAllChinese(String aText)
	{
		Assert.notEmpty(aText) ;
		final char[] chs = aText.toCharArray() ;
		final int len = chs.length ;
		for(int i=0 ; i<len ; i++)
		{
			if(!isChinese(chs[i]))
				return false ;
		}
		return true ;
	}
	
	public static boolean hasChinese(String aText)
	{
		Assert.notEmpty(aText) ;
		final char[] chs = aText.toCharArray() ;
		final int len = chs.length ;
		for(int i=0 ; i<len ; i++)
		{
			if(isChinese(chs[i]))
				return true ;
		}
		return false ;
	}

	/**
	 * 用UnicodeBlock来判定
	 * @param c
	 * @return
	 */
	public static boolean isChinese0(char c)
	{
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION 
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS ;
	}

	
	final static char[] sDigits = {
		'0' , '1' , '2' , '3' , '4' , '5' ,
		'6' , '7' , '8' , '9' , 'A' , 'B' ,
		'C' , 'D' , 'E' , 'F' , 'G' , 'H' ,
		'I' , 'J' , 'K' , 'L' , 'M' , 'N' ,
		'O' , 'P' , 'Q' , 'R' , 'S' , 'T' ,
		'U' , 'V' , 'W' , 'X' , 'Y' , 'Z' ,
		'a' , 'b' ,
		'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
		'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
		'o' , 'p' , 'q' , 'r' , 's' , 't' ,
		'u' , 'v' , 'w' , 'x' , 'y' , 'z'
	    };
	
	final static char[] sDigitsLowerCaseFirst = {
			'0' , '1' , '2' , '3' , '4' , '5' ,
			'6' , '7' , '8' , '9' ,
			'a' , 'b' ,
			'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
			'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
			'o' , 'p' , 'q' , 'r' , 's' , 't' ,
			'u' , 'v' , 'w' , 'x' , 'y' , 'z' ,
			'A' , 'B' ,
			'C' , 'D' , 'E' , 'F' , 'G' , 'H' ,
			'I' , 'J' , 'K' , 'L' , 'M' , 'N' ,
			'O' , 'P' , 'Q' , 'R' , 'S' , 'T' ,
			'U' , 'V' , 'W' , 'X' , 'Y' , 'Z'
		    };
	
	/**
	 * 生成随机字符串
	 * @param aLen
	 * @return
	 */
	public static String randomString(final int aLen)
	{
		Assert.isTrue(aLen<1024);
		char[] chs = new char[aLen] ;
		for(int i=0 ; i<aLen ; i++)
		{
			chs[i] = sDigitsLowerCaseFirst[(int)(Math.random() * sDigitsLowerCaseFirst.length)] ;
		}
		return new String(chs) ;
	}
	
	public static String randomString_LowerCase(final int aLen)
	{
		Assert.isTrue(aLen<1024);
		char[] chs = new char[aLen] ;
		for(int i=0 ; i<aLen ; i++)
		{
			chs[i] = sDigitsLowerCaseFirst[(int)(Math.random() * 36)] ;
		}
		return new String(chs) ;
	}
	
	public static String randomString_16(final int aLen)
	{
		Assert.isTrue(aLen<1024);
		char[] chs = new char[aLen] ;
		for(int i=0 ; i<aLen ; i++)
		{
			chs[i] = sDigitsLowerCaseFirst[(int)(Math.random() * 16)] ;
		}
		return new String(chs) ;
	}
	
	public static String getXPath(File aRootDir , File aFile)
	{
		String path = aFile.getAbsolutePath() ; 
		String newPath = path.replace(aRootDir.getAbsolutePath() , "") ;
		if(path.length() != newPath.length())
		{
			if(newPath.startsWith(File.separator))
				newPath = newPath.substring(File.separator.length(), newPath.length()) ;
			return "$"+newPath ;
		}
		else
			return path ;
	}
	
	/**
	 * 支持捕获组的正则表达式
	 * @param aPattern
	 * @param aReplacements				要和捕获组数量一致
	 * @return
	 */
	public static String replaceAll(String aRegex ,String aSource , String...aReplacements)
	{
		Pattern pattern = Pattern.compile(aRegex) ;
		Matcher matcher = pattern.matcher(aSource) ;
		StringBuilder strBld = new StringBuilder() ;
		int start = 0 ;
		while(matcher.find())
		{
			if(matcher.groupCount()>0)
			{
				for(int i=1 ; i<=matcher.groupCount(); i++)
				{
					int start0 = matcher.start(i) ;
					if(start0>start)
						strBld.append(aSource, start, start0) ;
					strBld.append(aReplacements[i-1]) ;
					start = matcher.end(i) ;
				}
			}
		}
		if(start != 0)
		{
			strBld.append(aSource, start, aSource.length()) ;
			return strBld.toString() ;
		}
		else
			return aSource ;
	}
	
	/**
	 * 从aFrom开始，找到aChs中的任何一个就返回
	 * @param aStr
	 * @param aFrom
	 * @param aChs
	 * @return
	 */
	public static int indexOf(String aStr , int aFrom , char...aChs)
	{
		int len = aStr.length() ;
		for(int i= aFrom ; i<len ; i++)
		{
			if(XC.contains(aChs, aStr.charAt(i)))
				return i ;
		}
		return -1 ;
	}
	
	/**
	 * 字符串aStr从头到尾逐个字符匹配，aMarks中某一个mark匹配上，就将返回
	 * @param aStr
	 * @param aMarks
	 * @return
	 */
	public static int indexOf(String aStr , String...aMarks)
	{
		return indexOf(aStr, 0, aMarks) ;
	}
	
	/**
	 * 
	 * @param aStr
	 * @param aFrom
	 * @param aMark
	 * @param aIndex		从0开始
	 * @return
	 */
	public static int indexOf_i(String aStr , int aFrom , char aMark , int aIndex)
	{
		int len = aStr.length() ;
		int count = 0 ;
		for(int i=aFrom ; i<len ; i++)
		{
			if(aStr.charAt(i) == aMark)
			{
				if(count == aIndex)
					return i ;
				count++ ;
			}
		}
		return -1 ;
	}
	
	public static int indexOf(String aStr , int aFrom , String...aMarks)
	{
		int len = aStr.length() ;
		for(int i= aFrom ; i<len ; i++)
		{
			for(String mark : aMarks)
			{
				if(aStr.charAt(i) == mark.charAt(0))
				{
					int strLen = mark.length() ;
					if(i+strLen>len)
						continue ;
					
					for(int k=i+1 , j=1 ; j<strLen ; j++ , k++)
					{
						if(aStr.charAt(k) != mark.charAt(j))
							continue ;
					}
					return i ;
				}
			}
		}
		return -1 ;
	}
	
	/**
	 * 字符串aStr中，从后往前，第aIndex个等于aCh字符的索引
	 * @param aStr
	 * @param aKey
	 * @param aIndex		从后往前第aIndex个，从0开始
	 * @return				没找到的话返回-1
	 */
	public static int lastIndexOf(String aStr , char aCh , int aIndex)
	{
		int len = aStr.length() ;
		int count = 0 ;
		for(int i=len-1 ; i>=0 ; i--)
		{
			if(aStr.charAt(i) == aCh)
			{
				if(count == aIndex)
					return i ;
				count++ ;
			}
		}
		return -1 ;
	}
	
	/**
	 * 
	 * @param aStr
	 * @param aCh
	 * @param aIndex			最后一段的序号是0
	 * @return					如果aStr中不存在字符aCh，将返回空字符串
	 */
	public static String lastSeg_i(String aStr , char aCh , int aIndex)
	{
		if(XString.isEmpty(aStr))
			return XString.sEmpty ;
		char[] chs = aStr.toCharArray() ;
		int i= chs.length ;
		int count = 0 ;
		int end = chs.length ;
		while(i-->0)
		{
			if(chs[i] == aCh)
			{
				if(count++ == aIndex)
					return new String(chs, i+1, end-i-1) ;
				else
					end = i ; 
			}
		}
		return XString.sEmpty ;
	}
	
	public static String seg_i(String aStr , char aCh , int aIndex)
	{
		if(XString.isEmpty(aStr))
			return XString.sEmpty ;
		char[] chs = aStr.toCharArray() ;
		final int len= chs.length ;
		int count = 0 ;
		int start = 0 ;
		for(int i=0 ; i<len ; i++)
		{
			if(chs[i] == aCh)
			{
				if(count++ == aIndex)
				{
					try
					{
						return new String(chs, start , i-start) ;
					}
					catch(Exception e)
					{
						e.printStackTrace(); 
					}
				}
				else
					start = i+1 ; 
			}
		}
		if(count == aIndex)
			return new String(chs , start , len-start) ;
		return null ;
	}
	
	/**
	 * 相当于aStr.substring(0 , aStr.length()-aRightIndent)
	 * @param aStr
	 * @param aRightIndent			右边缩进几个字符
	 * @return
	 */
	public static String subString(String aStr , int aRightIndent)
	{
		return aStr.substring(0, Math.max(0, aStr.length()-aRightIndent)) ;
	}
	
	/**
	 * 
	 * @param aStr
	 * @param aCh
	 * @param aFirst				true表示是第一个,false表示是最后一个匹配字符处开始
	 * @param aContains				是否包含用以匹配查找的字符
	 * @return
	 */
	public static String subString(String aStr , char aCh , boolean aFirst , boolean aContains)
	{
		if(XString.isEmpty(aStr))
			return XString.sEmpty ;
		char[] chArray = aStr.toCharArray() ;
		if(aFirst)
		{
			for(int i = 0 ; i<chArray.length ; i++)
			{
				if(aCh == chArray[i])
					return aContains?new String(chArray, 0, i+1)
							:new String(chArray, 0, i) ;
			}
			return XString.sEmpty ;
		}
		else
		{
			int i= chArray.length ;
			while(i-->0)
			{
				if(aCh == chArray[i])
					return aContains?new String(chArray, i, chArray.length-i)
							:new String(chArray, i+1, chArray.length-i-1) ;
			}
			return XString.sEmpty ;
		}
	}
	
	/**
	 * 找出字符串中所有指定字符的位置
	 * @param aStr
	 * @param aCh
	 * @return
	 */
	public static int[] indexAllOf(String aStr , char aCh)
	{
		Ints indexes = new Ints() ;
		int len = aStr.length() ;
		for(int i=0 ; i<len ; i++)
		{
			if(aStr.charAt(i) == aCh)
				indexes.add(i);
		}
		return indexes.toArray() ;
	}
	
	public static int lastIndexOf_Or(String aStr ,char...aChs)
	{
		int len = aStr.length() ;
		for(int i=len-1 ; i>=0 ; i--)
		{
			if(XC.contains(aChs, aStr.charAt(i)))
				return i ;
		}
		return -1 ;
	}
	
	/**
	 * 以aChs中任何一个Char开头，都将返回true
	 * @param aStr
	 * @param aChs
	 * @return
	 */
	public static boolean startWith(String aStr , char...aChs)
	{
		if(aStr == null || aStr.length()==0)
			return false ;
		return XC.contains(aChs, aStr.charAt(0)) ;
	}
	
	public static boolean endWithIgnoreCase(String aText , String aText0)
	{
		if(aText0 == null || aText0.isEmpty() || aText == null)
			throw new IllegalArgumentException() ;
		if(aText.length()<aText0.length())
			return false ;
		String text1 = aText.substring(aText.length()-aText0.length()) ;
		return aText0.equalsIgnoreCase(text1) ;
	}
	
	public static boolean endWithIgnoreCaseAny(String aText , String... aTexts)
	{
		Assert.notEmpty(aTexts) ;
		if(aTexts.length == 0)
			return endWithIgnoreCase(aText, aTexts[0]) ;
		else
		{
			for(String text : aTexts)
			{
				if(endWithIgnoreCase(aText, text))
					return true ;
			}
			return false ;
		}
	}
	
	/**
	 * 以aStrs中任意一个字符串开头，都将返回true
	 * @param aText
	 * @param aStrs
	 * @return
	 */
	public static boolean startWith(String aText , String...aStrs)
	{
		if(aText == null || aText.length() == 0)
			return false ;
		for(String str : aStrs)
		{
			if(aText.startsWith(str))
				return true ;
		}
		return false ;
	}
	
	/**
	 * 是否以数字开头			<br>
	 * 当aText不为null且长度大于等于1，第一个字符为0-9之间的数字的时候，返回true；否则返回false
	 * @param aText
	 * @return
	 */
	public static boolean startWithDigit(String aText)
	{
		if(aText != null && !aText.isEmpty())
		{
			char ch = aText.charAt(0) ;
			if(ch>='0' && ch<='9')
				return true ;
		}
		return false ;
	}
	
	public static boolean startWithIgnoreCase(String aText , String aStr)
	{
		if(aText.length()<aStr.length())
			return false ;
		return aText.substring(0, aStr.length()).equalsIgnoreCase(aStr) ;
	}
	
	public static boolean isCompatible(Charset aCharset , ByteBuffer aBytes)
	{
		try
		{
			aBytes.rewind() ;
			aCharset.newDecoder().onUnmappableCharacter(CodingErrorAction.REPORT).decode(aBytes) ;
			return true ;
		}
		catch (CharacterCodingException e)
		{
			return false ;
		}
	}
	
	public static boolean isCompatible(Charset aCharset , File aFile) throws IOException
	{
		return isCompatible(aCharset , StreamAssist.load(aFile)) ;
	}
	
	/**
	 * 检查字符序列为空字符串，或者每个字符都是whitespace字符，或者为null
	 * <pre>
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = true
     * StringUtils.isEmpty("bob")     = false
     * StringUtils.isEmpty("  bob  ") = false
     * </pre>
     * 
	 * @param cs
	 * @return
	 */
	public static boolean isBlank(final CharSequence cs)
	{
		int strLen;
		if (cs == null || (strLen = cs.length()) == 0)
			return true;
		for (int i = 0; i < strLen; i++)
		{
			if (!isBlank(cs.charAt(i)))
				return false;
		}
		return true;
	}
	
	public static boolean isNotBlank(CharSequence aStr)
	{
		return !isBlank(aStr) ;
	}
	
//	/**
//	 * null或者trim()为空字符串
//	 * @param aStr
//	 * @return
//	 */
//	public static boolean isEmpty(String aStr)
//	{
//		return aStr == null || aStr.trim().isEmpty() ;
//	}
	
	public static boolean isNotEmpty(String aStr)
	{
		return !isEmpty(aStr) ;
	}
	
	public static boolean isNotTrimEmpty(String aStr)
	{
		if(aStr == null)
			return false ;
		return !aStr.trim().isEmpty() ;
	}
	
	public static String trimAndCheck(String aStr)
	{
		if(aStr == null)
			return null ;
		aStr = aStr.trim() ;
		if(aStr.isEmpty())
			return null; ;
		return aStr ;
	}
	
	/**
	 * 检查字符序列为空字符串("")或者为null
	 * 
	 * <pre>
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = false
     * StringUtils.isEmpty("bob")     = false
     * StringUtils.isEmpty("  bob  ") = false
     * </pre>
     * 
	 * @param cs
	 * @return
	 */
	public static boolean isEmpty(CharSequence cs)
	{
		return cs == null || cs.length() == 0;
	}
	
	public static int count(String aText , char aCh , int aFrom)
	{
		if(aText == null)
			return 0 ;
		int len = aText.length() ;
		int count = 0 ;
		for(int i=aFrom ; i<len ; i++)
		{
			if(aText.charAt(i) == aCh)
				count++ ;
		}
		return count ;
	}
	
	public static int count(String aText , String aSplitText , int aFrom)
	{
		if(isEmpty(aText) || isEmpty(aSplitText))
			return 0 ;
		int len = aText.length() ;
		int splitTextLen = aSplitText.length() ;
		if(splitTextLen>len)
			return 0 ;
		int count = 0 ;
		for(int i=aFrom ; i<len ; )
		{
			if(partialEquals(aText, i, aSplitText, 0 , splitTextLen))
			{
				count++ ;
				i+=splitTextLen ;
			}
			else
				i++ ;
		}
		return count ;
	}
	
	public static boolean partialEquals(String aText1 , int aFrom1 , String aText2 , int aFrom2 , int aEqualLen)
	{
		Assert.isTrue(aFrom1>=0 , "字符串1的起始位置必需大于等于0，不能是$d" , aFrom1) ;
		Assert.isTrue(aFrom2>=0 , "字符串2的起始位置必需大于等于0，不能是$d" , aFrom2) ;
		Assert.isTrue(aEqualLen>0) ;
		if(isEmpty(aText1) || isEmpty(aText2))
			return false ;
		if(aText1.length()<aFrom1+aEqualLen || aText2.length()<aFrom2+aEqualLen)
			return false ;
		for(int j=0 ; j<aEqualLen ; j++)
		{
			if(aText1.charAt(aFrom1+j) != aText2.charAt(aFrom2+j))
				return false ;
		}
		return true ;
	}
	
	/**
	 * 判断指定字符串是否包含空白字符
	 * @param aText
	 * @return
	 */
	public static boolean containsBlank(String aText)
	{
		if(aText == null || aText.isEmpty())
			return false ;
		final int len = aText.length() ;
		for(int i=0 ; i<len ; i++)
		{
			if(isBlank(aText.charAt(i)))
				return true ;
		}
		return false ;
	}
	
	/**
	 * aText是否包含给定数组中的任何一个字符串
	 * @param aText
	 * @param aSegs
	 * @return
	 */
	public static boolean containsAny(String aText , String[] aSegs)
	{
		if(aSegs == null)
			return false ;
		for(String seg : aSegs)
		{
			if(aText.contains(seg))
				return true ;
		}
		return false ;
	}
	
	public static boolean containsAny(final CharSequence cs, final char... searchChars)
	{
		if (isEmpty(cs) || XC.isEmpty(searchChars))
		{
			return false;
		}
		final int csLength = cs.length();
		final int searchLength = searchChars.length;
		final int csLast = csLength - 1;
		final int searchLast = searchLength - 1;
		for (int i = 0; i < csLength; i++)
		{
			final char ch = cs.charAt(i);
			for (int j = 0; j < searchLength; j++)
			{
				if (searchChars[j] == ch)
				{
					if (Character.isHighSurrogate(ch))
					{
						if (j == searchLast)
						{
							// missing low surrogate, fine, like String.indexOf(String)
							return true;
						}
						if (i < csLast && searchChars[j + 1] == cs.charAt(i + 1))
						{
							return true;
						}
					}
					else
					{
						// ch is in the Basic Multilingual Plane
						return true;
					}
				}
			}
		}
		return false;
	}

	
	public static String wrap(String aText , String aHead , String aTail)
	{
		if(aText == null)
			aText = "" ;
		if(aHead != null)
			aText = aHead+aText ;
		if(aTail != null)
			aText = aText+aTail ;
		return aText ;
	}
	
	/**
	 * 以aChs中的任何一个char结尾，都返回true,否则返回false
	 * @param aText
	 * @param aChs
	 * @return
	 */
	public static boolean endWith(String aText , char...aChs)
	{
		if(aText == null || aText.isEmpty())
			return false ;
		return XC.contains(aChs, aText.charAt(aText.length()-1)) ;
	}
	
    /**
     * <p>Left pad a String with spaces (' ').</p>
     *
     * <p>The String is padded to the size of {@code size}.</p>
     *
     * <pre>
     * StringUtils.leftPad(null, *)   = null
     * StringUtils.leftPad("", 3)     = "   "
     * StringUtils.leftPad("bat", 3)  = "bat"
     * StringUtils.leftPad("bat", 5)  = "  bat"
     * StringUtils.leftPad("bat", 1)  = "bat"
     * StringUtils.leftPad("bat", -1) = "bat"
     * </pre>
     *
     * @param str  the String to pad out, may be null
     * @param size  the size to pad to
     * @return left padded String or original String if no padding is necessary,
     *  {@code null} if null String input
     */
    public static String leftPad(final String str, final int size) {
        return leftPad(str, size, ' ');
    }
    
    /**
     * <p>Left pad a String with a specified character.</p>
     *
     * <p>来自Apache Commons Lang3</p>
     *
     * <pre>
     * StringUtils.leftPad(null, *, *)     = null
     * StringUtils.leftPad("", 3, 'z')     = "zzz"
     * StringUtils.leftPad("bat", 3, 'z')  = "bat"
     * StringUtils.leftPad("bat", 5, 'z')  = "zzbat"
     * StringUtils.leftPad("bat", 1, 'z')  = "bat"
     * StringUtils.leftPad("bat", -1, 'z') = "bat"
     * </pre>
     *
     * @param str  the String to pad out, may be null
     * @param size  the size to pad to
     * @param padChar  the character to pad with
     * @return left padded String or original String if no padding is necessary,
     *  {@code null} if null String input
     *
     */
    public static String leftPad(final String str, final int size, final char padChar) {
        if (str == null) {
            return null;
        }
        final int pads = size - str.length();
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (pads > PAD_LIMIT) {
            return leftPad(str, size, String.valueOf(padChar));
        }
        return repeat(padChar, pads).concat(str);
    }
    
    /**
     * 字符串的左子串			<br>
     * 如果找不到相应序号的分隔符，将返回null		<br>
     * @param aText
     * @param aSplitCh
     * @param aLeftToRight
     * @param aIndex
     * @return
     */
    public static final String substringLeft(String aText , char aSplitCh , boolean aLeftToRight , int aIndex)
    {
    	if(XString.isEmpty(aText))
    		return null ;
    	char[] charArray = aText.toCharArray() ;
    	int len = charArray.length ;
    	int count = 0 ;
    	if(aLeftToRight)
    	{
    		for(int i=0 ; i<len ; i++)
    		{
    			if(charArray[i] == aSplitCh && count++ == aIndex)
    				return new String(charArray , 0 , i) ;
    		}
    	}
    	else
    	{
    		for(int i=len-1 ; i>=0 ; i--)
    		{
    			if(charArray[i] == aSplitCh && count++ == aIndex)
    				return new String(charArray , 0 , i) ;
    		}
    	}
    	return null ;
    }
    
    /**
     * 字符串的右子串			<br>
     * 如果找不到相应序号的分隔符，将返回null		<br>
     * @param aText
     * @param aSplitCh
     * @param aLeftToRight
     * @param aIndex
     * @return
     */
    public static final String substringRight(String aText , char aSplitCh , boolean aLeftToRight , int aIndex)
    {
    	return substringRight(aText, aSplitCh, aLeftToRight, aIndex, false) ;
    }
    public static final String substringRight(String aText , char aSplitCh , boolean aLeftToRight , int aIndex
    		, boolean aContainsSplitCh)
    {
    	char[] charArray = aText.toCharArray() ;
    	final int len = charArray.length ;
    	int count = 0 ;
    	if(aLeftToRight)
    	{
    		for(int i=0 ; i<len ; i++)
    		{
    			if(charArray[i] == aSplitCh && count++ == aIndex)
    			{
    				int start = aContainsSplitCh?i:i+1 ;
    				return new String(charArray , start , len-start) ;
    			}
    		}
    	}
    	else
    	{
    		for(int i=len-1 ; i>=0 ; i--)
    		{
    			if(charArray[i] == aSplitCh && count++ == aIndex)
    			{
    				int start = aContainsSplitCh?i:i+1 ;
    				return new String(charArray , start , len-start) ;
    			}
    		}
    	}
    	return null ;
    }
    
    /**
     * <p>Left pad a String with a specified String.</p>
     *
     * <p>Pad to a size of {@code size}.</p>
     *
     * <pre>
     * StringUtils.leftPad(null, *, *)      = null
     * StringUtils.leftPad("", 3, "z")      = "zzz"
     * StringUtils.leftPad("bat", 3, "yz")  = "bat"
     * StringUtils.leftPad("bat", 5, "yz")  = "yzbat"
     * StringUtils.leftPad("bat", 8, "yz")  = "yzyzybat"
     * StringUtils.leftPad("bat", 1, "yz")  = "bat"
     * StringUtils.leftPad("bat", -1, "yz") = "bat"
     * StringUtils.leftPad("bat", 5, null)  = "  bat"
     * StringUtils.leftPad("bat", 5, "")    = "  bat"
     * </pre>
     *
     * @param str  the String to pad out, may be null
     * @param size  the size to pad to
     * @param padStr  the String to pad with, null or empty treated as single space
     * @return left padded String or original String if no padding is necessary,
     *  {@code null} if null String input
     */
    public static String leftPad(final String str, final int size, String padStr) {
        if (str == null) {
            return null;
        }
        if (isEmpty(padStr)) {
            padStr = SPACE;
        }
        final int padLen = padStr.length();
        final int strLen = str.length();
        final int pads = size - strLen;
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (padLen == 1 && pads <= PAD_LIMIT) {
            return leftPad(str, size, padStr.charAt(0));
        }

        if (pads == padLen) {
            return padStr.concat(str);
        } else if (pads < padLen) {
            return padStr.substring(0, pads).concat(str);
        } else {
            final char[] padding = new char[pads];
            final char[] padChars = padStr.toCharArray();
            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }
            return new String(padding).concat(str);
        }
    }
    
    public static String repeat(String aJoinStr , String aStr , int aRepeat)
    {
        if (aStr == null) 
            return null;
        
        if (aRepeat <= 0)
            return sEmpty ;
        
        final int inputLength = aStr.length();
        if (aRepeat == 1 || inputLength == 0) {
            return aStr;
        }
        if (inputLength == 1)
            return repeat(aJoinStr , aStr.charAt(0), aRepeat) ;
        
        final int joinLen = XString.length(aJoinStr) ;
        final int outputLength = inputLength * aRepeat + joinLen*(aRepeat-1) ;
        final StringBuilder buf = new StringBuilder(outputLength);
        for (int i = 0; i < aRepeat; i++)
        {
        	if(i>0 && joinLen>0)
        		buf.append(aJoinStr) ;
            buf.append(aStr);
        }
        return buf.toString();
    }
    
    /**
     * <p>Repeat a String {@code repeat} times to form a
     * new String.</p>
     *
     * <pre>
     * StringUtils.repeat(null, 2) = null
     * StringUtils.repeat("", 0)   = ""
     * StringUtils.repeat("", 2)   = ""
     * StringUtils.repeat("a", 3)  = "aaa"
     * StringUtils.repeat("ab", 2) = "abab"
     * StringUtils.repeat("a", -2) = ""
     * </pre>
     *
     * @param str  the String to repeat, may be null
     * @param repeat  number of times to repeat str, negative treated as zero
     * @return a new String consisting of the original String repeated,
     *  {@code null} if null String input
     */
    public static String repeat(final String str, final int repeat) {
    	 if (str == null) {
             return null;
         }
         if (repeat <= 0) {
             return sEmpty ;
         }
         final int inputLength = str.length();
         if (repeat == 1 || inputLength == 0) {
             return str;
         }
         if (inputLength == 1 && repeat <= PAD_LIMIT) {
             return repeat(str.charAt(0), repeat);
         }

         final int outputLength = inputLength * repeat;
         switch (inputLength) {
             case 1 :
                 return repeat(str.charAt(0), repeat);
             case 2 :
                 final char ch0 = str.charAt(0);
                 final char ch1 = str.charAt(1);
                 final char[] output2 = new char[outputLength];
                 for (int i = repeat * 2 - 2; i >= 0; i--, i--) {
                     output2[i] = ch0;
                     output2[i + 1] = ch1;
                 }
                 return new String(output2);
             default :
                 final StringBuilder buf = new StringBuilder(outputLength);
                 for (int i = 0; i < repeat; i++) {
                     buf.append(str);
                 }
                 return buf.toString();
         }
    }
    
    /**
     * 将一个字符ch重复repeat遍
     * @param ch
     * @param repeat
     * @return
     */
	public static String repeat(final char ch, final int repeat)
	{
		final char[] buf = new char[repeat];
		for (int i = repeat - 1; i >= 0; i--)
		{
			buf[i] = ch;
		}
		return new String(buf);
	}
	
	public static String repeat(String aJoinStr , char aCh, final int aRepeat)
	{
		final int joinLen = XString.length(aJoinStr) ;
		if(joinLen == 0)
			return repeat(aCh, aRepeat) ;
		final char[] joinChs = aJoinStr.toCharArray() ; 
		char[] chs = new char[aRepeat+joinLen*(aRepeat-1)] ;
		int j = 0 ;
		for (int i=0 ; i<aRepeat ; i++)
		{
			if(i>0)
			{
				System.arraycopy(joinChs , 0 , chs , j , joinLen) ;
				j += joinLen ;
			}
			chs[j++] = aCh  ;
		}
		return new String(chs) ;
	}
	
    /**
     * <p>
     * 来自Apache Commons Lang3
     * 
     * <p>Checks if the CharSequence contains only Unicode digits.
     * A decimal point is not a Unicode digit and returns false.</p>
     *
     * <p>{@code null} will return {@code false}.
     * An empty CharSequence (length()=0) will return {@code false}.</p>
     *
     * <p>Note that the method does not allow for a leading sign, either positive or negative.
     * Also, if a String passes the numeric test, it may still generate a NumberFormatException
     * when parsed by Integer.parseInt or Long.parseLong, e.g. if the value is outside the range
     * for int or long respectively.</p>
     * 
     *
     * <pre>
     * StringUtils.isNumeric(null)   = false
     * StringUtils.isNumeric("")     = false
     * StringUtils.isNumeric("  ")   = false
     * StringUtils.isNumeric("123")  = true
     * StringUtils.isNumeric("\u0967\u0968\u0969")  = true
     * StringUtils.isNumeric("12 3") = false
     * StringUtils.isNumeric("ab2c") = false
     * StringUtils.isNumeric("12-3") = false
     * StringUtils.isNumeric("12.3") = false
     * StringUtils.isNumeric("-123") = false
     * StringUtils.isNumeric("+123") = false
     * </pre>
     *
     * @param cs  the CharSequence to check, may be null
     * @return {@code true} if only contains digits, and is non-null
     */
    public static boolean isNumeric(final CharSequence cs) {
        if (isEmpty(cs)) {
            return false;
        }
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (Character.isDigit(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }
    
	/** 
	 *
	 * 16进制字符串转字节数组 
	 * @param src  16进制字符串 
	 * @return 字节数组 
	 */
	public static byte[] hexString2Bytes(String src)
	{
		int l = src.length() / 2;
		byte[] ret = new byte[l];
		for (int i = 0; i < l; i++)
		{
			ret[i] = (byte) Integer.valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();
		}
		return ret;
	}
	
	/**
	 * 去除指定字符串字符之间的空白字符
	 * @param aText
	 * @return
	 */
	public static String deflate(String aText)
	{
		if(aText == null)
			return null ;
		aText = aText.trim() ;
		char[] chs = aText.toCharArray() ;
		int len = chs.length ;
		StringBuilder strBld = null ;
		for(int i=0 ; i<len ; i++)
		{
			if(isBlank(chs[i]))
			{
				strBld = new StringBuilder(len) ;
				strBld.append(chs, 0, i) ;
			}
			else if(strBld != null)
				strBld.append(chs[i]) ;
		}
		return strBld != null?strBld.toString():aText ;
	}
	
	public static String truncate(String aText , int aLowerLen , int aUpperLen)
	{
		if(aText != null && aText.length()>aUpperLen)
		{
			StringBuilder strBld = new StringBuilder() ;
			char[] chs = aText.toCharArray() ;
			int d = chs.length-aUpperLen ;
			for(int k = 0 ; k<chs.length ; k++)
			{
				if(Character.isLowerCase(chs[k]))
				{
					d-- ;
					continue ;
				}
				else if(Character.isUpperCase(chs[k]))
				{
					if(d<=0)
					{
						strBld.append(chs , k , chs.length-k) ;
						break ;
					}
					else
						strBld.append(chs[k]) ;
				}
			}
			aText = strBld.length()<aLowerLen?aText.substring(d):strBld.toString() ;
		}
		return aText ;
	}
	
	/**
	 * 
	 * @param aText
	 * @param openToken			例如：”{“
	 * @param closeToken		例如："}"
	 * @return		不返回null
	 */
	public static LinkedHashSet<String> extractParamNames(String aText
			, String openToken, String closeToken)
	{
		LinkedHashSet<String> paramNames = XC.linkedHashSet() ;
		if(aText == null || aText.isEmpty())
		{
			return paramNames ;
		}
		char[] src = aText.toCharArray();
		int offset = 0;
		// search open token
		int start = aText.indexOf(openToken, offset);
		if (start == -1)
		{
			return paramNames ;
		}
		StringBuilder expression = null;
		while (start > -1)
		{
			if (start > 0 && src[start - 1] == '\\')
			{
				// this open token is escaped. remove the backslash and continue.
				offset = start + openToken.length();
			}
			else
			{
				// found open token. let's search close token.
				if (expression == null)
				{
					expression = new StringBuilder();
				}
				else
				{
					expression.setLength(0);
				}
				offset = start + openToken.length();
				int end = aText.indexOf(closeToken, offset);
				while (end > -1)
				{
					if (end > offset && src[end - 1] == '\\')
					{
						// this close token is escaped. remove the backslash and continue.
						expression.append(src, offset, end - offset - 1).append(closeToken);
						offset = end + closeToken.length();
						end = aText.indexOf(closeToken, offset);
					}
					else
					{
						expression.append(src, offset, end - offset);
						offset = end + closeToken.length();
						break;
					}
				}
				if (end == -1)
				{
					// close token was not found.
					offset = src.length;
				}
				else
				{
					///////////////////////////////////////仅仅修改了该else分支下的个别行代码////////////////////////	
					int i = expression.indexOf(":") ;
					String paramName = i == -1?expression.toString():expression.substring(0, i) ;	
					paramNames.add(paramName) ;
					offset = end + closeToken.length();
					////////////////////////////////////////////////////////////////////////////////////////////////
				}
			}
			start = aText.indexOf(openToken, offset);
		}
		return paramNames ;
	}
	
	/**  
     * 从给定的文本中提取参数名，并将它们存储在一个LinkedHashSet中返回。  
     * 参数名被假定为以'${'开头，以'}'结尾的字符串。  
     * 例如，给定文本"Hello, ${name}! Welcome to ${city}."，  
     * 方法将提取并返回包含"name"和"city"的LinkedHashSet。  
     *  
     * @param aText 输入的文本，其中可能包含参数名。  
     * @return 一个LinkedHashSet(必然不为null)，包含从输入文本中提取的所有参数名。  
     *         如果输入文本中没有参数名，则返回一个空的LinkedHashSet。  
  	 *
	 * 这个方法会调用重载的 extractParamNames 方法，并默认将 aBraceCoupled 参数设为 true。
	 * 
	 * @see #extractParamNames(String, boolean)
	 */

	public static LinkedHashSet<String> extractParamNames(String aText)
	{
		return extractParamNames(aText, true) ;
	}
	
	/**
	 * 从给定的文本中提取参数名，并将它们存储在一个LinkedHashSet中返回。  
     * 参数名被假定为以'${'开头，以'}'结尾的字符串。  
     * 例如，给定文本"Hello, ${name}! Welcome to ${city}."，  
     * 方法将提取并返回包含"name"和"city"的LinkedHashSet。  
	 *
	 * @param aText 要处理的文本字符串，其中可能包含参数名。
	 * @param aBraceCoupled 一个布尔值，指示参数名是否必须被大括号紧密包裹。  
	 *                      如果为true，则参数名必须严格由'${'开头，'}'结尾，且内部不包含额外的'{'。  
	 *                      如果为false，则参数名可以由'${'开头，'}'结尾，但内部可以包含额外的'{'，  
	 *                      并且只有当遇到与开括号数量相匹配的闭括号时，才认为是一个完整的参数名。  
	 * @return 一个包含提取的参数名称的 LinkedHashSet，确保参数的顺序与其在文本中出现的顺序相同。
	 *
	 */
	public static LinkedHashSet<String> extractParamNames(String aText , boolean aBraceCoupled)
	{
		LinkedHashSet<String> paramNames = XC.linkedHashSet() ;
		char ch  ;
		int flag = 0 ;
		StringBuilder ckeyBld = null ;
		char[] chs = aText.toCharArray() ;
		final int len = chs.length ;
		int leftBraceCount = 0 ;
		for(int i=0 ; i<len ; i++)
		{
			ch = chs[i] ;
			switch(flag)
			{
			case 0:
				if(ch == '$')
				{
					flag = 1 ;
				}
				break ;
			case 1:
				if(ch == '{')
				{
					flag = 2 ;
					leftBraceCount++ ;
				}
				else
					flag = 0 ;
				break ;
			case 2:
				if(ch == '}' && (!aBraceCoupled || --leftBraceCount == 0))
				{
					flag = 0 ;
					String key = ckeyBld.toString().trim() ;
					paramNames.add(key) ;
					ckeyBld = null ;
				}
				else
				{
					if(ch == '{')
						leftBraceCount++ ;
					if(ckeyBld == null)
						ckeyBld = new StringBuilder() ;
					ckeyBld.append(ch) ;
				}
				break ;
			default:
				flag = 0 ;
			}
		}
		return paramNames ;
	}
	
	public static boolean haveParams(String aText)
	{
		char ch  ;
		int flag = 0 ;
		char[] chs = aText.toCharArray() ;
		final int len = chs.length ;
		for(int i=0 ; i<len ; i++)
		{
			ch = chs[i] ;
			switch(flag)
			{
			case 0:
				if(ch == '$')
				{
					flag = 1 ;
				}
				break ;
			case 1:
				if(ch == '{')
					flag = 2 ;
				else
					flag = 0 ;
				break ;
			case 2:
				if(ch == '}')
				{
					flag = 0 ;
					return true ;
				}
				break ;
			default:
				flag = 0 ;
			}
		}
		return false ;
	}
	
	public static LinkedHashSet<String> extractParamNames(Reader aReader) throws IOException
	{
		LinkedHashSet<String> paramNames = XC.linkedHashSet() ;
		int flag = 0 ;
		StringBuilder ckeyBld = null ;
		int ch = -1 ;
		while((ch = aReader.read()) != -1)
		{
			switch(flag)
			{
			case 0:
				if(ch == '$')
				{
					flag = 1 ;
				}
				break ;
			case 1:
				if(ch == '{')
					flag = 2 ;
				else
					flag = 0 ;
				break ;
			case 2:
				if(ch == '}')
				{
					flag = 0 ;
					String key = ckeyBld.toString().trim() ;
					paramNames.add(key) ;
					ckeyBld = null ;
				}
				else
				{
					if(ckeyBld == null)
						ckeyBld = new StringBuilder() ;
					ckeyBld.append((char)ch) ;
				}
				break ;
			default:
				flag = 0 ;
			}
		}
		return paramNames ;
	}
	
	public static String format(String aText , Map<String , ? extends Object> aCtx)
	{
		return format(aText, aCtx, true) ;
	}
	
	/**  
	 * 格式化给定的文本字符串，将文本中的占位符替换为上下文中的对应值。  
	 *   
	 * <p>占位符的格式为 `${key}`，其中 `key` 是上下文 `aCtx` 中的键。</p>  
	 *   
	 * <p>如果上下文 `aCtx` 中存在与占位符键对应的值，则将该值替换到文本中；如果不存在，  
	 * 则将占位符 `${key}` 保留在文本中。</p>  
	 *   
	 * @param aText 需要格式化的文本字符串，其中可以包含 `${key}` 格式的占位符。  
	 * @param aCtx 包含替换值的上下文，键为字符串类型，值为任意对象类型。  
	 * @return 格式化后的文本字符串，占位符被替换为上下文中的对应值（如果存在），  
	 *         或者返回原始文本字符串（如果上下文中没有对应的值，并且没有发生任何替换）。  
	 */
	public static String format(String aText , Map<String , ? extends Object> aCtx
			, boolean aBraceCoupled)
	{
		char ch  ;
		int flag = 0 ;
		StringBuilder ckeyBld = null ;
		StringBuilder newTextBld = null ;
		int pos = 0 ;
		char[] chs = aText.toCharArray() ;
		final int len = chs.length ;
		int leftBraceCount = 0 ;
		for(int i=0 ; i<len ; i++)
		{
			ch = chs[i] ;
			switch(flag)
			{
			case 0:
				if(ch == '$')
				{
					flag = 1 ;
					pos = i ;
				}
				else if(newTextBld != null)
					newTextBld.append(ch) ;
				break ;
			case 1:
				if(ch == '{')
				{
					flag = 2 ;
					leftBraceCount++ ;
				}
				else
				{
					flag = 0 ;
					newTextBld.append('$')
						.append(ch) ;
				}
				break ;
			case 2:
				if(ch == '}' && (!aBraceCoupled || --leftBraceCount == 0))
				{
					flag = 0 ;
					String key = ckeyBld.toString().trim() ;
					Object val = aCtx.get(key) ;
					if(val != null)
					{
						if(newTextBld == null)
						{
							newTextBld = new StringBuilder() ;
							newTextBld.append(aText, 0, pos) ;
						}
						newTextBld.append(val.toString()) ;
					}
					else
					{
						if(newTextBld == null)
							newTextBld = new StringBuilder() ;
						newTextBld.append("${").append(ckeyBld).append('}') ;	
					}	
					ckeyBld = null ;
				}
				else
				{
					if(ch == '{')
						leftBraceCount++ ;
					if(ckeyBld == null)
						ckeyBld = new StringBuilder() ;
					ckeyBld.append(ch) ;
				}
				break ;
			default:
				flag = 0 ;
			}
		}
		return newTextBld != null?newTextBld.toString():aText ;
	}
	
	/**
	 * 拼接成字符串
	 * @param aSegs				其中null元素将被忽略
	 * @return
	 */
	public static String splice(Object...aSegs)
	{
		if(XC.isEmpty(aSegs))
			return "" ;
		StringBuilder strBld = new StringBuilder() ;
		for(Object seg : aSegs)
		{
			if(seg != null)
				strBld.append(seg.toString()) ;
		}
		return strBld.toString() ;
	}
	
	public static String[] split(String aStr , String aReg)
	{
		return aStr==null?null:aStr.split(aReg) ;
	}
	
	public static Collection<String> split(final CharSequence s , char aSplitChar)
	{
        if(s.length() == 0)
            return Collections.emptyList() ;
        
        final List<String> list = new ArrayList<String>();
        final StringBuilder buf = new StringBuilder();
        final int len = s.length() ;
        char current ;
        for (int i=0 ; i<len ; i++)
        {
            current = s.charAt(i) ;
            if (aSplitChar == current)
            {
                list.add(buf.toString());
                buf.setLength(0);
            }
            else
                buf.append(current);
        }
        list.add(buf.toString()) ;
        return list;
    }
	
	public static List<String> split(final CharSequence s, final BitSet separators)
	{
        if(s.length() == 0)
            return Collections.emptyList();
        
        final List<String> list = new ArrayList<String>();
        final StringBuilder buf = new StringBuilder();
        final int len = s.length() ;
        char current ;
        for (int i=0 ; i<len ; i++)
        {
            current = s.charAt(i) ;
            if (separators.get(current))
            {
                list.add(buf.toString());
                buf.setLength(0);
            }
            else
                buf.append(current);
        }
        list.add(buf.toString()) ;
        return list;
    }
	
	/**
	 * 
	 * @param aJoin
	 * @param aSegs
	 * @return
	 */
	public static String join(String aJoinStr , Object...aSegs)
	{
		if(XC.isEmpty(aSegs))
			return "" ;
		StringBuilder strBld = new StringBuilder() ;
		boolean first = true ;
		for(Object seg : aSegs)
		{
			if(seg != null)
			{
				if(first)
					first = false ;
				else
					strBld.append(aJoinStr) ;
				strBld.append(seg.toString()) ;
			}
		}
		return strBld.toString() ;
	}
	
	public static String trim(String aText)
	{
		if(aText != null)
		{
			int len = aText.length() ; 
	        int st = 0;
	        char ch = 0 ;
	        while ((st < len) && ((ch = aText.charAt(st)) <= ' ' || ch == 65279)) {
	            st++;
	        }
	        while ((st < len) && ((ch = aText.charAt(len - 1)) <= ' ' || ch == 65279)) {
	            len--;
	        }
	        return ((st > 0) || (len < aText.length())) ? aText.substring(st, len) : aText ;
		}
		return null ;
	}
	
	public static String trim(String aText , char aCh)
	{
		if(aText != null)
		{
			int len = aText.length() ; 
	        int st = 0;
	        while ((st < len) && (aText.charAt(st) == aCh)) {
	            st++;
	        }
	        while ((st < len) && (aText.charAt(len - 1) == aCh)) {
	            len--;
	        }
	        return ((st > 0) || (len < aText.length())) ? aText.substring(st, len) : aText ;
		}
		return null ;
	}
	
	/**
	 * 在源数组上修改，返回的是源数组
	 * @param aTexts
	 * @return
	 */
	public static String[] trimArray(String...aTexts)
	{
		if(XC.isEmpty(aTexts))
			return aTexts ;
		for(int i=0 ; i<aTexts.length ; i++)
		{
			aTexts[i] = trim(aTexts[i]) ;
		}
		return aTexts ;
	}
	
	/**
	 * 如果指定字符串为null，将返回空字符串""，否则返回字符串本身
	 * @param aStr
	 * @return
	 */
	public static String defaultString(String aStr)
	{
		return aStr==null?sEmpty:aStr ;
	}

	  // Abbreviating
    //-----------------------------------------------------------------------
    /**
     * <p>Abbreviates a String using ellipses. This will turn
     * "Now is the time for all good men" into "Now is the time for..."</p>
     *
     * <p>Specifically:</p>
     * <ul>
     *   <li>If the number of characters in {@code str} is less than or equal to 
     *       {@code maxWidth}, return {@code str}.</li>
     *   <li>Else abbreviate it to {@code (substring(str, 0, max-3) + "...")}.</li>
     *   <li>If {@code maxWidth} is less than {@code 4}, throw an
     *       {@code IllegalArgumentException}.</li>
     *   <li>In no case will it return a String of length greater than
     *       {@code maxWidth}.</li>
     * </ul>
     *
     * <pre>
     * StringUtils.abbreviate(null, *)      = null
     * StringUtils.abbreviate("", 4)        = ""
     * StringUtils.abbreviate("abcdefg", 6) = "abc..."
     * StringUtils.abbreviate("abcdefg", 7) = "abcdefg"
     * StringUtils.abbreviate("abcdefg", 8) = "abcdefg"
     * StringUtils.abbreviate("abcdefg", 4) = "a..."
     * StringUtils.abbreviate("abcdefg", 3) = IllegalArgumentException
     * </pre>
     *
     * @param str  the String to check, may be null
     * @param maxWidth  maximum length of result String, must be at least 4
     * @return abbreviated String, {@code null} if null String input
     * @throws IllegalArgumentException if the width is too small
     * @since 2.0
     */
    public static String abbreviate(final String str, final int maxWidth) {
        return abbreviate(str, 0, maxWidth);
    }
	
    /**
     * <p>Abbreviates a String using ellipses. This will turn
     * "Now is the time for all good men" into "...is the time for..."</p>
     *
     * <p>Works like {@code abbreviate(String, int)}, but allows you to specify
     * a "left edge" offset.  Note that this left edge is not necessarily going to
     * be the leftmost character in the result, or the first character following the
     * ellipses, but it will appear somewhere in the result.
     *
     * <p>In no case will it return a String of length greater than
     * {@code maxWidth}.</p>
     *
     * <pre>
     * StringUtils.abbreviate(null, *, *)                = null
     * StringUtils.abbreviate("", 0, 4)                  = ""
     * StringUtils.abbreviate("abcdefghijklmno", -1, 10) = "abcdefg..."
     * StringUtils.abbreviate("abcdefghijklmno", 0, 10)  = "abcdefg..."
     * StringUtils.abbreviate("abcdefghijklmno", 1, 10)  = "abcdefg..."
     * StringUtils.abbreviate("abcdefghijklmno", 4, 10)  = "abcdefg..."
     * StringUtils.abbreviate("abcdefghijklmno", 5, 10)  = "...fghi..."
     * StringUtils.abbreviate("abcdefghijklmno", 6, 10)  = "...ghij..."
     * StringUtils.abbreviate("abcdefghijklmno", 8, 10)  = "...ijklmno"
     * StringUtils.abbreviate("abcdefghijklmno", 10, 10) = "...ijklmno"
     * StringUtils.abbreviate("abcdefghijklmno", 12, 10) = "...ijklmno"
     * StringUtils.abbreviate("abcdefghij", 0, 3)        = IllegalArgumentException
     * StringUtils.abbreviate("abcdefghij", 5, 6)        = IllegalArgumentException
     * </pre>
     *
     * @param str  the String to check, may be null
     * @param offset  left edge of source String
     * @param maxWidth  maximum length of result String, must be at least 4
     * @return abbreviated String, {@code null} if null String input
     * @throws IllegalArgumentException if the width is too small
     * @since 2.0
     */
    public static String abbreviate(final String str, int offset, final int maxWidth) {
        if (str == null) {
            return null;
        }
        if (maxWidth < 4) {
            throw new IllegalArgumentException("Minimum abbreviation width is 4");
        }
        if (str.length() <= maxWidth) {
            return str;
        }
        if (offset > str.length()) {
            offset = str.length();
        }
        if (str.length() - offset < maxWidth - 3) {
            offset = str.length() - (maxWidth - 3);
        }
        final String abrevMarker = "...";
        if (offset <= 4) {
            return str.substring(0, maxWidth - 3) + abrevMarker;
        }
        if (maxWidth < 7) {
            throw new IllegalArgumentException("Minimum abbreviation width with offset is 7");
        }
        if (offset + maxWidth - 3 < str.length()) {
            return abrevMarker + abbreviate(str.substring(offset), maxWidth - 3);
        }
        return abrevMarker + str.substring(str.length() - (maxWidth - 3));
    }
    
	public static boolean contains(final String seq, final int searchChar)
	{
		if (isEmpty(seq))
			return false;
		return seq.indexOf(searchChar) != -1;
	}
	
	public static boolean contains(final String seq, String aText)
	{
		if (isEmpty(seq))
			return false;
		return seq.contains(aText) ;
	}
    
    public static String intern(String aText)
    {
    	return aText != null? aText.intern():null ;
    }
    
    /**
     * 只支持10以内罗马字符的转化。
     * @return		在无法转换的时候将返回null
     */
    public static Integer toIntOfRomanDigit(String aStr)
    {
    	Assert.notNull(aStr) ;
    	aStr.trim() ;
    	switch(aStr)
    	{
    	case sRDS_1:
    	case "I":
    		return 1 ;
    	case sRDS_2:
    	case "II":
    		return 2 ;
    	case sRDS_3:
    	case "III":
    		return 3;
    	case sRDS_4:
    		return 4;
    	case sRDS_5:
    		return 5;
    	case sRDS_6:
    		return 6;
    	case sRDS_7:
    		return 7;
    	case sRDS_8:
    		return 8 ;
    	case sRDS_9:
    		return 9 ;
    	case sRDS_10:
    		return 10 ;
    	default:
    		return null ;
    	}
    }
    
    public static String getLegalRomanDigit(String aStr)
    {
    	Assert.notNull(aStr) ;
    	aStr.trim() ;
    	return switch(aStr)
    	{
    	case sRDS_1 , "I" -> sRDS_1 ;
    	case sRDS_2 , "II" -> sRDS_2 ;
    	case sRDS_3 , "III" -> sRDS_3 ;
    	case sRDS_4 -> sRDS_4 ;
    	case sRDS_5 -> sRDS_5 ;
    	case sRDS_6 -> sRDS_6 ;
    	case sRDS_7 -> sRDS_7;
    	case sRDS_8 -> sRDS_8 ;
    	case sRDS_9 -> sRDS_9 ;
    	case sRDS_10 -> sRDS_10 ;
    	default -> throw new IllegalStateException("不能转为罗马字符："+aStr) ;
    	} ;
    }
    
    public static String[] allNotEmptyGroups(Matcher aMatcher)
    {
    	int count = aMatcher.groupCount() ;
		List<String> segList = new ArrayList<>() ;
		for(int i=1 ; i<=count ; i++)
		{
			String seg = aMatcher.group(i) ;
			if(XString.isNotEmpty(seg))
				segList.add(seg) ;
		}
		return segList.toArray(JCommon.sEmptyStringArray) ;
    }
    
    /**
     * 是否包含中文
     * @param aText
     * @return
     */
    public static boolean containsChinese(String aText)
    {
    	if(isEmpty(aText))
    		return false ;
    	final int len = aText.length() ;
    	for(int i=0 ; i<len ; i++)
    	{
    		if(isChinese(aText.charAt(i)))
    			return true ;
    	}
    	return false ;
    }
    
    public static int length(String aText)
    {
    	return aText==null?0:aText.length() ;
    }
    
    public static char randomChar()
    {
    	return sDigits[(int)(Math.random()*sDigits.length)] ;
    }
    
    /**
     * 从aFrom处开始往后第一个非空白（不可显）字符
     * @param aText
     * @param aFrom
     * @return
     */
    public static final Character nextUnblankChar(String aText , int aFrom)
    {
    	final int len = aText.length() ;
    	for(int i=aFrom ; i<len ; i++)
    	{
    		char ch = aText.charAt(i) ;
    		if(!Character.isWhitespace(ch))
    			return ch ;
    	}
    	return null ;
    }
    
    public static char lastChar(String aText)
    {
    	Assert.notEmpty(aText) ;
    	return aText.charAt(aText.length()-1) ;
    }
    
    public static String nullIfEmpty(String aText)
    {
    	return isEmpty(aText)?null:aText ;
    }
    
    public static String escape(String aText , BiConsumer<StringBuilder, Character> aHandler , char...aChars)
    {
    	if(aText == null || aText.length() == 0)
    		return aText ;
    	StringBuilder strBld = null ;
    	final int strLen = aText.length() ;
    	if(aChars.length>5)
    	{
    		TCharSet charSet = new TCharHashSet(aChars) ;
    		for(int i=0 ; i<strLen ; i++)
    		{
    			if(charSet.contains(aText.charAt(i)))
    			{
    				if(strBld == null)
    				{
    					strBld = new StringBuilder(strLen + 2) ;
    					strBld.append(aText, 0 , i) ;
    				}
    				aHandler.accept(strBld, aText.charAt(i)) ;
    			}
    			else if(strBld != null)
    				strBld.append(aText.charAt(i)) ;
    		}
    	}
    	else
    	{
    		for(int i=0 ; i<strLen ; i++)
    		{
    			if(XC.contains(aChars, aText.charAt(i)))
    			{
    				if(strBld == null)
    				{
    					strBld = new StringBuilder(strLen + 2) ;
    					strBld.append(aText, 0 , i) ;
    				}
    				aHandler.accept(strBld, aText.charAt(i)) ;
    			}
    			else if(strBld != null)
    				strBld.append(aText.charAt(i)) ;
    		}
    	}
    	return strBld != null?strBld.toString():aText ;
    }
    
	/**
	 * 将经纬度转成“度分秒”的格式
	 * @param aValue
	 * @param aIsLongitude 				true表示是经度，false表示是纬度
	 * @return
	 */
	public static String toTextOfGeoCoord(Double aValue , boolean aIsLongitude)
	{
		if(aValue == null)
			return null ;
		double val = Math.abs(aValue.doubleValue()) ;
		int degrees = (int) val;
		int minutes = (int) ((val - degrees) * 60);
		double seconds = (((val-degrees) * 60) - minutes) * 60 ;
		seconds = XMath.retainEffectDigits(seconds , 3) ;
		
		return XString.splice(degrees , XString.sAngle_Degrees
				, minutes , XString.sAngle_Minutes 
				, seconds , XString.sAngle_Seconds 
				, aIsLongitude?(aValue>=0?"E":"W"):(aValue>=0?"N":"S")) ;
	}
	
	/**
	 * 北纬和东经为正，南纬和西经为负
	 * @param aText
	 * @return
	 */
	public static Double toDoubleOfGeoCoord(String aText)
	{
		if(isEmpty(aText))
			return null ;
		Matcher matcher = sPtn_GeoCood.matcher(aText) ;
		if(!matcher.matches())
			return null ;
		int degrees = Integer.parseInt(matcher.group(1)) ;
		int minutes = Integer.parseInt(matcher.group(2)) ;
		double seconds = Double.parseDouble(matcher.group(3)) ;
		
		double decimal = minutes/60.0 ;
		decimal += seconds/3600.0 ;
		decimal += degrees ;
		//保留6位小数
		decimal = XMath.retainEffectDigits(decimal , 6) ;
		String g4 = matcher.group(4) ;
		int sign = "W".equalsIgnoreCase(g4) || "S".equals(g4)?-1:1 ;
		return decimal * sign ;
	}
	
	public static void toLowerCase(String[] aStrArray)
	{
		if(XC.isEmpty(aStrArray))
			return ;
		int i = aStrArray.length ;
		while(i-->0)
		{
			if(aStrArray[i] != null)
				aStrArray[i] = aStrArray[i].toLowerCase() ;
		}
	}
	
	public static String applyPlaceHolder(String text
			, Map<String , ? extends Object> aParamValues)
	{
		return applyPlaceHolder("{", "}", text, aParamValues) ;
	}
	
	/**
	 * 如果openToken前面是有“\”的，则认为它被转义了		<br />
	 * 参数表达式中“:”后面的看成缺省值			<br />
	 * 没有缺省值，在参数值中又没有指定，则保留原有参数不变
	 * @param openToken
	 * @param closeToken
	 * @param text
	 * @param aParamValues
	 * @return
	 */
	public static String applyPlaceHolder(String openToken 
			, String closeToken
			, String text
			, Map<String , ? extends Object> aParamValues)
	{
		if (text == null || text.isEmpty())
		{
			return "";
		}
		char[] src = text.toCharArray();
		int offset = 0;
		// search open token
		int start = text.indexOf(openToken, offset);
		if (start == -1)
		{
			return text;
		}
		final StringBuilder builder = new StringBuilder();
		StringBuilder expression = null;
		while (start > -1)
		{
			if (start > 0 && src[start - 1] == '\\')
			{
				// this open token is escaped. remove the backslash and continue.
				builder.append(src, offset, start - offset - 1).append(openToken);
				offset = start + openToken.length();
			}
			else
			{
				// found open token. let's search close token.
				if (expression == null)
				{
					expression = new StringBuilder();
				}
				else
				{
					expression.setLength(0);
				}
				builder.append(src, offset, start - offset);
				offset = start + openToken.length();
				int end = text.indexOf(closeToken, offset);
				while (end > -1)
				{
					if (end > offset && src[end - 1] == '\\')
					{
						// this close token is escaped. remove the backslash and continue.
						expression.append(src, offset, end - offset - 1).append(closeToken);
						offset = end + closeToken.length();
						end = text.indexOf(closeToken, offset);
					}
					else
					{
						expression.append(src, offset, end - offset);
						offset = end + closeToken.length();
						break;
					}
				}
				if (end == -1)
				{
					// close token was not found.
					builder.append(src, start, src.length - start);
					offset = src.length;
				}
				else
				{
					///////////////////////////////////////仅仅修改了该else分支下的个别行代码////////////////////////	
					int i = expression.indexOf(":") ;
					String paramName = i == -1?expression.toString():expression.substring(0, i) ;	
					Object value = aParamValues.get(paramName) ;
					if(value == null)
					{
						if(i== -1)
							value = new String(src , start , end + closeToken.length()-start) ;
						else
							value = expression.substring(i+1) ;
					}
					builder.append(value);
					offset = end + closeToken.length();
					////////////////////////////////////////////////////////////////////////////////////////////////
				}
			}
			start = text.indexOf(openToken, offset);
		}
		if (offset < src.length)
		{
			builder.append(src, offset, src.length - offset);
		}
		return builder.toString();
	}
	
	public static String applyPlaceHolder(String aDefault , String openToken, String closeToken, String text
			, Object... aArgs)
	{
		int argsIndex = 0;
		if (text == null || text.isEmpty())
		{
			return "";
		}
		char[] src = text.toCharArray();
		int offset = 0;
		// search open token
		int start = text.indexOf(openToken, offset);
		if (start == -1)
		{
			return text;
		}
		final StringBuilder builder = new StringBuilder();
		StringBuilder expression = null;
		while (start > -1)
		{
			if (start > 0 && src[start - 1] == '\\')
			{
				// this open token is escaped. remove the backslash and continue.
				builder.append(src, offset, start - offset - 1).append(openToken);
				offset = start + openToken.length();
			}
			else
			{
				// found open token. let's search close token.
				if (expression == null)
				{
					expression = new StringBuilder();
				}
				else
				{
					expression.setLength(0);
				}
				builder.append(src, offset, start - offset);
				offset = start + openToken.length();
				int end = text.indexOf(closeToken, offset);
				while (end > -1)
				{
					if (end > offset && src[end - 1] == '\\')
					{
						// this close token is escaped. remove the backslash and continue.
						expression.append(src, offset, end - offset - 1).append(closeToken);
						offset = end + closeToken.length();
						end = text.indexOf(closeToken, offset);
					}
					else
					{
						expression.append(src, offset, end - offset);
						offset = end + closeToken.length();
						break;
					}
				}
				if (end == -1)
				{
					// close token was not found.
					builder.append(src, start, src.length - start);
					offset = src.length;
				}
				else
				{
					///////////////////////////////////////仅仅修改了该else分支下的个别行代码////////////////////////	
					String value = (argsIndex <= aArgs.length - 1)
							? (aArgs[argsIndex] == null ? aDefault : aArgs[argsIndex].toString())
							: aDefault ;
					builder.append(value);
					offset = end + closeToken.length();
					argsIndex++;
					////////////////////////////////////////////////////////////////////////////////////////////////
				}
			}
			start = text.indexOf(openToken, offset);
		}
		if (offset < src.length)
		{
			builder.append(src, offset, src.length - offset);
		}
		return builder.toString();
	}
	
	public static String applyPlaceHolder(boolean aWipeAwayPlaceHolder , String openToken, String closeToken, String text, Object... args)
	{
		if (args == null || args.length <= 0
				&& !aWipeAwayPlaceHolder)
		{
			return text;
		}
		int argsIndex = 0;
		if (text == null || text.isEmpty())
		{
			return "";
		}
		char[] src = text.toCharArray();
		int offset = 0;
		// search open token
		int start = text.indexOf(openToken, offset);
		if (start == -1)
		{
			return text;
		}
		final StringBuilder builder = new StringBuilder();
		StringBuilder expression = null;
		while (start > -1)
		{
			if (start > 0 && src[start - 1] == '\\')
			{
				// this open token is escaped. remove the backslash and continue.
				builder.append(src, offset, start - offset - 1).append(openToken);
				offset = start + openToken.length();
			}
			else
			{
				// found open token. let's search close token.
				if (expression == null)
				{
					expression = new StringBuilder();
				}
				else
				{
					expression.setLength(0);
				}
				builder.append(src, offset, start - offset);
				offset = start + openToken.length();
				int end = text.indexOf(closeToken, offset);
				while (end > -1)
				{
					if (end > offset && src[end - 1] == '\\')
					{
						// this close token is escaped. remove the backslash and continue.
						expression.append(src, offset, end - offset - 1).append(closeToken);
						offset = end + closeToken.length();
						end = text.indexOf(closeToken, offset);
					}
					else
					{
						expression.append(src, offset, end - offset);
						offset = end + closeToken.length();
						break;
					}
				}
				if (end == -1)
				{
					// close token was not found.
					builder.append(src, start, src.length - start);
					offset = src.length;
				}
				else
				{
					///////////////////////////////////////仅仅修改了该else分支下的个别行代码////////////////////////	
					String value = (argsIndex <= args.length - 1)
							? (args[argsIndex] == null ? "" : args[argsIndex].toString())
							: expression.toString();
					builder.append(value);
					offset = end + closeToken.length();
					argsIndex++;
					if(!aWipeAwayPlaceHolder && argsIndex >= args.length)
						break ;
					////////////////////////////////////////////////////////////////////////////////////////////////
				}
			}
			start = text.indexOf(openToken, offset);
		}
		if (offset < src.length)
		{
			builder.append(src, offset, src.length - offset);
		}
		return builder.toString();
	}
	
	/**
	 * 如果参数位置多余实际指定的参数数量，参数占位符将被去除，参数里面如果有参数名，将会被保留
	 * @param aFmt
	 * @param aArgs
	 * @return
	 */
	public static String msgFmt(String aFmt , Object...aArgs)
	{
		return XC.isEmpty(aArgs)?aFmt:applyPlaceHolder(true , "{", "}",aFmt, aArgs)  ;
	}
	
	/**
	 * 如果参数位置多余实际指定的参数数量，保留参数位置的内容不便
	 * @param aFmt
	 * @param aArgs
	 * @return
	 */
	public static String msgFmt_1(String aFmt , Object...aArgs)
	{
		return XC.isEmpty(aArgs)?aFmt:applyPlaceHolder(false , "{", "}",aFmt, aArgs)  ;
	}
	
	/**
	 * 
	 * @param aFmt
	 * @param aDefault		缺省值，没有指定参数时，使用aDefault缺省值占位
	 * @param aArgs
	 * @return
	 */
	public static String msgFmt_2(String aFmt , String aDefault , Object...aArgs)
	{
		return applyPlaceHolder(aDefault , "{", "}",aFmt, aArgs)  ;
	}
	
	public static String toString_friendly(double aVal , String aSuffix)
	{
		int rank = XMath.getRank(aVal) ;
		String unit = "" ;
		String[] units = new String[] {"万" , "亿"} ;
		if(rank>=4)
		{
			int j = rank/4 ;
			if(j>2)
				rank = 2 ;
			long d = (long)Math.pow(10, rank) ;
			unit = units[j-1] ;
			aVal = aVal/d ;
		}
		if(XString.isNotEmpty(aSuffix))
			unit += aSuffix ;
		return XMath.retainEffectDigits(aVal, 2)+unit ;
		
	}
	
	public static int firstUnequalsPosition(String aStr1 , String aStr2)
	{
		if(isEmpty(aStr1) || aStr2.isEmpty())
			return -1 ;
		final int minLen = Math.min(aStr1.length() , aStr2.length()) ;
		for(int i=0 ; i<minLen ; i++)
		{
			if(aStr1.charAt(i) != aStr2.charAt(i))
				return i ;
		}
		return minLen ;
	}
	
	public static boolean endsWith(CharSequence aBuf , String aTestStr)
	{
		if(aBuf == null || aBuf.length()<aTestStr.length())
			return false ;
		final int len = aTestStr.length() ;
		int offset = aBuf.length() - len ;
		for(int i=0 ; i<len ; i++)
		{
			if(aBuf.charAt(offset+i) != aTestStr.charAt(i))
				return false ;
		}
		return true ;
	}
	
	/**
	 * 移除下划线，将下划线后面紧跟的小写字母转变成大些字母
	 * @param aText
	 * @return
	 */
	public static String removeUnderLine(String aText)
	{
		if(XString.isEmpty(aText))
			return aText ;
		char[] chs = aText.toCharArray() ;
		int i=0 , last=-1  ;
		boolean nextUpper = false ;
		StringBuilder strBld = null ;
		while(i<chs.length)
		{
			if(chs[i] == '_')
				nextUpper = true ;
			else 
			{
				if(nextUpper)
				{
					if(strBld == null)
					{
						strBld = new StringBuilder() ;
						if(last>=0)
							strBld.append(chs, 0, last+1) ;
					}
					strBld.append(Character.toUpperCase(chs[i])) ;
					nextUpper = false ;
				}
				else if(strBld != null)
					strBld.append(chs[i]) ;
				else
					last = i ;
			}
			i++ ;
		}
		return strBld == null? aText : strBld.toString() ;
	}
	
	public static boolean equalsStrIgnoreCase(String aStr0 , String aStr1)
	{
		return aStr0!=null?aStr0.equalsIgnoreCase(aStr1):aStr1==null ;
	}
}
