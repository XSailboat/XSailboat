package team.sailboat.commons.fan.text;

import java.util.Comparator;

/**
 * 比较字符串，如果是中文字符将其转化为拼音再比较，如果不是中文字符，则按其编码顺序排列
 *
 * @author yyl
 * @since 2022年9月2日
 */
public class ChineseComparator implements Comparator<Object>
{
	static ChineseComparator sInstance = new ChineseComparator() ;
	
	public static ChineseComparator getInstance()
	{
		return sInstance ;
	}
	
	@Override
	public int compare(Object aO1, Object aO2)
	{
		if(aO1==aO2) return 0 ;
		else if(aO1==null) return 1 ;
		else if(aO2 == null) return -1 ;
		String s1 = aO1.toString() ;
		String s2 = aO2.toString() ;
		int i = 0;
		for(; i<s1.length()&&i<s2.length() ; i++)
		{
			int result = compare(s1.charAt(i) , s2.charAt(i)) ;
			if(result != 0) return result ;
		}
		if(i<s1.length())
			return -1 ;
		else if(i<s2.length())
			return 1 ;
		else return 0 ;
	}

	private int compare(char ch1 , char ch2)
	{
		return getPinyin(ch1).compareTo(getPinyin(ch2)) ;
	}
	
	static String getPinyin(char ch)
	{
		if(!XString.isChinese(ch))
			return Character.toString(Character.toLowerCase(ch)) ;
		
		String pinyin = ChineseSpelling.getPinYin(ch) ;
		return pinyin==null?Character.toString(Character.toLowerCase(ch)):pinyin ;
	}
	
	public static String getPinyin(String aText)
	{
		StringBuilder strBld = new StringBuilder() ;
		char[] chars = aText.toCharArray() ;
		for(char ch : chars)
			strBld.append(getPinyin(ch)) ;
		return strBld.toString() ;
	}
	
	/**
	 * 拼音首字母
	 * @return
	 */
	public static String getPinyinInitials(String aText)
	{
		StringBuilder strBld = new StringBuilder() ;
		char[] chars = aText.toCharArray() ;
		for(char ch : chars)
			strBld.append(getPinyin(ch).charAt(0)) ;
		return strBld.toString() ;
	}
	
	public static int comparePingYin(String aText0 , String aText1)
	{
		return sInstance.compare(aText0, aText1) ;
	}
	
}
