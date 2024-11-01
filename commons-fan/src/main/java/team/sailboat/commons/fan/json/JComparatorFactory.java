package team.sailboat.commons.fan.json;

import java.util.Comparator;
import java.util.Map;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.text.ChineseComparator;

public class JComparatorFactory
{
	public static Comparator<Object> newJSONObjectComparator_String(String aField , boolean aHead , String...aEndValues)
	{
		return newJSONObjectComparator_String(aField, true, aHead, aEndValues) ;
	}
	
	public static Comparator<Object> newJSONObjectComparator_String(String aField , boolean aAsc , boolean aHead , String...aEndValues)
	{
		int endLen = XC.count(aEndValues) ;
		Map<String, Integer> orderMap = XC.hashMap(aEndValues 
				, XC.fill(new Integer[endLen] , 0, endLen, 1, 1)) ;
		int cof = aAsc?1:-1 ;
		return (obj_1 , obj_2)->{
			JSONObject jo_1 = (JSONObject)obj_1 ;
			JSONObject jo_2 = (JSONObject)obj_2 ;
			if(jo_1 == jo_2)
				return 0 ;
			if(jo_1 == null)
				return 1*cof ;
			if(jo_2 == null)
				return -1*cof ;
			String str1 = jo_1.optString(aField) ;
			String str2 = jo_2.optString(aField) ;
			Integer order1 = orderMap.get(str1) ;
			Integer order2 = orderMap.get(str2) ;
			if(order1 == null && order2 == null)
				return ChineseComparator.comparePingYin(str1, str2)*cof ;
			else if(order1 == null)
				return aHead?1*cof:-1*cof ;
			else if(order2 == null)
				return aHead?-1*cof:1*cof ;
			else
				return (order1-order2)*cof ;
		} ;
	}
	
	public static Comparator<Object> newJSONObjectComparator_Long(String aField , boolean aAsc)
	{
		final int c = aAsc?1:-1 ;
		return (obj_1 , obj_2)->{
			JSONObject jo_1 = (JSONObject)obj_1 ;
			JSONObject jo_2 = (JSONObject)obj_2 ;
			if(jo_1 == jo_2)
				return 0 ;
			if(jo_1 == null)
				return c ;
			if(jo_2 == null)
				return -c ;
			Long v1 = jo_1.optLong_0(aField) ;
			Long v2 = jo_2.optLong_0(aField) ;
			if(v1 == v2)
				return 0 ;
			else if(v1 == null)
				return c ;
			else if(v2 == null)
				return -c ;
			else
				return v1-v2>0?c:-c ;
		} ;
	}
	
	public static Comparator<Object> newComparator_String()
	{
		return newComparator_String(null, 0) ;
	}
	
	public static Comparator<Object> newComparator_String(String[] aHeads , int aTailStartIndex)
	{
		
		int endLen = XC.count(aHeads) ;
		Map<String, Integer> orderMap = XC.hashMap(aHeads
				, XC.fill(new Integer[endLen] , 0, endLen, 1, 1)) ;
		return (obj_1 , obj_2)->{
			String str1 = XClassUtil.toString(obj_1) ;
			String str2 = XClassUtil.toString(obj_2) ;
			Integer order1 = orderMap.get(str1) ;
			Integer order2 = orderMap.get(str2) ;
			if(order1 == null && order2 == null)
				return ChineseComparator.comparePingYin(str1, str2) ;
			else if(order1 == null)
				return aTailStartIndex>=order2?1:-1 ;
			else if(order2 == null)
				return aTailStartIndex>=order1?-1:1 ;
			else
				return order1-order2 ;
		} ;
	}
}
