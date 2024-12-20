package team.sailboat.commons.fan.json;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import team.sailboat.commons.fan.collection.IMultiMap;
import team.sailboat.commons.fan.collection.SizeIter;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.infc.BiIteratorPredicate;
import team.sailboat.commons.fan.infc.IterateOpCode;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.struct.Tuples;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.fan.time.XTime;

/**
 * JSONObject不允许键为null
 *
 * @author yyl
 * @since May 15, 2020
 */
public class JSONObject implements JSONString, JSONCloneable , Map<String, Object>
{
	private Map<String, Object> mMap;
	
	/**
	 * 构造一个空的JSONObject
	 */
	public JSONObject()
	{
		mMap = new LinkedHashMap<>();
	}

	/**
	 * Construct a JSONObject from a JSONTokener.
	 * @param x A JSONTokener object containing the source string.
	 * @throws JSONException If there is a syntax error in the source string
	 *  or a duplicated key.
	 */
	public JSONObject(JSONTokener x) throws JSONException
	{
		this();
		_init(x) ;
	}
	
	public void reset(JSONTokener aTokener)
	{
		if(mMap != null && !mMap.isEmpty())
			mMap.clear() ;
		_init(aTokener) ;
	}
	
	protected void _init(JSONTokener x)
	{
		if(x != null)
		{
			char c;
			String key;
	
			if (x.nextClean() != '{')
			{
				throw x.syntaxError("A JSONObject text must begin with '{'");
			}
			for (;;)
			{
				c = x.nextClean();
				switch (c)
				{
				case 0:
					throw x.syntaxError("A JSONObject text must end with '}'");
				case '}':
					return;
				default:
					x.back();
					key = x.nextValue().toString();
				}
	
				/*
				 * The key is followed by ':'. We will also tolerate '=' or '=>'.
				 */
	
				c = x.nextClean();
				if (c == '=')
				{
					if (x.next() != '>')
					{
						x.back();
					}
				}
				else if (c != ':')
				{
					throw x.syntaxError("Expected a ':' after a key");
				}
				putOnce(key, x.nextValue());
	
				/*
				 * Pairs are separated by ','. We will also tolerate ';'.
				 */
	
				switch (x.nextClean())
				{
				case ';':
				case ',':
					if (x.nextClean() == '}')
					{
						return;
					}
					x.back();
					break;
				case '}':
					return;
				default:
					throw x.syntaxError("Expected a ',' or '}'");
				}
			}
		}
	}

	
	
	public JSONObject(IMultiMap<String, ?> aMap)
	{
		initWithMultiMap(aMap) ;
	}
	
	/**
	 * 
	 * @param aMap
	 */
	public JSONObject(Map<String, ?> aMap)
	{
		mMap = new LinkedHashMap<>() ;
		if(aMap != null)
		{
			for(Entry<String , ?> entry : aMap.entrySet())
			{
				Object val = entry.getValue() ;
				if(val == null)
					continue ;
				put(entry.getKey() , val , false) ;
			}
		}
	}

	/**
	 * 
	 * @param <T>
	 * @param aMap
	 * @param aIncludeSuperClass			如果aMap的值是一个JavaBean时，起作用。是否需要考虑JavaBean的祖先类型的get方法
	 */
	public <T extends Object> JSONObject(Map<String, T> aMap, boolean aIncludeSuperClass)
	{
		mMap = new LinkedHashMap<>();
		if (aMap != null)
		{
			Iterator<Entry<String, T>> i = aMap.entrySet().iterator();
			while (i.hasNext())
			{
				Map.Entry<String, T> e =  i.next() ;
				if (isStandardProperty(e.getValue().getClass()))
				{
					mMap.put(e.getKey(), e.getValue());
				}
				else
				{
					mMap.put(e.getKey(),
							new JSONObject(e.getValue(),
									aIncludeSuperClass));
				}
			}
		}
	}
	
	void initWithMultiMap(IMultiMap<String, ?> aMap)
	{
		if(mMap == null)
			mMap = new LinkedHashMap<>() ;
		if(aMap != null)
		{
			String[] keys = aMap.keySet().toArray(JCommon.sEmptyStringArray) ;
			for(String key : keys)
			{
				SizeIter<?> it = aMap.get(key) ;
				if(it == null || it.size() == 0)
					continue ;
				if(it.size() == 1)
				{
					Object val = it.iterator().next() ;
					if(val == null)
						continue ;
					if(val instanceof ToJSONObject)
						mMap.put(key , ((ToJSONObject)val).toJSONObject()) ;
					else if(val.getClass().isArray())
						mMap.put(key , new JSONArray(val)) ;
					else if(val instanceof Collection<?>)
						mMap.put(key, JSONArray.of((Collection<?>)val)) ;
					else
						mMap.put(key , val) ;
				}
				else
				{
					mMap.put(key, new JSONArray(it.toArray(new Object[0]))) ;
				}
			}
		}
	}
	
	/**
	 * 
	 * @param aBean			非要求aBean必需是一个JavaBean，如果是Map，CharSequence，IMultiMap均可以。
	 * 				如果他是一个JavaBean，那么等价于 new JSONObject(aBean , true) ;
	 */
	public JSONObject(Object aBean)
	{
		this();
		if (aBean != null)
		{
			
			if (aBean instanceof Map)
			{
				for (Object entry : ((Map) aBean).entrySet())
				{
					if (((Entry) entry).getKey() != null)
						put(JCommon.toString(((Entry) entry).getKey()), ((Entry) entry).getValue());
				}
			}
			else if(aBean instanceof CharSequence)
			{
				if(((CharSequence)aBean).length()>0)
					_init(new JSONTokener(aBean.toString()));
			}
			else if(aBean instanceof IMultiMap)
			{
				initWithMultiMap((IMultiMap<String, ?>) aBean) ;
			}
			else
				populateInternalMap(aBean, true);
		}
	}

	/**
	 * 
	 * @param bean			只能是一个JavaBean
	 * @param includeSuperClass		当aBean是一个JavaBean时，起作用。是否应该包括父类中的方法。如果为true，则包括父类中的方法；
	 * 				如果为false，则仅包括当前类声明的方法。
	 */
	public JSONObject(Object aBean , boolean aIncludeSuperClass)
	{
		this();
		populateInternalMap(aBean, aIncludeSuperClass);
	}

	/**
	 * 该方法用于将给定JavaBean对象aBean的属性值填充到当前JSONObject实例的内部映射（mMap）中。
	 * 它支持通过反射机制自动识别和转换JavaBean的getter方法（包括以get和is开头的无参方法）对应的属性值。
	 * 此外，该方法还提供了对数组、集合、映射（包括JSONObject实例）以及标准Java类型（如基本类型、字符串及其包装类）的支持。
	 * 对于自定义对象，如果其类加载器不是系统类加载器，则递归地将其转换为JSONObject。
	 * 
	 * @param aBean		要从中提取属性并填充到当前JSONObject的JavaBean对象。
	 * @param includeSuperClass		当aBean是一个JavaBean时，起作用。是否应该包括父类中的方法。如果为true，则包括父类中的方法；
	 * 				如果为false，则仅包括当前类声明的方法。
	 */
	private void populateInternalMap(Object aBean, boolean includeSuperClass)
	{
		if(aBean == null)
			return ;
		if(aBean instanceof ToJSONObject)
		{
			((ToJSONObject)aBean).setTo(this) ;
			return ;
		}
		
		Class<?> klass = aBean.getClass();

		/* If klass.getSuperClass is System class then force includeSuperClass to false. */

		if (klass.getClassLoader() == null)
		{
			includeSuperClass = false;
		}

		Method[] methods = (includeSuperClass) ? klass.getMethods() : klass.getDeclaredMethods();
		for (int i = 0; i < methods.length; i += 1)
		{
			try
			{
				Method method = methods[i];
				if (Modifier.isPublic(method.getModifiers()))
				{
					String name = method.getName();
					String key = "";
					if (name.startsWith("get"))
					{
						key = name.substring(3);
					}
					else if (name.startsWith("is"))
					{
						key = name.substring(2);
					}
					if (key.length() > 0 &&
							Character.isUpperCase(key.charAt(0))
							&&
							method.getParameterTypes().length == 0)
					{
						if (key.length() == 1)
						{
							key = key.toLowerCase();
						}
						else if (!Character.isUpperCase(key.charAt(1)))
						{
							key = key.substring(0, 1).toLowerCase() +
									key.substring(1);
						}

						Object result = method.invoke(aBean, (Object[]) null);
						if (result == null)
						{
//							mMap.put(key, NULL);
							mMap.put(key, null) ;
						}
						else if (result.getClass().isArray())
						{
							mMap.put(key, new JSONArray(result, includeSuperClass));
						}
						else if (result instanceof Collection)
						{ // List or Set
							mMap.put(key, new JSONArray((Collection) result, includeSuperClass));
						}
						else if (result instanceof Map)
						{
							if(result instanceof JSONObject)
								mMap.put(key, result) ;
							else
								mMap.put(key, new JSONObject((Map) result, includeSuperClass));
						}
						else if (isStandardProperty(result.getClass()))
						{ // Primitives, String and Wrapper
							mMap.put(key, result);
						}
						else
						{
							if (result.getClass().getPackage().getName().startsWith("java") ||
									result.getClass().getClassLoader() == null)
							{
								mMap.put(key, result.toString());
							}
							else
							{ // User defined Objects
								mMap.put(key, new JSONObject(result, includeSuperClass));
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	static boolean isStandardProperty(Class<?> clazz)
	{
		return clazz.isPrimitive() ||
				clazz.isAssignableFrom(Byte.class)
				||
				clazz.isAssignableFrom(Short.class)
				||
				clazz.isAssignableFrom(Integer.class)
				||
				clazz.isAssignableFrom(Long.class)
				||
				clazz.isAssignableFrom(Float.class)
				||
				clazz.isAssignableFrom(Double.class)
				||
				clazz.isAssignableFrom(Character.class)
				||
				clazz.isAssignableFrom(String.class)
				||
				clazz.isAssignableFrom(Boolean.class);
	}

	/**  
     * 将JavaBean对象或者Map对象的指定属性转换为一个JSONObject对象。  
     *  
     * @param aBean 要转换的JavaBean对象或者Map对象。如果传入null，则返回一个空的JSONObject。  
     * @param aNames 包含要转换的属性名的字符串数组。如果传入空数组或null，则返回一个空的JSONObject。  
     * @return 包含指定属性及其值的JSONObject对象。如果aBean为null或aNames为空，则返回一个空的JSONObject。  
     * @throws JSONException 如果在获取属性值的过程中发生异常，则包装该异常并抛出JSONException。  
     */  
	public JSONObject of(Object aBean , String[] aNames)
	{
		JSONObject jo = new JSONObject() ;
		if(aBean == null || XC.isEmpty(aNames))
			return jo ;
		
		if(aBean instanceof Map map)
		{	
			for(String name : aNames)
			{
				jo.put(name, map.get(name)) ;
			}
		}
		else
		{
			Class<?> c = aBean.getClass();
			for (int i = 0; i < aNames.length; i += 1)
			{
				String name = aNames[i];
				try
				{
					putOpt(name, c.getField(name).get(aBean));
				}
				catch (Exception e)
				{
					try
					{
						putOpt(name , c.getMethod(XClassUtil.getGetterMethodName(name))
									.invoke(aBean));
					}
					catch (Exception e1)
					{
						throw new JSONException(e1);
					}
				}
			}
		}
		return jo ;
	}
	
	/**
	 * 使用 JSONObject.of
	 * @param aSource
	 */
	@Deprecated
	public JSONObject(String aSource)
	{
		this() ;
		try
		{
			_init(XString.isNotEmpty(aSource)?new JSONTokener(aSource):null);
		}
		catch(JSONException e)
		{
			e.setSourceStr(aSource) ;
			throw e ;
		}
	}

	/**
	 * Accumulate values under a key. It is similar to the put method except
	 * that if there is already an object stored under the key then a
	 * JSONArray is stored under the key to hold all of the accumulated values.
	 * If there is already a JSONArray, then the new value is appended to it.
	 * In contrast, the put method replaces the previous value.
	 * @param key   A key string.
	 * @param value An object to be accumulated under the key.
	 * @return this.
	 * @throws JSONException If the value is an invalid number
	 *  or if the key is null.
	 */
	public JSONObject accumulate(String key, Object value)
			throws JSONException
	{
		testValidity(value , "健是{}" , key);
		Object o = opt(key);
		if (o == null)
		{
			put(key, value instanceof JSONArray ? new JSONArray().put(value) : value);
		}
		else if (o instanceof JSONArray)
		{
			((JSONArray) o).put(value);
		}
		else
		{
			put(key, new JSONArray().put(o).put(value));
		}
		return this;
	}

	/**
	 * Append values to the array under a key. If the key does not exist in the
	 * JSONObject, then the key is put in the JSONObject with its value being a
	 * JSONArray containing the value parameter. If the key was already
	 * associated with a JSONArray, then the value parameter is appended to it.
	 * @param key   A key string.
	 * @param value An object to be accumulated under the key.
	 * @return this.
	 * @throws JSONException If the key is null or if the current value
	 *  associated with the key is not a JSONArray.
	 */
	public JSONObject append(String key, Object value)
			throws JSONException
	{
		testValidity(value , "健是{}" , key) ;
		Object o = opt(key);
		if (o == null)
		{
			put(key, new JSONArray().put(value));
		}
		else if (o instanceof JSONArray)
		{
			put(key, ((JSONArray) o).put(value));
		}
		else
		{
			throw new JSONException("JSONObject[" + key
					+
					"] is not a JSONArray.");
		}
		return this;
	}

	/**
	 * Produce a string from a double. The string "null" will be returned if
	 * the number is not finite.
	 * @param  d A double.
	 * @return A String.
	 */
	static public String doubleToString(double d)
	{
		if (Double.isInfinite(d) || Double.isNaN(d))
		{
			return "null";
		}

		// Shave off trailing zeros and decimal point, if possible.

		String s = Double.toString(d);
		if (s.indexOf('.') > 0 && s.indexOf('e') < 0 && s.indexOf('E') < 0)
		{
			while (s.endsWith("0"))
			{
				s = s.substring(0, s.length() - 1);
			}
			if (s.endsWith("."))
			{
				s = s.substring(0, s.length() - 1);
			}
		}
		return s;
	}
	
	/**
	 * 等价与opt，如果需要抛出异常，需要调用getOrThrow
	 */
	@Override
	public Object get(Object key)
	{
		return opt(key);
	}

	/**  
     * 从JSONObject中获取指定键的值。如果键不存在，则抛出JSONException。  
     *  
     * @param aKey 要获取的键。  
     * @return 与指定键关联的值。  
     * @throws JSONException 如果指定的键不存在。  
     */  
	public Object getOrThrow(Object aKey) throws JSONException
	{
		Object o = opt(aKey);
        if (o == null) 
            throw new JSONException("JSONObject[" + quote(XClassUtil.toString(aKey)) + "]没有找到！");
        
		return o;
	}

	/**  
     * 从JSONObject中获取指定键的Boolean值。  
     *  
     * @param aKey 要获取的键。  
     * @return 与指定键关联的Boolean值。  
     * @throws JSONException 如果指定的键不存在或值不能转换为Boolean类型。  
     */ 
	public boolean getBoolean(String aKey) throws JSONException
	{
		Boolean b = XClassUtil.toBoolean( getOrThrow(aKey)) ;
		if(b != null)
			return b.booleanValue() ;
		throw new JSONException("JSONObject[" + quote(aKey) + "]不能转成Boolean类型！");
	}

	/**  
     * 从JSONObject中获取指定键的Double值。  
     *  
     * @param aKey 要获取的键。  
     * @return 与指定键关联的Double值。  
     * @throws JSONException 如果指定的键不存在或值不能转换为Double类型。  
     */ 
	public double getDouble(String aKey) throws JSONException
	{
		Double d = XClassUtil.toDouble(getOrThrow(aKey)) ;
		if(d != null)
			return d.doubleValue() ;
		throw new JSONException("JSONObject[" + quote(aKey) + "]不能转成Double类型！");
	}

	/**  
     * 从JSONObject中获取指定键的Integer值。  
     *  
     * @param aKey 要获取的键。  
     * @return 与指定键关联的Integer值。  
     * @throws JSONException 如果指定的键不存在或值不能转换为Integer类型。  
     */  
	public int getInt(String aKey) throws JSONException
	{
		Integer i = XClassUtil.toInteger(getOrThrow(aKey)) ;
		if(i != null)
			return i.intValue() ;
		throw new JSONException("JSONObject[" + quote(aKey) + "]不能转成Integer类型！");
	}

	/**  
     * 从JSONObject中获取指定键的JSONArray值。  
     *  
     * @param aKey 要获取的键。  
     * @return 与指定键关联的JSONArray对象。  
     * @throws JSONException 如果指定的键不存在或值不能转换为JSONArray类型。  
     */  
	public JSONArray getJSONArray(String aKey) throws JSONException
	{
		Object o = getOrThrow(aKey);
		if (o instanceof Iterable it)
			return JSONArray.of(it);
		throw new JSONException("JSONObject[" + quote(aKey) + "]不能转成JSONArray类型！");
	}

	/**  
     * 从JSONObject中获取指定键的JSONObject值。  
     *  
     * @param aKey 要获取的键。  
     * @return 与指定键关联的JSONObject对象。  
     * @throws JSONException 如果指定的键不存在或值不能转换为JSONObject类型。  
     */  
	public JSONObject getJSONObject(String aKey) throws JSONException
	{
		Object o = getOrThrow(aKey);
		if(o instanceof Map map)
			return JSONObject.of(map) ;
		else if(o instanceof String str)
			return JSONObject.of(str) ;
		throw new JSONException("JSONObject[" + quote(aKey) + "]不能转成JSONObject类型！");
	}
	
	/**  
     * 从JSONObject中获取指定键的Long值。  
     *  
     * @param aKey 要获取的键。  
     * @return 与指定键关联的Long值。  
     * @throws JSONException 如果指定的键不存在或值不能转换为Long类型。  
     */ 
	public long getLong(String aKey) throws JSONException
	{
		Long l = XClassUtil.toLong(getOrThrow(aKey)) ;
		if(l != null)
			return l.longValue() ;
		throw new JSONException("JSONObject[" + quote(aKey) + "]不能转成Long类型！");
	}

	/**
	 * Get an array of field names from a JSONObject.
	 *
	 * @return An array of field names, or null if there are no names.
	 */
	public static String[] getNames(JSONObject jo)
	{
		int length = jo.size() ;
		if (length == 0)
		{
			return null;
		}
		Iterator<?> i = jo.keys();
		String[] names = new String[length];
		int j = 0;
		while (i.hasNext())
		{
			names[j] = (String) i.next();
			j += 1;
		}
		return names;
	}

	/**
	 * Get an array of field names from an Object.
	 *
	 * @return An array of field names, or null if there are no names.
	 */
	public static String[] getNames(Object object)
	{
		if (object == null)
		{
			return null;
		}
		Class<?> klass = object.getClass();
		Field[] fields = klass.getFields();
		int length = fields.length;
		if (length == 0)
		{
			return null;
		}
		String[] names = new String[length];
		for (int i = 0; i < length; i += 1)
		{
			names[i] = fields[i].getName();
		}
		return names;
	}

	/**
	 * Get the string associated with a key.
	 *
	 * @param key   A key string.
	 * @return      A string which is the value.
	 * @throws   JSONException if the key is not found.
	 */
	public String getString(String key) throws JSONException
	{
		//===================================================================
		//changed by yyl
		Object obj = getOrThrow(key);
		return obj != null ? obj.toString() : null;
		//===================================================================
	}

	/**
	 * Determine if the JSONObject contains a specific key.
	 * @param key   A key string.
	 * @return      true if the key exists in the JSONObject.
	 */
	public boolean has(String key)
	{
		return this.mMap.containsKey(key);
	}

	public boolean isEmpty()
	{
		return mMap.isEmpty();
	}

	/**
	 * Determine if the value associated with the key is null or if there is
	 *  no value.
	 * @param key   A key string.
	 * @return      true if there is no value associated with the key or if
	 *  the value is the JSONObject.NULL object.
	 */
	public boolean isNull(String key)
	{
		return null == opt(key);
	}

	/**
	 * Get an enumeration of the keys of the JSONObject.
	 *
	 * @return An iterator of the keys.
	 */
	public Iterator<String> keys()
	{
		return this.mMap.keySet().iterator();
	}
	
	public Set<String> keySet()
	{
		return this.mMap.keySet() ;
	}

	public String[] keyArray()
	{
		return mMap.keySet().toArray(JCommon.sEmptyStringArray);
	}

	/**
	 * Produce a JSONArray containing the names of the elements of this
	 * JSONObject.
	 * @return A JSONArray containing the key strings, or null if the JSONObject
	 * is empty.
	 */
	public JSONArray names()
	{
		JSONArray ja = new JSONArray();
		Iterator<String> keys = keys();
		while (keys.hasNext())
		{
			ja.put(keys.next());
		}
		return ja.size() == 0 ? null : ja;
	}
	
	@Override
	public Collection<Object> values()
	{
		return mMap.values() ;
	}
	
	public JSONArray valuesAsJa()
	{
		return JSONArray.of(mMap.values()) ;
	}

	/**
	 * Produce a string from a Number.
	 * @param  n A Number
	 * @return A String.
	 * @throws JSONException If n is a non-finite number.
	 */
	static public String numberToString(Number n)
			throws JSONException
	{
		if (n == null)
		{
			throw new JSONException("Null pointer");
		}
		testValidity(n);

		// Shave off trailing zeros and decimal point, if possible.

		String s = n.toString();
		if (s.indexOf('.') > 0 && s.indexOf('e') < 0 && s.indexOf('E') < 0)
		{
			while (s.endsWith("0"))
			{
				s = s.substring(0, s.length() - 1);
			}
			if (s.endsWith("."))
			{
				s = s.substring(0, s.length() - 1);
			}
		}
		return s;
	}

	/**
	 * Get an optional value associated with a key.
	 * @param key   A key string.
	 * @return      An object which is the value, or null if there is no value.
	 */
	public Object opt(Object aKey)
	{
		if(aKey == null)
			return null ;
		Object val = this.mMap.get(aKey) ;
//		return val == NULL?null:val ;
		return val ;
	}
	
	public Object opt(String key , Object aDefault)
	{
		return key == null ? aDefault : this.mMap.get(key);
	}
	
	public Entry<String , Object> optAny(String...aKeys)
	{
		if(XC.isEmpty(aKeys))
			return null ;
		Object val = null ;
		for(String key : aKeys)
		{
			val = opt(key) ;
			if(val != null)
				return Tuples.of(key, val) ;
		}
		return null ;
	}

	public void forEach(BiConsumer<? super String, ? super Object> aConsumer)
	{
//		mMap.forEach((key , val)->aConsumer.accept(key, val==NULL?null:val)) ;
		mMap.forEach((key , val)->aConsumer.accept(key, val)) ;
	}
	
	public Map.Entry<String , Object> getFirstEntry()
	{
		return XC.getFirst(mMap.entrySet()) ;
	}

	/**
	 * Get an optional boolean associated with a key.
	 * It returns false if there is no such key, or if the value is not
	 * Boolean.TRUE or the String "true".
	 *
	 * @param key   A key string.
	 * @return      The truth.
	 */
	public Boolean optBoolean(String key)
	{
		Object obj = opt(key);
		return null == obj?null:XClassUtil.toBoolean(obj) ;
	}
	
	public void forEachBool(BiConsumer<String, Boolean> aConsumer)
	{
//		mMap.forEach((key , val)->aConsumer.accept(key, val==NULL?null:XClassUtil.toBoolean(val))) ;
		mMap.forEach((key , val)->aConsumer.accept(key, XClassUtil.toBoolean(val))) ;
	}
	
	public void forEachJSONObject(BiConsumer<String, JSONObject> aConsumer)
	{
//		mMap.forEach((key , val)->aConsumer.accept(key, val==NULL || !(val instanceof JSONObject)
//				?null:(JSONObject)val)) ;
		mMap.forEach((key , val)->aConsumer.accept(key, val==null || !(val instanceof JSONObject)
				?null:(JSONObject)val)) ;
	}

	/**
	 * Get an optional boolean associated with a key.
	 * It returns the defaultValue if there is no such key, or if it is not
	 * a Boolean or the String "true" or "false" (case insensitive).
	 *
	 * @param key              A key string.
	 * @param defaultValue     The default.
	 * @return      The truth.
	 */
	public boolean optBoolean(String key, boolean defaultValue)
	{
		Object obj = opt(key);
		return obj == null?defaultValue:XClassUtil.toBoolean(obj , defaultValue) ;
	}

	/**  
	 * 将指定的键值对添加到JSONObject中，如果值为null，则移除对应的键。  
	 *  
	 * <p>此方法允许您将一个键和一个值（作为集合）添加到JSONObject中。如果提供的值为null，  
	 * 则此方法会从JSONObject中移除与键相关联的条目。如果值非null，则将其转换为一个JSONArray  
	 * 并与键一起存储。这允许将多个值关联到同一个键，尽管它们被存储为JSON数组。  
	 *  
	 * @param aKey 要添加或移除的键。  
	 * @param aValue 要与键关联的值，作为集合。如果为null，则移除键。  
	 * @return 修改后的JSONObject实例，以支持链式调用。  
	 * @throws JSONException 如果在操作过程中发生JSON错误。  
	 */
	public JSONObject put(String aKey, Collection<?> aValue) throws JSONException
	{
		if(aValue == null)
			mMap.remove(aKey) ;
		else
		{
			mMap.put(aKey, JSONArray.of(aValue)) ;
		}
		return this;
	}

	/**  
     * 根据键名获取对应的Double值，如果键名不存在或者对应的值无法转换为Double，则返回null。  
     *  
     * @param aKey 键名  
     * @return 对应的Double值，如果键名不存在或者对应的值无法转换为Double，则返回null  
     */  
	public Double optDouble(String aKey)
	{
		Object obj = opt(aKey);
		return obj == null?null:XClassUtil.toDouble(obj) ;
	}

	/**  
     * 根据键名获取对应的Double值，如果键名不存在或者对应的值无法转换为Double，则返回默认值。  
     *  
     * @param aKey 键名  
     * @param aDefaultValue 默认值  
     * @return 对应的Double值，如果键名不存在或者对应的值无法转换为Double，则返回默认值  
     */  
	public double optDouble(String aKey , double aDefaultValue)
	{
		try
		{
			Object o = opt(aKey);
			return o instanceof Number ? ((Number) o).doubleValue() : Double.valueOf((String) o).doubleValue();
		}
		catch (Exception e)
		{
			return aDefaultValue;
		}
	}

	/**  
	 * 从映射中获取与指定键相关联的double值。  
	 * 如果映射中不存在该键，并且aPutDefaultIfAbsent为true，则将该键和默认值放入映射中。  
	 *  
	 * @param aKey 键，用于从映射中检索值。  
	 * @param aDefaultValue 如果键不存在且aPutDefaultIfAbsent为true，则将此值放入映射中，并返回此值。  
	 * @param aPutDefaultIfAbsent 如果键不存在，是否将默认值放入映射中。  
	 * @return 映射中与键相关联的double值；如果键不存在且aPutDefaultIfAbsent为true，则返回默认值。  
	 *         如果键存在但其值无法转换为double，则尝试返回aDefaultValue。  
	 */ 
	public double optDouble(String aKey, double aDefaultValue, boolean aPutDefaultIfAbsent)
	{
		Object o = mMap.get(aKey);
		if (o == null)
		{
			if (aPutDefaultIfAbsent)
				this.mMap.put(aKey, Double.valueOf(aDefaultValue));
			return aDefaultValue;
		}
		return XClassUtil.toDouble(o , aDefaultValue) ;
	}
	
	public char optChar(String aKey , char aDefault)
	{
		Object obj = opt(aKey);
		return obj == null?aDefault:XClassUtil.toChar(obj, aDefault) ;
	}
	
	public Character optCharacter(String aKey)
	{
		Object obj = opt(aKey);
		return obj == null?null:XClassUtil.toCharacter(obj) ;
	}

	public Integer optInteger(String key)
	{
		Object obj = opt(key);
		return obj == null?null:XClassUtil.toInteger(obj) ;
	}
	
	/**  
	 * 从JSON对象中检索与指定键相关联的整数值。  
	 * 如果找到了该键且其值可以转换为整数，则返回该整数值；如果未找到该键或其值无法转换为整数，  
	 * 则返回提供的默认值。  
	 *  
	 * @param aKey         用于检索整数值的键。  
	 * @param aDefaultValue 如果未找到键或其值无法转换为整数，则返回此默认值。  
	 * @return 与指定键相关联的整数值（如果找到且可以转换），否则返回提供的默认值。  
	 */  
	public int optInt(String aKey, int aDefaultValue)
	{
		Integer val = optInteger(aKey) ;
		return val==null?aDefaultValue:val.intValue() ;
	}
	/**  
	 * 根据提供的键从映射中检索整数值。  
	 *  
	 * @param key 要检索的键。  
	 * @param defaultValue 如果键不存在或对应的值为null时返回的默认值。  
	 * @param aPutDefaultIfAbsent 如果键不存在且此参数为true，则将默认值与键一起放入映射中。  
	 * @return 如果键存在且对应的值不为null，则返回该整数值；否则，如果键不存在或对应的值为null且aPutDefaultIfAbsent为true，则将默认值放入映射中并返回该默认值；如果键不存在且aPutDefaultIfAbsent为false，则直接返回默认值。  
	 */ 
	public int optInt(String key, int defaultValue , boolean aPutDefaultIfAbsent)
	{
		Integer val = optInteger(key) ;
		if(val == null)
		{
			if(aPutDefaultIfAbsent && key != null)
			{
				this.mMap.put(key, defaultValue) ;
			}
			return defaultValue ;
		}
		return val.intValue() ;
	}

	/**  
	 * 尝试从当前对象中获取与指定键关联的JSONArray对象。  
	 * 如果未找到或关联的值不是JSONArray类型，则返回null。  
	 *  
	 * @param aKey 与要获取的JSONArray对象关联的键。  
	 * @return 与指定键关联的JSONArray对象，如果未找到或值，则返回null。  
	 */
	public JSONArray optJSONArray(String aKey)
	{
		return optJSONArray(aKey, JOptions.sDefault) ;
	}
	
	/**  
	 * 尝试从当前对象中获取与指定键关联的JSONArray对象。  
	 * 如果未找到或关联的值不是JSONArray类型，则根据提供的JOptions对象返回null或创建一个新的JSONArray。  
	 *  
	 * @param aKey 与要获取的JSONArray对象关联的键。  
	 * @param aOption 指定在找不到JSONArray或值类型不匹配时的行为选项。  
	 * @return 与指定键关联的JSONArray对象，如果未找到或值不是JSONArray类型且选项为默认，则返回null；  
	 *         根据选项，可能会返回一个新的JSONArray实例。  
	 */  
	public JSONArray optJSONArray(String aKey , JOptions aOption)
	{
		return optJSONArray(aKey, aOption, null, 0) ;
	}
	
	/**  
	 * 尝试从当前对象中获取与指定键关联的JSONArray对象。  
	 * 如果未找到或关联的值不是JSONArray类型，则根据提供的选项、初始值和长度创建一个新的JSONArray，并可能将其注入到当前对象中。  
	 *  
	 * @param aKey 与要获取的JSONArray对象关联的键。  
	 * @param aOption 指定在找不到JSONArray或值类型不匹配时的行为选项。  
	 * @param aInitValue 用于初始化新JSONArray对象的值（如果选项要求创建新实例）。  
	 * @param aLen 新JSONArray对象的初始长度（如果选项要求创建新实例）。  
	 * @return 与指定键关联的JSONArray对象，如果已存在且为JSONArray类型；  
	 *         否则，根据选项返回一个新的JSONArray实例，如果选项要求，还会将其注入到当前对象中。  
	 * @throws IllegalArgumentException 如果提供了不接受的选项。  
	 */  
	public JSONArray optJSONArray(String aKey , JOptions aOption , Object aInitValue , int aLen)
	{
		Object o = opt(aKey);
		if(o instanceof JSONArray)
			return  (JSONArray) o ;
		if(aOption == null)
			aOption = JOptions.sDefault ;
		switch(aOption)
		{
		case sDefault:
			return null ;
		case sNewIfNotExists:
			return JSONArray.newInstance(aInitValue, aLen) ;
		case sNewAndInjectIfNotExists:
		{
			JSONArray ja = JSONArray.newInstance(aInitValue, aLen) ;
			put(aKey, ja) ;
			return ja ;
		}
		default:
			throw new IllegalArgumentException("不接受的选项："+aOption) ;
		}
	}
	
	public JSONArray getJSONArray(String aKey , boolean aCreateOneIfNull)
	{
		Object o = opt(aKey);
		if(o == null)
		{
			JSONArray ja = new JSONArray() ;
			put(aKey, ja) ;
			return ja ;
		}
		if(o instanceof JSONArray)
			return (JSONArray) o ;
		throw new JSONException("JSONObject[" + quote(aKey)
				+ "] is not a JSONArray.");
	}

	/**
	 * Get an optional JSONObject associated with a key.
	 * It returns null if there is no such key, or if its value is not a
	 * JSONObject.
	 *
	 * @param key   A key string.
	 * @return      A JSONObject which is the value.
	 */
	public JSONObject optJSONObject(String aKey)
	{
		return optJSONObject(aKey, null) ;
	}
	
	public JSONObject optJSONObject(String aKey , JOptions aOption)
	{
		if(aOption == null)
			aOption = JOptions.sDefault ;
		Object o = opt(aKey) ;
		if(o == null && (aOption == JOptions.sNewAndInjectIfNotExists
				|| aOption == JOptions.sNewIfNotExists))
		{
			o = new JSONObject() ;
			if(aOption == JOptions.sNewAndInjectIfNotExists)
				put(aKey, o) ;
			return (JSONObject)o ;
		}
		if(o instanceof JSONObject)
			return (JSONObject)o ;
		else if(aOption == JOptions.sNewAndInjectIfNotExists 
				|| aOption == JOptions.sNewIfNotExists)
			throw new IllegalStateException("当前JSONObject对象中存在键，它的值类型是"+o.getClass().getName()) ;
		else
			return null ;
	}

	public JSONObject pathJSONObject(String... aPathSegs)
	{
		JSONObject obj = getTail(aPathSegs) ;
		return obj == null?null:obj.optJSONObject(XC.getLast(aPathSegs)) ;
	}
	
	public JSONObject pathJSONObject(JOptions aOption , String... aPathSegs)
	{
		if(aOption == null || aOption == JOptions.sDefault)
			return pathJSONObject(aPathSegs) ;
		if(aOption == JOptions.sNewIfNotExists)
		{
			JSONObject jo = this ;
			for(String pathSeg : aPathSegs)
			{
				jo = jo.optJSONObject(pathSeg) ;
				if(jo == null)
					return new JSONObject() ;
			}
			return jo ;
		}
		else if(aOption == JOptions.sNewAndInjectIfNotExists)
		{
			JSONObject jo = this ;
			for(String pathSeg : aPathSegs)
			{
				JSONObject jo_0 = jo.optJSONObject(pathSeg) ;
				if(jo_0 == null)
				{
					jo_0 = new JSONObject() ;
					jo.put(pathSeg, jo_0) ;
				}
				jo = jo_0 ;
			}
			return jo ;
		}
		else
			throw new IllegalArgumentException("为支持的选项："+aOption.name()) ;
	}
	
	public JSONArray pathJSONArray(String... aPathSegs)
	{
		JSONObject obj = getTail(aPathSegs) ;
		return obj==null?null:obj.optJSONArray(aPathSegs[aPathSegs.length-1]) ;
	}
	
	public JSONArray pathJSONArray(JOptions aOption , String... aPathSegs)
	{
		JSONObject obj = getTail(aPathSegs) ;
		String lastKey = aPathSegs[aPathSegs.length-1] ;
		JSONArray ja = obj==null?null:obj.optJSONArray(lastKey) ;
		if(ja != null)
			return ja ;
		if(aOption == null)
			aOption = JOptions.sDefault ;
		switch(aOption)
		{
		case sDefault:
			return null ;
		case sNewIfNotExists:
			return new JSONArray() ;
		case sNewAndInjectIfNotExists:
		{
			ja = new JSONArray() ;
			if(obj == null)
			{
				obj = this;
				for (int i = 0; i < aPathSegs.length-1; i++)
				{
					obj = obj.optJSONObject(aPathSegs[i] , aOption);
				}
			}
			obj.put(lastKey, ja) ;
			return ja ;
		}
		default:
			throw new IllegalArgumentException("不接受的选项："+aOption) ;
		}
	}
	
	public Integer pathInteger(String...aPathSegs)
	{
		JSONObject obj = getTail(aPathSegs) ;
		return obj == null?null:obj.optInteger(XC.getLast(aPathSegs)) ;
	}
	
	public Integer pathInt(int aDefaultVal , String...aPathSegs)
	{
		JSONObject obj = getTail(aPathSegs) ;
		return obj == null?null:obj.optInt(XC.getLast(aPathSegs) , aDefaultVal) ;
	}
	
	protected JSONObject getTail(String...aPathSegs)
	{
		if (XC.isEmpty(aPathSegs))
			return null;
		JSONObject obj = this;
		for (int i = 0; i < aPathSegs.length-1; i++)
		{
			obj = obj.optJSONObject(aPathSegs[i]);
			if (obj == null)
				return null;
		}
		return obj ;
	}

	/**
	 * Get an optional long value associated with a key,
	 * or zero if there is no such key or if the value is not a number.
	 * If the value is a string, an attempt will be made to evaluate it as
	 * a number.
	 *
	 * @param key   A key string.
	 * @return      An object which is the value.
	 */
	public long optLong(String key)
	{
		return optLong(key, 0);
	}

	/**
	 * Get an optional long value associated with a key,
	 * or the default if there is no such key or if the value is not a number.
	 * If the value is a string, an attempt will be made to evaluate it as
	 * a number.
	 *
	 * @param key   A key string.
	 * @param defaultValue     The default.
	 * @return      An object which is the value.
	 */
	public long optLong(String key, long defaultValue)
	{
		Object obj = opt(key);
		return obj == null?defaultValue:XClassUtil.toLong(obj, defaultValue) ;
	}
	
	public Long optLong_0(String key)
	{
		Object obj = opt(key);
		return null == obj?null:XClassUtil.toLong(obj) ;
	}

	/**
	 * Get an optional string associated with a key.
	 * It returns an empty string if there is no such key. If the value is not
	 * a string and is not null, then it is coverted to a string.
	 *
	 * @param key   A key string.
	 * @return      A string which is the value.
	 */
	public String optString(String key)
	{
		return optString(key, "");
	}
	
	public String[] optStringArray(String key)
	{
		Object val = opt(key) ;
		if(val == null)
			return null ;
		if(val instanceof JSONArray)
		{
			return ((JSONArray)val).toStringArray() ;
		}
		return toString(val, "").split(",") ;
 	}
	
	public Object opt__source(String aKey)
	{
		Object obj = opt(aKey) ;
		return obj instanceof Polytope?((Polytope)obj).getSource():obj ;
	}
	
	public Object opt__facade(String aKey)
	{
		Object obj = opt(aKey) ;
		return obj instanceof Polytope?((Polytope)obj).getFacade():obj ;
	}
		
	public String pathString(String aDefaultVal , String...aPathSegs)
	{
		JSONObject jobj = getTail(aPathSegs) ;
		return jobj == null?aDefaultVal:jobj.optString(XC.getLast(aPathSegs)) ;
	}

	/**
	 * Get an optional string associated with a key.
	 * It returns the defaultValue if there is no such key.
	 *
	 * @param key   A key string.
	 * @param defaultValue     The default.
	 * @return      A string which is the value.
	 */
	public String optString(String key, String defaultValue)
	{
		return toString(opt(key) , defaultValue);
	}
	
	String toString(Object aVal , String defaultValue)
	{
		return aVal == null /**|| aVal == NULL**/ ?defaultValue:
			(aVal instanceof Double?new BigDecimal((Double)aVal).toString():aVal.toString()) ;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> T optEnum(String aKey , T aDefaultValue)
	{
		Assert.notNull(aDefaultValue) ;
		String v = optString(aKey, null) ;
		if(XString.isNotEmpty(v))
			return (T)Enum.valueOf(aDefaultValue.getClass(), v) ;
		return aDefaultValue ;
	}
	
	public <T extends Enum<T>> T optEnum(String aKey , Class<T> aEnumClass)
	{
		String v = optString(aKey, null) ;
		if(XString.isNotEmpty(v))
			return (T) Enum.valueOf(aEnumClass , v) ;
		return null ;
	}
	
	/**
	 * 根据键和枚举类型从当前对象中获取对应的枚举值。
	 * 
	 * @param <T> 枚举类型，必须是枚举的子类。
	 * @param aKey 要获取的键。
	 * @param aEnumClass 枚举的Class对象。
	 * @return 返回与键对应的枚举值。
	 * @throws IllegalArgumentException 如果键不存在或对应的值不是指定的枚举类型。
	 */
	public <T extends Enum<T>> T getEnum(String aKey , Class<T> aEnumClass)
	{
		T v = optEnum(aKey, aEnumClass) ;
		Assert.notNull(v, "不存在键：%s", aKey) ;
		return v ;
	}
	
	/**
	 * 根据键从当前对象中获取对应的字符串值，并去除字符串两端的空白字符。
	 * 
	 * @param aKey 要获取的键。
	 * @param aDefaultValue 如果键不存在或对应的值为null，则返回此默认值。
	 * @return 返回与键对应的字符串值（已去除两端空白字符），如果键不存在或对应的值为null，则返回默认值。
	 */
	public String optStringAndTrim(String aKey, String aDefaultValue)
	{
		Object obj = opt(aKey);
		return obj == null ? aDefaultValue : XString.trim(obj.toString()) ;
	}

	/**
	 * 将指定的布尔值放入当前对象中，与指定的键关联。
	 * 
	 * @param aKey 要关联的键。
	 * @param aValue 要放入的布尔值。
	 * @return 返回当前对象，便于链式调用。
	 * @throws JSONException 如果在放入值时发生JSON异常。
	 */
	public JSONObject put(String aKey, boolean aValue) throws JSONException
	{
		put(aKey, aValue ? Boolean.TRUE : Boolean.FALSE);
		return this;
	}

	/**
	 * 将指定的双精度浮点值放入当前对象中，与指定的键关联。
	 * 
	 * @param aKey 要关联的键。
	 * @param aValue 要放入的双精度浮点值。
	 * @return 返回当前对象，便于链式调用。
	 * @throws JSONException 如果在放入值时发生JSON异常。
	 */
	public JSONObject put(String aKey, double aValue) throws JSONException
	{
		put(aKey, Double.valueOf(aValue));
		return this;
	}

	/**
	 * 将指定的整数值放入当前对象中，与指定的键关联。
	 * 
	 * @param aKey 要关联的键。
	 * @param aValue 要放入的整数值。
	 * @return 返回当前对象，便于链式调用。
	 * @throws JSONException 如果在放入值时发生JSON异常。
	 */
	public JSONObject put(String aKey, int aValue) throws JSONException
	{
		put(aKey, Integer.valueOf(aValue));
		return this;
	}

	/**
	 * 将指定的长整数值放入当前对象中，与指定的键关联。
	 * 
	 * @param aKey 要关联的键。
	 * @param aValue 要放入的长整数值。
	 * @return 返回当前对象，便于链式调用。
	 * @throws JSONException 如果在放入值时发生JSON异常。
	 */
	public JSONObject put(String aKey, long aValue) throws JSONException
	{
		put(aKey, Long.valueOf(aValue));
		return this;
	}

	/**
	 * 将指定的键值对放入JSONObject中。
	 * 
	 * @param key   要放入的键，不能为null。
	 * @param value 要放入的值，如果为null，则从JSONObject中移除该键；
	 *              如果为JSONObject类型，则直接放入；
	 *              否则，将Map转换为JSONObject后放入。
	 * @return 返回当前JSONObject对象，便于链式调用。
	 * @throws JSONException 如果在将Map转换为JSONObject时发生错误。
	 */
	public JSONObject put(String key, Map<String , Object> value) throws JSONException
	{
		if(value == null)
		{
			mMap.remove(key) ;
		}
		else
		{
			if(value instanceof JSONObject)
				mMap.put(key, value) ;
			else
				mMap.put(key, new JSONObject(value));
		}
		return this;
	}
	
	/**
	 * 
	 * @param aKey
	 * @param aValue
	 * @param aStoreNullValue	指示是否存储null值。如果为true，则允许存储null值；
	 * 					如果为false，则不存储null值，并尝试从JSONObject中移除该键（如果已存在）。
	 * 
	 * @return
	 * @throws JSONException
	 */
	public JSONObject put(String aKey, Object aValue , boolean aStoreNullValue) throws JSONException
	{
		if (aKey == null)
			throw new JSONException("键为null！");
		
		if (aValue != null)
		{
			testValidity(aValue , "健是{}" , aKey);
			mMap.put(aKey , toJSONElement(aValue)) ;
		}
		else if(aStoreNullValue)
			mMap.put(aKey, null) ;
		
		else
		{
			remove(aKey);
		}
		return this;
	}

	/**
	 * Put a key/value pair in the JSONObject. If the value is null,
	 * then the key will be removed from the JSONObject if it is present.
	 * @param key   A key string.
	 * @param value An object which is the value. It should be of one of these
	 *  types: Boolean, Double, Integer, JSONArray, JSONObject, Long, String,
	 *  or the JSONObject.NULL object.
	 * @return this.
	 * @throws JSONException If the value is non-finite number
	 *  or if the key is null.
	 */
	public JSONObject put(String key, Object value) throws JSONException
	{
		return put(key, value, false) ;
	}

	/**
	 * 此方法尝试将一个键值对放入JSONObject中，但仅当指定的键key尚不存在时。
	 * 如果键已经存在，则抛出一个JSONException异常，表明尝试添加一个重复的键。
	 * 如果键和值均非空，并且键不存在于JSONObject中，则使用put方法将键值对添加到JSONObject中，并返回当前JSONObject实例，以便进行链式调用。
	 * 
	 * @param aKey
	 * @param aValue
	 * @return
	 * @throws JSONException
	 */
	public JSONObject putOnce(String aKey, Object aValue) throws JSONException
	{
		if (aKey != null && aValue != null)
		{
			if (opt(aKey) != null)
			{
				throw new JSONException("重复的键：" + aKey);
			}
			put(aKey, aValue);
		}
		return this;
	}
	
	public JSONObject putIfAbsent(String key, Object value) throws JSONException
	{
		if (opt(key) == null)
			put(key, value);
		return this;
	}
	
	public JSONObject putIf(boolean aValid , String aKey , Object aValue)
	{
		if(aValid)
			put(aKey, aValue) ;
		return this ;
	}
	
	public JSONObject putIf(boolean aValid , String aKey , Supplier<? extends Object> aValue)
	{
		if(aValid)
			put(aKey, aValue.get()) ;
		return this ;
	}

	/**
	 * Put a key/value pair in the JSONObject, but only if the
	 * key and the value are both non-null.
	 * @param key   A key string.
	 * @param value An object which is the value. It should be of one of these
	 *  types: Boolean, Double, Integer, JSONArray, JSONObject, Long, String,
	 *  or the JSONObject.NULL object.
	 * @return this.
	 * @throws JSONException If the value is a non-finite number.
	 */
	public JSONObject putOpt(String key, Object value) throws JSONException
	{
		if (key != null && value != null)
		{
			put(key, value);
		}
		return this;
	}

	/**
	 * Produce a string in double quotes with backslash sequences in all the
	 * right places. A backslash will be inserted within </, allowing JSON
	 * text to be delivered in HTML. In JSON text, a string cannot contain a
	 * control character or an unescaped quote or backslash.
	 * @param string A String
	 * @return  A String correctly formatted for insertion in a JSON text.
	 */
	public static String quote(String string)
	{
		if (string == null || string.length() == 0)
		{
			return "\"\"";
		}

		char b;
		char c = 0;
		int i;
		int len = string.length();
		StringBuffer sb = new StringBuffer(len + 4);
		String t;

		sb.append('"');
		for (i = 0; i < len; i += 1)
		{
			b = c;
			c = string.charAt(i);
			switch (c)
			{
			case '\\':
			case '"':
				sb.append('\\');
				sb.append(c);
				break;
			case '/':
				if (b == '<')
				{
					sb.append('\\');
				}
				sb.append(c);
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\r':
				sb.append("\\r");
				break;
			default:
				if (c < ' ' || (c >= '\u0080' && c < '\u00a0')
						||
						(c >= '\u2000' && c < '\u2100'))
				{
					t = "000" + Integer.toHexString(c);
					sb.append("\\u" + t.substring(t.length() - 4));
				}
				else
				{
					sb.append(c);
				}
			}
		}
		sb.append('"');
		return sb.toString();
	}

	/**
	 * Remove a name and its value, if present.
	 * @param key The name to be removed.
	 * @return The value that was associated with the name,
	 * or null if there was no value.
	 */
	public Object remove(String key)
	{
		return this.mMap.remove(key);
	}
	
	public JSONObject removeEntry(String aKey)
	{
		this.mMap.remove(aKey) ;
		return this ;
	}
	
	public JSONObject removeIf(Predicate<Entry<String , Object>> aPred)
	{
		Iterator<Entry<String , Object>> it = mMap.entrySet().iterator() ;
		while(it.hasNext())
		{
			if(aPred.test(it.next()))
				it.remove();
		}
		return this ;
	}

	/**
	 * 
	 * 返回此JSON对象的键按字母升序排列后的迭代器
	 * 
	 * @return
	 */
	public Iterator<String> sortedKeys()
	{
		return new TreeSet<>(mMap.keySet()).iterator();
	}
	
	/**  
     * 对此JSON对象的键按字幕升序进行重新排列，并返回当前JSON对象.  
     *   
     * <p>此方法首先创建一个新的LinkedHashMap来保持插入顺序，  
     * 然后使用一个TreeSet对原始Map（mMap）的键进行排序。  
     * 排序后的键会依次被放入新的LinkedHashMap中，  
     * 并使用原始Map中的值进行填充。  
     * 最后，将原始Map（mMap）更新为排序后的新Map，  
     * 并返回当前对象（this），以便支持链式调用。</p>  
     *   
     * <p>注意：此方法改变了原始Map（mMap）的内容，  
     * 使其键按照自然顺序（或键的compareTo方法定义的顺序）排序。</p>  
     *   
     * @return 返回当前对象（this），以便支持链式调用。  
     *         返回的JSONObject对象内部Map的键已经排序。  
     */  
	public JSONObject sortedByKey()
	{
		Map<String , Object> newMap = new LinkedHashMap<>() ;
		for(String key : new TreeSet<>(mMap.keySet()))
		{
			newMap.put(key, mMap.get(key)) ;
		}
		mMap = newMap ;
		return this ;
	}

	/**
	 * Try to convert a string into a number, boolean, or null. If the string
	 * can't be converted, return the string.
	 * @param s A String.
	 * @return A simple JSON value.
	 */
	static public Object stringToValue(String s)
	{
		if (s.equals(""))
		{
			return s;
		}
		if (s.equalsIgnoreCase("true"))
		{
			return Boolean.TRUE;
		}
		if (s.equalsIgnoreCase("false"))
		{
			return Boolean.FALSE;
		}
		if (s.equalsIgnoreCase("null"))
		{
//			return JSONObject.NULL;
			return null ;
		}

		/*
		 * If it might be a number, try converting it. We support the 0- and 0x-
		 * conventions. If a number cannot be produced, then the value will just
		 * be a string. Note that the 0-, 0x-, plus, and implied string
		 * conventions are non-standard. A JSON parser is free to accept
		 * non-JSON forms as long as it accepts all correct JSON forms.
		 */

		char b = s.charAt(0);
		if ((b >= '0' && b <= '9') || b == '.' || b == '-' || b == '+')
		{
			if (b == '0')
			{
				if (s.length() > 2 &&
						(s.charAt(1) == 'x' || s.charAt(1) == 'X'))
				{
					try
					{
						return Integer.valueOf(Integer.parseInt(s.substring(2),
								16));
					}
					catch (Exception e)
					{
						/* Ignore the error */
					}
				}
				else
				{
					try
					{
						return Integer.valueOf(Integer.parseInt(s, 8));
					}
					catch (Exception e)
					{
						/* Ignore the error */
					}
				}
			}
			try
			{
				if (s.indexOf('.') > -1 || s.indexOf('e') > -1 || s.indexOf('E') > -1)
				{
					return Double.valueOf(s);
				}
				else
				{
					Long myLong = Long.valueOf(s);
					if (myLong.longValue() == myLong.intValue())
					{
						return Integer.valueOf(myLong.intValue());
					}
					else
					{
						return myLong;
					}
				}
			}
			catch (Exception f)
			{
				/* Ignore the error */
			}
		}
		return s;
	}

	/**
	 * Throw an exception if the object is an NaN or infinite number.
	 * @param o The object to test.
	 * @throws JSONException If o is a non-finite number.
	 */
	static void testValidity(Object o ) throws JSONException
	{
		testValidity(o, null) ;
	}
	static void testValidity(Object o , String aAddMsg , Object... aMsgArgs) throws JSONException
	{
		if (o != null)
		{
			if (o instanceof Double)
			{
				if (((Double) o).isInfinite() || ((Double) o).isNaN())
				{
					String addMsg = XString.isNotEmpty(aAddMsg)?XString.msgFmt(aAddMsg, aMsgArgs):"" ;
					throw new JSONException(
							"JSON does not allow non-finite numbers."+addMsg);
				}
			}
			else if (o instanceof Float)
			{
				if (((Float) o).isInfinite() || ((Float) o).isNaN())
				{
					String addMsg = XString.isNotEmpty(aAddMsg)?XString.msgFmt(aAddMsg, aMsgArgs):"" ;
					throw new JSONException(
							"JSON does not allow non-finite numbers."+addMsg);
				}
			}
		}
	}

	/**
	 * Produce a JSONArray containing the values of the members of this
	 * JSONObject.
	 * @param names A JSONArray containing a list of key strings. This
	 * determines the sequence of the values in the result.
	 * @return A JSONArray of values.
	 * @throws JSONException If any of the values are non-finite numbers.
	 */
	public JSONArray toJSONArray(JSONArray names) throws JSONException
	{
		if (names == null || names.size() == 0)
		{
			return null;
		}
		JSONArray ja = new JSONArray();
		for (int i = 0; i < names.size() ; i += 1)
		{
			ja.put(this.opt(names.getString(i)));
		}
		return ja;
	}

	/**
	 * Make a JSON text of this JSONObject. For compactness, no whitespace
	 * is added. If this would not result in a syntactically correct JSON text,
	 * then null will be returned instead.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 *
	 * @return a printable, displayable, portable, transmittable
	 *  representation of the object, beginning
	 *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
	 *  with <code>}</code>&nbsp;<small>(right brace)</small>.
	 */
	public String toString()
	{
		try
		{
			Iterator<String> keys = keys();
			StringBuffer sb = new StringBuffer("{");

			while (keys.hasNext())
			{
				if (sb.length() > 1)
				{
					sb.append(',');
				}
				Object o = keys.next();
				sb.append(quote(o.toString()));
				sb.append(':');
				sb.append(valueToString(this.mMap.get(o)));
			}
			sb.append('}');
			return sb.toString();
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Make a prettyprinted JSON text of this JSONObject.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * @param indentFactor The number of spaces to add to each level of
	 *  indentation.
	 * @return a printable, displayable, portable, transmittable
	 *  representation of the object, beginning
	 *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
	 *  with <code>}</code>&nbsp;<small>(right brace)</small>.
	 * @throws JSONException If the object contains an invalid number.
	 */
	public String toString(int indentFactor) throws JSONException
	{
		return toString(indentFactor, 0);
	}

	/**
	 * Make a prettyprinted JSON text of this JSONObject.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * @param indentFactor The number of spaces to add to each level of
	 *  indentation.
	 * @param indent The indentation of the top level.
	 * @return a printable, displayable, transmittable
	 *  representation of the object, beginning
	 *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
	 *  with <code>}</code>&nbsp;<small>(right brace)</small>.
	 * @throws JSONException If the object contains an invalid number.
	 */
	public String toString(int indentFactor, int indent) throws JSONException
	{
		int j;
		int n = size() ;
		if (n == 0)
		{
			return "{}";
		}
		Iterator<String> keys = sortedKeys();
		StringBuffer sb = new StringBuffer("{");
		int newindent = indent + indentFactor;
		Object o;
		if (n == 1)
		{
			o = keys.next();
			sb.append(quote(o.toString()));
			sb.append(": ");
			sb.append(valueToString(this.mMap.get(o),
					indentFactor,
					indent));
		}
		else
		{
			while (keys.hasNext())
			{
				o = keys.next();
				if (sb.length() > 1)
				{
					sb.append(",\n");
				}
				else
				{
					sb.append('\n');
				}
				for (j = 0; j < newindent; j += 1)
				{
					sb.append(' ');
				}
				sb.append(quote(o.toString()));
				sb.append(": ");
				sb.append(valueToString(mMap.get(o),
						indentFactor,
						newindent));
			}
			if (sb.length() > 1)
			{
				sb.append('\n');
				for (j = 0; j < indent; j += 1)
				{
					sb.append(' ');
				}
			}
		}
		sb.append('}');
		return sb.toString();
	}

	/**
	 * Make a JSON text of an Object value. If the object has an
	 * value.toJSONString() method, then that method will be used to produce
	 * the JSON text. The method is required to produce a strictly
	 * conforming text. If the object does not contain a toJSONString
	 * method (which is the most common case), then a text will be
	 * produced by other means. If the value is an array or Collection,
	 * then a JSONArray will be made from it and its toJSONString method
	 * will be called. If the value is a MAP, then a JSONObject will be made
	 * from it and its toJSONString method will be called. Otherwise, the
	 * value's toString method will be called, and the result will be quoted.
	 *
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * @param value The value to be serialized.
	 * @return a printable, displayable, transmittable
	 *  representation of the object, beginning
	 *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
	 *  with <code>}</code>&nbsp;<small>(right brace)</small>.
	 * @throws JSONException If the value is or contains an invalid number.
	 */
	public static String valueToString(Object value) throws JSONException
	{
		if (value == null || value.equals(null))
		{
			return "null";
		}
		if (!(value instanceof String))
		{
			if(value instanceof Polytope)
			{
				return valueToString(((Polytope)value).getFacade()) ;
			}
			if (value instanceof JSONString)
			{
				try
				{
					return ((JSONString) value).toJSONString();
				}
				catch (Exception e)
				{
					throw new JSONException(e);
				}
			}
			if (value instanceof Number)
			{
				return numberToString((Number) value);
			}
			if (value instanceof Boolean)
			{
				return value.toString();
			}
			if (value instanceof JSONSerializer)
			{
				StringBuilder strBld = new StringBuilder();
				((JSONSerializer) value).serialize(new JSONWriter(strBld));
				return strBld.toString();
			}

			if (value instanceof Map)
			{
				return new JSONObject((Map) value).toString();
			}
			if (value instanceof Collection)
			{
				return new JSONArray((Collection) value).toString();
			}
			if (value.getClass().isArray())
			{
				return new JSONArray(value).toString();
			}
		}
		return quote(value.toString());
	}

	/**
	 * Make a prettyprinted JSON text of an object value.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * @param value The value to be serialized.
	 * @param indentFactor The number of spaces to add to each level of
	 *  indentation.
	 * @param indent The indentation of the top level.
	 * @return a printable, displayable, transmittable
	 *  representation of the object, beginning
	 *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
	 *  with <code>}</code>&nbsp;<small>(right brace)</small>.
	 * @throws JSONException If the object contains an invalid number.
	 */
	static String valueToString(Object value, int indentFactor, int indent)
			throws JSONException
	{
		if (value == null || value.equals(null))
		{
			return "null";
		}
		try
		{
			if (value instanceof JSONString)
			{
				return ((JSONString) value).toString(indentFactor , indent+indentFactor);
			}
		}
		catch (Exception e)
		{
			/* forget about it */
		}
		if (value instanceof Number)
		{
			return numberToString((Number) value);
		}
		if (value instanceof Boolean)
		{
			return value.toString();
		}
		if (value instanceof JSONArray)
		{
			return ((JSONArray) value).toString(indentFactor, indent);
		}
		if (value instanceof Map)
		{
			JSONObject.of((Map) value).toString(indentFactor, indent);
		}
		if (value instanceof Collection)
		{
			return JSONArray.of((Collection) value).toString(indentFactor, indent);
		}
		if (value.getClass().isArray())
		{
			return new JSONArray(value).toString(indentFactor, indent);
		}
		return quote(value.toString());
	}

	/**
	 * Write the contents of the JSONObject as JSON text to a writer.
	 * For compactness, no whitespace is added.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 *
	 * @return The writer.
	 * @throws JSONException
	 */
	public Writer write(Writer writer) throws JSONException
	{
		try
		{
			boolean b = false;
			Iterator<String> keys = keys();
			writer.write('{');

			while (keys.hasNext())
			{
				if (b)
				{
					writer.write(',');
				}
				Object k = keys.next();
				writer.write(quote(k.toString()));
				writer.write(':');
				Object v = this.mMap.get(k);
				if (v instanceof JSONObject)
				{
					((JSONObject) v).write(writer);
				}
				else if (v instanceof JSONArray)
				{
					((JSONArray) v).write(writer);
				}
				else
				{
					writer.write(valueToString(v));
				}
				b = true;
			}
			writer.write('}');
			return writer;
		}
		catch (IOException e)
		{
			throw new JSONException(e);
		}
	}
	
	/**
	 * 浅拷贝，拷贝指定的“键-值”对
	 * @param aSource
	 * @param aKeys		如果没有指定键，将不发生拷贝		
	 * @return
	 */
	public JSONObject copyFrom(JSONObject aSource , String...aKeys)
	{
		if(XC.isNotEmpty(aKeys) && aSource != null)
		{
			for(String key : aKeys)
			{
				put(key , aSource.opt(key)) ;
			}
		}
		return this ;
	}
	
	public JSONObject copyFromIfAbsentOrNull(JSONObject aSource , boolean aClone)
	{
		aSource.forEach((k , v)->{
			if(isNull(k))
			{
				if(aClone && v instanceof Cloneable)
					v = XClassUtil.clone(v) ;
				put(k, v) ;
			}
		});
		return this ;
	}
	
	public JSONObject copyAllFrom(JSONObject aSource)
	{
		if(aSource != null)
		{
			for(String key : aSource.keySet())
			{
				put(key, aSource.opt(key)) ;
			}
		}
		return this ;
	}
	
	/**
	 * 从指定对象复刻信息，原来的内容会被清除掉
	 * @param aJo
	 * @return
	 */
	public JSONObject duplicate(JSONObject aJo)
	{
		if(aJo != null)
		{
			if(aJo.isEmpty())
				clear() ;
			else if(mMap == null)
				mMap = new LinkedHashMap<>() ;
			else
				mMap.clear();
			
			aJo.forEach((key, val) -> {
				if (val instanceof JSONCloneable)
					mMap.put(key, ((JSONCloneable) val).clone());
				else
					mMap.put(key, val);
			});
		}
		return this ;
	}

//	public boolean isJSONExtend()
//	{
//		return mMap.size() == 1
//				&& ParserRegister.getDefault().get((String) mMap.keySet().iterator().next()) != null;
//	}
//
//	public Object convertToExtendObject() throws JSONException
//	{
//		String key = (String) mMap.keySet().iterator().next();
//		JSONArray jsArray = getJSONArray(key);
//		return ParserRegister.getDefault().get(key).parse(jsArray);
//	}

	public Map<String, String> toStringMap()
	{
		return toStringMap(new LinkedHashMap<>()) ;
	}
	
	public Map<String, String> toStringMap(Map<String, String> aMap)
	{
		for(Entry<String , Object> entry : mMap.entrySet())
			aMap.put(entry.getKey(), entry.getValue().toString()) ;
		return aMap ;
	}
	
	public Map<String, Object> toMap()
	{
		return toMap(new LinkedHashMap<String, Object>()) ;
	}
	public Map<String, Object> toMap(Map<String, Object> aMap)
	{
		forEach((key , value)->{
			if(value instanceof JSONObject)
				aMap.put(key, ((JSONObject)value).toMap()) ;
			else if(value instanceof JSONArray)
				aMap.put(key, ((JSONArray)value).toList()) ;
//			else if(NULL.equals(value))
//				aMap.put(key, null) ;
			else
				aMap.put(key, value) ;
		}) ;
		return aMap ;
	}

	@Override
	public String toJSONString()
	{
		return toString();
	}

	public JSONObject injectWith(Consumer<JSONObject> aInjector)
	{
		if (aInjector != null)
			aInjector.accept(this);
		return this;
	}

	public Map<String, Object> getDataMap()
	{
		return mMap;
	}

	@Override
	public boolean equals(Object aObj)
	{
		if (this == aObj)
			return true;
		if (aObj instanceof JSONObject)
		{
			if (((JSONObject) aObj).mMap.size() != mMap.size())
				return false;
			for (Entry<String, Object> entry : ((JSONObject) aObj).mMap.entrySet())
			{
				if (JCommon.unequals(entry.getValue(), mMap.get(entry.getKey())))
					return false;
			}
			return true;
		}
		return false;
	}

	public JSONObject clone()
	{
		JSONObject clone = new JSONObject();
		forEach((key, val) -> {
			if (val instanceof JSONCloneable)
				clone.mMap.put(key, ((JSONCloneable) val).clone());
			else
				clone.mMap.put(key, val);
		});
		return clone;
	}
	
	public void clear()
	{
		mMap.clear() ;
	}
	
	public JSONObject clear0()
	{
		this.mMap.clear() ;
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
	
	protected boolean depthFirstVisitEntry_0(Stack<JSONEntry> aStack , BiIteratorPredicate<String, Object> aBiItPred)
	{
		if(!isEmpty())
		{
			JSONEntry entry = null ;
			if(aStack != null)
			{
				entry = new JSONEntry(this) ;
				aStack.push(entry) ;
			}
			try
			{
				bp_0428_1439:for(String key : keyArray())
				{
					Object val = mMap.get(key) ;
					if(entry != null)
						entry.setIndex(key) ;
					switch(aBiItPred.visit(key, val))
					{
					case IterateOpCode.sContinue :
					{
						if(val != null)
						{
							if(val instanceof JSONObject)
							{
								if(!((JSONObject)val).depthFirstVisitEntry_0(aStack , aBiItPred))
									return false ;
							}
							else if(val instanceof JSONArray && !((JSONArray)val).depthFirstVisitEntry_0(aStack , aBiItPred))
							{
								return false ;
							}
						}
					}
						continue ;
					case IterateOpCode.sBreak :
						break bp_0428_1439 ;
					case IterateOpCode.sInterrupted :
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
	
	public JSONObject sum(JSONObject aOtherJo , double aDefaultVal , String... aFields)
	{
		if(aOtherJo == null || XC.isEmpty(aFields))
			return this ;
		for(String field : aFields)
		{
			put(field, optDouble(field, aDefaultVal) + aOtherJo.optDouble(field, aDefaultVal)) ;
		}
		return this ;
	}
	
	public JSONObject sum(DoubleConsumer aConsumer , double aDefaultVal , String... aFields)
	{
		if(aConsumer != null)
		{
			double sumV = 0 ;
			for(String field : aFields)
			{
				sumV += optDouble(field, aDefaultVal);
			}
			aConsumer.accept(sumV);
		}
		return this ;
	}
	
	public JSONObject rekey(String aOldKey , String aNewKey)
	{
		Assert.isNotTrue(this.mMap.containsKey(aNewKey) , "不能将健%1$s重命名为%2$s，因为已经存在同名的键" , aOldKey , aNewKey) ;
		Object val = this.mMap.remove(aOldKey) ;
		if(val != null)
			this.mMap.put(aNewKey , val) ;
		return this ;
	}
	
	/**
	 * 
	 * @param aConverter			返回null表示不进行转换
	 * @return
	 */
	public JSONObject rekeys(Function<String, String> aConverter)
	{
		String[] keys = mMap.keySet().toArray(JCommon.sEmptyStringArray) ;
		for(String key : keys)
		{
			String newKey = aConverter.apply(key) ;
			if(newKey != null && !newKey.equals(key))
			{
				Object val = this.mMap.remove(key) ;
				if(val != null)
					this.mMap.put(newKey , val) ;
			}
		}
		return this ;
	}
	
	public JSONObject rekeysRemoveUnderline()
	{
		return rekeys(XString::removeUnderLine) ;
	}
	
	public JSONObject revalue(Function<Object,Object> aFunc , String... aKeys)
	{
		if(XC.isNotEmpty(aKeys))
		{
			for(String key : aKeys)
			{
				put(key , aFunc.apply(opt(key))) ;
			}
		}
		return this ;
	}
	
	public JSONObject revalue(Function<Object,Object> aFunc , Predicate<String> aPred)
	{
		for(String key : keyArray())
		{
			if(aPred.test(key))
				put(key , aFunc.apply(opt(key))) ;
		}
		return this ;
	}

	@Override
	public int size()
	{
		return mMap.size() ;
	}

	@Override
	public boolean containsKey(Object aKey)
	{
		return mMap.containsKey(aKey) ;
	}

	@Override
	public boolean containsValue(Object aValue)
	{
		return mMap.containsValue(aValue) ;
	}

	@Override
	public Object remove(Object aKey)
	{
		return mMap.remove(aKey) ;
	}

	@Override
	public void putAll(Map<? extends String, ?> aM)
	{
		if(aM != null)
		{
			aM.forEach((key , value)->{
				put(key, value) ;
			}) ;
		}
	}

	@Override
	public Set<Entry<String, Object>> entrySet()
	{
		return mMap.entrySet() ;
	}
	
	/**
	 * 构建一个新的空的JSONObject对象
	 * @return
	 */
	public static JSONObject one()
	{
		return new JSONObject() ;
	}
	
	/**  
	 * 将给定的Map转换为JSONObject。  
	 *  
	 * <p>如果传入的Map实例已经是JSONObject类型，则直接返回该JSONObject实例；  
	 * 否则，使用传入的Map创建一个新的JSONObject实例并返回。</p>  
	 *  
	 * @param aMap 要转换的Map，其键应为String类型，值可以是任何类型。  
	 * @return 转换后的JSONObject实例，或者如果aMap已经是JSONObject类型，则直接返回aMap。  
	 * @throws JSONException 如果在转换过程中发生错误，例如Map中包含无法转换为JSON的键或值。  
	 */ 
	public static JSONObject of(Map<String, ?> aMap)
	{
		if(aMap instanceof JSONObject)
			return (JSONObject)aMap ;
		else
			return new JSONObject(aMap) ;
	}
	
	/**
	 * 根据提供的Iterable、键函数和值函数生成一个JSONObject。
	 *
	 * @param <T> Iterable中元素的类型。
	 * @param aIt 要迭代的元素集合，不能为null（但集合内部可以为空）。
	 * @param aKeyFunc 一个函数，接受Iterable中的元素作为输入，并返回该元素对应的键（String类型）。
	 * @param aValFunc 一个函数，接受Iterable中的元素作为输入，并返回该元素对应的值（Object类型）。
	 * @return 一个JSONObject，其键和值由aKeyFunc和aValFunc根据aIt中的元素生成。
	 *         如果aIt为null，则返回一个空的JSONObject。
	 *         注意：如果aKeyFunc对多个元素返回相同的键，则后面的值会覆盖前面的值。
	 */
	public static <T> JSONObject of(Iterable<T> aIt , Function<T, String> aKeyFunc
			, Function<T , Object> aValFunc)
	{
		JSONObject jo = new JSONObject() ;
		if(aIt != null)
		{
			for(T ele : aIt)
			{
				jo.put(aKeyFunc.apply(ele) , aValFunc.apply(ele)) ;
			}
		}
		return jo ;
	}
	
	/**
	 * 
	 * @param aSource
	 * @return			返回结果不为null
	 */
	public static JSONObject of(String aSource)
	{
		JSONObject jo = new JSONObject() ;
		if(XString.isNotEmpty(aSource))
		{
			try
			{
				jo._init(new JSONTokener(aSource));
			}
			catch(JSONException e)
			{
				e.setSourceStr(aSource) ;
				throw e ;
			}
		}
		return jo ;
	}
	
	static class JSONEntrySet extends AbstractSet<Map.Entry<String, Object>>
	{
		Set<Map.Entry<String, Object>> mSet ;
		
		public JSONEntrySet(Set<Map.Entry<String, Object>> aSet)
		{
			mSet = aSet ;
		}

		@Override
		public int size()
		{
			return mSet.size() ;
		}

		@Override
		public Iterator<Entry<String, Object>> iterator()
		{
			return mSet.iterator() ;
		}

		@Override
		public boolean contains(Object aO)
		{
			return mSet.contains(aO) ;
		}
		
		@Override
		public boolean remove(Object aO)
		{
			return mSet.remove(aO) ;
		}

		
	}
}