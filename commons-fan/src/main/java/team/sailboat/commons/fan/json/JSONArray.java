package team.sailboat.commons.fan.json;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.infc.BiIteratorPredicate;
import team.sailboat.commons.fan.infc.EConsumer;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.text.XString;

/**
 * 
 *
 * @author yyl
 * @since 2024年9月10日
 */
public class JSONArray extends AbstractList<Object> implements JSONString , JSONCloneable , List<Object>
{

	/**
	 * The arrayList where the JSONArray's properties are kept.
	 */
	List<Object> mEleList  ;

//	Object mDefaultVal = JSONObject.NULL ;
	Object mDefaultVal = null ;
	
	/**
	 * Construct an empty JSONArray.
	 */
	public JSONArray()
	{
		mEleList = new ArrayList<>() ;
	}
	
	
	public JSONArray(int aInitCapacity)
	{
		mEleList = new ArrayList<>(aInitCapacity) ;
	}
	
	public JSONArray(int aInitCapacity , Object aDefaultVal)
	{
		this(aInitCapacity) ;
		mDefaultVal = aDefaultVal ;
	}

	/**
	 * Construct a JSONArray from a JSONTokener.
	 * 
	 * @param aTokener
	 *            A JSONTokener
	 * @throws JSONException
	 *             If there is a syntax error.
	 */
	public JSONArray(JSONTokener aTokener) throws JSONException
	{
		this();
		_init(aTokener) ;
	}

	/**
	 * Construct a JSONArray from a source JSON text.
	 * 
	 * @param source
	 *            A string that begins with <code>[</code>&nbsp;<small>(left
	 *            bracket)</small> and ends with <code>]</code>
	 *            &nbsp;<small>(right bracket)</small>.
	 * @throws JSONException
	 *             If there is a syntax error.
	 */
	public JSONArray(String source) throws JSONException
	{
		this(XString.isEmpty(source)?null:new JSONTokener(source));
	}
	
	public void reset(JSONTokener aTokener)
	{
		if(mEleList != null && !mEleList.isEmpty())
			mEleList.clear() ;
		_init(aTokener) ;
	}
	
	protected void _init(JSONTokener aTokener)
	{
		if(aTokener == null)
			return ;
		char c = aTokener.nextClean();
		char q;
		if (c == '[') {
			q = ']';
		} else if (c == '(') {
			q = ')';
		} else {
			throw aTokener.syntaxError("A JSONArray text must start with '['");
		}
		if (aTokener.nextClean() == ']') {
			return;
		}
		aTokener.back();
		for (;;) {
			if (aTokener.nextClean() == ',') {
				aTokener.back();
				mEleList.add(null);
			} else {
				aTokener.back();
				put(aTokener.nextValue());
			}
			c = aTokener.nextClean();
			switch (c) {
			case ';':
			case ',':
				if (aTokener.nextClean() == ']') {
					return;
				}
				aTokener.back();
				break;
			case ']':
			case ')':
				if (q != c) {
					throw aTokener.syntaxError("Expected a '" + Character.valueOf(q) + "'");
				}
				return;
			default:
				throw aTokener.syntaxError("Expected a ',' or ']'");
			}
		}
	}

	@Deprecated
	public JSONArray(Collection<?> collection)
	{
		this() ;
		if(collection != null)
			collection.forEach(this::put);
	}

	/**
	 * Construct a JSONArray from a collection of beans. The collection should
	 * have Java Beans.
	 * 
	 * @throws JSONException
	 *             If not an array.
	 */	
	public JSONArray(Collection<?> collection, boolean includeSuperClass)
	{
		this() ;
		if (collection != null) {
			Iterator<?> iter = collection.iterator();
			while (iter.hasNext()) {
				Object o = iter.next() ;
				if(o instanceof JSONObject)
					mEleList.add(o);
				else if (o instanceof Map)
				{
					mEleList.add(new JSONObject((Map) o, includeSuperClass));
				}
				else if (o instanceof Collection)
				{
					put((Collection)o) ;
				}
				else if (!JSONObject.isStandardProperty(o.getClass()))
				{
					mEleList.add(new JSONObject(o, includeSuperClass));
				}
				else
				{
					put(o);
				}
			}
		}
	}

	/**
	 * Construct a JSONArray from an array
	 * 
	 * @throws JSONException
	 *             If not an array.
	 */
	public JSONArray(Object array) throws JSONException {
		this();
		if(array != null)
		{ 
			if (array.getClass().isArray()) {
				int length = Array.getLength(array);
				for (int i = 0; i < length; i += 1) {
					put(Array.get(array, i));
				}
			}
			else if(array instanceof Iterable<?>)
			{
				for(Object ele : (Iterable<?>)array)
					put(ele) ;
			}
			else
			{
				throw new JSONException(
						"JSONArray入参可以是数组，或者可迭代对象，不能是："+array.getClass().getName()) ;
			}
		}
	}

	/**
	 * Construct a JSONArray from an array with a bean. The array should have
	 * Java Beans.
	 * 
	 * @throws JSONException
	 *             If not an array.
	 */
	public JSONArray(Object array, boolean includeSuperClass)
			throws JSONException {
		this();
		if (array.getClass().isArray()) {
			int length = Array.getLength(array);
			for (int i = 0; i < length; i += 1) {
				Object o = Array.get(array, i);
				if (JSONObject.isStandardProperty(o.getClass())) {
					mEleList.add(o);
				} else {
					mEleList.add(new JSONObject(o, includeSuperClass));
				}
			}
		} else {
			throw new JSONException(
					"JSONArray initial value should be a string or collection or array.");
		}
	}

	/**
	 * Get the object value associated with an index.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return An object value.
	 * @throws JSONException
	 *             If there is no value for the index.
	 */
	public Object get(int index) throws JSONException {
		Object o = opt(index);
		if (o == null) {
			throw new JSONException("JSONArray[" + index + "] not found.");
		}
		return o;
	}

	/**
	 * Get the boolean value associated with an index. The string values "true"
	 * and "false" are converted to boolean.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The truth.
	 * @throws JSONException
	 *             If there is no value for the index or if the value is not
	 *             convertable to boolean.
	 */
	public boolean getBoolean(int index) throws JSONException {
		Object o = get(index);
		if (o.equals(Boolean.FALSE)
				|| (o instanceof String && ((String) o)
						.equalsIgnoreCase("false"))) {
			return false;
		} else if (o.equals(Boolean.TRUE)
				|| (o instanceof String && ((String) o)
						.equalsIgnoreCase("true"))) {
			return true;
		}
		throw new JSONException("JSONArray[" + index + "] is not a Boolean.");
	}

	/**
	 * Get the double value associated with an index.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The value.
	 * @throws JSONException
	 *             If the key is not found or if the value cannot be converted
	 *             to a number.
	 */
	public double getDouble(int index) throws JSONException {
		Object o = get(index);
		try {
			return o instanceof Number ? ((Number) o).doubleValue() : Double
					.valueOf((String) o).doubleValue();
		} catch (Exception e) {
			throw new JSONException("JSONArray[" + index + "] is not a number.");
		}
	}

	/**
	 * Get the int value associated with an index.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The value.
	 * @throws JSONException
	 *             If the key is not found or if the value cannot be converted
	 *             to a number. if the value cannot be converted to a number.
	 */
	public int getInt(int index) throws JSONException {
		Object o = get(index);
		return o instanceof Number ? ((Number) o).intValue()
				: (int) getDouble(index);
	}

	/**
	 * Get the JSONArray associated with an index.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return A JSONArray value.
	 * @throws JSONException
	 *             If there is no value for the index. or if the value is not a
	 *             JSONArray
	 */
	public JSONArray getJSONArray(int index) throws JSONException {
		Object o = get(index);
		if (o instanceof JSONArray) {
			return (JSONArray) o;
		}
		throw new JSONException("JSONArray[" + index + "] is not a JSONArray.");
	}

	/**
	 * Get the JSONObject associated with an index.
	 * 
	 * @param index
	 *            subscript
	 * @return A JSONObject value.
	 * @throws JSONException
	 *             If there is no value for the index or if the value is not a
	 *             JSONObject
	 */
	public JSONObject getJSONObject(int index) throws JSONException {
		Object o = get(index);
		if (o instanceof JSONObject) {
			return (JSONObject) o;
		}
		throw new JSONException("JSONArray[" + index + "] is not a JSONObject.");
	}

	/**
	 * Get the long value associated with an index.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The value.
	 * @throws JSONException
	 *             If the key is not found or if the value cannot be converted
	 *             to a number.
	 */
	public long getLong(int index) throws JSONException {
		Object o = get(index);
		return o instanceof Number ? ((Number) o).longValue()
				: (long) getDouble(index);
	}

	/**
	 * Get the string associated with an index.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return A string value.
	 * @throws JSONException
	 *             If there is no value for the index.
	 */
	public String getString(int index) throws JSONException {
		return get(index).toString();
	}

	/**
	 * Determine if the value is null.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return true if the value at the index is null, or if there is no value.
	 */
	public boolean isNull(int index) {
//		return JSONObject.NULL.equals(opt(index));
		return null == opt(index) ;
	}

	/**
	 * Make a string from the contents of this JSONArray. The
	 * <code>separator</code> string is inserted between each element. Warning:
	 * This method assumes that the data structure is acyclical.
	 * 
	 * @param separator
	 *            A string that will be inserted between the elements.
	 * @return a string.
	 * @throws JSONException
	 *             If the array contains an invalid number.
	 */
	public String join(String separator) throws JSONException
	{
		int len = size();
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < len; i += 1) {
			if (i > 0) {
				sb.append(separator);
			}
			sb.append(JSONObject.valueToString(mEleList.get(i)));
		}
		return sb.toString();
	}
	
	public int size()
	{
		return mEleList.size() ;
	}

	/**
	 * Get the optional object value associated with an index.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return An object value, or null if there is no object at that index.
	 */
	public Object opt(int index)
	{
		if(index < 0 || index >= size())
			return null ;
		Object val = mEleList.get(index) ;
//		return JSONObject.NULL.equals(val)?null:val ;
		return val ;
	}
	
	public Object opt__source(int aIndex)
	{
		Object obj = opt(aIndex) ;
		return obj instanceof Polytope?((Polytope)obj).getSource():obj ;
	}
	
	public Object opt__facade(int aIndex)
	{
		Object obj = opt(aIndex) ;
		return obj instanceof Polytope?((Polytope)obj).getFacade():obj ;
	}

	/**
	 * Get the optional boolean value associated with an index. It returns false
	 * if there is no value at that index, or if the value is not Boolean.TRUE
	 * or the String "true".
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The truth.
	 */
	public boolean optBoolean(int index) {
		return optBoolean(index, false);
	}

	/**
	 * Get the optional boolean value associated with an index. It returns the
	 * defaultValue if there is no value at that index or if it is not a Boolean
	 * or the String "true" or "false" (case insensitive).
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @param defaultValue
	 *            A boolean default.
	 * @return The truth.
	 */
	public boolean optBoolean(int index, boolean defaultValue) {
		try {
			return getBoolean(index);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Get the optional double value associated with an index. NaN is returned
	 * if there is no value for the index, or if the value is not a number and
	 * cannot be converted to a number.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The value.
	 */
	public double optDouble(int index) {
		return optDouble(index, Double.NaN);
	}

	/**
	 * Get the optional double value associated with an index. The defaultValue
	 * is returned if there is no value for the index, or if the value is not a
	 * number and cannot be converted to a number.
	 * 
	 * @param index
	 *            subscript
	 * @param defaultValue
	 *            The default value.
	 * @return The value.
	 */
	public double optDouble(int index, double defaultValue) {
		try {
			return getDouble(index);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Get the optional int value associated with an index. Zero is returned if
	 * there is no value for the index, or if the value is not a number and
	 * cannot be converted to a number.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The value.
	 */
	public int optInt(int index) {
		return optInt(index, 0);
	}

	/**
	 * Get the optional int value associated with an index. The defaultValue is
	 * returned if there is no value for the index, or if the value is not a
	 * number and cannot be converted to a number.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @param defaultValue
	 *            The default value.
	 * @return The value.
	 */
	public int optInt(int index, int defaultValue) {
		try {
			return getInt(index);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Get the optional JSONArray associated with an index.
	 * 
	 * @param index
	 *            subscript
	 * @return A JSONArray value, or null if the index has no value, or if the
	 *         value is not a JSONArray.
	 */
	public JSONArray optJSONArray(int index) {
		Object o = opt(index);
		return o instanceof JSONArray ? (JSONArray) o : null;
	}

	/**
	 * Get the optional JSONObject associated with an index. Null is returned if
	 * the key is not found, or null if the index has no value, or if the value
	 * is not a JSONObject.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return A JSONObject value.
	 */
	public JSONObject optJSONObject(int index) {
		Object o = opt(index);
		return o instanceof JSONObject ? (JSONObject) o : null;
	}

	/**
	 * Get the optional long value associated with an index. Zero is returned if
	 * there is no value for the index, or if the value is not a number and
	 * cannot be converted to a number.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The value.
	 */
	public long optLong(int index) {
		return optLong(index, 0);
	}

	/**
	 * Get the optional long value associated with an index. The defaultValue is
	 * returned if there is no value for the index, or if the value is not a
	 * number and cannot be converted to a number.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @param defaultValue
	 *            The default value.
	 * @return The value.
	 */
	public long optLong(int index, long defaultValue) {
		try {
			return getLong(index);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Get the optional string value associated with an index. It returns an
	 * empty string if there is no value at that index. If the value is not a
	 * string and is not null, then it is coverted to a string.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return A String value.
	 */
	public String optString(int index) {
		return optString(index, "");
	}

	/**
	 * Get the optional string associated with an index. The defaultValue is
	 * returned if the key is not found.
	 * 
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @param defaultValue
	 *            The default value.
	 * @return A String value.
	 */
	public String optString(int index, String defaultValue)
	{
		Object o = opt(index);
		return o != null ? o.toString() : defaultValue;
	}

	/**
	 * 将一个布尔值添加到当前JSONArray对象中。
	 *
	 * @param aValue 要添加的布尔值。
	 * @return 返回当前对象实例，以便支持链式调用。
	 */
	public JSONArray put(boolean aValue)
	{
		mEleList.add(aValue ? Boolean.TRUE : Boolean.FALSE);
		return this;
	}

	/**
	 * 将一个双精度浮点数添加到当前JSONArray对象中。
	 *
	 * @param aValue 双精度浮点数值。
	 * @return 返回当前对象实例，以便支持链式调用。
	 */
	public JSONArray put(double value) throws JSONException
	{
		mEleList.add(Double.valueOf(value)) ;
		return this;
	}

	/**
	 * 将一个整型数值添加到当前JSONArray对象中。
	 *
	 * @param aValue 要添加的整型数值。
	 * @return 返回当前对象实例，以便支持链式调用。
	 */
	public JSONArray put(int value)
	{
		mEleList.add(Integer.valueOf(value));
		return this;
	}
	
	/**
	 * 将一个长整型数值添加到当前JSONArray对象中。
	 *
	 * @param aValue 要添加的长整型数值。
	 * @return 返回当前对象实例，以便支持链式调用。
	 */
	public JSONArray put(long value)
	{
		mEleList.add(Long.valueOf(value)) ;
		return this;
	}

	/**
	 * 将一个Map类型的对象转换为JSONObject并添加到内部的列表中。
	 * 如果提供的Map是null，则向列表中添加null。
	 *
	 * @param aValue 要添加到列表中的Map值。
	 * @return 返回当前对象实例，以便支持链式调用。
	 */
	public JSONArray put(Map aValue)
	{
		if(aValue == null)
			mEleList.add(null) ;
		mEleList.add(JSONObject.of(aValue)) ;
		return this;
	}
	
	@Override
	public void add(int aIndex, Object aElement)
	{
		insert(aIndex, aElement) ;
	}
	
	/**  
	 * 将一个可迭代对象转成JSONArray添加到当前JSONArray对象中。  
	 *   
	 * @param aC 要添加的可迭代对象。如果此对象为null，则直接将null添加到JSONArray中。  
	 *           如果此对象是JSONArray的实例，则直接将整个JSONArray作为一个元素添加到当前JSONArray中。  
	 *           否则，将此可迭代对象转成JSONArray，添加到当前JSONArray中。  
	 * @return 返回当前JSONArray对象，以便支持链式调用。  
	 */  
	public JSONArray put(Iterable<?> aC)
	{
		if(aC == null)
			mEleList.add(null) ;
		else if(aC.getClass().equals(JSONArray.class))
			mEleList.add(aC) ;
		else
		{
			JSONArray ja = new JSONArray() ;
			for(Object ele : aC)
				ja.put(ele) ;
			mEleList.add(ja) ;
		}
		return this ;
	}

	/**  
	 * 将一个对象添加到JSONArray中，根据对象的类型进行不同的处理。  
	 *   
	 * @param aValue 要添加到JSONArray中的对象。  
	 *               - 如果对象为null，则直接将null添加到JSONArray中。  
	 *               - 如果对象是JSONArray的实例，则直接将该对象添加到JSONArray中。  
	 *               - 如果对象实现了ToJSONObject接口，则调用其toJSONObject方法将对象转换为JSONObject后添加到JSONArray中。  
	 *               - 如果对象是Map的实例，则使用JSONObject.of方法将Map转换为JSONObject后添加到JSONArray中。  
	 *               - 如果对象是数组，则使用JSONArray.of方法将数组转换为JSONArray后添加到JSONArray中。  
	 *               - 如果对象是Iterable的实例，则转换为JSONArray后添加到JSONArray中。  
	 *               - 如果对象是Enum的实例，则将该枚举的名称（name()方法返回的值）作为字符串添加到JSONArray中。  
	 *               - 对于其他类型的对象，直接将其添加到JSONArray中。  
	 *   
	 * @return 返回当前JSONArray对象，以便进行链式调用。  
	 */  
	public JSONArray put(Object aValue)
	{
		mEleList.add(toJSONElement(aValue)) ;
		return this;
	}
	
	public JSONArray setLength(int aLen)
	{
		Assert.isTrue(aLen>=0 , "长度不能小于0！") ;
		final int len = size() ; 
		if(aLen>len)
		{
			// 需要补充元素
			for(int i= size() ; i<aLen ; i++)
			{
				put(mDefaultVal) ;
			}
		}
		else if(aLen < len)
		{
			// 删掉多余的
			XC.remove(mEleList , aLen) ;
		}
		return this ;
	}
	
	public JSONArray putIfNotNull(Object value)
	{
		if(value != null)
		{
			if(value instanceof ToJSONObject)
				mEleList.add(((ToJSONObject)value).toJSONObject()) ;
			else
				mEleList.add(value) ;
		}
		return this;
	}
	
	public JSONArray resetIf(Predicate<Object> aPred , Object aNewVal , int aStartIndex , int aLen)
	{
		final int size = mEleList.size() ;
		for(int j=size ; j<aStartIndex ; j++)
			mEleList.add(null) ;
		for(int i=0 ; i<aLen; i++)
		{
			int k = aStartIndex+i ;
			if(k<size)
			{
				if(aPred.test(mEleList.get(k)))
					mEleList.set(k, aNewVal) ;
			}
			else if(aPred.test(null))
				mEleList.add(aNewVal) ;
		}
		return this ;
	}

	/**
	 * Put or replace a boolean value in the JSONArray. If the index is greater
	 * than the length of the JSONArray, then null elements will be added as
	 * necessary to pad it out.
	 * 
	 * @param index
	 *            The subscript.
	 * @param value
	 *            A boolean value.
	 * @return this.
	 * @throws JSONException
	 *             If the index is negative.
	 */
	public JSONArray put(int index, boolean value) throws JSONException {
		put(index, value ? Boolean.TRUE : Boolean.FALSE);
		return this;
	}

	/**
	 * Put a value in the JSONArray, where the value will be a JSONArray which
	 * is produced from a Collection.
	 * 
	 * @param index
	 *            The subscript.
	 * @param value
	 *            A Collection value.
	 * @return this.
	 * @throws JSONException
	 *             If the index is negative or if the value is not finite.
	 */
	public JSONArray put(int index, Collection<?> value) throws JSONException
	{
		put(index, new JSONArray(value));
		return this;
	}
	
	public JSONArray putAll(Collection<?> aObjs)
	{
		if(XC.isNotEmpty(aObjs))
		{
			for(Object jobj :aObjs)
			{
				if(jobj != null)
					mEleList.add(jobj);
			}
		}
		return this ;
	}
	
	public JSONArray putAll(Iterable<?> aObjs)
	{
		if(aObjs !=null)
		{
			for(Object jobj :aObjs)
			{
				if(jobj != null)
				{
					put(jobj) ;
				}
			}
		}
		return this ;
	}

	/**
	 * Put or replace a double value. If the index is greater than the length of
	 * the JSONArray, then null elements will be added as necessary to pad it
	 * out.
	 * 
	 * @param index
	 *            The subscript.
	 * @param value
	 *            A double value.
	 * @return this.
	 * @throws JSONException
	 *             If the index is negative or if the value is not finite.
	 */
	public JSONArray put(int index, double value) throws JSONException {
		put(index, Double.valueOf(value));
		return this;
	}

	/**
	 * Put or replace an int value. If the index is greater than the length of
	 * the JSONArray, then null elements will be added as necessary to pad it
	 * out.
	 * 
	 * @param index
	 *            The subscript.
	 * @param value
	 *            An int value.
	 * @return this.
	 * @throws JSONException
	 *             If the index is negative.
	 */
	public JSONArray put(int index, int value) throws JSONException
	{
		put(index, Integer.valueOf(value));
		return this;
	}

	/**
	 * Put or replace a long value. If the index is greater than the length of
	 * the JSONArray, then null elements will be added as necessary to pad it
	 * out.
	 * 
	 * @param index
	 *            The subscript.
	 * @param value
	 *            A long value.
	 * @return this.
	 * @throws JSONException
	 *             If the index is negative.
	 */
	public JSONArray put(int index, long value) throws JSONException
	{
		put(index, Long.valueOf(value));
		return this;
	}

	/**
	 * Put a value in the JSONArray, where the value will be a JSONObject which
	 * is produced from a Map.
	 * 
	 * @param index
	 *            The subscript.
	 * @param value
	 *            The Map value.
	 * @return this.
	 * @throws JSONException
	 *             If the index is negative or if the the value is an invalid
	 *             number.
	 */
	public JSONArray put(int index, Map value) throws JSONException {
		put(index, new JSONObject(value));
		return this;
	}

	/**
	 * Put or replace an object value in the JSONArray. If the index is greater
	 * than the length of the JSONArray, then null elements will be added as
	 * necessary to pad it out.
	 * 
	 * @param index
	 *            The subscript.
	 * @param value
	 *            The value to put into the array. The value should be a
	 *            Boolean, Double, Integer, JSONArray, JSONObject, Long, or
	 *            String, or the JSONObject.NULL object.
	 * @return this.
	 * @throws JSONException
	 *             If the index is negative or if the the value is an invalid
	 *             number.
	 */
	public JSONArray put(int index, Object value) throws JSONException
	{
		JSONObject.testValidity(value , "第{}个元素" , index);
		if (index < 0) {
			throw new JSONException("JSONArray[" + index + "] not found.");
		}
		if (index < size())
		{
			if(value != null && value instanceof ToJSONObject)
				mEleList.set(index, ((ToJSONObject)value).toJSONObject());
			else
				mEleList.set(index, value) ;
		}
		else
		{
			while (index != size()) {
				put(mDefaultVal);
			}
			put(value);
		}
		return this;
	}

	/**
	 * Remove an index and close the hole.
	 * 
	 * @param index
	 *            The index of the element to be removed.
	 * @return The value that was associated with the index, or null if there
	 *         was no value.
	 */
	public Object remove(int index)
	{
		return mEleList.remove(index) ;
	}
	
	public boolean remove(Object aObj)
	{
		Iterator<Object> it = mEleList.iterator() ;
		boolean removed = false ;
		while(it.hasNext())
		{
			if(JCommon.equals(aObj, it.next()))
			{
				it.remove() ;
				removed = true ;
			}
		}
		return removed ;
	}
	
	public Object removeLast()
	{
		if(!mEleList.isEmpty())
			return mEleList.remove(mEleList.size()-1) ;
		return null ;
	}
	
	/**
	 * 
	 * @param aStartIndex
	 * @param aEndIndex				不包含
	 * @return
	 */
	public JSONArray removeRange0(int aStartIndex , int aEndIndex)
	{
		final int len = mEleList.size() ;
		final int startIndex = Math.max(0, aStartIndex) ;
		if(len>startIndex)
		{
			int endIndex = Math.min(aEndIndex, len) ;
			if(endIndex>startIndex)
				mEleList.subList(aStartIndex, aEndIndex).clear() ;
		}
		return this ;
	}
	
	/**
	 * 从集合中移除所有满足给定谓词条件的元素。
	 * 
	 * @param aPred 一个谓词函数，它接受集合中的元素（类型为Object）作为参数，并返回一个布尔值。
	 *              如果谓词函数对某个元素返回true，则该元素将从集合中移除。
	 * @return 如果至少有一个元素被移除，则返回true；否则返回false。
	 */
	public boolean removeIf(Predicate<Object> aPred)
	{
		Iterator<Object> it = mEleList.iterator() ;
		boolean removed = false ;
		while(it.hasNext())
		{
			if(aPred.test(it.next()))
			{
				it.remove() ;
				removed = true ;
			}
		}
		return removed ;
	}
	
	/**
	 * 从JSONArray中移除所有满足给定谓词条件的JSONArray元素。
	 * 
	 * <p>此方法遍历JSONArray中的每个元素（假设元素本身也是JSONArray类型），
	 * 并使用提供的谓词函数来测试每个元素。如果谓词函数对某个JSONArray元素返回true，
	 * 则该元素将从JSONArray中移除。</p>
	 * 
	 * <p>注意：此方法直接修改调用它的JSONArray对象，并返回修改后的对象本身，
	 * 支持链式调用。</p>
	 * 
	 * @param aPred 一个谓词函数，它接受一个JSONArray对象作为参数，并返回一个布尔值。
	 *              如果谓词函数对某个JSONArray对象返回true，则该对象将从JSONArray中移除。
	 * @return 返回操作后的当前JSONArray对象（支持链式调用）。
	 */
	public JSONArray removeIf_JSONArray(Predicate<JSONArray> aPred)
	{
		Iterator<Object> it = mEleList.iterator() ;
		while(it.hasNext())
		{
			if(aPred.test((JSONArray)it.next()))
				it.remove() ;
		}
		return this ;
	}
	
	/**
	 * 从JSONArray中移除所有满足给定谓词条件的JSONObject元素。
	 * 
	 * @param aPred 一个谓词函数，它接受一个JSONObject对象作为参数，并返回一个布尔值。
	 *              如果谓词函数对某个JSONObject对象返回true，则该对象将从JSONArray中移除。
	 * @return 返回操作后的当前JSONArray对象（支持链式调用）。
	 */
	public JSONArray removeIf_JSONObject(Predicate<JSONObject> aPred)
	{
		Iterator<Object> it = mEleList.iterator() ;
		while(it.hasNext())
		{
			if(aPred.test((JSONObject)it.next()))
				it.remove() ;
		}
		return this ;
	}
	
	/**
	 * 从JSONArray中保留所有满足给定谓词条件的JSONArray元素，移除不满足条件的元素。
	 * 
	 * @param aPred 一个谓词函数，它接受一个JSONArray对象作为参数，并返回一个布尔值。
	 *              如果谓词函数对某个JSONArray对象返回true，则该对象将保留在JSONArray中；
	 *              如果返回false，则该对象将被移除。
	 * @return 返回操作后的当前JSONArray对象（支持链式调用）。
	 */
	public JSONArray retainIf_JSONArray(Predicate<JSONArray> aPred)
	{
		Iterator<Object> it = mEleList.iterator() ;
		while(it.hasNext())
		{
			if(!aPred.test((JSONArray)it.next()))
				it.remove() ;
		}
		return this ;
	}
	
	/**
	 * 从JSONArray中保留所有满足给定谓词条件的JSONObject元素，移除不满足条件的元素。
	 * 
	 * @param aPred 一个谓词函数，它接受一个JSONObject对象作为参数，并返回一个布尔值。
	 *              如果谓词函数对某个JSONObject对象返回true，则该对象将保留在JSONArray中；
	 *              如果返回false，则该对象将被移除。
	 * @return 返回操作后的当前JSONArray对象（支持链式调用）。
	 */
	public JSONArray retainIf_JSONObject(Predicate<JSONObject> aPred)
	{
		Iterator<Object> it = mEleList.iterator() ;
		while(it.hasNext())
		{
			if(!aPred.test((JSONObject)it.next()))
				it.remove() ;
		}
		return this ;
	}

	/**
	 * Produce a JSONObject by combining a JSONArray of names with the values of
	 * this JSONArray.
	 * 
	 * @param names
	 *            A JSONArray containing a list of key strings. These will be
	 *            paired with the values.
	 * @return A JSONObject, or null if there are no names or if this JSONArray
	 *         has no values.
	 * @throws JSONException
	 *             If any of the names are null.
	 */
	public JSONObject toJSONObject(JSONArray aNames) throws JSONException
	{
		final int len = aNames.size() ;
		if (aNames == null || len == 0 || size() == 0)
		{
			return null;
		}
		JSONObject jo = new JSONObject();
		for (int i = 0; i < len ; i += 1)
		{
			jo.put(aNames.getString(i) , this.opt(i));
		}
		return jo;
	}
	
	/**
	 * 
	 * 此JSONArray内的元素必需是JSONObject		<br />
	 * 此方法遍历JSONArray中的每一个JSONObject类型的元素，并从每个JSONObject中提取出由参数aName指定的值。
	 * 这个值随后被用作键，而原始的JSONObject本身则作为值，被添加到一个新的JSONObject中。最终，这个方法返回这个新构建的JSONObject。
	 * 例如下面的JSONArray：
	 * <pre>
	 * [  
     *   {"id": "1", "name": "Alice", "age": 30},  
     *   {"id": "2", "name": "Bob", "age": 25}  
     * ]
	 * </pre>
	 * 调用toJSONObject("name")将返回：
	 * <pre>
	 * {  
     *   "Alice": {"id": "1", "name": "Alice", "age": 30},  
     *   "Bob": {"id": "2", "name": "Bob", "age": 25}  
     * }
	 * </pre>
	 * @param aName
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toJSONObject(String aName) throws JSONException
	{
		JSONObject jo = new JSONObject();
		final int len = size() ;
		for (int i = 0; i < len ; i++)
		{
			JSONObject eleJo = getJSONObject(i) ;
			jo.put(eleJo.getString(aName), eleJo) ;
		}
		return jo;
	}

	/**
	 * Make a JSON text of this JSONArray. For compactness, no unnecessary
	 * whitespace is added. If it is not possible to produce a syntactically
	 * correct JSON text then null will be returned instead. This could occur if
	 * the array contains an invalid number.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * 
	 * @return a printable, displayable, transmittable representation of the
	 *         array.
	 */
	public String toString() {
		try {
			return '[' + join(",") + ']';
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Make a prettyprinted JSON text of this JSONArray. Warning: This method
	 * assumes that the data structure is acyclical.
	 * 
	 * @param indentFactor
	 *            The number of spaces to add to each level of indentation.
	 * @return a printable, displayable, transmittable representation of the
	 *         object, beginning with <code>[</code>&nbsp;<small>(left
	 *         bracket)</small> and ending with <code>]</code>
	 *         &nbsp;<small>(right bracket)</small>.
	 * @throws JSONException
	 */
	public String toString(int indentFactor) throws JSONException {
		return toString(indentFactor, 0);
	}

	/**
	 * Make a prettyprinted JSON text of this JSONArray. Warning: This method
	 * assumes that the data structure is acyclical.
	 * 
	 * @param indentFactor
	 *            The number of spaces to add to each level of indentation.
	 * @param indent
	 *            The indention of the top level.
	 * @return a printable, displayable, transmittable representation of the
	 *         array.
	 * @throws JSONException
	 */
	public String toString(int indentFactor, int indent) throws JSONException {
		int len = size() ;
		if (len == 0) {
			return "[]";
		}
		int i;
		StringBuffer sb = new StringBuffer("[");
		if (len == 1) {
			sb.append(JSONObject.valueToString(mEleList.get(0),
					indentFactor, indent));
		} else {
			int newindent = indent + indentFactor;
			sb.append('\n');
			for (i = 0; i < len; i += 1) {
				if (i > 0) {
					sb.append(",\n");
				}
				for (int j = 0; j < newindent; j += 1) {
					sb.append(' ');
				}
				sb.append(JSONObject.valueToString(mEleList.get(i),
						indentFactor, newindent));
			}
			sb.append('\n');
			for (i = 0; i < indent; i += 1) {
				sb.append(' ');
			}
		}
		sb.append(']');
		return sb.toString();
	}

	/**
	 * Write the contents of the JSONArray as JSON text to a writer. For
	 * compactness, no whitespace is added.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * 
	 * @return The writer.
	 * @throws JSONException
	 */
	public Writer write(Writer writer) throws JSONException {
		try {
			boolean b = false;
			final int len = size() ;

			writer.write('[');

			for (int i = 0; i < len; i += 1) {
				if (b) {
					writer.write(',');
				}
				Object v = mEleList.get(i);
				if (v instanceof JSONObject) {
					((JSONObject) v).write(writer);
				} else if (v instanceof JSONArray) {
					((JSONArray) v).write(writer);
				} else {
					writer.write(JSONObject.valueToString(v));
				}
				b = true;
			}
			writer.write(']');
			return writer;
		} catch (IOException e) {
			throw new JSONException(e);
		}
	}
	
	public String[] toStringArray()
	{
		int len = size() ;
		String[] array = new String[len] ; 
		for(int i=0 ; i<len ; i++)
		{
			array[i] = optString(i, null) ;
		}
		return array ;
	}
	
	public List<Object> toList()
	{
		return toList(XC.arrayList()) ;
	}
	
	public List<Object> toList(List<Object> aList)
	{
		forEach((obj)->{
			if(obj instanceof JSONObject)
				aList.add(((JSONObject)obj).toMap()) ;
			else if(obj instanceof JSONArray)
				aList.add(((JSONArray)obj).toList()) ;
//			else if(JSONObject.NULL.equals(obj))
//				aList.add(null) ;
			else
				aList.add(obj) ;
		});
		return aList ;
	}
	
	public Object[] toArray()
	{
		if(mEleList == null || mEleList.isEmpty())
			return JCommon.sEmptyObjectArray ;
		final int len = mEleList.size() ;
		Object[] arr = new Object[len] ;
		for(int i=0 ; i<len ; i++)
		{
			arr[i] = isNull(i)?null:mEleList.get(i) ;
		}
		return arr ;
	}
	
	@SuppressWarnings("unchecked")
	public <U extends Collection<E> , E> U toCollection(U aList , String aCSN)
	{
		forEach((obj)->{
			aList.add((E) XClassUtil.typeAdapt(obj, aCSN)) ;
		});
		return aList ;
	}

	@Override
	public String toJSONString()
	{
		return toString() ;
	}
	
	public JSONArray sort0(Comparator<Object> aComp)
	{
		mEleList.sort(aComp) ;
		return this ;
	}
	
	public JSONArray sort(Comparator<Object> aComp , int aFromIndex , int aToIndex)
	{
		mEleList.subList(aFromIndex, aToIndex).sort(aComp) ;
		return this ;
	}
	
	public JSONArray merge(JSONArray aJArray)
	{
		if(aJArray != null && aJArray.size() > 0)
		{
			mEleList.addAll(aJArray.mEleList) ;
		}
		return this ;
	}
	
	public JSONArray retain(int aStartIndex , int aEndIndex)
	{
		aStartIndex = Math.max(0, aStartIndex) ;
		if(aStartIndex<mEleList.size())
		{
			if(aStartIndex>0)
				mEleList.subList(0, aStartIndex).clear();
			aEndIndex -= aStartIndex ;
			if(aEndIndex>=0 && aEndIndex<mEleList.size())
				mEleList.subList(aEndIndex, mEleList.size()).clear();
		}
		return this ;
	}
	
	public JSONArray retainInIndexes(int...aIndexes)
	{
		final int len = mEleList.size() ;
		int[] orderIndexes = new int[len] ;
		for(int index : aIndexes)
		{
			if(index < len)
				orderIndexes[index] = 1 ;
		}
		Iterator<Object> it = mEleList.iterator() ;
		int i=0 ;
		while(it.hasNext())
		{
			it.next() ;
			if(orderIndexes[i++] != 1)
				it.remove();
		}
		return this ;
	}
	
	public JSONArray retainInIndexes(boolean aOrderByIndex , int...aIndexes)
	{
		if(aOrderByIndex)
		{
			final int len = mEleList.size() ;
			for(int index : aIndexes)
			{
				if(index < len)
				{
					mEleList.add(mEleList.get(index)) ;
				}
			}
			mEleList.subList(0, len).clear();
			return this ;
		}
		else
		{
			return retainInIndexes(aIndexes) ;
		}
	}
	
	public JSONArray subJSONArray(int aStartIndex , int aEndIndex)
	{
		aStartIndex = Math.max(0, aStartIndex) ;
		aEndIndex = Math.min(mEleList.size(), aEndIndex) ;
		JSONArray sub = new JSONArray() ;
		if(aStartIndex<aEndIndex)
		{
			sub.mEleList.addAll(mEleList.subList(aStartIndex, aEndIndex)) ;
		}
		return sub ;
	}
	
	public JSONArray subJSONArrayInIndexes(int... aIndexes)
	{
		JSONArray sub = new JSONArray() ;
		for(int index : aIndexes)
			sub.mEleList.add(mEleList.get(index)) ;
		return sub ;
	}
	
	@Override
	public JSONArray clone()
	{
		JSONArray clone = new JSONArray() ;
		for(Object obj : mEleList)
		{
			if(obj instanceof JSONCloneable)
			{
				clone.mEleList.add(((JSONCloneable)obj).clone()) ;
			}
			else
				clone.mEleList.add(obj) ;
		}
		return clone ;
	}
	
	/**
	 * 交换内部数据
	 * @param aJArray
	 */
	public void swapData(JSONArray aJArray)
	{
		if(aJArray != null)
		{
			List<Object> tempList = mEleList ;
			this.mEleList = aJArray.mEleList ;
			aJArray.mEleList = tempList ;
		}
	}
	
	public void clear()
	{
		mEleList.clear(); 
	}
	
	@Override
	public void forEach(Consumer<? super Object> aAction)
	{
		mEleList.forEach(aAction) ;
	}
	
	public JSONArray forEachObj(Consumer<Object> aConsumer)
	{
		mEleList.forEach(aConsumer) ;
		return this ;
	}
	
	public JSONArray forEachJSONArray(Consumer<JSONArray> aConsumer)
	{
		for(Object obj : mEleList)
		{
			aConsumer.accept((JSONArray) obj);
		}
		return this ;
	}
	
	public JSONArray forEachJSONObject(Consumer<JSONObject> aConsumer)
	{
		for(Object obj : mEleList)
		{
			aConsumer.accept((JSONObject) obj);
		}
		return this ;
	}
	
	public <E extends Throwable> JSONArray forEachJSONObject_E(EConsumer<JSONObject , E> aConsumer) throws E
	{
		for(Object obj : mEleList)
		{
			aConsumer.accept((JSONObject) obj);
		}
		return this ;
	}
	
	/**
	 * 条件性地将一个对象放入JSONArray中。
	 * 
	 * @param aCnd 条件表达式，如果为true，则将对象放入JSONArray。
	 * @param aObj 要放入的对象。
	 * @return 返回当前JSONArray对象，便于链式调用。
	 */
	public JSONArray putIf(boolean aCnd , Object aObj)
	{
		if(aCnd)
			put(aObj) ;
		return this ;
	}
	
	/**
	 * 如果JSONArray中不存在指定的对象，则将其添加进去。
	 * 注意：此处的“不存在”是基于toJSONElement转换后的对象进行比较的。
	 * 
	 * @param aObj 要检查并可能添加的对象。
	 * @return 返回当前JSONArray对象，便于链式调用。
	 */
	public JSONArray putIfAbsent(Object aObj)
	{
		Object ele = toJSONElement(aObj) ;
		if(!mEleList.contains(ele))
			mEleList.add(ele) ;
		return this ;
	}
	
	/**
	 * 遍历一个Iterable集合，如果JSONArray中不存在集合中的对象，则将其添加进去。
	 * 注意：此处的“不存在”是基于toJSONElement转换后的对象进行比较的。
	 * 
	 * @param aObjs 要检查并可能添加的Iterable集合。
	 * @return 返回当前JSONArray对象，便于链式调用。
	 */
	public JSONArray putAnyIfAbsent(Iterable<?> aObjs)
	{
		for(Object obj : aObjs)
		{
			putIfAbsent(obj) ;
		}
		return this ;
	}
	
	/**
	 * 检查JSONArray是否包含指定的对象。
	 * 
	 * 此方法通过调用toJSONElement将输入对象转换为JSON元素，并检查JSONArray中是否包含该元素。
	 * 
	 * @param aObj 要检查的对象。
	 * @return 如果JSONArray包含指定的对象（转换为JSON元素后），则返回true；否则返回false。
	 */
	public boolean contains(Object aObj)
	{
		return mEleList.contains(toJSONElement(aObj)) ;
	}
	
	/**
	 * 在JSONArray的指定位置插入一个集合中的所有元素。
	 * 
	 * 如果集合为空，则不进行任何操作。
	 * 如果指定的索引小于0，则将其设置为0。
	 * 如果指定的索引小于JSONArray的大小，则在该索引处插入集合中的元素（转换为JSON元素后）。
	 * 如果指定的索引大于或等于JSONArray的大小，则将集合中的所有元素添加到JSONArray的末尾。
	 * 
	 * @param aIndex 插入的起始索引。
	 * @param aValue 要插入的集合。
	 * @return 返回当前JSONArray对象，便于链式调用。
	 */
	public JSONArray insertAll(int aIndex , Collection<?> aValue)
	{
		if(XC.isEmpty(aValue))
			return this ;
		if(aIndex<0)
			aIndex = 0 ;
		int i = aIndex ;
		if(aIndex < mEleList.size())
		{
			mEleList.addAll(i , aValue.stream().map(this::toJSONElement).collect(Collectors.toList())) ;
		}
		else
			putAll(aValue) ;
		return this ;
	}
	
	/**
	 * 在JSONArray的指定位置插入一个对象。
	 * 
	 * 如果指定的索引小于0，则将其设置为0。
	 * 如果指定的索引小于JSONArray的大小，则在该索引处插入对象（转换为JSON元素后）。
	 * 如果指定的索引大于或等于JSONArray的大小，则调用put方法在JSONArray的末尾添加对象（注意：这里的put方法可能与insert方法的行为有所不同，特别是关于对象转换为JSON元素的处理）。
	 * 
	 * @param aIndex 插入的索引。
	 * @param aValue 要插入的对象。
	 * @return 返回当前JSONArray对象，便于链式调用。
	 */
	public JSONArray insert(int aIndex , Object aValue)
	{
		if(aIndex<0)
			aIndex = 0 ;
		if(aIndex < mEleList.size())
		{
			mEleList.add(aIndex, toJSONElement(aValue)) ;
		}
		else
			put(aIndex, aValue) ;
		return this ;
	}
	
	/**
	 * 如果遍历的过程中对数据进行了结构性的修改，那么修改之后应该立刻中断遍历，不要再继续遍历
	 * @param aStack
	 * @param aBiItPred
	 */
	public final void depthFirstVisitEntry(Stack<JSONEntry> aStack , BiIteratorPredicate<String, Object> aBiItPred)
	{
		depthFirstVisitEntry_0(aStack , aBiItPred) ;
	}
	
	/**
	 * 从指定对象复刻信息，原来的内容会被清除掉
	 * @param aJa
	 * @return
	 */
	public JSONArray duplicate(JSONArray aJa)
	{
		if(aJa != null)
		{
			if(aJa.isEmpty())
				clear() ;
			else
			{
				if(mEleList == null)
					mEleList = new ArrayList<>() ;
				else
					mEleList.clear();
				
				for(Object obj : aJa.mEleList)
				{
					if(obj instanceof JSONCloneable)
					{
						mEleList.add(((JSONCloneable)obj).clone()) ;
					}
					else
						mEleList.add(obj) ;
				}
			}
		}
		return this ;
	}
	
	public boolean isEmpty()
	{
		return mEleList.size() == 0 ;
	}
	
	public boolean isNotEmpty()
	{
		return !isEmpty() ;
	}
	
	protected boolean depthFirstVisitEntry_0(Stack<JSONEntry> aStack , BiIteratorPredicate<String, Object> aBiItPred)
	{
		final int len = mEleList.size() ;
		if(len>0)
		{
			JSONEntry entry = null ;
			if(aStack != null)
			{
				entry = new JSONEntry(this) ;
				aStack.push(entry) ;
			}
			try
			{
				for(int i=0 ; i<len ; i++)
				{
					Object ele_i = opt(i) ;
					if(ele_i != null && ele_i instanceof JSONObject)
					{
						if(entry != null)
							entry.setIndex(i) ;
						if(!((JSONObject)ele_i).depthFirstVisitEntry_0(aStack , aBiItPred))
							return false ;
					}
				}
			}
			finally
			{
				if(aStack != null)
					aStack.pop() ;
			}
		}
		return true ;
	}
	
	/**
	 * 前后颠倒
	 * @return
	 */
	public JSONArray reverse()
	{
		Collections.reverse(mEleList) ;
		return this ;
	}
	
	public static JSONArray newInstance(Object aInitValue , int aLen)
	{
		JSONArray ja = new JSONArray() ;
		int i = aLen ;
		while(i-->0)
			ja.put(aInitValue) ;
		
		return ja ;
	}
	
	@Override
	public boolean equals(Object aObj)
	{
		if(aObj == this)
			return true ;
		if(!(aObj instanceof JSONArray))
			return false ;
		JSONArray ja = (JSONArray)aObj ;
		if(ja.size() != size())
			return false ;
		final int len = size() ;
		for(int i=0 ; i<len ; i++)
		{
			if(JCommon.unequals(opt(i), ja.opt(i)))
				return false ;
		}
		return true ;
	}
	
	public <T> T find(Predicate<Object> aPred)
	{
		if(isEmpty())
			return null ;
		for(Object ele : mEleList)
		{
			if(aPred.test(ele))
				return (T) ele ;
		}
		return null ;
	}
	
	/**
	 * 构建一个新的JSONArray对象
	 * @return
	 */
	public static JSONArray one()
	{
		return new JSONArray() ;
	}
	
	/**
	 * 返回结果不为null
	 * @param aCollection
	 * @return
	 */
	public static JSONArray of(Iterable<?> aCollection)
	{
		if(aCollection != null && JSONArray.class.equals(aCollection.getClass()))
			return (JSONArray)aCollection ;
		JSONArray ja = new JSONArray() ;
		if(aCollection != null)
		{ 
			for(Object ele : aCollection)
			{
				ja.put(ele) ;
			}
		}
		return ja ;
	}
	
	/**
	 * 
	 * @param aArray
	 * @return			返回结果不为null
	 */
	public static JSONArray of(Object[] aArray)
	{
		JSONArray ja = new JSONArray() ;
		if(aArray != null)
		{ 
			final int length = aArray.length ;
			for (int i = 0; i < length; i += 1) {
				ja.put(aArray[i]) ;
			}
		}
		return ja ;
	}
}