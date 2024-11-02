package team.sailboat.bd.base.model;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import team.sailboat.bd.base.model.dag.InstGenWay;
import team.sailboat.commons.fan.dpa.anno.BForwardMethod;
import team.sailboat.commons.fan.dpa.anno.BReverseMethod;
import team.sailboat.commons.fan.json.JSONException;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.time.XTime;

/**
 * 调度配置
 *
 * @author yyl
 * @since 2021年6月17日
 */
public class DispatchConfiguration implements ToJSONObject
{
	public static final int sDefaultScheduleTimeoutInHours = 72 ; 
	
	/**
	 * 缺省的运行时长限制，10分钟
	 */
	public static final int sDefaultRunTimeLimit = 10 ;
	
	public static final Date sDefaultValidTimeSpaceLower = new Date(0) ;
	
	public static final Date sDefaultValidTimeSpaceUpper = XTime.parse$yyyyMMdd_0("9999-01-01") ;
 	
	/**
	 * 实例的生成方式
	 */
	InstGenWay mInstGenWay = InstGenWay.Tp1 ;
	/**
	 * 是否空跑
	 */
	boolean mRunWithNoLoad = false ;
	
	/**
	 * 生效时间的下限
	 */
	Date mValidTimeSpaceLower = sDefaultValidTimeSpaceLower ;
	
	/**
	 * 生效时间的上限
	 */
	Date mValidTimeSpaceUpper = sDefaultValidTimeSpaceUpper ;
	
	/**
	 * 调度计划
	 */
	String mSchedule = "00 20 0 * * ?" ;
	
	/**
	 * 超时时间，0表示系统缺省，大于0的时候，单位是小时
	 */
	int mTimeout = 0 ;
	
//	/**
//	 * 冻结、暂停调度
//	 */
//	boolean mBlocked ;
	
	/**
	 * 运行时长限制，单位是分钟，缺省是10分钟
	 */
	int mRunTimeLimit = sDefaultRunTimeLimit ;
	
	public DispatchConfiguration()
	{
		mValidTimeSpaceLower = Date.from(LocalDate.of(1970, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()) ;
		mValidTimeSpaceUpper = Date.from(LocalDate.of(9999, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()) ;
	}
	
	public InstGenWay getInstGenWay()
	{
		return mInstGenWay;
	}
	public boolean setInstGenWay(InstGenWay aInstGenWay)
	{
		if(mInstGenWay != aInstGenWay)
		{
			mInstGenWay = aInstGenWay;
			return true ;
		}
		return false ;
	}

	public boolean isRunWithNoLoad()
	{
		return mRunWithNoLoad;
	}
	public boolean setRunWithNoLoad(boolean aRunWithNoLoad)
	{
		if(mRunWithNoLoad != aRunWithNoLoad)
		{
			mRunWithNoLoad = aRunWithNoLoad;
			return true ;
		}
		return false ;
	}

	public Date getValidTimeSpaceLower()
	{
		return mValidTimeSpaceLower;
	}
	public boolean setValidTimeSpaceLower(Date aValidTimeSpaceLower)
	{
		if(mValidTimeSpaceLower != aValidTimeSpaceLower)
		{
			mValidTimeSpaceLower = aValidTimeSpaceLower;
			return true ;
		}
		return false ;
	}

	public Date getValidTimeSpaceUpper()
	{
		return mValidTimeSpaceUpper;
	}
	public boolean setValidTimeSpaceUpper(Date aValidTimeSpaceUpper)
	{
		if(mValidTimeSpaceUpper != aValidTimeSpaceUpper)
		{
			mValidTimeSpaceUpper = aValidTimeSpaceUpper;
			return true ;
		}
		return false ;
	}
	
	/**
	 * 节点的调度计划，cron表达式
	 * @return
	 */
	public String getSchedule()
	{
		return mSchedule;
	}
	public boolean setSchedule(String aSchedule)
	{
		if(JCommon.unequals(mSchedule, aSchedule))
		{
			mSchedule = aSchedule;
			return true ;
		}
		return false ;
	}

	/**
	 * 超时时间，0表示系统缺省，大于0的时候，单位是小时
	 * @return
	 */
	public int getTimeout()
	{
		return mTimeout;
	}
	public boolean setTimeout(int aTimeout)
	{
		if(mTimeout != aTimeout)
		{
			mTimeout = aTimeout;
			return true ;
		}
		return false ;
	}
	
	/**
	 * 运行时长限制，单位是分钟，缺省是10分钟
	 * @return
	 */
	public int getRunTimeLimit()
	{
		return mRunTimeLimit;
	}
	public boolean setRunTimeLimit(int aRunTimeLimit)
	{
		if(aRunTimeLimit <= 0)
			aRunTimeLimit = sDefaultRunTimeLimit ;
		if(mRunTimeLimit != aRunTimeLimit)
		{
			mRunTimeLimit = aRunTimeLimit ;
			return true ;
		}
		return false ;
	}
	
//	public boolean isBlocked()
//	{
//		return mBlocked;
//	}
//	public boolean setBlocked(boolean aBlocked)
//	{
//		if(mBlocked != aBlocked)
//		{
//			mBlocked = aBlocked;
//			return true ;
//		}
//		return false ;
//	}
	
	
	@Override
	public boolean equals(Object aObj)
	{
		if(aObj == this)
			return true ;
		if(aObj == null || !(aObj instanceof DispatchConfiguration))
			return false ;
		DispatchConfiguration other = (DispatchConfiguration)aObj ;
		return mInstGenWay == other.mInstGenWay
				&& mRunWithNoLoad == other.mRunWithNoLoad 
				&& JCommon.equals(mValidTimeSpaceLower, other.mValidTimeSpaceLower)
				&& JCommon.equals(mValidTimeSpaceUpper, other.mValidTimeSpaceUpper)
				&& JCommon.equals(mSchedule, other.mSchedule)
				&& mTimeout == other.mTimeout
				&& mRunTimeLimit == other.mRunTimeLimit
//				&& mBlocked == other.mBlocked
				;
	}

	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return aJSONObj.put("instGenWay" , mInstGenWay.name())
				.put("runWithNoLoad" , mRunWithNoLoad)
				.put("validTimeSpaceLower" , XTime.format$yyyyMMdd(mValidTimeSpaceLower , "1970-01-01"))
				.put("validTimeSapceUpper" , XTime.format$yyyyMMdd(mValidTimeSpaceUpper , "9999-01-01"))
				.put("schedule", mSchedule)
				.put("timeout" , mTimeout)
				.put("runTimeLimit" , mRunTimeLimit)
//				.put("blocked", mBlocked)
				;
	}
	
	@Override
	public String toString()
	{
		return toJSONString() ;
	}
	
	public static DispatchConfiguration parse(JSONObject aJObj) throws ParseException
	{
		DispatchConfiguration conf = new DispatchConfiguration() ;
		conf.mInstGenWay = InstGenWay.valueOf(aJObj.optString("instGenWay")) ;
		conf.mRunWithNoLoad = aJObj.optBoolean("runWithNoLoad" , conf.mRunWithNoLoad) ;
		conf.mValidTimeSpaceLower = JCommon.defaultIfNull(XTime.parse$yyyyMMdd(aJObj.optString("validTimeSpaceLower")) 
				, conf.mValidTimeSpaceLower) ;
		conf.mValidTimeSpaceUpper = JCommon.defaultIfNull(XTime.parse$yyyyMMdd(aJObj.optString("validTimeSpaceUpper"))
				, conf.mValidTimeSpaceUpper) ;
		conf.mSchedule = aJObj.optString("schedule") ;
		conf.mTimeout = aJObj.optInt("timeout" , conf.mTimeout) ;
		conf.mRunTimeLimit = aJObj.optInt("runTimeLimit" , sDefaultRunTimeLimit) ;
//		conf.mBlocked = aJObj.optBoolean("blocked" , false) ;
		return conf ;
	}
	
	public static class SerDe
	{
		@BForwardMethod
		public static String forward(DispatchConfiguration aSource)
		{
			return aSource==null?null:aSource.toJSONString() ;
		}
		
		@BReverseMethod
		public static DispatchConfiguration reverse(Object aSource) throws JSONException, ParseException
		{
			return aSource == null?null:parse(JSONObject.of(aSource.toString())) ;
		}
	}	
	
	public static DispatchConfiguration create(InstGenWay aInstGenWay
			, boolean aRunWithNoLoad
			, String aSchedule
			, Date aTimeSpaceLower
			, Date aTimeSpaceUpper
			, int aTimeout
			, int aRunTimeLimit)
	{
		DispatchConfiguration conf = new DispatchConfiguration() ;
		conf.setInstGenWay(aInstGenWay) ;
		conf.setRunWithNoLoad(aRunWithNoLoad) ;
		conf.setSchedule(aSchedule) ;
		conf.setValidTimeSpaceLower(aTimeSpaceLower) ;
		conf.setValidTimeSpaceUpper(aTimeSpaceUpper) ;
		conf.setTimeout(aTimeout) ;
		conf.setRunTimeLimit(aRunTimeLimit) ;
		return conf ;
	}
}
