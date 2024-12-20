package team.sailboat.commons.fan.collection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * 
 * Map的流式操作方法
 *
 * @author yyl
 * @since 2024年12月5日
 */
public class StreamMap<K , V , M extends Map<K ,V>>
{
	M map ;
	
	private StreamMap(M aMap)
	{
		map = aMap;
	}
	
	public StreamMap<K, V, M> putIfValue(K aKey , V aValue , Predicate<V> aPred)
	{
		if(aPred.test(aValue))
			map.put(aKey, aValue) ;
		return this ;
	}
	
	public StreamMap<K, V, M> putIfValueNotEmpty(K aKey , V aValue)
	{
		boolean empty = switch(aValue)
		{
			case null -> true ;
			case CharSequence s -> s.isEmpty() ;
			case Collection c -> c.isEmpty() ;
			default -> throw new IllegalArgumentException("不支持的值类型："+aValue.getClass().getName()) ;
		} ;
		if(!empty)
			map.put(aKey, aValue) ;
		return this ;
	}
	
	public M get()
	{
		return map ;
	}
	
	
	public static <K , V , M extends Map<K ,V>> StreamMap<K ,  V , M> of(M aMap)
	{
		return new StreamMap<>(aMap) ;
	}
	
	public static <K , V> StreamMap<K , V , HashMap<K , V>> hashMap(Class<K> aKeyClass
			, Class<V> aValueClass)
	{
		return new StreamMap<>(new HashMap<>()) ;
	}
}
