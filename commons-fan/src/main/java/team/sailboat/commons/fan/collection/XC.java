package team.sailboat.commons.fan.collection;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.struct.Bits;
import team.sailboat.commons.fan.text.XString;

public class XC
{
	public static LinkedHashSet<Integer> linkedHashSet(int...aArray)
	{
		if(aArray != null)
		{
			LinkedHashSet<Integer> set = new LinkedHashSet<>(Math.max(aArray.length, 4)) ;
			for(int t : aArray)
				set.add(t) ;
			return set ;
		}
		else
			return new LinkedHashSet<>() ;
	}
	
	public static <T> LinkedHashSet<T> linkedHashSet(T[] aArray , Predicate<T> aPred)
	{
		if(aArray != null)
		{
			LinkedHashSet<T> set = new LinkedHashSet<>(Math.max(aArray.length, 4)) ;
			for(T t : aArray)
			{
				if(aPred == null || aPred.test(t))
					set.add(t) ;
			}
			return set ;
		}
		else
			return new LinkedHashSet<>() ;
	}
	
	public static <T> List<T> asList(T[] aArray , int aFrom , int aTo)
	{
		return Arrays.asList(Arrays.copyOfRange(aArray, aFrom , aTo)) ;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> LinkedList<T> linkedList(T...aArray)
	{
		LinkedList<T> list = new LinkedList<T>() ;
		if(aArray != null)
		{
			for(T t : aArray)
				list.add(t) ;
		}
		return list ;
	}
	
	public static <T , R> LinkedList<R> linkedList(Function<T, R> aFunc , T...aArray)
	{
		LinkedList<R> list = new LinkedList<>() ;
		if(aArray != null)
		{
			for(T t : aArray)
				list.add(aFunc.apply(t)) ;
		}
		return list ;
	}
	
	/**
	 * 
	 * @param aArray		可以为null
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> Set<T> hashSet(T...aArray)
	{
		Set<T> set = new HashSet<>() ;
		if(XC.isNotEmpty(aArray))
		{
			for(T t : aArray)
				set.add(t) ;
		}
		return set ;
	}
	
	public static <T , R> HashSet<R> hashSet(Function<T,R> aFunc , T...aArray)
	{
		HashSet<R> set = new HashSet<>() ;
		if(aArray != null)
		{
			for(T t : aArray)
			{
				R r = aFunc.apply(t) ;
				if(r != null)
					set.add(r) ;
			}
		}
		return set ;
	}
	
	public static <T , R> HashSet<R> hashSet(Function<T,R> aFunc , Iterable<T> aArray)
	{
		HashSet<R> set = new HashSet<>() ;
		if(aArray != null)
		{
			for(T t : aArray)
			{
				R r = aFunc.apply(t) ;
				if(r != null)
					set.add(r) ;
			}
		}
		return set ;
	}
	
	public static <T> HashSet<T> hashSet(Iterable<T> aCollection)
	{
		HashSet<T> set = new HashSet<>() ;
		if(aCollection != null)
		{
			for(T t : aCollection)
				set.add(t) ;
		}
		return set ;
	}
	
	public static <T> TreeSet<T> treeSet(T... aArray)
	{
		TreeSet<T> set = new TreeSet<>() ;
		if(isNotEmpty(aArray))
		{
			for(T t : aArray)
				set.add(t) ;
		}
		return set ;
	}
	
	public static <K , V> HashMap<K , V> hashMap(K[] aKeys , V[] aVals)
	{
		final int len = count(aKeys) ;
		Assert.isTrue(len == count(aVals) , "键的数量和值的数量不一致，键的数量是%1$d , 值的数量是%2$d"
				, len , count(aVals));
		HashMap<K , V> map = new HashMap<>(len) ;
		for(int i=0 ; i<len ; i++)
		{
			map.put(aKeys[i], aVals[i]) ;
		}
		return map ;
	}
	
	public static <T , K> LinkedHashMap<K , T> linkedHashMap(Iterable<T> aCollection
			, Function<T, K> aFunc)
	{
		LinkedHashMap<K , T> map = linkedHashMap() ;
		if(aCollection != null)
		{
			for(T t : aCollection)
			{
				K key = aFunc.apply(t) ;
				if(key != null)
					map.put(key, t) ;
			}
		}
		return map ;
	}
	
	public static <K , V> LinkedHashMap<K , V> linkedHashMap(K[] aKeys , V[] aVals)
	{
		final int len = count(aKeys) ;
		Assert.isTrue(len == count(aVals) , "键的数量和值的数量不一致，键的数量是%1$d , 值的数量是%2$d"
				, len , count(aVals));
		LinkedHashMap<K , V> map = new LinkedHashMap<>(len) ;
		for(int i=0 ; i<len ; i++)
		{
			map.put(aKeys[i], aVals[i]) ;
		}
		return map ;
	}
	
	/**
	 * 
	 * @param aArray		按照键、值、键、值的顺序
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <K , V> HashMap<K , V> hashMap(Object...aArray)
	{
		final int len = count(aArray) ;
		Assert.isTrue(len%2 == 0 , "数组的长度必须是偶数，不能是%d" , len);
		HashMap<K , V> map = new HashMap<>(len) ;
		for(int i=0 ; i<len ;)
		{
			map.put((K)aArray[i++], (V)aArray[i++]) ;
		}
		return map ;
	}
	
	public static <K , V> HashMap<K , V> hashMap(Collection<V> aVals , Function<V, K> aKeyExtractor
			, boolean aIgnoreNullKey)
	{
		if(isNotEmpty(aVals))
		{
			HashMap<K , V> map = new HashMap<>(Math.min(512, count(aVals))) ;
			for(V ele : aVals)
			{
				K k = aKeyExtractor.apply(ele) ;
				if(!aIgnoreNullKey || k != null)
					map.put(k, ele) ;
			}
			return map ;
		}
		else
			return new HashMap<>() ;
	}
	
	/**
	 * 
	 * @param aArray		按照键、值、键、值的顺序
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <K , V> LinkedHashMap<K , V> linkedHashMap(Object...aArray)
	{
		final int len = count(aArray) ;
		Assert.isTrue(len%2 == 0 , "数组的长度必须是偶数，不能是%d" , len);
		LinkedHashMap<K , V> map = new LinkedHashMap<>(len) ;
		for(int i=0 ; i<len ;)
		{
			map.put((K)aArray[i++], (V)aArray[i++]) ;
		}
		return map ;
	}
	
	public static <E> ArrayList<E> arrayList()
	{
		return new ArrayList<>() ;
	}
	
	public static <E> ArrayList<E> arrayList(int aInitSize)
	{
		return new ArrayList<>(aInitSize) ;
	}
	
	@SafeVarargs
	public static <E> ArrayList<E> arrayList(E... aEles)
	{
		ArrayList<E> list = new ArrayList<>() ;
		if(isNotEmpty(aEles))
		{
			for(int i=0 ; i<aEles.length ; i++)
				list.add(aEles[i]) ;
		}
		return list ;
	}
	
	public static <E> ArrayList<E> arrayList(Collection<? extends E> aC)
	{
		if(aC != null)
			return new ArrayList<>(aC) ;
		else
			return arrayList() ;
	}
	
	public static <E> ArrayList<E> arrayList(Iterator<?> aC)
	{
		ArrayList<E> list = arrayList() ;
		if(aC != null)
		{
			while(aC.hasNext())
			{
				list.add((E) aC.next()) ;
			}
		}
		return list ;
	}
	
	public static <E> HashSet<E> hashSet()
	{
		return new HashSet<E>() ;
	}
	
	public static <E> HashSet<E> hashSet(int aInitSize)
	{
		return new HashSet<E>(aInitSize) ;
	}
	
	public static <E> Set<E> syncHashSet()
	{
		return Collections.synchronizedSet(new HashSet<E>()) ;
	}
	
	public static <K , V> HashMap<K , V> hashMap()
	{
		return new HashMap<K, V>() ;
	}
	
	public static <K , V> HashMap<K , V> hashMap(int aInitSize)
	{
		return new HashMap<K, V>(aInitSize) ;
	}
	
	public static <K , V> HashMap<K, V> hashMap(Map<? extends K, ? extends V> aMap)
	{
		return aMap == null?hashMap():new HashMap<K, V>(aMap) ;
	}
	
	public static <K , V> IMultiMap<K, V> multiMap()
	{
		return new HashMultiMap<K, V>() ;
	}
	
	public static <K , V> LinkedHashMap<K , V> linkedHashMap()
	{
		return new LinkedHashMap<K, V>() ;
	}
	
	public static <K , V> HashMap<K, V> linkedHashMap(Map<? extends K, ? extends V> aMap)
	{
		return aMap == null?linkedHashMap():new LinkedHashMap<K, V>(aMap) ;
	}
	
	public static <K , V> TreeMap<K , V> treeMap()
	{
		return new TreeMap<>() ;
	}
	
	public static <K , V> TreeMap<K , V> treeMap(Comparator<K> aComp)
	{
		return new TreeMap<>(aComp) ;
	}
	
	public static <V> LinkedHashSet<V> linkedHashSet()
	{
		return new LinkedHashSet<V>() ;
	}
	
	public static <V> TreeSet<V> treeSet()
	{
		return new TreeSet<>() ;
	}
	
	public static <V> TreeSet<V> treeSet(Comparator<V> aComp)
	{
		return new TreeSet<>(aComp) ;
	}
	
	@SafeVarargs
	public static <V> LinkedHashSet<V> linkedHashSet(V...aEles)
	{
		final LinkedHashSet<V> set = new LinkedHashSet<>() ;
		if(aEles != null && aEles.length > 0)
		{
			for(V ele : aEles)
			{
				if(ele != null)
					set.add(ele) ;
			}
		}
		
		return set ;
	}
	
	public static <E> LinkedList<E> linkedList()
	{
		return new LinkedList<E>() ;
	}
	
	public static <E> LinkedList<E> linkedList(Collection<? extends E> aC)
	{
		return new LinkedList<E>(aC) ;
	}
	
	public static <E> void addIf(Collection<E> aC , E aEle , boolean aCnd)
	{
		if(aCnd)
			aC.add(aEle) ;
	}
	
	public static <E> void addIfNotNull(Collection<E> aC , E aEle)
	{
		if(aEle != null)
			aC.add(aEle) ;
	}
	
	public static <E> void set(Collection<E> aC , E[] aEles)
	{
		if(aC != null)
		{
			aC.clear();
			XC.addAll(aC , aEles) ;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <S, T> T[] convert(S[] aArray , Function<S, T> aConvertor , T[] aArray_0)
	{
		if(aArray == null)
			return null ;
		if(aArray_0 == null)
			throw new IllegalArgumentException("T[] 数组不能为null") ;
		T[] array = aArray_0 ;
		if (aArray_0.length < aArray.length)
            array = (T[]) Arrays.copyOf(aArray_0, aArray.length , aArray_0.getClass());
		int i = 0 ;
        for(S ele : aArray)
        	array[i++] = aConvertor.apply(ele) ;
        return array ;
	}
	
	/**
	 * 
	 * @param aArray	可以为null
	 * @param aT
	 * @return
	 */
	public static boolean contains(Object[] aArray , Object aT)
	{
		if(aArray != null && aArray.length>0)
		{
			for(Object t : aArray)
				if(JCommon.equals(t, aT)) return true ;
		}
		return false ;
	}
	
	public static boolean containsIgnoreCase(String[] aArray , String aEle)
	{
		if(aArray != null && aArray.length>0)
		{
			for(String t : aArray)
				if(XString.equalsStrIgnoreCase(t, aEle))
					return true ;
		}
		return false ;
	}
	
	/**
	 * 如果aCollection包含aObjs数组中的任何一个元素，都返回true ;
	 * @param aCollection
	 * @param aObjs			如果aObjs==null或者aObjs.length == 0 必然返回false
	 * @return
	 */
	public static boolean containsAny(Collection<?> aCollection , Object... aObjs)
	{
		if(isNotEmpty(aObjs))
		{
			for(Object obj : aObjs)
				if(aCollection.contains(obj))
					return true ;
		}
		return false ;
	}
	
	public static boolean containsAny(Collection<?> aCollection , Iterable<String> aC2)
	{
		if(aC2 != null)
		{
			for(Object obj : aC2)
				if(aCollection.contains(obj))
					return true ;
		}
		return false ;
	}
	
	public static boolean containsAny(Object[] aArray , Object...aEles)
	{
		if(aArray != null && aArray.length>0)
		{
			for(Object t : aArray)
			{
				for(Object ele : aEles)
					if(JCommon.equals(t, ele))
						return true ;
			}
		}
		return false ;
	}
	
	public static boolean containsAll(Object[] aArray , Object...aEles)
	{
		if(aArray != null && aArray.length>0 && aEles != null && aEles.length>0)
		{
			for(Object ele0 : aEles)
			{
				if(!contains(aArray, ele0))
					return false ;
			}
			return true ;
		}
		return false ;
	}
	
	/**
	 * 
	 * @param aArray	允许为null
	 * @param aT
	 * @return
	 */
	public static boolean contains(int[] aArray , int aT)
	{
		if(aArray != null && aArray.length>0)
		{
			for(int t : aArray)
				if(t == aT) return true ;
		}
		return false ;
	}
	
	/**
	 * 
	 * @param aArray
	 * @param aT		不包括
	 * @param aFrom
	 * @param aTo
	 * @return
	 */
	public static boolean contains(int[] aArray , int aT , int aFrom , int aTo)
	{
		if(aFrom>=0 && aTo>aFrom)
		{
			if(aArray != null && aArray.length>0)
			{
				int end = Math.min(aTo, aArray.length) ;
				for(int i=aFrom ; i<end ; i++)
					if(aArray[i] == aT)
						return true ;
			}
			return false ;
		}
		else
			throw new IllegalArgumentException() ;
	}
	
	public static boolean contains(char[] aArray , char aT)
	{
		if(aArray != null && aArray.length>0)
		{
			for(int t : aArray)
				if(t == aT) return true ;
		}
		return false ;
	}
	
	public static <K , V> ConcurrentHashMap<K , V> concurrentHashMap()
	{
		return new ConcurrentHashMap<>() ;
	}
	
	public static <K> TObjectIntMap<K> intMap()
	{
		return new TObjectIntHashMap<K>() ;
	}
	
	public static <K , V> MapBuilder<K,V> map(K aKey , V aValue)
	{
		MapBuilder<K,V> bld = new _MapBuilder<>() ;
		bld.map(aKey, aValue) ;
		return bld ;
	}
	
	public static interface MapBuilder<K,V>
	{
		MapBuilder<K, V> map(K aKey , V aVal) ;
		
		TObjectIntMap<K> intMap() ;
	}
	
	static class _MapBuilder<K , V> implements MapBuilder<K, V>
	{
		List<K> mKeyList = XC.arrayList() ;
		List<V> mValList = XC.arrayList() ;
		
		@Override
		public TObjectIntMap<K> intMap()
		{
			TObjectIntMap<K> intMap = new TObjectIntHashMap<>() ;
			final int len = mKeyList.size() ;
			for(int i=0 ; i<len ; i++) 
			{
				intMap.put(mKeyList.get(i), XClassUtil.toInteger(mValList.get(i) , 0)) ;
			}
			return intMap ;
		}

		@Override
		public MapBuilder<K , V> map(K aKey, V aVal)
		{
			mKeyList.add(aKey) ;
			mValList.add(aVal) ;
			return this ;
		}
	}
	
	public static <T> void insert(List<T> aList ,T aEle ,  int aIndex)
	{
		if(aIndex<0 || aIndex>aList.size())
			throw new IllegalArgumentException("index="+aIndex+"大于数组长度"+aList.size()) ;
		 if(aIndex == aList.size())
			 aList.add(aEle) ;
		 else
		 {
			 int len = aList.size() ;
			 aList.add(aList.get(len-1)) ;
			 for(int i= len-1 ; i>aIndex ; i--)
				aList.set(i, aList.get(i-1)) ;
			 aList.set(aIndex, aEle) ;
		 }
	}
	
	public static <T> void intersect(List<T> aList1 , List<T> aList2
			, Collection<T> aResult)
	{
		Set<T> set ;
		List<T> list ;
		if(aList1.size() > aList2.size())
		{
			set = new HashSet<>(aList2) ;
			list = aList1 ;
		}
		else
		{
			set = new HashSet<>(aList1) ;
			list = aList2 ;
		}
		for(T ele : list)
		{
			if(set.contains(ele))
				aResult.add(ele) ;
		}
	}
	
	public static <T> ArrayList<T> intersect_0(List<T> aList1 , List<T> aList2)
	{
		ArrayList<T> result = new ArrayList<>() ;
		intersect(aList1, aList2, result) ;
		return result ;
	}
	
	public static <T> TIntObjectMap<T> intKeyMap()
	{
		return new TIntObjectHashMap<>() ;
	}
	
	/**
	 * 如果找不到就返回-1
	 * @param <T>
	 * @param aArray
	 * @param aT
	 * @return
	 */
	public static <T> int indexOf(T[] aArray , T aT)
	{
		if(aArray != null && aArray.length>0)
		{
			for(int i=0 ; i<aArray.length ; i++)
				if(JCommon.equals(aArray[i], aT)) return i ;
		}
		return -1 ;
	}
	
	public static <T , U> int indexOf(T[] aArray , U aU , BiPredicate<T, U> aTester)
	{
		if(aArray != null && aArray.length>0)
		{
			for(int i=0 ; i<aArray.length ; i++)
				if(aTester.test(aArray[i], aU))
					return i ;
		}
		return -1 ;
	}
	
	public static <T> int indexOf(T[] aArray , Predicate<T> aJudge)
	{
		if(aArray != null && aArray.length>0)
		{
			for(int i=0 ; i<aArray.length ; i++)
				if(aJudge.test(aArray[i]))
					return i ;
		}
		return -1 ;
	}
	
	public static int indexOfIgnoreCase(String[] aArray , String aText)
	{
		return indexOfIgnoreCase(aArray, aText, 0) ;
	}
	
	public static int indexOfIgnoreCase(String[] aArray , String aText , int aFrom)
	{
		if(aArray != null && aArray.length>0)
		{
			for(int i=aFrom ; i<aArray.length ; i++)
				if(XString.equalsStrIgnoreCase(aArray[i], aText))
					return i ;
		}
		return -1 ;
	}
	
	public static int indexOf(byte[] aArray , byte[] aSeg)
	{
		if(aArray != null && aSeg != null && aSeg.length>0 && aArray.length>=aSeg.length)
		{
			int end = aArray.length-aSeg.length ;
			for(int i=0 ; i<=end ; i++)
			{
				if(aArray[i] == aSeg[0])
				{
					boolean fit = true ;
					for(int j=1 ; j<aSeg.length ; j++)
					{
						if(aArray[i+j] != aSeg[j])
						{
							fit = false ;
							break ;
						}
					}
					if(fit)
						return i ;
				}
			}
		}
		return -1 ;
	}

	/**
	 * 
	 * @param aArray
	 * @param aSeg
	 * @param aFrom
	 * @param aTo		不包含
	 * @return
	 */
	public static int indexOf(byte[] aArray , byte[] aSeg , int aFrom , int aTo)
	{
		
		if(aArray != null && aSeg != null && aSeg.length>0 && aArray.length>=aTo && aTo-aFrom>=aSeg.length)
		{
			int end = aTo-aSeg.length ;
			for(int i=aFrom ; i<=end ; i++)
			{
				if(aArray[i] == aSeg[0])
				{
					boolean fit = true ;
					for(int j=1 ; j<aSeg.length ; j++)
					{
						if(aArray[i+j] != aSeg[j])
						{
							fit = false ;
							break ;
						}
					}
					if(fit)
						return i ;
				}
			}
		}
		return -1 ;
	}
	
	public static int indexOf(int[] aArray , int aEle)
	{
		if(aArray != null && aArray.length>0)
		{
			for(int i=0 ; i<aArray.length ; i++)
				if(aArray[i] == aEle) return i ;
		}
		return -1 ;
	}
	
	public static int indexOf(char[] aArray , char aEle)
	{
		return indexOf(aArray, aEle, 0) ;
	}
	
	public static int indexOf(char[] aArray , char aEle , int aFrom)
	{
		if(aArray != null && aArray.length>0)
		{
			for(int i=aFrom ; i<aArray.length ; i++)
				if(aArray[i] == aEle) return i ;
		}
		return -1 ;
	}
	
	public static <T> int[] indexOfAll(T[] aArray , T[]aEles)
	{
		if(aEles==null || aEles.length == 0)
			return new int[0] ;
		else
		{
			int[] indexes = new int[aEles.length] ;
			for(int i=0 ; i<aEles.length ; i++)
				indexes[i] = indexOf(aArray , aEles[i]) ;
			return indexes ;
		}
	}
	
	public static int addAndGet(Map<String, AtomicInteger> aCountMap , String aKey , int aDelta)
	{
		AtomicInteger count = aCountMap.get(aKey) ;
		if(count == null)
		{
			count = new AtomicInteger(aDelta) ;
			aCountMap.put(aKey, count) ;
			return aDelta ;
		}
		else
			return count.addAndGet(aDelta) ;
	}
	
	public static <T> void forEach(Enumeration<T> aEnum , Consumer<T> aConsumer)
	{
		while(aEnum.hasMoreElements())
		{
			aConsumer.accept(aEnum.nextElement()) ;
		}
	}
	
	public static <T> void forEach(T[] aArray , Consumer<T> aConsumer)
	{
		if(isEmpty(aArray))
			return ;
		for(T ele : aArray)
			aConsumer.accept(ele) ;
	}

	/**
	 * 
	 * @param <T>
	 * @param aList
	 * @param aJudge		如果aJudge为null或者执行返回true，则添加到返回列表里面
	 * @return
	 */
	public static <T> List<T> extract(Collection<T> aList , Predicate<T> aJudge)
	{
		if(aList != null && aList.size()>0)
		{
			List<T> list = new ArrayList<T>(aList.size()) ;
			for(T obj : aList)
			{
				if(aJudge == null || aJudge.test(obj))
					list.add(obj) ;
			}
			return list ;
		}
		return Collections.emptyList() ;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] filter(T[] aArray , Predicate<T> aFilter)
	{
		if(aArray == null || aArray.length == 0)
			return aArray ;
		List<T> list = null ;
		int i=0 ;
		for(T ele : aArray)
		{
			if(aFilter.test(ele))
			{
				if(list != null)
					list.add(ele) ;
			}
			else if(list == null)
			{
				list = new ArrayList<>(aArray.length) ;
				if(i>0)
					list.addAll(asList(aArray, 0, i)) ;
			}
			i++ ;
		}
		return list == null?aArray:list.toArray((T[])Array.newInstance(aArray.getClass().getComponentType(), list.size())) ;
	}
	
	public static <T> void filter(T[] aArray , Predicate<T> aFilter , Collection<T> aResultColl)
	{
		if(aArray == null || aArray.length == 0)
			return ;
		final int len = aArray.length ;
		for(int i=0 ; i<len ; i++)
		{
			if(aFilter.test(aArray[i]))
				aResultColl.add(aArray[i]) ;
		}
	}
	
	public static <T> List<T> filter(Collection<T> aList , Predicate<T> aJudge)
	{
		if(aList != null && aList.size()>0)
		{
			List<T> list = new ArrayList<T>(aList.size()) ;
			for(T obj : aList)
			{
				if(aJudge == null || aJudge.test(obj))
					list.add(obj) ;
			}
			return list ;
		}
		return Collections.emptyList() ;
	}
	
	public static <T> int filterCount(List<T> aList , Predicate<T> aJudge)
	{
		if(aList != null && aList.size()>0)
		{
			int count = 0 ;
			for(T obj : aList)
				if(aJudge.test(obj)) count++ ;
			return count ;
		}
		return 0 ;
	}
	
	/**
	 * 
	 * @param aArray
	 * @param aFrom
	 * @param aStartI				起始值，以后每个值累加aCumulativeAmount，然后置入
	 * @param aAsc					是累加1,还是累减1
	 * @return
	 */
	public static void fill(int[] aArray , int aFrom , int aTo , int aStartI , int aCumulativeAmount)
	{
		aFrom = Math.max(0, aFrom) ;
		aTo = Math.min(aArray.length , aTo) ;
		int val = aStartI ;
		for(int i=aFrom ; i<aTo ; i++)
		{
			aArray[i] = val ;
			val += aCumulativeAmount ;
		}
	}
	
	public static Integer[] fill(Integer[] aArray , int aFrom , int aTo , int aFirstVal , int aStep)
	{
		int endIndex = Math.min(aArray.length , aTo) ;
		int dist = 0 ;
		for(int i=aFrom ; i<endIndex ; i++)
		{
			aArray[i] = aFirstVal + dist ;
			dist += aStep ;
		}
		return aArray ;
	}
	
	/**
	 * 在指定可迭代集合中查找第一个满足给定条件的元素。
	 * 
	 * @param <T>            集合中元素的类型。
	 * @param aIt            要搜索的可迭代集合。
	 * @param aPredicate     用于测试集合中元素的谓词（条件）。
	 * @return               如果找到满足条件的元素，则返回包含该元素的Optional对象；
	 *                       否则返回空的Optional对象。
	 *                       如果输入的可迭代集合为null或没有元素满足条件，则返回Optional.empty()。
	 */
	public static <T> Optional<T> findFirst(Iterable<T> aIt , Predicate<T> aPredicate)
	{
		if(aIt != null)
		{
			for(T ele : aIt)
			{
				if(aPredicate.test(ele))
					return Optional.of(ele) ;
			}
		}
		return Optional.empty() ;
	}
	
	/**
	 * 在指定数组中查找第一个满足给定条件的元素的索引。
	 * 
	 * @param <T>            数组元素的类型。
	 * @param aSrc           要搜索的数组。
	 * @param aPredicate     用于测试数组元素的谓词（条件）。
	 * @param aStartIndex    开始搜索的位置（包含）。
	 * @return               如果找到满足条件的元素，则返回该元素的索引；否则返回-1。
	 *                       如果输入数组为null或从aStartIndex开始没有元素满足条件，则也返回-1。
	 */
	public static <T> int findFirstIndex(T[] aSrc , Predicate<T> aPredicate , int aStartIndex)
	{
		if(aSrc != null)
		{
			for(int i=aStartIndex ; i<aSrc.length ; i++)
			{
				if(aPredicate.test(aSrc[i]))
				{
					return i ;
				}
			}
		}
		return -1 ;
	}
	
	/**
	 * 在指定数组中查找第一个满足给定条件的元素。
	 * 
	 * @param <T>            数组元素的类型。
	 * @param aSrc           要搜索的数组。
	 * @param aPredicate     用于测试数组元素的谓词（条件）。
	 * @param aStartIndex    开始搜索的位置（包含）。
	 * @return               如果找到满足条件的元素，则返回包含该元素的Optional对象；
	 *                       否则返回空的Optional对象。
	 *                       如果输入数组为null或从aStartIndex开始没有元素满足条件，则返回Optional.empty()。
	 */
	public static <T> Optional<T> findFirst(T[] aSrc , Predicate<T> aPredicate , int aStartIndex)
	{
		int index = findFirstIndex(aSrc, aPredicate, aStartIndex) ;
		return index == -1?Optional.empty() : Optional.of(aSrc[index]) ;
	}
	
	@SuppressWarnings("unchecked")
	public static <K , V> Entry<K , V>[] findTopN(Map<K, V> aMap , Comparator<Entry<K , V>> aComp , final int aN)
	{
		final Entry<K , V>[] resultArray = new Entry[aN] ;
		int size = 0 ;
		for(Entry<K , V> entry : aMap.entrySet())
		{
			if(size < aN)
			{
				resultArray[size++] = entry ;
				if(size>1)
					Arrays.sort(resultArray, 0 , size , aComp);
			}
			else
			{
				if(aComp.compare(resultArray[size-1] , entry)>0)
				{
					resultArray[size-1] = entry ;
					if(size>1)
						Arrays.sort(resultArray, aComp);
				}
			}
		}
		return resultArray ;
	}
	
	public static <K> int get(Map<K, AtomicInteger> aMap , K aKey , int aDefaultValve)
	{
		if(aMap == null)
			return aDefaultValve ;
		AtomicInteger v = aMap.get(aKey) ;
		return v == null ? aDefaultValve : v.get() ;
	}
	
	public static <E> Queue<E> circularQueue(int aSize)
	{
		return new CircularFifoQueue<E>(aSize) ;
	}
	
	public static <K , V> Map<K, V> autoCleanHashMap_idle(int aMinutes)
	{
		return AutoCleanHashMap.withExpired_Idle(aMinutes) ;
	}
	
	public static <E> BlockingQueue<E> blockingQueue_linked()
	{
		return new LinkedBlockingQueue<E>() ;
	}
	
	public static void addAll(List<Integer> aList , int...aEles)
	{
		if(aEles != null && aEles.length>0)
		{
			for(int ele : aEles)
				aList.add(ele) ;
		}
	}
	
	/**
	 * 
	 * @param aList
	 * @param aEles		已防null
	 */
	@SuppressWarnings("unchecked")
	public static <T> void addAll(Collection<T> aList , T...aEles)
	{
		if(aEles != null && aEles.length>0)
		{
			if(aEles.length == 1 && aEles[0] == null)
				return ;
			for(T ele : aEles)
				aList.add(ele) ;
		}
	}
	
	/**
	 * 
	 * @param aList
	 * @param aEles
	 * @param aFrom			包含
	 * @param aTo			不包含
	 */
	public static <T> void addAll(Collection<T> aList , T[] aEles , int aFrom , int aTo)
	{
		if(aEles != null && aEles.length>0)
		{
			if(aEles.length == 1 && aEles[0] == null)
				return ;
			aFrom = Math.max(0, aFrom) ;
			aTo = Math.min(aEles.length, aTo) ; 
			for(int i=aFrom ; i<aTo ; i++)
				aList.add(aEles[i]) ;
		}
	}
	
	public static <T> void addAll(Collection<T> aList , Enumeration<T> aEles)
	{
		if(aEles == null)
			return ;
		while(aEles.hasMoreElements())
			aList.add(aEles.nextElement()) ;
	}
	
	public static <T> void addAll(Collection<T> aList , Iterable<? extends T> aEles)
	{
		if(aEles == null)
			return ;
		for(T ele : aEles)
			aList.add(ele) ;
	}
	
	public static <T> void addAll(Collection<T> aList , Iterator<T> aEles)
	{
		if(aEles == null)
			return ;
		while(aEles.hasNext())
			aList.add(aEles.next()) ;
	}
	
	public static <T> void addAll(Collection<T> aList , Iterator<T> aEles , Predicate<T> aPred)
	{
		if(aEles == null)
			return ;
		if(aPred == null)
			addAll(aList, aEles) ;
		else
		{
			while(aEles.hasNext())
			{
				T ele = aEles.next() ;
				if(aPred.test(ele))
					aList.add(aEles.next()) ;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> void addAllNotNull(Collection<T> aList , T...aEles)
	{
		if(aEles != null && aEles.length>0)
		{
			if(aEles.length == 1 && aEles[0] == null)
				return ;
			for(T ele : aEles)
				if(ele != null)
					aList.add(ele) ;
		}
	}
	
	public static boolean isEmpty(Collection<?> aList)
	{
		return aList==null||aList.size() == 0 ;
	}
	
	public static boolean isEmpty(SizeIter<?> aIt)
	{
		return aIt==null||aIt.isEmpty() ;
	}
	
	public static boolean isNotEmpty(Collection<?> aCollection)
	{
		return !isEmpty(aCollection) ;
	}
	
	public static boolean isNotEmpty(JSONArray aJa)
	{
		return !isEmpty(aJa) ;
	}
	
	@SuppressWarnings("rawtypes")
	public static boolean isNotEmpty(IMultiMap aMap)
	{
		return !isEmpty(aMap) ;
	}
	
	@SuppressWarnings("rawtypes")
	public static boolean isEmpty(IMultiMap aMap)
	{
		return aMap == null || aMap.isEmpty() ;
	}
	
	@SuppressWarnings("rawtypes")
	public static boolean isNotEmpty(Map aMap)
	{
		return aMap != null && aMap.size()>0 ;
	}
	
	@SuppressWarnings("rawtypes")
	public static boolean isEmpty(Map aMap)
	{
		return aMap == null || aMap.size() == 0 ;
	}
	
	/**
	 * 数组不为null且数组长度大于0
	 * @param aArray
	 * @return
	 */
	public static boolean isNotEmpty(byte[] aArray)
	{
		return !isEmpty(aArray) ;
	}
	
	/**
	 * 数据不为null且数组的长度大于0
	 * @param aArray
	 * @return
	 */
	public static boolean isNotEmpty(Object[] aArray)
	{
		return !isEmpty(aArray) ;
	}
	
	/**
	 * 数组为null或者数组长度为0，或者数组长度为1，但是第一个元素是null
	 * @param aArray
	 * @return
	 */
	public static <T> boolean isEmpty(T[] aArray)
	{
		return aArray==null || aArray.length == 0 || (aArray.length == 1 && aArray[0] == null) ;
	}
	
	public static boolean isEmpty(byte[] aArray)
	{
		return aArray == null || aArray.length == 0 ;
	}
	
	public static boolean isEmpty(int[] aArray)
	{
		return aArray == null || aArray.length == 0 ;
	}
	
	/**
	 * 数组为{@code null}或者长度为0
	 * @param aChs
	 * @return
	 */
	public static boolean isEmpty(char[] aChs)
	{
		return aChs == null || aChs.length == 0 ;
	}
	
	public static <T> T get(T[] aArray , int aIndex)
	{
		if(aArray != null && aIndex>=0 && aIndex<aArray.length)
			return aArray[aIndex] ;
		else
			return null ;
	}
	
	public static Object get(Object aArray , int aIndex)
	{
		if(aArray == null)
			return null ;
		if(aArray.getClass().isArray())
		{
			if(aIndex>=0 && aIndex<Array.getLength(aArray))
				return Array.get(aArray, aIndex) ;
			else
				return null ;
		}
		else
			return aIndex==0?aArray:null ;
	}
	
	public static <T> T getFirst(List<T> aColl)
	{
		if(isEmpty(aColl))
			return null ;
		return aColl.get(0) ;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T getFirst(Collection<T> aColl)
	{
		if(isEmpty(aColl))
			return null ;
		if(aColl instanceof List)
			return (T) ((List)aColl).get(0) ;
		return aColl.iterator().next() ;
	}
	
	public static <T> T getFirst(SizeIter<T> aColl)
	{
		if(isEmpty(aColl))
			return null ;
		return aColl.iterator().next() ;
	}
	
	public static <T> T getFirst(Iterable<T> aColl)
	{
		if(aColl == null)
			return null ;
		return aColl.iterator().next() ;
	}
	
	
	public static <T> T getLast(T[] aArray)
	{
		return aArray == null?null:aArray[aArray.length-1] ;
	}
	
	public static <T> T getLast(List<T> aColl)
	{
		if(isEmpty(aColl))
			return null ;
		return aColl.get(aColl.size()-1) ;
	}
	
	public static boolean isNotEmpty(float[] aArray)
	{
		return aArray != null && aArray.length>0 ;
 	}
	
	public static boolean isNotEmpty(int[] aArray)
	{
		return aArray != null && aArray.length>0 ;
	}
	
	public static boolean isArray(Object aObj)
	{
		return aObj != null && aObj.getClass().isArray() ;
	}
	
	/**
	 * 
	 * @param aChs0		被测试字符数组
	 * @param aChs1		匹配字符
	 * @return
	 */
	public static boolean endWith(char[] aChs0 , int aEndTo , char[] aChs1)
	{
		if(aEndTo < aChs1.length)
			return false ;
		for(int i=1 ; i<=aChs1.length ; i++)
		{
			if(aChs0[aEndTo-i] != aChs1[aChs1.length-i])
				return false ;
		}
		return true ;
	}
	
	public static <E> List<E> emptyList()
	{
		return Collections.emptyList() ;
	}
	
	public static <T , R> ArrayList<R> extractAsArrayList(T[] aArray , Function<T, R> aFunc)
	{
		ArrayList<R> list = new ArrayList<>() ;
		if(aArray != null)
		{
			for(T ele : aArray)
			{
				R r = aFunc.apply(ele) ;
				if(r != null)
					list.add(r) ;
			}
		}
		return list ;
	}
	
	public static <T , R> ArrayList<R> extractAsArrayList(Iterable<T> aIt , Function<T, R> aFunc)
	{
		ArrayList<R> list = new ArrayList<>() ;
		if(aIt != null)
		{
			for(T ele : aIt)
			{
				R r = aFunc.apply(ele) ;
				if(r != null)
					list.add(r) ;
			}
		}
		return list ;
	}
	
	/**
	 * 
	 * @param aCollection
	 * @param aFunc
	 * @return			返回的数组必然不为null，数组长度和aCollection长度相同，它们之间存在一一对应关系	<br>
	 * 			<b>注意：</b>返回的数组中可能包含null
	 */
	@SuppressWarnings("unchecked")
	public static <T , R> R[] extract(Collection<T> aCollection , Function<T, R> aFunc , Class<R> aClass)
	{
		if(isEmpty(aCollection))
			return (R[]) Array.newInstance(aClass, 0) ;
		R[] array = (R[]) Array.newInstance(aClass, aCollection.size()) ;
		int i=0 ;
		for(T ele : aCollection)
		{
			array[i++] = aFunc.apply(ele) ;
		}
		return array ;
	}
	
	/**
	 * 
	 * @param aArray
	 * @param aFunc
	 * @return
	 */
	public static <T , R> R[] extract(T[] aArray , Function<T, R> aFunc , Class<R> aType)
	{
		@SuppressWarnings("unchecked")
		R[] array = (R[]) Array.newInstance(aType, aArray.length) ;
		int i=0 ;
		for(T ele : aArray)
			array[i++] = aFunc.apply(ele) ;
		return array ;
	}
	
	public static <T , R , C extends Collection<R>> C extract(T[] aArray , Function<T, R> aFunc , Supplier<C> aSupplier)
	{
		C c = aSupplier.get() ;
		if(isNotEmpty(aArray))
		{
			for(T ele : aArray)
			{
				R r = aFunc.apply(ele) ;
				if(r != null)
					c.add(r) ;
			}
		}
		return c ;
	}
	
	public static <T> int[] extract(T[] aArray , Function<T, Integer> aFunc , int aDefaultVal)
	{
		int[] array = new int[aArray.length] ;
		int i=0 ;
		for(T ele : aArray)
		{
			Integer v = aFunc.apply(ele) ;
			array[i++] = v == null? aDefaultVal:v.intValue() ;
		}
		return array ;
	}
	
	public static <R> R[] extract(int[] aArray , Function<Integer, R> aFunc , Class<R> aType)
	{
		@SuppressWarnings("unchecked")
		R[] array = (R[]) Array.newInstance(aType, aArray.length) ;
		int i=0 ;
		for(int ele : aArray)
			array[i++] = aFunc.apply(ele) ;
		return array ;
	}
	
	public static <T> T[] extractNotNull(T[] aArray)
	{
		if(isEmpty(aArray))
			return aArray ; 
		for(int i=0 ; i<aArray.length ; i++)
		{
			if(aArray[i] == null)
			{
				int nullCount = 1 ;
				for(int j=i+1 ; j<aArray.length ; j++)
				{
					if(aArray[j] == null)
						nullCount++ ;
				}
				T[] newA = Arrays.copyOf(aArray, aArray.length-nullCount) ;
				int k = i ;
				for(int j=i+1 ; j<aArray.length ; j++)
				{
					if(aArray[j] != null)
						newA[k++] = aArray[j] ;
				}
				return newA ;
			}
		}
		return aArray ;
	}
	
	public static <T , R> List<R> extractNotNull(T[] aArray , Function<T, R> aFunc)
	{
		List<R> list = new ArrayList<>() ;
		for(T ele : aArray)
		{
			R r = aFunc.apply(ele) ;
			if(r != null)
				list.add(r) ;
		}
		return list ;
	}
	
	public static <T , R> void extractNotNull(T[] aArray , Function<T, R> aFunc , Collection<R> aTargetList)
	{
		for(T ele : aArray)
		{
			R r = aFunc.apply(ele) ;
			if(r != null)
				aTargetList.add(r) ;
		}
	}
	
	public static <T> void extract(T[] aArray , Predicate<T> aPredicate , Collection<T> aTargetList)
	{
		for(T ele : aArray)
		{
			if(aPredicate.test(ele))
				aTargetList.add(ele) ;
		}
	}
	
	public static <T , R> ArrayList<R> extractAsArrayList(T[] aArray , Predicate<T> aPredicate
			 , Function<T, R> aFunc)
	{
		ArrayList<R> list = arrayList() ;
		for(T ele : aArray)
		{
			if(aPredicate.test(ele))
			{
				list.add(aFunc.apply(ele)) ;
			}
		}
		return list ;
	}
	
	/**
	 * 该方法用于从一个给定的集合（aCollection）中提取元素，并对每个元素应用一个指定的函数（aFunc），
	 * 然后将函数处理后的结果添加到另一个集合（aTarget）中。
	 * 
	 * @param <T>
	 * @param <R>
	 * @param aCollection	需要进行元素提取和转换的源集合。
	 * @param aFunc			一个函数，用于将源集合中的元素（类型为T）转换为另一种类型（类型为R）的元素。
	 * @param aTarget		目标集合，用于存储函数处理后的结果。
	 */
	public static <T , R> void extract(Collection<T> aCollection , Function<T, R> aFunc , Collection<R> aTarget)
	{
		if(isNotEmpty(aCollection))
		{
			for(T ele : aCollection)
				aTarget.add(aFunc.apply(ele)) ;
		}
	}
	
	public static <T> Collection<T> extract(T[] aArray , Predicate<T> aPredicate)
	{
		List<T> list = new ArrayList<>() ;
		extract(aArray, aPredicate, list);
		return list ;
	}
	
	public static <T> void extract(T[] aArray , Predicate<T> aPredicate , Collection<T> aTargetList
			, Collection<T> aExcludeEles)
	{
		if(aExcludeEles == null)
			extract(aArray, aPredicate, aTargetList);
		else
		{
			for(T ele : aArray)
			{
				if(aPredicate.test(ele))
					aTargetList.add(ele) ;
				else
					aExcludeEles.add(ele) ;
			}
		}
	}
	
	/**
	 * 移除从aBeginj开始的所有元素
	 * @param aBegin
	 * @return
	 */
	public static <T> Object[] remove(List<T> aList , int aBegin)
	{
		if(aList.size()>aBegin && aBegin>=0)
		{
			Object[] removed = aList.subList(aBegin, aList.size()).toArray() ;
			Iterator<?> it = aList.listIterator(aBegin) ;
			while(it.hasNext())
			{
				it.next() ;
				it.remove() ;
			}
			return removed ;
		}
		return null ;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] remove(T[] aArray , Predicate<T> aPredicate)
	{
		if(aArray == null)
			return null ;
		LinkedList<Integer> indexes = new LinkedList<Integer>() ;
		for(int i=0 ; i<aArray.length ; i++)
		{
			if(aPredicate.test(aArray[i]))
				indexes.add(i) ;
		}
		if(indexes.size()>0)
		{
			T[] newArray = null ;
			if(indexes.size() != aArray.length)
			{
				newArray = (T[])Array.newInstance(aArray.getClass().getComponentType()
						, aArray.length-indexes.size()) ;
				int index = 0 ;
				int excludeSeq = indexes.poll() ; 
				for(int i=0 ; i<aArray.length ; i++)
				{
					if(i != excludeSeq)
						newArray[index++] = aArray[i] ;
					else if(!indexes.isEmpty())
						excludeSeq = indexes.poll() ;
				}
			}
			return newArray ;
		}
		else
			return aArray ;
	}
	

	public static <T> T[] remove(T[] aArray , Set<T> aEleSet)
	{
		return remove(aArray, aEleSet::contains) ;
 	}
	
	/**
	 * 如果源数组中存在被期望移除的元素，则构建一个不包含被移除元素的新数组。
	 * 如果源数组中不存在被期望移除的元素，则返回源数组
	 * 此操作不破坏源数组
	 * @param <T>
	 * @param aArray			可以为null
	 * @param aEles
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] remove(T[] aArray , T...aEles)
	{
		return remove(aArray, (ele)->contains(aEles, ele)) ;
	}
	
	public static void removeAll(Map<? , ?> aMap , Collection<?> aKeys)
	{
		if(isNotEmpty(aMap)
				&& isNotEmpty(aKeys))
		{
			for(Object key : aKeys)
			{
				aMap.remove(key) ;
			}
		}
	}
	
	public static void removeAll(Set<?> aSet , Object[] aObjs)
	{
		if(isNotEmpty(aObjs))
		{
			for(Object obj : aObjs)
				aSet.remove(obj) ;
		}
	}
	
	/**
	 * 在指定地数组上直接移除指定元素
	 * @param aArray
	 * @param aIndex
	 * @return
	 */
	public static <T> T removeDirect(T[] aArray , int aIndex)
	{
		if(aArray != null && aIndex>=0 && aIndex<aArray.length)
		{
			T oldVal = aArray[aIndex] ;
			System.arraycopy(aArray, aIndex+1 , aArray, aIndex , aArray.length-aIndex-1) ;
			aArray[aArray.length-1] = null ;
			return oldVal ;
		}
		return null ;
	}
	
	public static <T> T removeLast(List<T> aColl)
	{
		if(isEmpty(aColl))
			return null ;
		return aColl.remove(aColl.size()-1) ;
	}
	
	public static int countNotNull(Object[] aArray)
	{
		int count = 0 ;
		if(aArray != null && aArray.length>0)
			for(Object obj : aArray)
				if(obj != null) count++ ;
		return count ;
	}
	
	public static int countNotEmpty(String[] aArray)
	{
		if(isNotEmpty(aArray))
		{
			final int len = aArray.length ;
			int count = 0 ;
			for(int i=0 ; i<len ; i++)
			{
				if(XString.isNotEmpty(aArray[i]))
					count++ ;
			}
			return count ;
		}
		return 0 ;
	}
	
	public static int count(Object[] aArray)
	{
		return aArray==null?0:aArray.length ;
	}
	
	public static int count(int[] aArray)
	{
		return aArray==null?0:aArray.length ;
	}
	
	public static int count(Object[] aArray , boolean aIgnoreOnlyOneNull)
	{
		if(aIgnoreOnlyOneNull && aArray != null && aArray.length == 1 && aArray[0] == null)
		{
			return 0 ;
		}
		else
			return aArray==null?0:aArray.length ;
	}
	
	public static int count(byte[] aArray)
	{
		return aArray==null?0:aArray.length ;
	}
	
	public static int count(double[] aArray)
	{
		return aArray==null?0:aArray.length ;
	}
	
	public static <T> int count(Collection<T> aList)
	{
		return aList == null?0:aList.size() ;
	}
	
	@SuppressWarnings("rawtypes")
	public static <T> int count(Map aMap)
	{
		return aMap == null?0:aMap.size() ;
	}
	
	public static int count(JSONArray aJa)
	{
		return aJa==null?0:aJa.size() ;
	}
	
	/**
	 * 剔除重复的元素
	 * @param <T>
	 * @param aArray
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] cutRepeated(T[] aArray)
	{
		if(isEmpty(aArray))
			return aArray ;
		LinkedHashSet<T> linkedSet = linkedHashSet(aArray) ;
		return linkedSet.size() == aArray.length?aArray 
				:linkedSet.toArray((T[]) Array.newInstance(aArray.getClass().getComponentType() , linkedSet.size())) ;
	}
	
	/**
	 * 清除null元素
	 * @param <T>
	 * @param aArray
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] cleanNull(T[] aArray)
	{
		int len = countNotNull(aArray) ;
		if(len != aArray.length)
		{
			T[] newArray = (T[])Array.newInstance(aArray.getClass().getComponentType()
					, len) ;
			int index = 0 ;
			for(T ele : aArray)
			{
				if(ele != null)
					newArray[index++] = ele ;
			}
			return newArray ;
		}
		return aArray ;
	}
	
	public static <T> T[] cloneArray(T[] aArray)
	{
		return aArray != null?Arrays.copyOf(aArray , aArray.length):null ;
	}
	
	/**
	 * 深度克隆，会调用数组元素的克隆方法
	 * @param <T>
	 * @param aDest
	 * @param aSrc
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> boolean deepClone(Collection<T> aDest , Collection<T> aSrc)
	{
		Method cloneMethod = null ;
		for(T tobj : aSrc)
		{
			if(cloneMethod == null)
			{
				try
				{
					cloneMethod = tobj.getClass().getMethod("clone") ;
					if(!Bits.hit(cloneMethod.getModifiers() , Method.PUBLIC)) 
						return false ;
				}
				catch(Exception e)
				{
					return false ;
				}
			}
			try
			{
				aDest.add((T)cloneMethod.invoke(tobj)) ;
			}
			catch(Exception e)
			{
				return false ;
			}
		}
		return true ;
	}
	
	/**
	 * 
	 * @param aSrc			已防null
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> ArrayList<T> deepCloneArrayList(List<T> aSrc)
	{
		if(aSrc == null)
			return null ;
		ArrayList<T> list = new ArrayList<T>() ;
		Method cloneMethod = null ;
		for(T tobj : aSrc)
		{
			if(cloneMethod == null)
			{
				try
				{
					cloneMethod = tobj.getClass().getMethod("clone") ;
				}
				catch(Exception e)
				{
					return null ;
				}
			}
			try
			{
				list.add((T)cloneMethod.invoke(tobj)) ;
			}
			catch(Exception e)
			{
				return null ;
			}
		}
		return list ;
	}
	
	/**
	 * 将aArrays合并成 类型为aType的数组
	 * 注意aType的内容不会参与合并，只是表示类型,意义和toArray的参数T[]相同
	 * @param <T>
	 * @param aType		和toArray的参数T[]相同	
	 * @param aArrays
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] mergeArrays(T[] aType ,Object[]...aArrays)
	{
		int len = 0 ;
		for(Object[] array : aArrays)
			if(array != null) len += array.length ;
		T[] copy = null ;
		if(aType.length <len)
		{
			copy = ((Object)aType == (Object)Object[].class)
            ? (T[]) new Object[len]
            : (T[]) Array.newInstance(aType.getClass().getComponentType(),len);
		}
		else if(aType.length == len)
			copy = aType ;
		else if(aType.length>len)
		{
			copy = aType ;
			copy[len] = null ;
		}
		int index = 0 ;
		for(Object[] array : aArrays)
		{
			if(array != null)
			{
				System.arraycopy(array, 0, copy, index, array.length) ;
				index += array.length ;
			}
		}
		return copy ;
	}
	
	/**
	 * 将aArray和aObjs数组合并，得到一个新的数组
	 * @param aArray		不能为null
	 * @param aType			合并后数组的类型。类似Collection的toArray方法。
	 *   <ul>
	 *     <li>当合并后新数组的长度大于给定的数组aType的长度，将构造一个指定类型的新数组</li>
	 *     <li>如果两者相等，将把数据填充如aType数组中</li>
	 *     <li>如果指定的数组aType长度更大，那么将把数据填充入aType数组中，紧接着数据尾部的空闲位置将被置null</li>
	 *   </ul>
	 * @param aObjs			
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] merge(Object[] aArray ,T[] aType , Object...aObjs)
	{
		int len = aArray.length+aObjs.length ;
		if(aType.length<len)
			aType = (T[]) Arrays.copyOf(aArray, len, aType.getClass());
		else if(aType.length == len)
			System.arraycopy(aArray, 0, aType, 0, len);
		else if (aType.length > len)
			aType[len] = null;
		System.arraycopy(aObjs, 0, aType, aArray.length, aObjs.length) ;
		return aType ;
	}
	
	/**
	 * 数组合并，没有考虑元素重复
	 * @param <T>
	 * @param aArray		可以为null
	 * @param aObjs
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] merge(T[] aArray , T...aObjs)
	{
		if(aArray != null && aObjs != null && aObjs.length>0)
		{
			int len = aArray.length+aObjs.length ;
			T[] aType = (T[]) Arrays.copyOf(aArray, len, aArray.getClass());
			System.arraycopy(aObjs, 0, aType, aArray.length, aObjs.length) ;
			return aType ;
		}
		else if(aArray != null)
			return aArray ;
		else return aObjs ;
	}
	
	public static int[] merge(int[] aArray , int...aObjs)
	{
		if(aArray != null && aObjs != null && aObjs.length>0)
		{
			int len = aArray.length+aObjs.length ;
			int[] aType = Arrays.copyOf(aArray, len) ;
			System.arraycopy(aObjs, 0, aType, aArray.length, aObjs.length) ;
			return aType ;
		}
		else if(aArray != null)
			return aArray ;
		else return aObjs ;
	}
	
	public static byte[] merge(byte[] aArray , byte...aObjs)
	{
		if(aArray != null && aObjs != null && aObjs.length>0)
		{
			int len = aArray.length+aObjs.length ;
			byte[] aType = Arrays.copyOf(aArray, len) ;
			System.arraycopy(aObjs, 0, aType, aArray.length, aObjs.length) ;
			return aType ;
		}
		else if(aArray != null)
			return aArray ;
		else return aObjs ;
	}
	
	public static float[] merge(float[] aArray , float...aObjs)
	{
		if(aArray != null && aObjs != null && aObjs.length>0)
		{
			int len = aArray.length+aObjs.length ;
			float[] aType = Arrays.copyOf(aArray, len) ;
			System.arraycopy(aObjs, 0, aType, aArray.length, aObjs.length) ;
			return aType ;
		}
		else if(aArray != null)
			return aArray ;
		else return aObjs ;
	}
	
	public static double[] merge(double[] aArray , double...aObjs)
	{
		if(aArray != null && aObjs != null && aObjs.length>0)
		{
			int len = aArray.length+aObjs.length ;
			double[] aType = Arrays.copyOf(aArray, len) ;
			System.arraycopy(aObjs, 0, aType, aArray.length, aObjs.length) ;
			return aType ;
		}
		else if(aArray != null)
			return aArray ;
		else return aObjs ;
	}
	
	/**
	 * 合并并且清理，合并后的数组中，不会有重复的元素
	 * @param <T>
	 * @param aArray
	 * @param aEles
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] mergeAndClean(T[] aArray , T...aEles )
	{
		if(aArray == null)
			return cutRepeated(aEles) ;
		else
		{
			List<T> list = linkedList(aArray) ;
			cutRepeated(list) ;
			for(T ele : aEles)
				if(!list.contains(ele)) list.add(ele) ;
			T[] newArray = (T[])Array.newInstance(aArray.getClass().getComponentType()
					, list.size()) ;
			return list.toArray(newArray) ;
		}
	}
	
	/**
	 * 剔除重复的
	 * @param aList
	 */
	public static void cutRepeated(List<?> aList)
	{
		int index = 0 ;
		while(index < aList.size()-1)
		{
			Object target = aList.get(index++) ;
			ListIterator<?> it = aList.listIterator(index) ;
			while(it.hasNext())
			{
				if(JCommon.equals(it.next(), target))
					it.remove() ;
			}
		}
	}
	
	public static <T> void cutRepeated(List<T> aList , Comparator<T> aComparator)
	{
		int index = 0 ;
		while(index < aList.size()-1)
		{
			T target = (T)aList.get(index++) ;
			ListIterator<T> it = aList.listIterator(index) ;
			while(it.hasNext())
			{
				if(aComparator.compare(it.next(), target) == 0)
					it.remove() ;
			}
		}
	}
	
	public static String[] mergeAndClean(String[] aArray , String...aEles )
	{
		if(aArray == null)
			return linkedHashSet(aEles).toArray(JCommon.sEmptyStringArray) ;
		else
		{
			LinkedHashSet<String> set = linkedHashSet(aArray) ;
			if(isNotEmpty(aEles))
			{
				for(String ele : aEles)
					set.add(ele) ;
			}
			return set.toArray(JCommon.sEmptyStringArray) ;
		}
	}
	
	public static <E> ICircularBoundedQueue<E> synchronizedQueue(ICircularBoundedQueue<E> aQueue)
	{
		return new SyncCircularBoundedQueue<>(aQueue) ;
	}
	
	public static <E> IBoundedList<E> synchronizedList(IBoundedList<E> aList)
	{
		return new SyncBoundedArrayList<>(aList) ;
	}
	
	/**
	 * 用于从给定的源数组 aSource 中提取一个子数组。它允许用户指定起始索引 aFrom 和最大长度 aMaxLen，以决定子数组的范围和大小。
	 * 如果源数组为 null，则直接返回 null。此方法支持泛型，因此可以应用于任何类型的数组。
	 * @param <T>
	 * @param aSource
	 * @param aFrom
	 * @param aMaxLen	最大长度，表示子数组的最大长度。如果 aFrom + aMaxLen 超过了源数组的长度，
	 * 				则实际提取的子数组长度将调整为从 aFrom 开始到源数组末尾的长度。
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] subArray(T[] aSource , int aFrom , int aMaxLen)
	{
		if(aSource == null)
			return null ;
		int len = Math.min(aMaxLen , aSource.length-aFrom) ;
		T[] array = (T[])Array.newInstance(aSource.getClass().getComponentType() 
				, len) ;
		System.arraycopy(aSource, aFrom , array , 0 , len);
		return array ;
	}
	
	/**
	 * 
	 * @param <T>
	 * @param aSource
	 * @param aFrom
	 * @param aTo			不包含
	 * @return
	 */
	public static <T> T[] copyRange(T[] aSource , int aFrom , int aTo)
	{
		return subArray(aSource, aFrom, aTo-aFrom) ;
	}
	
	static class SyncCircularBoundedQueue<E> extends SyncBoundedCollection<E> implements ICircularBoundedQueue<E>
	{

		ICircularBoundedQueue<E> mQ ;
		Object mMutex = new Object() ;
		
		public SyncCircularBoundedQueue(ICircularBoundedQueue<E> aQ)
		{
			super(aQ) ;
			mQ = aQ ;
		}

		@Override
		public E[] poll(int aLen, Class<?> aComponentClass)
		{
			synchronized (mMutex)
			{
				return mQ.poll(aLen, aComponentClass) ;
			}
		}

		@Override
		public E get(int aIndex)
		{
			synchronized (mMutex)
			{
				return mQ.get(aIndex) ;
			}
		}

		@Override
		public E[] get(int aOffset, int aLen, Class<?> aComponentType)
		{
			synchronized (mMutex)
			{
				return mQ.get(aOffset, aLen, aComponentType) ;
			}
		}

		@Override
		public boolean offer(E aE)
		{
			synchronized(mMutex)
			{
				return mQ.offer(aE) ;
			}
		}

		@Override
		public E remove()
		{
			synchronized(mMutex)
			{
				return mQ.remove() ;
			}
		}

		@Override
		public E poll()
		{
			synchronized (mMutex)
			{
				return mQ.poll() ;
			}
		}

		@Override
		public E element()
		{
			synchronized (mMutex)
			{				
				return mQ.element() ;
			}
		}

		@Override
		public E peek()
		{
			synchronized (mMutex)
			{				
				return mQ.peek() ;
			}
		}
		
	}
	
	static class SyncBoundedArrayList<E> extends SyncBoundedCollection<E> implements IBoundedList<E>
	{

		IBoundedList<E> mList ;
		
		public SyncBoundedArrayList(IBoundedList<E> aList)
		{
			super(aList) ;
			mList = aList ;
		}

		@Override
		public boolean addAll(int aIndex, Collection<? extends E> aC)
		{
			synchronized (mMutex)
			{				
				return mList.addAll(aIndex , aC) ;
			}
		}

		@Override
		public E get(int aIndex)
		{
			synchronized (mMutex)
			{
				return mList.get(aIndex) ;
			}
		}

		@Override
		public E set(int aIndex, E aElement)
		{
			synchronized (mMutex)
			{
				return mList.set(aIndex, aElement) ;
			}
		}

		@Override
		public void add(int aIndex, E aElement)
		{
			synchronized (mMutex)
			{
				mList.set(aIndex, aElement) ;
			}
		}

		@Override
		public E remove(int aIndex)
		{
			synchronized (mMutex)
			{
				return mList.remove(aIndex) ;
			}
		}

		@Override
		public int indexOf(Object aO)
		{
			synchronized (mMutex)
			{
				return mList.indexOf(aO) ;
			}
		}

		@Override
		public int lastIndexOf(Object aO)
		{
			synchronized (mMutex)
			{
				return mList.lastIndexOf(aO) ;
			}
		}

		@Override
		public ListIterator<E> listIterator()
		{
			synchronized (mMutex)
			{
				return mList.listIterator() ;
			}
		}

		@Override
		public ListIterator<E> listIterator(int aIndex)
		{
			synchronized (mMutex)
			{
				return mList.listIterator(aIndex) ;
			}
		}

		@Override
		public List<E> subList(int aFromIndex, int aToIndex)
		{
			synchronized (mMutex)
			{
				return mList.subList(aFromIndex, aToIndex) ;
			}
		}
		
		@Override
		public E getLast()
		{
			synchronized (mMutex)
			{
				return mList.getLast() ;
			}
		}
		
		@Override
		public E getFirst()
		{
			synchronized (mMutex)
			{
				return mList.getFirst() ;
			}
		}
		
		@Override
		public E removeFirst()
		{
			synchronized (mMutex)
			{
				return mList.removeFirst() ;
			}
		}
		
	}
}
