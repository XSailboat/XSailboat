package team.sailboat.commons.fan.json;

import java.util.Comparator;
import java.util.Map;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.text.ChineseComparator;

/**
 * 
 * 比较器工厂
 *
 * @author yyl
 * @since 2024年11月4日
 */
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
	
	/**  
	 * 创建一个自定义的Comparator<Object>，用于根据给定的字符串数组（aHeads）的顺序对对象进行比较。  
	 * 如果对象转换为字符串后存在于aHeads数组中，则按照aHeads中的顺序进行比较；  
	 * 如果不存在于aHeads数组中，则根据aTailStartIndex参数和另一个对象的顺序值进行比较，  
	 * 或者使用拼音比较器（ChineseComparator.comparePingYin）进行比较。  
	 *  
	 * @param aHeads 字符串数组，定义了比较顺序的头部列表。  
	 * @param aTailStartIndex 一个整数，定义了不在aHeads中的字符串的默认顺序索引。  
	 *                         如果aTailStartIndex大于等于某个在aHeads中的字符串的顺序值，  
	 *                         则不在aHeads中的字符串被认为大于该字符串；否则，被认为小于。  
	 * @return 一个Comparator<Object>，用于比较两个对象。  
	 */  
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
