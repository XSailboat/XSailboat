package team.sailboat.commons.fan.dpa;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import team.sailboat.commons.fan.collection.XC;

/**
 * 
 * DBean的某个字段的Map型映射索引
 *
 * @author yyl
 * @since 2024年11月25日
 */
public class MapIndex<T extends DBean>
{
	final Class<T> mClass ;
	final T[] mEmptyArray ;
	Map<Object , Map<String, T>> mDataMap = XC.linkedHashMap() ;

	/**
	 * 构造函数，初始化mClass和mEmptyArray
	 * @param aClass 泛型T的Class对象
	 */
	MapIndex(Class<T> aClass)
	{
		mClass = aClass ;
		mEmptyArray = (T[])Array.newInstance(mClass, 0) ;
	}
	
	/**
	 * 根据键aKey获取对应的泛型T数组，返回结果不为null
	 * @param aKey 主Map的键
	 * @return 泛型T的数组，若没有找到对应的Map，则返回空数组
	 */
	public synchronized T[] get(Object aKey)
	{
		Map<String, T> map = mDataMap.get(aKey) ;
		if(map != null)
			return map.values().toArray(mEmptyArray) ;
		return mEmptyArray ;
	}
	
	/**
	 * 根据键aKey和谓词aPred获取满足条件的泛型T数组，返回结果不为null
	 * @param aKey 主Map的键
	 * @param aPred 用于筛选泛型T的谓词
	 * @return 满足条件的泛型T的数组，若没有找到对应的Map或没有满足条件的元素，则返回空数组	<br>
	 * 			返回结果不为null
	 */
	public T[] get(Object aKey , Predicate<T> aPred)
	{
		return XC.filter(get(aKey) , aPred) ;
	}
	
	/**
	 * 根据键aKey获取对应的泛型T的数量
	 * @param aKey 主Map的键
	 * @return 泛型T的数量，若没有找到对应的Map，则返回0
	 */
	public int getAmount(Object aKey)
	{
		Map<String, T> map = mDataMap.get(aKey) ;
		return map != null?map.size():0 ;
	}
	
	/**
	 * 根据键aKey获取对应的泛型T的第一个元素
	 * @param aKey 主Map的键
	 * @return 泛型T的第一个元素，若没有找到对应的Map，则返回null
	 */
	public T getFirst(Object aKey)
	{
		Map<String, T> map = mDataMap.get(aKey) ;
		if(map != null)
			return XC.getFirst(map.values()) ;
		return null ;
	}
	
	/**
	 * 根据键aKey和谓词aPred获取满足条件的泛型T的第一个元素
	 * @param aKey 主Map的键
	 * @param aPred 用于筛选泛型T的谓词
	 * @return 满足条件的泛型T的第一个元素，若没有找到对应的Map或没有满足条件的元素，则返回null
	 */
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
	
	/**
	 * 根据键aKey获取对应的泛型T的第一个元素，并包装为Optional对象
	 * @param aKey 主Map的键
	 * @return 泛型T的第一个元素的Optional对象，若没有找到对应的Map，则返回Optional.empty()
	 */
	public Optional<T> getFirstOp(Object aKey)
	{
		return Optional.of(getFirst(aKey)) ;
	}
	
	/**
	 * 判断是否存在指定的索引条目且有数据
	 * @param aKey 主Map的键
	 * @return 若存在指定的索引条目且有数据，则返回true；否则返回false
	 */
	public boolean hasIndex(Object aKey)
	{
		return XC.isNotEmpty(mDataMap.get(aKey)) ;
	}
	
	/**
	 * 获取所有索引的数组
	 * @return 所有索引的数组
	 */
	public Object[] indexes()
	{
		return mDataMap.keySet().toArray() ;
	}
	
	/**
	 * 向主Map中添加数据
	 * @param aKey 主Map的键
	 * @param aValue 要添加的泛型T对象
	 */
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
	
	/**
	 * 从主Map中移除数据
	 * @param aKey 主Map的键
	 * @param aValue 要移除的泛型T对象
	 */
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
