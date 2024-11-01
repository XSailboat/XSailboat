package team.sailboat.commons.fan.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * 汉语拼音
 *
 * @author yyl
 * @since 2024年10月15日
 */
public class ChineseSpelling
{
	
	static String[] sPinYins = null ;
	
	static
	{
		sPinYins = new String[65536] ;
		URL url = ChineseSpelling.class.getResource("unicode_to_hanyu_pinyin.txt") ;
		try
		{
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream() , "UTF-8")))
			{
				String line = null ;
				while((line = reader.readLine()) != null)
				{
					int ucode = Integer.parseInt(line.substring(0, 4), 16) ;
					sPinYins[ucode] = line.substring(6, XString.indexOf(line, 7, ')' , ',')-1).intern() ;
				}
			}
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e) ;
		}
	}
	
	/**
	 * 获取汉字拼音首字母缩写
	 * 
	 * @param aText
	 * @param aCharChecker			如果不是汉字，那么这个字符是否接受的判断方法。<br>
	 * 								当为null时，表示在返回结果中不包含任何非汉字拼音首字母<gr>
	 * 								如果判断结果为true，那么将添加到返回结果中
	 * @param aCharAmountLimit		返回结果的字符数量限制。只返回不大于指定字符数量的字符串
	 * @return
	 */
	public static String getAbbreviation(String aText , Predicate<Character> aCharChecker , int aCharAmountLimit)
	{
		if(XString.isEmpty(aText))
			return aText ;
		char[] chs = aText.toCharArray() ;
		StringBuilder strBld = new StringBuilder() ;
		for(char ch : chs)
		{
			if(XString.isChinese(ch))
				strBld.append(sPinYins[ch]==null?"?":sPinYins[ch].charAt(0)) ;
			else if(aCharChecker != null && aCharChecker.test(ch))
				strBld.append(ch) ;
			if(aCharAmountLimit>0 && aCharAmountLimit==strBld.length())
				break ;
		}
		return strBld.toString() ;
	}
	
	public static String getPinYin(char aCh)
	{
		return sPinYins[aCh] ;
	}

	/**
	 * 获取汉字拼音
	 * @param aChsStr
	 * @return
	 */
	public static String getSpelling(String aChsStr)
	{
		if(XString.isEmpty(aChsStr))
			return aChsStr ;
		
		final int len = aChsStr.length() ;
		char ch  ;
		StringBuilder strBld = null ;
		for(int i=0 ; i<len ; i++)
		{
			ch = aChsStr.charAt(i) ;
			if(XString.isChinese(ch))
			{
				if(strBld == null)
					strBld = new StringBuilder().append(aChsStr, 0, i) ;
				strBld.append(sPinYins[ch]!=null?sPinYins[ch]:ch) ;
			}
			else if(strBld != null)
				strBld.append(ch) ;
		}
		return strBld == null?aChsStr:strBld.toString() ;
	}
	
	public static Collection<String> getProbablePinYins(String aChsStr)
	{
		if(XString.isEmpty(aChsStr))
			return Collections.emptyList() ;
		
		final int len = aChsStr.length() ;
		char ch  ;
		List<String>[] lists = new List[len] ;
		for(int i=0 ; i<len ; i++)
		{
			ch = aChsStr.charAt(i) ;
			if(XString.isChinese(ch))
			{
				lists[i] = sPinYins[ch] == null?Arrays.asList(Character.toString(ch))
						: getSimilarPinYins(sPinYins[ch]) ;
			}
			else
				lists[i] = Arrays.asList(Character.toString(ch)) ;
		}
		List<String> list = new ArrayList<>(lists[0]) ;
		for(int i=1 ; i<len ; i++)
		{
			int size = list.size() ;
			for(String py : lists[i])
			{
				for(int k=0 ; k<size ; k++)
					list.add(list.get(k)+py) ;
			}
			list.subList(0, size).clear();
		}
		return list ;
	}
	
	public static List<String> getSimilarPinYins(String aPinYin)
	{
		List<String> pinyinList = new ArrayList<>() ;
		pinyinList.add(aPinYin) ;
		if(aPinYin.length()>=2)
		{
			char ch = aPinYin.charAt(0) ;
			switch(ch)
			{
			case 'z':
			case 'c':
			case 's':
				if(aPinYin.charAt(1) == 'h')
				{
					pinyinList.add(ch+aPinYin.substring(2)) ;
				}
				else
				{
					pinyinList.add(ch+"h"+aPinYin.substring(1)) ;
				}
				break ;
			case 'r':
				pinyinList.add("l"+aPinYin.substring(1)) ;
				break ;
			case 'l':
				pinyinList.add("r"+aPinYin.substring(1)) ;
				break ;
			}
			
			ch = aPinYin.charAt(aPinYin.length()-1) ;
			if(ch == 'g')
			{
				if((aPinYin.endsWith("ang") && !aPinYin.endsWith("iang")) || aPinYin.endsWith("eng")
						|| aPinYin.endsWith("ing"))
				{
					int size = pinyinList.size() ;
					for(int i=0 ; i<size ; i++)
					{
						String pinyin = pinyinList.get(i) ;
						pinyinList.add(pinyin.substring(0, pinyin.length()-1)) ;
					}
				}	
			}
			else
			{
				if((aPinYin.endsWith("an") && !aPinYin.endsWith("ian")) 
						|| aPinYin.endsWith("en")
						|| aPinYin.endsWith("in"))
				{
					int size = pinyinList.size() ;
					for(int i=0 ; i<size ; i++)
					{
						pinyinList.add(pinyinList.get(i)+"g") ;
					}
				}	
			}
		}
		return pinyinList ;
	}

	public static void main(String[] args)
	{
		System.out.println(getSpelling("abc中文字符dd:江d苏,南京崀崊"));
//		System.out.println(Integer.toHexString((int)'崊'));
//		System.out.println((char)Integer.parseInt("4E0D", 16));
	}
}
