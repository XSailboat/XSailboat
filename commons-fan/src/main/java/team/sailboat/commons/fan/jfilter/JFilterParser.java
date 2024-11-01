package team.sailboat.commons.fan.jfilter;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.function.Supplier;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.fan.time.XTime;

public class JFilterParser<T> implements FilterConstant
{
	Supplier<IFilterBuilder<T>> mBuilderSupplier ;
	
	Predicate<JFilterNode> mPreFilter ;
	
	public JFilterParser(Supplier<IFilterBuilder<T>> aBuilderSupplier)
	{
		mBuilderSupplier = aBuilderSupplier ;
	}
	
	public JFilterParser(Supplier<IFilterBuilder<T>> aBuilderSupplier , Predicate<JFilterNode> aPredFilter)
	{
		mBuilderSupplier = aBuilderSupplier ;
		mPreFilter = aPredFilter ;
	}
	
	public T parseFilter(JSONObject aJFilter , Object...aArgs)
	{
		IFilterBuilder<T> builder = mBuilderSupplier.get() ;
		builder.setArgs(aArgs);
		String parentNodePath = "" ;
		if(aJFilter != null)
		{
			parseEndFilter(builder , aJFilter , parentNodePath) ;

			for(String unionName : new String[] {sUN_should , sUN_must , sUN_must_not})
			{
				Object obj = aJFilter.optJSONArray(unionName) ;
				if(obj != null)
				{
					parseUnion(builder , obj , unionName , parentNodePath) ;
				}
			}
		}
		return builder.build() ;
	}
	
	protected boolean parseEndFilter(IFilterBuilder<T> aBuilder , JSONObject aJObj , String aParentNodePath)
	{
		JSONObject jobj = aJObj.optJSONObject(sTag_term) ;
		if(jobj != null 
				&& (mPreFilter == null || mPreFilter.test(new JFilterNode(true, sTag_term , jobj , aParentNodePath))))
		{
			return parseTerm(aBuilder , jobj , aParentNodePath+"."+sTag_term) ;
		}
		jobj = aJObj.optJSONObject(sTag_contains) ;
		if(jobj != null
				&& (mPreFilter == null || mPreFilter.test(new JFilterNode(true, sTag_contains , jobj , aParentNodePath))))
		{
			return parseContains(aBuilder , jobj , aParentNodePath+"."+sTag_contains) ;
		}
		jobj = aJObj.optJSONObject(sTag_startsWith) ;
		if(jobj != null
				&& (mPreFilter == null || mPreFilter.test(new JFilterNode(true, sTag_startsWith , jobj , aParentNodePath))))
		{
			return parseStartsWith(aBuilder , jobj , aParentNodePath+"."+sTag_startsWith) ;
		}
		jobj = aJObj.optJSONObject(sTag_endsWith) ;
		if(jobj != null
				&& (mPreFilter == null || mPreFilter.test(new JFilterNode(true, sTag_endsWith , jobj , aParentNodePath))))
		{
			return parseEndsWith(aBuilder , jobj , aParentNodePath+"."+sTag_endsWith) ;
		}
		jobj = aJObj.optJSONObject(sTag_expr) ;
		if(jobj != null
				&& (mPreFilter == null || mPreFilter.test(new JFilterNode(true, sTag_expr , jobj , aParentNodePath))))
		{
			return parseExpr(aBuilder , jobj , aParentNodePath+"."+sTag_expr) ;
		}
		
		String fieldName = aJObj.optString(sTag_null , null) ;
		if(XString.isNotEmpty(fieldName)
				&& (mPreFilter == null || mPreFilter.test(new JFilterNode(true, sTag_null , null , aParentNodePath))))
		{
			return aBuilder.isNull(fieldName) ;
		}
		jobj = aJObj.optJSONObject(sTag_range) ;
		if(jobj != null
				&& (mPreFilter == null || mPreFilter.test(new JFilterNode(true, sTag_range , jobj , aParentNodePath))))
		{
			return parseRange(aBuilder ,jobj , aParentNodePath+"."+sTag_range) ;
		}
		jobj = aJObj.optJSONObject(sTag_in) ;
		if(jobj != null
				&& (mPreFilter == null || mPreFilter.test(new JFilterNode(true, sTag_range , jobj , aParentNodePath))))
		{
			return parseIn(aBuilder, jobj, aParentNodePath+"."+sTag_in) ;
		}
		
		return false ;
	}
	
	protected boolean parseTerm(IFilterBuilder<T> aBuilder , JSONObject aJObj , String aParentNodePath)
	{
		String[] keys = aJObj.keyArray() ;
		Assert.isTrue(XC.count(keys) == 1 , "term中的内容不合法：%s" , aJObj) ;
		FilterField field = aBuilder.parseFilterField(keys[0]) ;
		String fieldName = field.getName() ;
		String type = field.getType() ;
		if(type == null)
		{
			Object val =  aJObj.opt(keys[0]) ;
			if(mPreFilter == null || mPreFilter.test(new JFilterNode(false, fieldName , val , aParentNodePath)))
				return aBuilder.term(fieldName , val) ;
			return false ;
		}
		switch(type)
		{
		case XClassUtil.sCSN_String:
		{
			String val = aJObj.optString(keys[0]) ;
			if(mPreFilter == null || mPreFilter.test(new JFilterNode(false, fieldName , val , aParentNodePath)))
				return aBuilder.termString(fieldName , val) ;
			return false ;
		}
		case XClassUtil.sCSN_Double:
		case XClassUtil.sCSN_Float:
		{
			Double val = aJObj.optDouble(keys[0]) ;
			if(mPreFilter == null || mPreFilter.test(new JFilterNode(false, fieldName , val , aParentNodePath)))
				return aBuilder.termNumber(fieldName, val) ;
			return false ;
		}
		case XClassUtil.sCSN_Integer:
		{
			Integer val = aJObj.optInteger(keys[0]) ;
			if(mPreFilter == null || mPreFilter.test(new JFilterNode(false, fieldName , val , aParentNodePath)))
				return aBuilder.termNumber(fieldName, val) ;
			return false ;
		}
		case XClassUtil.sCSN_Long:
		{
			Long val = aJObj.optLong(keys[0]) ;
			if(mPreFilter == null || mPreFilter.test(new JFilterNode(false, fieldName , val , aParentNodePath)))
				return aBuilder.termNumber(fieldName , val) ;
			return false ;
		}
		case XClassUtil.sCSN_DateTime:
			try
			{
				Date val = XTime.adaptiveParse(aJObj.optString(keys[0])) ;
				if(mPreFilter == null || mPreFilter.test(new JFilterNode(false, fieldName , val , aParentNodePath)))
					return aBuilder.termDate(fieldName, val) ;
				return false ;
			}
			catch (ParseException e)
			{
				throw new IllegalStateException(e) ;
			}
		default:
			throw new IllegalStateException("未知类型："+type) ;
		}
	}
	
	protected boolean parseContains(IFilterBuilder<T> aBuilder , JSONObject aJObj , String aParentNodePath)
	{
		String[] keys = aJObj.keyArray() ;
		Assert.isTrue(XC.count(keys) == 1 , "contains中的内容不合法：%s" , aJObj) ;
		FilterField field = aBuilder.parseFilterField(keys[0]) ;
		String val = aJObj.optString(keys[0]) ;
		if(mPreFilter == null || mPreFilter.test(new JFilterNode(false, field.getName() , val , aParentNodePath)))
			return aBuilder.contains(field.getName() , val) ;
		return false ;
	}
	
	protected boolean parseStartsWith(IFilterBuilder<T> aBuilder , JSONObject aJObj , String aParentNodePath)
	{
		String[] keys = aJObj.keyArray() ;
		Assert.isTrue(XC.count(keys) == 1 , "startsWith中的内容不合法：%s" , aJObj) ;
		FilterField field = aBuilder.parseFilterField(keys[0]) ;
		String val = aJObj.optString(keys[0]) ;
		if(mPreFilter == null || mPreFilter.test(new JFilterNode(false, field.getName() , val , aParentNodePath)))
			return aBuilder.startsWith(field.getName() , val) ;
		return false ;
	}
	
	protected boolean parseEndsWith(IFilterBuilder<T> aBuilder , JSONObject aJObj , String aParentNodePath)
	{
		String[] keys = aJObj.keyArray() ;
		Assert.isTrue(XC.count(keys) == 1 , "endsWith中的内容不合法：%s" , aJObj) ;
		FilterField field = aBuilder.parseFilterField(keys[0]) ;
		String val = aJObj.optString(keys[0]) ;
		if(mPreFilter == null || mPreFilter.test(new JFilterNode(false, field.getName() , val , aParentNodePath)))
			return aBuilder.endsWith(field.getName() , val) ;
		return false ;
	}
	
	protected boolean parseExpr(IFilterBuilder<T> aBuilder , JSONObject aJObj , String aParentNodePath)
	{
		String[] keys = aJObj.keyArray() ;
		Assert.isTrue(XC.count(keys) == 1 , "expr中的内容不合法：%s" , aJObj) ;
		FilterField field = aBuilder.parseFilterField(keys[0]) ;
		String val = aJObj.optString(keys[0]) ;
		if(mPreFilter == null || mPreFilter.test(new JFilterNode(false, field.getName() , val , aParentNodePath)))
			return aBuilder.expr(field.getName() , val) ;
		return false ;
	}
	
	protected boolean parseIn(IFilterBuilder<T> aBuilder , JSONObject aJObj , String aParentNodePath)
	{
		String[] keys = aJObj.keyArray() ;
		Assert.isTrue(XC.count(keys) == 1 , "in中的内容不合法：%s" , aJObj) ;
		JSONArray arrayJa = aJObj.optJSONArray(keys[0]) ;
		Assert.notNull(arrayJa , "没有指定字段%s的数组" , keys[0]) ;
		FilterField field = aBuilder.parseFilterField(keys[0]) ;
		String fieldName = field.getName() ;
		if(mPreFilter != null && !mPreFilter.test(new JFilterNode(false, field.getName() , arrayJa , aParentNodePath)))
			return false ;
		String type = field.getType() ;
		if(type == null)
		{
			List<String> values = XC.arrayList() ;
			return aBuilder.inStrings(keys[0], arrayJa.toCollection(values, XClassUtil.sCSN_String)) ;
		}
		switch(type)
		{
		case XClassUtil.sCSN_Double:
		{
			List<Double> values = XC.arrayList() ;
			return aBuilder.inNumbers(keys[0], arrayJa.toCollection(values, type)) ;
		}
		case XClassUtil.sCSN_Float:
		{
			List<Float> values = XC.arrayList() ;
			return aBuilder.inNumbers(keys[0], arrayJa.toCollection(values, type)) ;
		}
		case XClassUtil.sCSN_Integer:
		{
			List<Integer> values = XC.arrayList() ;
			return aBuilder.inNumbers(keys[0], arrayJa.toCollection(values, type)) ;
		}
		case XClassUtil.sCSN_Long:
		{
			List<Long> values = XC.arrayList() ;
			return aBuilder.inNumbers(keys[0], arrayJa.toCollection(values, type)) ;
		}
		case XClassUtil.sCSN_String:
		{
			List<String> values = XC.arrayList() ;
			return aBuilder.inStrings(keys[0], arrayJa.toCollection(values, type)) ;
		}
		case XClassUtil.sCSN_DateTime:
		{
			List<String> values = XC.arrayList() ;
			return aBuilder.inStrings(keys[0], arrayJa.toCollection(values, XClassUtil.sCSN_String)) ;
		}
		default:
			throw new IllegalStateException(String.format("字段%1$s的类型%2$s未支持", fieldName
					, type)) ;
		}
		
	}
	
	protected boolean parseRange(IFilterBuilder<T> aBuilder , JSONObject aJObj , String aParentNodePath)
	{
		String[] keys = aJObj.keyArray() ;
		Assert.isTrue(XC.count(keys) == 1 , "range中的内容不合法：%s" , aJObj) ;
		JSONObject rangeJObj = aJObj.optJSONObject(keys[0]) ;
		Assert.notNull(rangeJObj , "没有指定字段%s的范围" , keys[0]) ;
		FilterField field = aBuilder.parseFilterField(keys[0]) ;
		String fieldName = field.getName() ;
		if(mPreFilter != null && !mPreFilter.test(new JFilterNode(false, field.getName() , rangeJObj , aParentNodePath)))
			return false ;
		String type = field.getType() ;
		if(type == null)
		{
			Object maxVal = rangeJObj.opt(sTag_lt) ;
			boolean upperEquals = false ;
			if(maxVal == null)
			{
				maxVal = rangeJObj.opt(sTag_lte) ;
				upperEquals = true ;
			}
			Object minVal = rangeJObj.opt(sTag_gt) ;
			boolean lowerEquals = false ;
			if(minVal == null)
			{
				minVal = rangeJObj.opt(sTag_gte) ;
				lowerEquals = true ;
			}
			return aBuilder.range(fieldName , maxVal , minVal , upperEquals , lowerEquals) ;
		}
		switch(type)
		{
		case XClassUtil.sCSN_Double:
		case XClassUtil.sCSN_Float:
		case XClassUtil.sCSN_Integer:
		case XClassUtil.sCSN_Long:
		{
			Object maxVal = rangeJObj.opt("lt") ;
			boolean upperEquals = false ;
			if(maxVal == null)
			{
				maxVal = rangeJObj.optDouble("lte") ;
				upperEquals = true ;
			}
			Object minVal = rangeJObj.optDouble("gt") ;
			boolean lowerEquals = false ;
			if(minVal == null)
			{
				minVal = rangeJObj.optDouble("gte") ;
				lowerEquals = true ;
			}
			return aBuilder.rangeNumber(fieldName
					, maxVal != null?Double.parseDouble(maxVal.toString()):null 
					, minVal != null?Double.parseDouble(minVal.toString()):null 
					, upperEquals , lowerEquals) ;
		}
		case XClassUtil.sCSN_String:
		{
			String maxVal = rangeJObj.optString("lt") ;
			boolean upperEquals = false ;
			if(maxVal == null)
			{
				maxVal = rangeJObj.optString("lte") ;
				upperEquals = true ;
			}
			String minVal = rangeJObj.optString("gt") ;
			boolean lowerEquals = false ;
			if(minVal == null)
			{
				minVal = rangeJObj.optString("gte") ;
				lowerEquals = true ;
			}
			return aBuilder.rangeString(fieldName
					, maxVal, minVal, upperEquals, lowerEquals) ;
		}
		case XClassUtil.sCSN_DateTime:
		{
			String maxVal = rangeJObj.optString("lt") ;
			boolean upperEquals = false ;
			if(maxVal == null)
			{
				maxVal = rangeJObj.optString("lte") ;
				upperEquals = true ;
			}
			String minVal = rangeJObj.optString("gt") ;
			boolean lowerEquals = false ;
			if(minVal == null)
			{
				minVal = rangeJObj.optString("gte") ;
				lowerEquals = true ;
			}
			try
			{
				return aBuilder.rangeDate(fieldName
						, maxVal == null?null:XTime.adaptiveParse(maxVal) 
						, minVal == null?null:XTime.adaptiveParse(minVal) 
						, upperEquals, lowerEquals) ;
			}
			catch (Exception e)
			{
				throw new IllegalStateException(e) ;
			}
		}
		default:
			throw new IllegalStateException(String.format("字段%1$s的类型%2$s未支持", fieldName
					, type)) ;
		}
		
	}
	
//	protected boolean parseShould(IFilterBuilder<T> aBuilder , Object aObj , String aParentNodePath)
//	{
//		return parseUnion(aBuilder, aObj, sUN_should , aParentNodePath) ;
//	}
	
	protected boolean parseItem(IFilterBuilder<T> aBuilder , JSONObject aJObj , String aParentNodePath)
	{
		boolean result = parseEndFilter(aBuilder , aJObj , aParentNodePath) ;
		if(!result)
		{
			Entry<String , Object> entry = aJObj.optAny(sUN_must , sUN_must_not , sUN_should) ;
			if(entry != null)
			{
				return parseUnion(aBuilder, entry.getValue(), entry.getKey() , aParentNodePath) ;
			}
		}
		return result ;
	}
	
//	protected boolean parseMust(IFilterBuilder<T> aBuilder , Object aObj , String aParentNodePath)
//	{
//		return parseUnion(aBuilder, aObj, sUN_must , aParentNodePath) ;
//	}
	
//	protected boolean parseMustNot(IFilterBuilder<T> aBuilder , Object aObj , String aParentNodePath)
//	{
//		return parseUnion(aBuilder, aObj, sUN_must_not , aParentNodePath) ;
// 	}
	
	protected boolean parseUnion(IFilterBuilder<T> aBuilder , Object aObj , String aUnionName , String aParentNodePath)
	{
		boolean result = false ;
		if(mPreFilter == null || mPreFilter.test(new JFilterNode(true, aUnionName, aObj, aParentNodePath)))
		{
			JSONArray jarray = asJSONArray(aObj) ;
			final int len = jarray.size() ;
			if(len>0)
			{
				aBuilder.unionBegin(aUnionName) ;
				try
				{
					for(int i=0 ; i<len ; i++)
					{
						result |= parseItem(aBuilder , jarray.optJSONObject(i) , aParentNodePath+".["+i+"]") ;
					}
				}
				finally
				{
					aBuilder.unionEnd(aUnionName) ;
				}
			}
		}
		return result ;
	}
	
	protected JSONArray asJSONArray(Object aObj)
	{
		if(aObj instanceof JSONArray)
			return (JSONArray)aObj ;
		else if(aObj instanceof JSONObject)
			return new JSONArray().put(aObj) ;
		else
			throw new IllegalStateException("格式不正确："+aObj.toString()) ;
	}
	
	public static class JFilterNode
	{
		boolean mLogicKey ;
		String mKey ;
		Object mValue ;
		String mPath ;
		
		JFilterNode()
		{}
		
		JFilterNode(boolean aLogicKey , String aKey , Object aValue , String aPath)
		{
			mLogicKey = aLogicKey ;
			mKey = aKey ;
			mValue = aValue ;
			mPath = aPath ;
		}
	}
}
