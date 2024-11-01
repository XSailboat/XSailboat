package team.sailboat.commons.fan.struct;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.JCommon;

public class Tuples
{
	public static class T2<E1 , E2> implements Serializable  , Entry<E1, E2>
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		
		protected E1 mE1 ;
		
		protected E2 mE2 ;
		
		public T2(E1 aE1 , E2 aE2)
		{
			mE1 = aE1 ;
			mE2 = aE2 ;
		}
		
		public T2()
		{
		}
		
		public void set(E1 aE1 , E2 aE2)
		{
			mE1 = aE1 ;
			mE2 = aE2 ;
		}

		public E1 getEle_1()
		{
			return mE1 ;
		}
		
		public E2 getEle_2(){
			return mE2 ;
		}
		
		public void setEle_1(E1 aE1)
		{
			mE1 = aE1 ;
		}
		
		public void setEle_2(E2 aE2)
		{
			mE2 = aE2 ;
		}

		@Override
		public boolean equals(Object obj)
		{
			if(obj instanceof T2)
			{
				@SuppressWarnings("rawtypes")
				T2 t2 = (T2)obj ;
				return JCommon.equals(t2.getEle_1() , getEle_1())
						&& JCommon.equals(t2.getEle_2(), getEle_2()) ;
			}
			return super.equals(obj);
		}
		
		@Override
		public String toString()
		{
			return JCommon.toString(mE1)+","+JCommon.toString(mE2) ;
		}

		@Override
		public E1 getKey()
		{
			return mE1 ;
		}

		@Override
		public E2 getValue()
		{
			return mE2 ;
		}

		@Override
		public E2 setValue(E2 aValue)
		{
			mE2 = aValue ;
			return mE2 ;
		}
		
		public static <K,V> V getValue(List<Entry<K, V>> aList , K aKey)
		{
			for(Entry<K, V> entry : aList)
				if(JCommon.equals(entry.getKey(), aKey)) return entry.getValue() ;
			return null ;
		}
		
		public static <K,V> K getKey(List<? extends Entry<K, V>> aList , V aValue)
		{
			for(Entry<K, V> entry : aList)
				if(JCommon.equals(entry.getValue(), aValue)) return entry.getKey() ;
			return null ;
		}
		
		public static <K,V> List<V> getValues(List<? extends Entry<K, V>> aList , K aKey)
		{
			List<V> list = new ArrayList<>() ;
			for(Entry<K, V> entry : aList)
			{
				if(JCommon.equals(entry.getKey(), aKey))
				{
					list.add(entry.getValue()) ;
				}
			}
			return list ;
		}
		
		public static <K,V> List<V> getValues(List<? extends Entry<K, V>> aList)
		{
			List<V> list = new ArrayList<>() ;
			for(Entry<K, V> entry : aList)
			{
				list.add(entry.getValue()) ;
			}
			return list ;
		}
		
		/**
		 * 键或者值任何其中一个和aObj相等，都返回true
		 * @param aList
		 * @param aObj
		 * @return
		 */
		public static <K , V> boolean has(List<Entry<K, V>> aList , Object aObj)
		{
			for(Entry<K, V> entry : aList)
			{
				if(JCommon.equals(entry.getKey(), aObj) 
						|| JCommon.equals(entry.getValue(), aObj))
					return true ;
			}
			return false ;
		}
		
		public static <K , V> boolean hasKey(List<Entry<K, V>> aList , K aObj)
		{
			for(Entry<K, V> entry : aList)
			{
				if(JCommon.equals(entry.getKey(), aObj))
					return true ;
			}
			return false ;
		}
		
		public static <K , V> int indexKey(List<Entry<K, V>> aList , K aObj)
		{
			int i = 0 ;
			for(Entry<K, V> entry : aList)
			{
				if(JCommon.equals(entry.getKey(), aObj))
					return i ;
				i++ ;
			}
			return -1 ;
		}
		
		public static <K , V> boolean hasValue(List<Entry<K, V>> aList , V aObj)
		{
			for(Entry<K, V> entry : aList)
			{
				if(JCommon.equals(entry.getValue(), aObj))
					return true ;
			}
			return false ;
		}
		
		public static <K , V> boolean hasEntry(List<Entry<K, V>> aList , K aKey , V aValue
				, boolean aExacting)
		{
			for(Entry<K, V> entry : aList)
			{
				if(JCommon.equals(entry.getKey(), aKey) 
						&& JCommon.equals(entry.getValue(), aValue)
						||
						(!aExacting && JCommon.equals(entry.getKey(), aValue) 
						&& JCommon.equals(entry.getValue(), aKey)))
				{
					return true ;
				}
			}
			return false ;
		}
	}
	
	public static class T3<E1 , E2 , E3> implements Serializable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		
		protected E1 mE1 ;
		
		protected E2 mE2 ;
		
		protected E3 mE3 ;
		
		public T3(E1 aE1 , E2 aE2 , E3 aE3)
		{
			mE1 = aE1 ;
			mE2 = aE2 ;
			mE3 = aE3 ;
		}
		
		public T3()
		{
		}
		
		public void set(E1 aE1 , E2 aE2 , E3 aE3)
		{
			mE1 = aE1 ;
			mE2 = aE2 ;
			mE3 = aE3 ;
		}

		public E1 getEle_1()
		{
			return mE1 ;
		}
		
		public E2 getEle_2(){
			return mE2 ;
		}
		
		public void setEle_1(E1 aE1)
		{
			mE1 = aE1 ;
		}
		
		public void setEle_2(E2 aE2)
		{
			mE2 = aE2 ;
		}
		
		public E3 getEle_3()
		{
			return mE3 ;
		}
		public void setEle_3(E3 aE3)
		{
			mE3 = aE3 ;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof T3)
			{
				@SuppressWarnings("rawtypes")
				T3 t2 = (T3)obj ;
				return JCommon.equals(t2.getEle_1() , getEle_1())
						&& JCommon.equals(t2.getEle_2(), getEle_2())
						&& JCommon.equals(t2.getEle_3() , getEle_3()) ;
			}
			return super.equals(obj);
		}
		
		@Override
		public String toString()
		{
			return JCommon.toString(mE1)+","+JCommon.toString(mE2)+","+JCommon.toString(mE3) ;
		}
	}

	
	public static <E1 , E2> T2<E1, E2> of(E1 aE1 , E2 aE2)
	{
		return new T2<E1, E2>(aE1, aE2) ;
	}
	
	public static <E1 , E2 , E3> T3<E1 , E2 , E3> of(E1 aE1 , E2 aE2 , E3 aE3)
	{
		return new T3<>(aE1 , aE2 , aE3) ;
	}
	
	public static <E1,E2> List<E1> getEle_1s(List<T2<E1, E2>> aList)
	{
		List<E1> e1List = XC.arrayList() ;
		if(aList != null)
		{
			for(T2<E1, E2> t : aList)
				e1List.add(t.getEle_1()) ;
		}
		return e1List ;
	}
	
	public static <E1,E2> E2 getEle_2(List<T2<E1, E2>> aList , E1 aEle_1)
	{
		for(T2<E1, E2> entry : aList)
			if(JCommon.equals(entry.getEle_1(), aEle_1)) 
				return entry.getEle_2() ;
		return null ;
	}
	
	public static <E1,E2> E1 getEle_1(List<T2<E1, E2>> aList , E2 aEle_2)
	{
		for(T2<E1, E2> entry : aList)
			if(JCommon.equals(entry.getEle_2() , aEle_2))
				return entry.getEle_1() ;
		return null ;
	}
	
	public static <E1,E2> Collection<E2> getEle_2s(List<T2<E1, E2>> aList , E1 aE1)
	{
		List<E2> list = new ArrayList<>() ;
		for(T2<E1, E2> entry : aList)
		{
			if(JCommon.equals(entry.getEle_1() , aE1))
			{
				list.add(entry.getEle_2()) ;
			}
		}
		return list ;
	}
	
	/**
	 * 键或者值任何其中一个和aObj相等，都返回true
	 * @param aList
	 * @param aObj
	 * @return
	 */
	public static <K , V> boolean has(List<T2<K, V>> aList , Object aObj)
	{
		for(T2<K, V> entry : aList)
		{
			if(JCommon.equals(entry.getEle_1(), aObj) 
					|| JCommon.equals(entry.getEle_2(), aObj))
				return true ;
		}
		return false ;
	}
	
	public static <K , V> boolean hasEle_1(List<T2<K, V>> aList , K aObj)
	{
		for(T2<K, V> entry : aList)
		{
			if(JCommon.equals(entry.getEle_1(), aObj))
				return true ;
		}
		return false ;
	}
	
	public static <K , V> int indexEle_1(List<T2<K, V>> aList , K aObj)
	{
		int i = 0 ;
		for(T2<K, V> entry : aList)
		{
			if(JCommon.equals(entry.getEle_1(), aObj))
				return i ;
			i++ ;
		}
		return -1 ;
	}
	
	public static <K , V> boolean hasEle_2(List<T2<K, V>> aList , V aObj)
	{
		for(T2<K, V> entry : aList)
		{
			if(JCommon.equals(entry.getEle_2(), aObj))
				return true ;
		}
		return false ;
	}
	
	public static <K , V> boolean has(List<T2<K, V>> aList , K aKey , V aValue
			, boolean aExacting)
	{
		for(T2<K, V> entry : aList)
		{
			if(JCommon.equals(entry.getEle_1(), aKey) 
					&& JCommon.equals(entry.getEle_2(), aValue)
					||
					(!aExacting && JCommon.equals(entry.getEle_1(), aValue) 
					&& JCommon.equals(entry.getEle_2(), aKey)))
			{
				return true ;
			}
		}
		return false ;
	}
}
