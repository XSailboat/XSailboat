package team.sailboat.ms.ac.data;

import java.util.Map;
import java.util.function.BiConsumer;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.struct.XInt;

/**
 * 
 * 某天的访问次数计数
 *
 * @author yyl
 * @since 2024年11月18日
 */
public class VisitTimes
{
	/**
	 * 统计日期
	 */
	String mDate ;
	
	/**
	 * 某天的总访问次数
	 */
	int mTotalTimes ;
	
	/**
	 * 某个应用的某天的被访问次数		<br />
	 * 键是应用id
	 */
	public final Map<String, XInt> mAppTimes = XC.concurrentHashMap() ;
	
	/**
	 * 当天某个人的访问次数				<br />
	 * 访问的各个应用次数之和
	 * 键是用户id
	 */
	public final Map<String, XInt> mUserTimes = XC.concurrentHashMap() ;
	
	/**
	 * 某个人、访问某个应用的次数		<br />
	 * 第1列是应用Id，第2列是用户id，第3列是访问次数
	 */
	public final Table<String, String, XInt> mAppUserTimes = HashBasedTable.create() ;
	
	/**
	 * 
	 * @param aDateStr		格式：yyyy-MM-dd
	 */
	public VisitTimes(String aDateStr)
	{
		mDate = aDateStr ;
	}
	
	/**
	 * 
	 * 当天被访问过，有统计数据的应用id
	 * 
	 * @return
	 */
	public String[] getAppIds()
	{
		return mAppTimes.keySet().toArray(JCommon.sEmptyStringArray) ;
	}
	
	/**
	 * 
	 * 记录一次用户访问某个应用
	 * 
	 * @param aAppId
	 * @param aUserId
	 * @param aTimes
	 */
	public void record(String aAppId , String aUserId , int aTimes)
	{
		mTotalTimes += aTimes ;
		
		XInt times = mAppTimes.get(aAppId) ;
		if(times == null)
		{
			times = new XInt(aTimes) ;
			mAppTimes.put(aAppId, times) ;
		}
		else
			times.plus(aTimes) ;
		
		times = mUserTimes.get(aUserId) ;
		if(times == null)
		{
			times = new XInt(aTimes) ;
			mUserTimes.put(aUserId, times) ;
		}
		else
			times.plus(aTimes) ;
		
		times = mAppUserTimes.get(aAppId, aUserId) ;
		if(times == null)
		{
			times = new XInt(aTimes) ;
			mAppUserTimes.put(aAppId, aUserId, times) ;
		}
		else
			times.plus(aTimes) ;
	}
	
	/**
	 * 
	 * 取得当天的总访问次数
	 * 
	 * @return
	 */
	public int getTotalTimes()
	{
		return mTotalTimes;
	}
	
	/**
	 * 
	 * 取得当天某个应用的访问次数
	 * 
	 * @param aAppId
	 * @param aDefaultTimes		没有数据时的缺省值
	 * @return
	 */
	public int getAppTimes(String aAppId , int aDefaultTimes)
	{
		XInt times = mAppTimes.get(aAppId) ;
		return times == null?aDefaultTimes:times.get() ;
	}
	
	/**
	 * 
	 * 取得当天某个应用的访问次数
	 * 
	 * @param aUserId
	 * @param aDefaultTimes
	 * @return
	 */
	public int getUserTimes(String aUserId , int aDefaultTimes)
	{
		XInt times = mUserTimes.get(aUserId) ;
		return times == null?aDefaultTimes:times.get() ;
	}
	
	/**
	 * 
	 * 遍历当天的各个用户的访问次数数据
	 * 
	 * @param aVtConsumer
	 */
	public void forEachUserTimes(BiConsumer<String , XInt> aVtConsumer)
	{
		mUserTimes.forEach(aVtConsumer) ;
	}
	
	/**
	 * 
	 * 取得当天，某个人访问某个应用的次数
	 * 
	 * @param aAppId
	 * @param aUserId
	 * @param aDefaultTimes
	 * @return
	 */
	public int getAppUserTimes(String aAppId , String aUserId , int aDefaultTimes)
	{
		XInt times = mAppUserTimes.get(aAppId, aUserId) ;
		return times == null?aDefaultTimes:times.get() ;
	}
}
