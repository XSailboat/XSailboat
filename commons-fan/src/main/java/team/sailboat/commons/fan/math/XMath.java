package team.sailboat.commons.fan.math;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;

import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.text.XString;

public class XMath
{
	static final int[] sSizeInts = new int[]{9 , 99 , 999 , 9_999 , 99_999 
		, 999_999 , 9_999_999 , 99_999_999 , 999_999_999 , Integer.MAX_VALUE} ;
	
	static final int[] sSizeNegInts = new int[]{-9 , -99 , -999 , -9_999 , -99_999 
		, -999_999 , -9_999_999 , -99_999_999 , -999_999_999 , Integer.MIN_VALUE} ;
	
	/**
	 * 两数是否同号
	 * @return
	 */
	public static boolean isSameSign(double n1 , double n2)
	{
		return (n1>=0&&n2>=0) || (n1<=0&&n2<=0) ;
	}
	
	public static double distance(float x1 , float y1 , float x2 , float y2)
	{
		return Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y2-y2)) ;
	}
	
	public static double distance(double[] aV1 , double[] aV2)
	{
		double r = 0 ;
		for(int i=0 ; i<aV1.length ; i++)
		{
			r += (aV1[i] - aV2[i]) * (aV1[i] - aV2[i]) ; 
		}
		return Math.sqrt(r) ;
	}
	
	public static double max(double...vals)
	{
		double max = vals[0] ;
		for(int i = 1 ; i<vals.length ; i++)
			if(vals[i]>max) max = vals[i] ;
		return max ;
	}
	
	public static Long maxLong(Iterable<Long> vals)
	{
		if(vals == null)
			return null ;
		Long max = null ;
		for(Long val : vals)
		{
			if(max == null || (val != null && val.longValue() > max.longValue()))
				max = val ;
		}
		return max ;
	}
	
	public static Integer maxInteger(Iterable<Integer> vals)
	{
		if(vals == null)
			return null ;
		Integer max = null ;
		for(Integer val : vals)
		{
			if(max == null || (val != null && val.intValue() > max.intValue()))
				max = val ;
		}
		return max ;
	}
	
	public static Double maxDouble(Iterable<Double> vals)
	{
		if(vals == null)
			return null ;
		Double max = null ;
		for(Double val : vals)
		{
			if(max == null || (val != null && val.doubleValue() > max.doubleValue()))
				max = val ;
		}
		return max ;
	}
	
	public static Date maxDate(Iterable<Date> vals)
	{
		if(vals == null)
			return null ;
		Date max = null ;
		for(Date val : vals)
		{
			if(max == null || (val != null && val.getTime() > max.getTime()))
				max = val ;
		}
		return max ;
	}
	
	public static String maxString(Iterable<String> vals)
	{
		if(vals == null)
			return null ;
		String max = null ;
		for(String val : vals)
		{
			if(max == null || (val != null && val.compareTo(max) > 0))
				max = val ;
		}
		return max ;
	}
	
	public static long max(long...vals)
	{
		long max = vals[0] ;
		for(int i = 1 ; i<vals.length ; i++)
			if(vals[i]>max) max = vals[i] ;
		return max ;
	}
	
	public static int max(int...aVals)
	{
		int max = aVals[0] ;
		for(int i = 1 ; i<aVals.length ; i++)
			if(aVals[i]>max) max = aVals[i] ;
		return max ;
	}
	
	public static Float max(Float...aVals)
	{
		Float max = null ;
		for(Float val : aVals)
		{
			if(max == null) max = val ;
			else if(val != null && max<val)
				max = val ;
		}
		return max ;
	}
	
	public static int signum(long aValue)
	{
		return aValue == 0 ?0:(aValue<0?-1:1) ;
	}
	
	public static <T> T extremeValue(boolean aMax , Comparator<T> aComparator , T[] aArray)
	{
		if(aArray != null)
		{
			if(aArray.length>=2)
			{
				T max = null ;
				for(int i=0 ; i<aArray.length ; i++)
				{
					if(aArray[i] != null)
					{
						if(max == null) max = aArray[i] ;
						else if(aMax && aComparator.compare(max , aArray[i])<0
								|| (!aMax && aComparator.compare(max, aArray[i])>0))
							max = aArray[i] ;
					}
				}
				return max ;
			}
			return aArray[0] ;
		}
		return null ;
	}
	
	public static Float min(Float...aVals)
	{
		Float min = null ;
		for(Float val : aVals)
		{
			if(min == null) min = val ;
			else if(val != null && min>val)
				min = val ;
		}
		return min ;
	}
	
	public static int min(int...aVals)
	{
		int min = aVals[0] ;
		for(int i=1 ; i<aVals.length ; i++)
			if(aVals[i]<min) min = aVals[i] ;
		return min ;
	}
	
	public static double min(double...vals)
	{
		double min = vals[0] ;
		for(int i=1 ; i<vals.length ; i++)
			if(vals[i]<min) min = vals[i] ;
		return min ;
	}
	
	public static float max(float...vals)
	{
		float max = vals[0] ;
		for(int i = 1 ; i<vals.length ; i++)
			if(vals[i]>max) max = vals[i] ;
		return max ;
	}
	
	public static float min(float...vals){
		float min = vals[0] ;
		for(int i=1 ; i<vals.length ; i++)
			if(vals[i]<min) min = vals[i] ;
		return min ;
	}
	
	/**
	 * 取科学记数法的基数
	 * 如12取10，56取10，120取100，0.12取0.1，0.048取0.01
	 * @param aVal	如果aVal<0，将会取其绝对值；aVal不能为0
	 * @return	
	 */
	public static double toTenRadix(double aVal)
	{
		double radix = 1 ;
		boolean flag = true ;
		if(aVal<0) aVal = -aVal ;
		else if(aVal == 0) throw new IllegalArgumentException("参数不能为0") ;
		do
		{
			if(aVal>=10)
			{
				aVal /= 10 ;
				radix *= 10 ;
			}
			else if(aVal<1)
			{
				aVal *= 10 ;
				radix /= 10 ;
			}
			else flag = false ;
		}
		while(flag) ;
		return radix ;
	}
	
	public static int getRank(double aVal)
	{
		int rank = 0 ;
		if(aVal<0) 
			aVal = -aVal ;
		else if(aVal == 0) 
			return 0 ; ;
		boolean flag = true ;
		do
		{
			if(aVal>=10)
			{
				aVal /= 10 ;
				rank++ ;
			}
			else if(aVal<1)
			{
				aVal *= 10 ;
				rank-- ;
			}
			else flag = false ;
		}while(flag) ;
		return rank ;
	}
	
	/**
	 * [10,100)为1阶，[0 ,10)为0阶 
	 * @param aVal
	 * @return
	 */
	public static int getRank(float aVal)
	{
		return getRank((double)aVal) ;
	}
	
	/**
	 * 将aVal的小数点移位成纯小数，1除以此小数是否得到整数
	 * @param aVal
	 * @return
	 */
	public static boolean canBeDivide(double aVal)
	{
		if(aVal<0) aVal = -aVal ;
		else if(aVal == 0) throw new IllegalArgumentException("参数不能为0") ;
		do
		{
			if(aVal>1) aVal /= 10 ;
			else if(aVal<0.1) aVal*= 10 ;
			else break ;
		}while(true) ;
		aVal = 1/aVal ;
		return ((int)aVal) == aVal ;
	}
	
	/**
	 * (float)[0]/[1] == aVal
	 * @param aVal
	 * @return
	 */
	public static int[] toScale(float aVal)
	{
		int scale = 1 ;
		while(((int)aVal) != aVal)
		{
			aVal *= 10 ;
			scale *= 10 ;
		}
		return new int[]{(int)aVal , scale} ;
	}
	
	/**
	 * 取得精度
	 * @param aVal
	 * @return
	 */
	public static int getPrecision(float aVal)
	{
		int pre = 0 ;
		while((int)aVal != aVal && pre<6)
		{
			pre++ ;
			aVal*=10 ;
		}
		return pre ;
	}
	
	/**
	 * 整数位数
	 * @param aVal
	 * @return
	 */
	public static int size(int aVal)
	{
		if(aVal>=0)
		{
			for(int i=0 ; ; i++)
			{
				if(aVal<=sSizeInts[i])
					return i+1 ;
			}
		}
		else
		{
			for(int i=0 ; ; i++)
			{
				if(aVal>=sSizeNegInts[i])
					return i+1 ;
			}
		}
	}
	
	/**
	 * aVal四舍五入精确到某一位
	 * @param aVal
	 * @param aScale 精度 ，小数点后一位为1，小数点前一位为-1
	 * @return
	 */
	public static double retainEffectDigits(double aVal , int aScale)
	{
		return new BigDecimal(aVal).setScale(aScale, BigDecimal.ROUND_HALF_UP).doubleValue() ;
	}
	
	public static int sum(int...aVals)
	{
		int sum = 0 ;
		for(int val : aVals)
			sum += val ;
		return sum ;
	}
	
	public static float sum(float...aVals)
	{
		float sum = 0 ;
		for(float val : aVals)
			sum += val ;
		return sum ;
	}
	
	public static float absSum(float...aVals)
	{
		float sum = 0 ;
		for(float val : aVals)
			sum += Math.abs(val) ;
		return sum ;
	}
	
	public static double sum(double...aVals)
	{
		double sum = 0 ;
		for(double val : aVals)
			sum += val ;
		return sum ;
	}
	
	public static void abs(Float[] aArray)
	{
		for(int i=0 ; i<aArray.length ; i++)
		{
			if(aArray[i] != null)
				aArray[i] = Math.abs(aArray[i]) ;
		}
	}
	
	public static void abs(float[] aArray)
	{
		for(int i=0 ; i<aArray.length ; i++)
			aArray[i] = Math.abs(aArray[i]) ;
	}
	
	public static boolean isIn(float aX0 , float aX1 , float aX)
	{
		if(aX0>=aX1)
			return aX0>=aX && aX>=aX1 ;
		else return aX0<=aX && aX<=aX1 ; 
	}
	
	public static double getDistance(double aX1 , double aY1 , double aX2 , double aY2)
	{
		return Math.sqrt(Math.pow(aX1-aX2 , 2)+ Math.pow(aY1-aY2, 2)) ;
	}
	
	/**
	 * 平方
	 * @param aX
	 * @return
	 */
	public static float sqr(float aX)
	{
		return aX*aX ;
	}
	
	public static double sqr(double aX)
	{
		return aX*aX ;
	}
	
	public static float[] mulitply(float[] aArray , float aFactor)
	{
		if(aArray != null && aArray.length>0)
		{
			float[] result = new float[aArray.length] ;
			int i=0 ;
			while(i<aArray.length)
			{
				result[i] = aArray[i]*aFactor ;
				i++ ;
			}
			return result ;
		}
		return new float[0] ;
	}
	
	public static void mulitply0(float[] aArray , float aFactor)
	{
		if(aArray != null && aArray.length>0)
		{
			int i=0 ;
			while(i<aArray.length)
			{
				aArray[i] = aArray[i]*aFactor ;
				i++ ;
			}
		}
	}
	
	/**
	 * 左闭右闭区间
	 * @param aLeft
	 * @param aRight
	 * @param aVal
	 * @return
	 */
	public static boolean inSpace_L_R(double aLeft , double aRight , double aVal)
	{
		return aVal>=aLeft && aVal<=aRight ;
	}
	
	public static boolean inSpace_L_R(long aLeft , long aRight , long aVal)
	{
		return aVal>=aLeft && aVal<=aRight ;
	}
	
	/**
	 * [aLeft , aRight]
	 * @param aLeft
	 * @param aRight
	 * @param aVal
	 * @return
	 */
	public static boolean betweenL_R(long aLeft , long aRight , double aVal)
	{
		return aVal>=aLeft && aVal<=aRight ;
	}
	
	/**
	 * [aLeft , aRight]
	 * @param aLeft
	 * @param aRight
	 * @param aVal
	 * @return
	 */
	public static boolean betweenL_R(long aLeft , long aRight , long aVal)
	{
		return aVal>=aLeft && aVal<=aRight ;
	}
	
	/**
	 * 左闭右开区间
	 * @param aLeft
	 * @param aRight
	 * @param aVal
	 * @return
	 */
	public static boolean inSpace_L_r(double aLeft , double aRight , double aVal)
	{
		return aVal>=aLeft && aVal<aRight ;
	}
	
	public static boolean inSpace_l_R(double aLeft , double aRight , double aVal)
	{
		return aVal>aLeft && aVal<=aRight ;
	}
	
	/**
	 * 开区间
	 * @param aLeft
	 * @param aRight
	 * @param aVal
	 * @return
	 */
	public static boolean inSpace_l_r(double aLeft , double aRight , double aVal)
	{
		return aVal>aLeft && aVal<aRight ;
	}
	
	/**
	 * 取绝对值小的那个数
	 * @param aX1
	 * @param aX2
	 * @return
	 */
	public static float absMin(float aX1 , float aX2)
	{
		return Math.abs(aX1)>Math.abs(aX2)?aX2:aX1 ;
	}
	
	/**
	 * 取绝对值大的那个数
	 * @param aX1
	 * @param aX2
	 * @return
	 */
	public static float absMax(float aX1 , float aX2)
	{
		return Math.abs(aX1)>Math.abs(aX2)?aX1:aX2 ;
	}
	
	/**
     * <p>检查这个字符串是否是有效的Java数字表示</p>
     *
     * <p>Valid numbers include hexadecimal marked with the <code>0x</code> or
     * <code>0X</code> qualifier, octal numbers, scientific notation and numbers 
     * marked with a type qualifier (e.g. 123L).</p>
     * 
     * <p>Non-hexadecimal strings beginning with a leading zero are
     * treated as octal values. Thus the string <code>09</code> will return
     * <code>false</code>, since <code>9</code> is not a valid octal value.
     * However, numbers beginning with {@code 0.} are treated as decimal.</p>
     *
     * <p><code>null</code> and empty/blank {@code String} will return
     * <code>false</code>.</p>
     *
     * @param str  the <code>String</code> to check
     * @return <code>true</code> if the string is a correctly formatted number
     * @since 3.3 the code supports hex {@code 0Xhhh} and octal {@code 0ddd} validation
     */
    public static boolean isNumber(final String str) {
        if (XString.isEmpty(str)) {
            return false;
        }
        final char[] chars = str.toCharArray();
        int sz = chars.length;
        boolean hasExp = false;
        boolean hasDecPoint = false;
        boolean allowSigns = false;
        boolean foundDigit = false;
        // deal with any possible sign up front
        final int start = (chars[0] == '-') ? 1 : 0;
        if (sz > start + 1 && chars[start] == '0') { // leading 0
            if (
                 (chars[start + 1] == 'x') || 
                 (chars[start + 1] == 'X') 
            ) { // leading 0x/0X
                int i = start + 2;
                if (i == sz) {
                    return false; // str == "0x"
                }
                // checking hex (it can't be anything else)
                for (; i < chars.length; i++) {
                    if ((chars[i] < '0' || chars[i] > '9')
                        && (chars[i] < 'a' || chars[i] > 'f')
                        && (chars[i] < 'A' || chars[i] > 'F')) {
                        return false;
                    }
                }
                return true;
           } else if (Character.isDigit(chars[start + 1])) {
               // leading 0, but not hex, must be octal
               int i = start + 1;
               for (; i < chars.length; i++) {
                   if (chars[i] < '0' || chars[i] > '7') {
                       return false;
                   }
               }
               return true;               
           }
        }
        sz--; // don't want to loop to the last char, check it afterwords
              // for type qualifiers
        int i = start;
        // loop to the next to last char or to the last char if we need another digit to
        // make a valid number (e.g. chars[0..5] = "1234E")
        while (i < sz || (i < sz + 1 && allowSigns && !foundDigit)) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                foundDigit = true;
                allowSigns = false;

            } else if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent   
                    return false;
                }
                hasDecPoint = true;
            } else if (chars[i] == 'e' || chars[i] == 'E') {
                // we've already taken care of hex.
                if (hasExp) {
                    // two E's
                    return false;
                }
                if (!foundDigit) {
                    return false;
                }
                hasExp = true;
                allowSigns = true;
            } else if (chars[i] == '+' || chars[i] == '-') {
                if (!allowSigns) {
                    return false;
                }
                allowSigns = false;
                foundDigit = false; // we need a digit after the E
            } else {
                return false;
            }
            i++;
        }
        if (i < chars.length) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                // no type qualifier, OK
                return true;
            }
            if (chars[i] == 'e' || chars[i] == 'E') {
                // can't have an E at the last byte
                return false;
            }
            if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                // single trailing decimal point after non-exponent is ok
                return foundDigit;
            }
            if (!allowSigns
                && (chars[i] == 'd'
                    || chars[i] == 'D'
                    || chars[i] == 'f'
                    || chars[i] == 'F')) {
                return foundDigit;
            }
            if (chars[i] == 'l'
                || chars[i] == 'L') {
                // not allowing L with an exponent or decimal point
                return foundDigit && !hasExp && !hasDecPoint;
            }
            // last character is illegal
            return false;
        }
        // allowSigns is true iff the val ends in 'E'
        // found digit it to make sure weird stuff like '.' and '1E-' doesn't pass
        return !allowSigns && foundDigit;
    }
    
    /**
     * 计算百分比计算结果
     * @param aWeight
     * @param aTotal
     * @return
     */
    public static String getPrecentInStr(double aWeight , double aTotal)
    {
    	return XString.toPercents(aWeight/aTotal , 2 , true) ;
    }
    
    public static double sigmoid(double x)
    {
    	return 1/(1+Math.exp(-x)) ;
    }
    
    /**
     * 插值
     * @return
     */
    public static double interpolation(double aX1 , double aY1 , double aX2 , double aY2 , double aX)
    {
    	Assert.isTrue(aX1 != aX2 , "x1=%1$s, x2=%2$s，不能相同！" , aX1 , aX2) ;
    	return aY1 + (aX-aX1)*(aY2-aY1)/(aX2-aX1) ;
    }
}
