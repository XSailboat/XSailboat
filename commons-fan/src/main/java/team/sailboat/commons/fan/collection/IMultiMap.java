package team.sailboat.commons.fan.collection;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import team.sailboat.commons.fan.http.IURLBuilder;
import team.sailboat.commons.fan.infc.BiIteratorPredicate;

public interface IMultiMap<K , V>
{
	IMultiMap<K, V> put(K aKey , V aVal) ;
	
	IMultiMap<K, V> putAll(K aKey , @SuppressWarnings("unchecked") V...aVals) ;
	
	default void putAll(Map<K , V> aMap)
	{
		if(XC.isNotEmpty(aMap))
		{
			for(Entry<K , V> entry : aMap.entrySet())
				put(entry.getKey(), entry.getValue()) ;
		}
	}
	
	default void putAll(IMultiMap<K , V> aMap)
	{
		if(XC.isNotEmpty(aMap))
		{
			for(Entry<K , V> entry : aMap.entrySet())
				put(entry.getKey(), entry.getValue()) ;
		}
	}
	
	SizeIter<V> removeAll(K aKey) ;
	
	IMultiMap<K, V> remove(K aKey , V aValue) ;
	
	boolean containsKey(K aKey) ;
	
	boolean containsEntry(K aKey , V aValue) ;
	
	default void putAll(K aKey , Iterable<V> aVals)
	{
		if(aVals != null)
		{
			for(V val : aVals)
				put(aKey, val) ;
		}
	}
	
	void set(K aKey , @SuppressWarnings("unchecked") V...aVals) ;
	
	SizeIter<V> get(Object aKey) ;
	V getFirst(K aKey) ;
	
	void clear() ;
	int size() ;
	boolean isEmpty() ;
	
	SizeIter<Entry<K , V>> entrySet() ;
	
	Collection<K> keySet() ;
	
	List<V> values() ;
	
	void forEach(BiConsumer<K, V> aConsumer) ;
	
	void iterateEntry(BiIteratorPredicate<K , V> aIt) ;
	
	default PropertiesEx toPropertiesEx()
	{
		PropertiesEx propEx = new PropertiesEx() ;
		forEach(propEx::put) ;
		return propEx ;
	}
	
	public static IMultiMap<String, String> parseFromUrlParams(String aParamsStr)
	{
		return IURLBuilder.parseQueryStr(aParamsStr) ;
	}
}
