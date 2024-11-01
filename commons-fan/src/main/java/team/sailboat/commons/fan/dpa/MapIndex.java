package team.sailboat.commons.fan.dpa;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import team.sailboat.commons.fan.collection.XC;

public class MapIndex<T extends DBean>
{
	Class<T> mClass ;
	T[] mEmptyArray ;
	Map<Object , Map<String, T>> mDataMap = XC.linkedHashMap() ;

	MapIndex(Class<T> aClass)
	{
		mClass = aClass ;
		mEmptyArray = (T[])Array.newInstance(mClass, 0) ;
	}
	
	/**
	 * 
	 * @param aKey
	 * @return		返回结果不为null
	 */
	public synchronized T[] get(Object aKey)
	{
		Map<String, T> map = mDataMap.get(aKey) ;
		if(map != null)
			return map.values().toArray(mEmptyArray) ;
		return mEmptyArray ;
	}
	
	/**
	 * 
	 * @param aKey
	 * @param aPred
	 * @return			返回结果不为null
	 */
	public T[] get(Object aKey , Predicate<T> aPred)
	{
		return XC.filter(get(aKey) , aPred) ;
	}
	
	public int getAmount(Object aKey)
	{
		Map<String, T> map = mDataMap.get(aKey) ;
		return map != null?map.size():0 ;
	}
	
	public T getFirst(Object aKey)
	{
		Map<String, T> map = mDataMap.get(aKey) ;
		if(map != null)
			return XC.getFirst(map.values()) ;
		return null ;
	}
	
	public T getFirst(Object aKey , Predicate<T> aPred)
	{
		Map<String, T> map = mDataMap.get(aKey) ;
		if(map != null)
		{
			for(T v : map.values())
			{
				if(aPred.test(v))
					return v ;
			}
		}
		return null ;
	}
	
	public Optional<T> getFirstOp(Object aKey)
	{
		return Optional.of(getFirst(aKey)) ;
	}
	
	/**
	 * 是否存在指定的索引条目且有数据
	 * @param aKey
	 * @return
	 */
	public boolean hasIndex(Object aKey)
	{
		return XC.isNotEmpty(mDataMap.get(aKey)) ;
	}
	
	public Object[] indexes()
	{
		return mDataMap.keySet().toArray() ;
	}
	
	synchronized void add(Object aKey , T aValue)
	{
		Map<String, T> map = mDataMap.get(aKey) ;
		if(map == null)
		{
			map = XC.linkedHashMap() ;
			mDataMap.put(aKey, map) ;
		}
		map.put(DBean.getBID(aValue), aValue) ;
	}
	
	synchronized void remove(Object aKey , T aValue)
	{
		Map<String, T> map = mDataMap.get(aKey) ;
		if(map != null)
		{
			map.remove(DBean.getBID(aValue)) ;
			if(map.isEmpty())
				mDataMap.remove(aKey) ;
		}
	}

}
