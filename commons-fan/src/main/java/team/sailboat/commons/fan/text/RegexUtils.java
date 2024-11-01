package team.sailboat.commons.fan.text;

import java.util.regex.Pattern;

import gnu.trove.set.TCharSet;
import gnu.trove.set.hash.TCharHashSet;
import team.sailboat.commons.fan.lang.Assert;

/**
 * 正则工具类
 * 提供验证邮箱、手机号、电话号码、身份证号码、数字等方法
 */ 
public final class RegexUtils
{ 
	static Pattern sIPv4Ptn ;
	
	/**
	 * yyyy-MM-dd HH:mm:ss.SSS
	 */
	static Pattern sDT_$yyyyMMddHHmmssSSS ;
	
	static final Pattern sLegalDBFieldNamePtn = Pattern.compile("^[a-zA-Z_\u4E00-\u9FA5]([0-9a-zA-Z_\u4E00-\u9FA5]*|([0-9a-zA-Z_\u4E00-\u9FA5]*\\.[0-9a-zA-Z_\u4E00-\u9FA5]+))$") ;
	
	static final Pattern sLegalFieldNameNoCnPtn = Pattern.compile("^[a-zA-Z_]([0-9a-zA-Z_]*)$") ;
	
	static final Pattern sLegalFieldNameNoCnPtn_lowerCase = Pattern.compile("^[a-z_]([0-9a-z_]*)$") ;
	
	static final TCharSet sSpecialChars = new TCharHashSet(new char[] {'\\', '$' , '(', ')', '*', '+', '.', '[', ']', '?'
			, '^', '{', '}', '|'})  ;
	
	static final Pattern sIntegerPattern = Pattern.compile("^[-\\+]?[\\d]+$") ;
			
	
	public static String checkDBFieldName(String aFieldName)
	{
		Assert.isTrue(sLegalDBFieldNamePtn.matcher(aFieldName).matches() 
				, "数据库字段名只能是下划线，大小写字母，数字和汉字构成，且第一个字母非数字，在非收尾位置可以最多包含一个“.”，不能是：%s" , aFieldName) ;
		return aFieldName ;
	}
	
	/**
	 * 字段名只能是下划线，小写字母，数字构成，且第一个字母非数字
	 * 
	 * @param aFieldName
	 * @return
	 */
	public static String checkFieldNameNoCn(String aFieldName)
	{
		Assert.isTrue(sLegalFieldNameNoCnPtn.matcher(aFieldName).matches() 
				, "字段名只能是下划线，大小写字母，数字构成，且第一个字母非数字，不能是：%s" , aFieldName) ;
		return aFieldName ;
	}
	
	/**
	 * 字段名只能是下划线，小写字母，数字构成，且第一个字母非数字
	 * 
	 * @param aFieldName
	 * @return
	 */
	public static String checkFieldName_LowerCase(String aFieldName)
	{
		Assert.isTrue(sLegalFieldNameNoCnPtn_lowerCase.matcher(aFieldName).matches() 
				, "字段名只能是下划线，小写字母，数字构成，且第一个字母非数字，不能是：%s" , aFieldName) ;
		return aFieldName ;
	}
	
	/**
	 * 字段名只能是下划线，小写字母，数字构成，且第一个字母非数字
	 * @param aFieldName
	 * @return
	 */
	public static boolean isFieldName_LowerCase(String aFieldName)
	{
		return sLegalFieldNameNoCnPtn_lowerCase.matcher(aFieldName).matches()  ;
	}
	
    /**
     * 验证Email
     * @param email email地址，格式：zhangsan@sina.com，zhangsan@xxx.com.cn，xxx代表邮件服务商
     * @return 验证成功返回true，验证失败返回false
     */ 
    public static boolean checkEmail(String email) { 
        String regex = "\\w+@\\w+\\.[a-z]+(\\.[a-z]+)?";
        return Pattern.matches(regex, email); 
    } 
     
    /**
     * 验证身份证号码
     * @param idCard 居民身份证号码15位或18位，最后一位可能是数字或字母
     * @return 验证成功返回true，验证失败返回false
     */ 
    public static boolean checkIdCard(String idCard) { 
        String regex = "[1-9]\\d{13,16}[a-zA-Z0-9]{1}"; 
        return Pattern.matches(regex,idCard); 
    } 
     
    /**
     * 验证手机号码（支持国际格式，+86135xxxx...（中国内地），+00852137xxxx...（中国香港））
     * @param mobile 移动、联通、电信运营商的号码段
     *<p>移动的号段：134(0-8)、135、136、137、138、139、147（预计用于TD上网卡）
     *、150、151、152、157（TD专用）、158、159、187（未启用）、188（TD专用）</p>
     *<p>联通的号段：130、131、132、155、156（世界风专用）、185（未启用）、186（3g）</p>
     *<p>电信的号段：133、153、180（未启用）、189</p>
     * @return 验证成功返回true，验证失败返回false
     */ 
    public static boolean checkMobile(String mobile) { 
        String regex = "(\\+\\d+)?1[3458]\\d{9}$"; 
        return Pattern.matches(regex,mobile); 
    } 
     
    /**
     * 验证固定电话号码
     * @param phone 电话号码，格式：国家（地区）电话代码 + 区号（城市代码） + 电话号码，如：+8602085588447
     * <p><b>国家（地区） 代码 ：</b>标识电话号码的国家（地区）的标准国家（地区）代码。它包含从 0 到 9 的一位或多位数字，
     *  数字之后是空格分隔的国家（地区）代码。</p>
     * <p><b>区号（城市代码）：</b>这可能包含一个或多个从 0 到 9 的数字，地区或城市代码放在圆括号——
     * 对不使用地区或城市代码的国家（地区），则省略该组件。</p>
     * <p><b>电话号码：</b>这包含从 0 到 9 的一个或多个数字 </p>
     * @return 验证成功返回true，验证失败返回false
     */ 
    public static boolean checkPhone(String phone) { 
        String regex = "(\\+\\d+)?(\\d{3,4}\\-?)?\\d{7,8}$"; 
        return Pattern.matches(regex, phone); 
    } 
     
    /**
     * 验证整数和浮点数（正负整数和正负浮点数）
     * @param decimals 一位或多位0-9之间的浮点数，如：1.23，233.30
     * @return 验证成功返回true，验证失败返回false
     */ 
    public static boolean checkDecimals(String decimals) { 
        String regex = "\\-?[1-9]\\d+(\\.\\d+)?"; 
        return Pattern.matches(regex,decimals); 
    }  
     
    /**
     * 验证空白字符
     * @param blankSpace 空白字符，包括：空格、\t、\n、\r、\f、\x0B
     * @return 验证成功返回true，验证失败返回false
     */ 
    public static boolean checkBlankSpace(String blankSpace) { 
        String regex = "\\s+"; 
        return Pattern.matches(regex,blankSpace); 
    } 
     
    /**
     * 验证中文
     * @param chinese 中文字符
     * @return 验证成功返回true，验证失败返回false
     */ 
    public static boolean checkChinese(String chinese) { 
        String regex = "^[\u4E00-\u9FA5]+$"; 
        return Pattern.matches(regex,chinese); 
    } 
     
    /**
     * 验证日期（年月日）
     * @param birthday 日期，格式：1992-09-03，或1992.09.03
     * @return 验证成功返回true，验证失败返回false
     */ 
    public static boolean checkBirthday(String birthday) { 
        String regex = "[1-9]{4}([-./])\\d{1,2}\\1\\d{1,2}"; 
        return Pattern.matches(regex,birthday); 
    } 
     
    /**
     * 验证URL地址
     * @param url 格式：http://blog.csdn.net:80/xyang81/article/details/7705960? 或 http://www.csdn.net:80
     * @return 验证成功返回true，验证失败返回false
     */ 
    public static boolean checkURL(String url) { 
        String regex = "(https?://(w{3}\\.)?)?\\w+\\.\\w+(\\.[a-zA-Z]+)*(:\\d{1,5})?(/\\w*)*(\\??(.+=.*)?(&.+=.*)?)?"; 
        return Pattern.matches(regex, url); 
    } 
     
    /**
     * 匹配中国邮政编码
     * @param postcode 邮政编码
     * @return 验证成功返回true，验证失败返回false
     */ 
    public static boolean checkPostcode(String postcode) { 
        String regex = "[1-9]\\d{5}"; 
        return Pattern.matches(regex, postcode); 
    } 
     
    /**
     * 匹配IP地址
     * @param ipAddress IPv4标准地址
     * @return 验证成功返回true，验证失败返回false
     */ 
    public static boolean checkIPv4(String ipAddress)
    {
    	if(sIPv4Ptn == null)
    		sIPv4Ptn = Pattern.compile("([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}") ; 
        return sIPv4Ptn.matcher(ipAddress).matches() ; 
    } 
    
    /**
     * 验证整数
     * @param integer
     * @return
     */
    public static boolean checkInteger(String integer)
    {
    	if(integer == null || integer.isEmpty())
    		return false ;
    	String regex = "^[\\-|\\+]?\\d+$" ;
    	return Pattern.matches(regex, integer) ;
    }
     
	public static boolean isDouble(String str)
	{
		if(str == null || str.isEmpty())
			return false ;
		String regex = "^[-\\+]?[.\\d]*$" ;
		return Pattern.matches(regex, str) ;
	}
	
	public static boolean isInteger(String aStr)
	{
		return sIntegerPattern.matcher(aStr).matches() ;
	}
	
	/**
	 * 用于判断名字有效的正则表达式
	 */
	public static String checkValidSchemaName()
	{
		return "[\\$\\p{Alnum}_[\u4e00-\u9fa5]]+[\\p{Alnum}_[\u4e00-\u9fa5]\\^/\\-\\$]*";
	}
	
	public static String escape(String aText)
	{
	    if (XString.isNotEmpty(aText))
	    {  
	    	StringBuilder strBld = new StringBuilder(aText.length()) ;
	    	char[] chs = aText.toCharArray() ;
	        for (int i=0 ; i<chs.length ; i++)
	        {
	            if(sSpecialChars.contains(chs[i]))
	            	strBld.append('\\') ;
	            strBld.append(chs[i]) ;
	        }
	        return strBld.length() != aText.length()?strBld.toString():aText ;
	    }  
	    return aText ;  
	}
	
	public static boolean check$yyyyMMddHHmmssSSS(String aDateStr)
	{
		if(sDT_$yyyyMMddHHmmssSSS == null)
			sDT_$yyyyMMddHHmmssSSS = Pattern.compile("^((([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29))\\s+([0-1]?[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])\\.([0-9]{3})$") ;
		return sDT_$yyyyMMddHHmmssSSS.matcher(aDateStr).matches() ;
	}
} 