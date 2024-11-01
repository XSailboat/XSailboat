package team.sailboat.commons.fan.app;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.lang.XClassUtil;

public class AppContext
{
	public static final Charset sUTF8 = Charset.forName("UTF-8") ;
	
	public static final Charset sDefaultEncoding = sUTF8 ;
	
	static final String sTLKey_link = "__link" ;
	
	static Map<String , Object> sMap = XC.concurrentHashMap() ;
	static Map<String , ThreadLocal<Object>> sTLMap = XC.concurrentHashMap() ;
	private static final Object sNullObject = new Object() ;
	
	static String sAppName ;
	
	public static String getAppName()
	{
		return sAppName ;
	}
	
	public static void setAppName(String aAppName)
	{
		sAppName = aAppName ;
	}
	
	public static void setThreadLocal(String aKey , Object aVal)
	{
		ThreadLocal<Object> tl = sTLMap.get(aKey) ;
		if(aVal != null)
		{
			if(tl == null)
			{
				synchronized (sTLMap)
				{
					tl = sTLMap.get(aKey) ;
					if(tl == null)
					{
						tl = new ThreadLocal<>() ;
						sTLMap.put(aKey, tl) ; 
					}
				}
			}
			tl.set(aVal) ;
		}
		else if(tl != null)
		{
			tl.remove();
		}
	}
	
	
	public static Object get(String aKey)
	{
		Object val = _getThreadLocal(aKey) ;
		if(val == sNullObject)
			return sMap.get(aKey) ;
		return val ;
	}
	
	public static int getInt(String aKey)
	{
		Object val = get(aKey) ;
		Assert.notNull(val, "AppContext中不存在键：%s", aKey) ;
		Integer v = XClassUtil.toInteger(val) ;
		Assert.notNull(val, "AppContext中键%s的值是%s，不能转成int", aKey , val) ;
		return v.intValue() ;
	}
	
	public static <T> T get(String aKey , T aDefault)
	{
		Object obj = get(aKey) ;
		return obj != null?(T)obj:aDefault ;
	}
	
	public static boolean get(String aKey , boolean aDefaultVal)
	{
		Object val = get(aKey) ;
		if(val == null)
			return aDefaultVal ;
		Assert.isInstanceOf(Boolean.class, val) ;
		return ((Boolean)val).booleanValue() ;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T get(String aKey , Class<T> aClazz)
	{
		Object obj = get(aKey) ;
		return obj != null?(T)obj:null ;
	}
	
	public static Object getThreadLocal(String aKey)
	{
		Object val = _getThreadLocal(aKey) ;
		return val == sNullObject?null:val ;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getThreadLocal(String aKey , T aDefaultValue)
	{
		Object val = _getThreadLocal(aKey) ;
		return val == sNullObject?aDefaultValue:(T)val ;
	}
	
	public static Object removeThreadLocal(String aKey)
	{
		Object val = _removeThreadLocal(aKey) ;
		return val == sNullObject?null:val ;
	}
	
	static Object _getThreadLocal(String aKey)
	{
		ThreadLocal<Object> tl = sTLMap.get(aKey) ;
		return tl==null?getFromInjectedThreadContext((Map<String, Object>) sTLMap.get(sTLKey_link), aKey):tl.get() ;
	}
	
	static Object _removeThreadLocal(String aKey)
	{
		ThreadLocal<Object> tl = sTLMap.get(aKey) ;
		if(tl == null)
		{
			return removeFromInjectedThreadContext((Map<String, Object>) sTLMap.get(sTLKey_link), aKey) ;
		}
		else
		{
			Object val = tl.get() ;
			tl.remove(); 
			return val ;
		}
	}
	
	static Object getFromInjectedThreadContext(Map<String , Object> aInjectedMap , String aKey)
	{
		if(aInjectedMap == null)
			return sNullObject ;
		Object val = aInjectedMap.get(aKey) ;
		if(val != null)
			return val ;
		val = aInjectedMap.get(sTLKey_link) ;
		return val == null?sNullObject:getFromInjectedThreadContext((Map<String, Object>) val, aKey) ;
	}
	
	static Object removeFromInjectedThreadContext(Map<String , Object> aInjectedMap , String aKey)
	{
		if(aInjectedMap == null)
			return sNullObject ;
		Object val = aInjectedMap.remove(aKey) ;
		if(val != null)
			return val ;
		val = aInjectedMap.get(sTLKey_link) ;
		if(val == null)
			return sNullObject ;
		else
			return removeFromInjectedThreadContext((Map<String, Object>) val, aKey) ;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getThreadLocal(String aKey , Class<T> aClazz)
	{
		Object obj = getThreadLocal(aKey) ;
		return obj != null?(T)obj:null ;
	}
	
	public static void releaseThreadLocal(String aKey)
	{
		sTLMap.remove(aKey) ;
	}
	
	/**
	 * <strong>注意：</strong>本线程ThreadLocal中的此键数据将被移除
	 * @param aKey
	 * @param aValue
	 */
	public static void set(String aKey , Object aValue)
	{
		sTLMap.remove(aKey) ;
		if(aValue == null)
			sMap.remove(aKey) ;
		else
			sMap.put(aKey, aValue) ;
	}
	
	static Map<String , Object> getThreadContext()
	{
		if(!sTLMap.isEmpty())
		{
			Map<String, Object> map = new HashMap<>() ;
			for(String key : sTLMap.keySet().toArray(JCommon.sEmptyStringArray))
			{
				ThreadLocal<Object> tl = sTLMap.get(key) ;
				if(tl != null)
					map.put(key, tl.get()) ;
			}
			return map ;
		}
		return Collections.emptyMap() ;
	}
	
	public static void injectThreadContext(Map<String , Object> aMap)
	{
		setThreadLocal(sTLKey_link, aMap); 
	}
	
	public static void removeInjectedThreadContext()
	{
		sTLMap.remove(sTLKey_link) ;
	}
}
