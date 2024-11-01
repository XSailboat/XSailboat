package team.sailboat.commons.fan.sys;

import team.sailboat.commons.fan.math.XMath;
import team.sailboat.commons.fan.text.XString;
/**
 * JVM内存评估预测。
 * 
 *
 * @author yl
 * @version 1.0 
 * @since 2015-9-22
 */
public class MemoryAssist
{
	
	public static long sKB = 1024 ;
	
	public static long sMB = 1048576 ;
	
	public static long sGB = 1073741824 ;
	
	static MemoryAssist sInstance ;
	
	public static MemoryAssist getInstance()
	{
		if(sInstance == null)
			sInstance = new MemoryAssist() ;
		return sInstance ;
	}
	
	long mMax ;
	long mMin ;
	long mCurrent ;
	/**
	 * 大于0表示上升
	 */
	long mExtent ;
	long mAmplitude ;
	
	protected MemoryAssist()
	{}
	
	public void detect()
	{
		setCurrent(getUsedMemory()) ;
 	}
	
	public void setCurrent(long aCurrent)
	{
		long extent = aCurrent-mCurrent ;
		if(!XMath.isSameSign(extent, mExtent))
		{//异号
			if(extent>0)
			{//刚开始上升
				mMin = mCurrent ;	
				if(aCurrent>mMax)
					mMax = aCurrent ;
			}
			else if(extent<0)
			{//刚开始下降
				mMax = mCurrent ;
				if(aCurrent<mMin)
					mMin = aCurrent ;
			}
			mAmplitude = mMax - mMin ;
		}
		else
		{
			if(extent>0)
			{
				if(aCurrent>mMax)
					mMax = aCurrent ;
			}
			else if(extent<0)
			{
				if(aCurrent<mMin)
					mMin = aCurrent ;
			}
		}
		mExtent = extent ;
		mCurrent = aCurrent ;
	}
	
	public long getMax()
	{
		return mMax ;
	}
	
	public long getMin()
	{
		return mMin ;
	}
	
	public long getMedium()
	{
		return (mMax+mMin)/2 ;
	}
	
	public long getAmplitude()
	{
		return mAmplitude;
	}
	
	@Override
	public String toString()
	{
		StringBuilder strBld = new StringBuilder() ;
		strBld.append("当前："+MemoryAssist.toAutoB(mCurrent , 2)+"  ") ;
		strBld.append("最大："+MemoryAssist.toAutoB(mMax , 2)+"  ") ;
		strBld.append("最小："+MemoryAssist.toAutoB(mMin , 2)+"  ") ;
		strBld.append("振幅："+MemoryAssist.toAutoB(mAmplitude , 2)+"  ") ;
		strBld.append("增幅："+MemoryAssist.toAutoB(mExtent , 2)+"  ") ;
		return strBld.toString();
	}
	
	
	/**
	 * 取得可用的内存
	 * @return
	 */
	public static long getUsableMemory()
	{
		Runtime runtime = Runtime.getRuntime() ;
		return runtime.maxMemory()-runtime.totalMemory()+runtime.freeMemory() ;
	}
	
	public static long getUsedMemory()
	{
		Runtime runtime = Runtime.getRuntime() ;
		return runtime.totalMemory()-runtime.freeMemory() ;
	}
	
	public static int compareMB(long aB , float aMB)
	{
		long b = (long)(aMB*sMB) ;
		if(aB<b)
			return -1 ;
		else if(aB == b)
			return 0 ;
		else
			return 1 ;
	}
	
	public static String toKB(long aVal , int aPrecision)
	{
		String prefix = "" ;
		if(aVal<0)
		{
			aVal = -aVal ;
			prefix = "-" ;
		}
		return prefix + XString.format(aVal/1024d , aPrecision , true)+"KB" ;
	}
	
	public static String toMB(long aVal , int aPrecision)
	{
		String prefix = "" ;
		if(aVal<0)
		{
			aVal = -aVal ;
			prefix = "-" ;
		}
		return prefix + XString.format(aVal/1048576d , aPrecision , true)+"MB" ;
	}
	
	public static String toGB(long aVal , int aPrecision)
	{
		String prefix = "" ;
		if(aVal<0)
		{
			aVal = -aVal ;
			prefix = "-" ;
		}
		return prefix + XString.format(aVal/1073741824d , aPrecision , true)+"GB" ;
	}
	
	/**
	 * <p>
	 * 以指定的精度，自适应单位输出容量大小<br>
	 * @param aVal			以字节为单位
	 * @param aPrecision
	 * @return
	 */
	public static String toAutoB(long aVal , int aPrecision)
	{
		long absVal = Math.abs(aVal) ;
		if((absVal>=0 && absVal<=sKB))
			return aVal+"B" ;
		else if(absVal<=sMB)
			return toKB(aVal, aPrecision) ;
		else if(absVal<=sGB)
			return toMB(aVal, aPrecision) ;
		else return toGB(aVal, aPrecision) ;
	}
	
	/**
	 * <p>
	 * 以指定的精度，自适应单位输出容量大小<br>
	 * @param aVal			以MB为单位
	 * @param aPrecision
	 * @return
	 */
	public static String toAutoMB(int aVal , int aPrecision)
	{
		if(aVal<2048)
			return aVal+"MB" ;
		else
			return XString.format(aVal/1024f , aPrecision , true)+"GB" ;
	}
	
	/**
	 * aVal * 1KB						<br>
	 * 需要调用者注意不能超出最大int
	 * @param aVal
	 * @return
	 */
	public static int multiKB(int aVal)
	{
		return (int)(sKB*aVal) ;
	}
	
	public static long multiMB(int aVal)
	{
		return sMB*aVal ;
	}
}
