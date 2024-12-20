package team.sailboat.base.def;

import java.util.Map;

import team.sailboat.commons.fan.collection.XC;

/**
 * 
 * 统计期类型
 *
 * @author yyl
 * @since 2024年11月20日
 */
public enum TimeRangeType
{
	
	year("年度"),
	quarter("季度") ,
	month("月度") ,
	week("每周") ,
	day("每天") ;
	;
	
	static final Map<String , TimeRangeType> sDisplayNameMap = XC.concurrentHashMap() ;
	static boolean sClassInit = false;
	
	final String displayName ;
	
	private TimeRangeType(String aDisplayName)
	{
		displayName = aDisplayName ;
	}
	
	public String getDisplayName()
	{
		return displayName;
	}
	
	/**
	 * 根据显示名称获取对应的TimeRangeType枚举实例。
	 *
	 * @param aDisplayName 要查找的显示名称（displayName）。
	 * @return 返回与给定显示名称匹配的TimeRangeType枚举实例。
	 * @throws IllegalArgumentException 如果给定的显示名称无效（即，不存在与给定显示名称匹配的TimeRangeType枚举实例），则抛出此异常。
	 */
	public static TimeRangeType valueOfDisplayName(String aDisplayName)
	{
		if(!sClassInit)
		{
			synchronized (sDisplayNameMap)
			{
				if(!sClassInit)
				{
					XC.extract(values() , TimeRangeType::getDisplayName , sDisplayNameMap , true) ;
					sClassInit = true ;
				}
			}
		}
		TimeRangeType trt = sDisplayNameMap.get(aDisplayName) ;
		if(trt != null)
			return trt ;
		throw new IllegalArgumentException("无效的TimeRangeType的displayName："+aDisplayName) ;
	}
}
