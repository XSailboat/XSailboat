package team.sailboat.commons.fan.collection;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.struct.Tuples;

/**
 * <p>
 * 
 *
 * @author yyl
 * @since 2016年9月29日
 */
public class CLinkedHashMap<K , V> 
{
	static final int sPoleDist = 100 ;
	LinkedEntry<K , V> mHead ;
	LinkedEntry<K , V> mTail ;
	final HashMap<K, LinkedEntry<K , V>> mMap = XC.hashMap() ;
	final List<LinkedEntry<K , V>> mPoleList = XC.arrayList() ;
	
	public V get(Object aKey)
	{
		LinkedEntry<K , V> entry = mMap.get(aKey) ;
		return entry != null?entry.getValue():null ;
	}
	
	public V putIfAbsent(K aKey, V aValue)
	{
		LinkedEntry<K, V> entry = LinkedEntry.of(aKey, aValue, null) ;
		LinkedEntry<K, V> entry_1 = mMap.putIfAbsent(aKey, entry) ;
		if(entry_1 != null)
			return entry_1.getValue() ;
		mTail.mNext = entry ;
		entry.mPrev = mTail ;
		mTail = entry ;
		return null ;
	}

	public synchronized boolean remove(Object aKey, Object aValue)
	{
		LinkedEntry<K , V> entry = mMap.get(aKey) ;
		if(entry != null && JCommon.equals(entry.getValue() , aValue))
		{
			remove(aKey) ;
			return true ;
			
		}
		return false;
	}

	public synchronized V put(K aKey, V aValue)
	{
		LinkedEntry<K, V> entry = mMap.get(aKey) ;
		if(entry != null)
		{
			V oldValue = entry.getValue() ;
			entry.setValue(aValue) ;
			return oldValue ;
		}
		else
		{
			entry = LinkedEntry.of(aKey, aValue, mTail) ;
			mMap.put(aKey, entry) ;
			if(mTail == null)
			{
				Assert.isTrue(mHead == null) ;
				mHead = entry ;
			}
			mTail = entry ;
			return null ;
		}
	}

	public synchronized boolean replace(K aKey, V aOldValue, V aNewValue)
	{
		LinkedEntry<K, V> entry = mMap.get(aKey) ;
		if(entry != null && JCommon.equals(entry.getValue() , aOldValue))
		{
			entry.setValue(aNewValue) ;
			return true ;
		}
		return false ;
	}
	
	public V getFirst()
	{
		LinkedEntry<K , V> head = mHead ;
		if(head == null)
			return null ;
		else
			return head.getValue() ;
	}
	
	public synchronized List<Entry<K , V>> getN(int aFrom , int aLen)
	{
		int poleNum = aFrom/sPoleDist ;
		LinkedEntry<K, V> startEntry = null ;
		int dist ;
		if(mPoleList.isEmpty())
		{
			startEntry = mHead ;
			dist = aFrom ;
		}
		else if(poleNum>=mPoleList.size())
		{
			startEntry = XC.getLast(mPoleList) ;
			dist = aFrom - sPoleDist*mPoleList.size() ;
		}
		else
		{
			startEntry = poleNum>0?mPoleList.get(poleNum-1):mHead ;
			dist = aFrom - sPoleDist*poleNum ;
		}
		if(startEntry == null)
			return Collections.emptyList() ;
		LinkedEntry<K, V> entry = startEntry ;
		int remainAmount = aLen+dist ;
		List<Entry<K, V>> list = XC.arrayList(Math.min(aLen , 1000)) ;
		int offset = 0 ;
		while(entry != null && remainAmount>0)
		{
			if(offset++ == sPoleDist)
			{
				offset = 0 ;
				if(poleNum++ >= mPoleList.size())
					mPoleList.add(entry) ;
			}
			if(dist == 0)
				list.add(Tuples.of(entry.getKey() , entry.getValue())) ;
			else
				dist-- ;
			remainAmount-- ;
			entry = entry.mNext ;
		}
		return list ;
	}

	/**
	 * 
	 * @param aKey
	 * @param aValue
	 * @return
	 */
	public synchronized V replace(K aKey, V aValue)
	{
		LinkedEntry<K, V> entry = mMap.get(aKey) ;
		if(entry != null)
		{
			V oldValue = entry.getValue() ;
			entry.setValue(aValue) ;
			return oldValue ;
		}
		return null ;
	}

	public synchronized V remove(Object aKey)
	{
		LinkedEntry<K, V> entry = mMap.remove(aKey) ;
		if(entry != null)
		{
			mPoleList.clear();
			if(entry == mTail)
			{
				LinkedEntry<K, V> prev = entry.mPrev ;
				if(prev == null)
				{
					Assert.isTrue(entry == mHead) ;
					mHead = null ;
				}
				else
					prev.mNext = null ;
				mTail = prev ;
			}
			else if(entry == mHead)
			{
				LinkedEntry<K, V> next = entry.mNext ;
				if(next == null)
				{
					Assert.isTrue(entry == mTail) ;
					mTail = null ;
				}
				else
					next.mPrev = null ;
				mHead = next ;
			}
			else
			{
				LinkedEntry<K, V> prev = entry.mPrev ;
				LinkedEntry<K, V> next = entry.mNext ;
				prev.mNext = next ;
				next.mPrev = prev ;
			}
			entry.setKey(null) ;
			return entry.getValue() ;
		}
		return null ;
	}
	
	public int size()
	{
		return mMap.size() ;
	}
	
	public boolean isEmpty()
	{
		return mMap.isEmpty() ;
	}
	
	public void forEach(BiConsumer<? super K, ? super V> aAction)
	{
		LinkedEntry<K, V> entry = mHead ;
		if(entry != null)
		{
			while(entry != null)
			{
				if(entry.getKey() != null)
					aAction.accept(entry.getKey() ,entry.getValue()) ;
				entry = entry.mNext ;
			}
		}
	}
	
	public void forEachValues(Predicate<V> aPred)
	{
		LinkedEntry<K, V> entry = mHead ;
		if(entry != null)
		{
			while(entry != null)
			{
				if(entry.getKey() != null)
				{
					if(!aPred.test(entry.getValue()))
						return ;
				}
				entry = entry.mNext ;
			}
		}
	}
	
	static class LinkedEntry<K , V> extends Tuples.T2<K, V>
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		LinkedEntry<K,V> mNext;
		LinkedEntry<K,V> mPrev ;
		
		public LinkedEntry(K aKey , V aValue)
		{
			super(aKey, aValue) ;
		}
		
		public void setKey(K aKey)
		{
			setEle_1(aKey) ;
		}
		
		
		static <K , V> LinkedEntry<K , V> of(K aKey , V aValue , LinkedEntry<K , V> aPrev)
		{
			LinkedEntry<K, V> entry = new LinkedEntry<>(aKey, aValue) ;
			if(aPrev != null)
			{
				entry.mPrev = aPrev ;
				aPrev.mNext = entry ;
			}
			return entry ;
		}
		
	}
	
}
