package team.sailboat.commons.fan.collection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;

import team.sailboat.commons.fan.infc.BiIteratorPredicate;
import team.sailboat.commons.fan.infc.IterateOpCode;
import team.sailboat.commons.fan.lang.JCommon;

/**
 * 假定单个值的站多数
 *
 * @author yyl
 * @since 2018年11月7日
 */
public class HashMultiMap<K , V> implements IMultiMap<K, V>
{
	Map<K, Object> mMap ;
	
	public HashMultiMap()
	{
		mMap = new LinkedHashMap<>() ;
	}
	
	public HashMultiMap(IMultiMap<K, V> aMap)
	{
		this() ;
		putAll(aMap) ;
	}
	
	public HashMultiMap(Map<K, V> aMap)
	{
		this() ;
		putAll(aMap) ;
	}

	@Override
	public IMultiMap<K, V> put(K aKey, V aVal)
	{
		Object r = mMap.get(aKey) ;
		if(r != null)
		{
			if(aVal != null)
			{
				if(r instanceof __Handle)
					mMap.put(aKey, ((__Handle)r).asCollection(aVal)) ;
				else
				{
					((Set<V>)r).add(aVal) ;
				}
			}
		}
		else
		{
			if(aVal == null)
				mMap.put(aKey, null) ;
			else
				mMap.put(aKey, new __Handle<V>(aVal)) ;
		}
		return this ;
	}

	@Override
	public SizeIter<V> get(Object aKey)
	{
		return asSizeIter(mMap.get(aKey)) ;
	}
	
	private SizeIter<V> asSizeIter(Object aObj)
	{
		if(aObj == null)
			return null ;
		if(aObj instanceof __Handle)
			return SizeIter.create(((__Handle<V>)aObj).mVal) ;
		return SizeIter.create((Set<V>)aObj) ;
	}

	@SuppressWarnings("unchecked")
	@Override
	public V getFirst(K aKey)
	{
		Object r = mMap.get(aKey) ;
		if(r == null)
			return null ;
		if(r instanceof __Handle)
			return ((__Handle<V>)r).mVal ;
		return XC.getFirst((Set<V>)r) ;
	}

	@Override
	public IMultiMap<K, V> putAll(K aKey, V... aVals)
	{
		Object r = mMap.get(aKey) ;
		if(XC.isEmpty(aVals))
		{
			if(r == null)
				mMap.put(aKey, null) ;
		}
		else
		{
			if(r == null)
			{
				putDirectly(aKey, aVals);
			}
			else
			{
				if(r instanceof __Handle)
				{
					Set<V> set = new HashSet<>() ;
					set.add(((__Handle<V>)r).mVal) ;
					XC.addAllNotNull(set, aVals) ;
					if(set.size()>1)
						mMap.put(aKey, set) ;
				}
				else
				{
					XC.addAllNotNull((Set<V>)r , aVals) ;
				}
			}
		}
		return this ;
	}
	
	private void putDirectly(K aKey , V[] aVals)
	{
		if(aVals.length == 1)
		{
			if(aVals[0] != null)
				mMap.put(aKey, new __Handle<>(aVals[0])) ;
			else
				mMap.put(aKey, null) ;
		}
		else
		{
			Set<V> set = XC.linkedHashSet(aVals , (v)->v!=null) ;
			if(set.size()>1)
				mMap.put(aKey, set) ;
			else if(set.size() == 1)
				mMap.put(aKey, new __Handle<>(XC.getFirst(set))) ;
			else
				mMap.put(aKey , null) ;
		}
	}
	
	@Override
	public void set(K aKey, V... aVals)
	{
		if(XC.isEmpty(aVals))
			mMap.put(aKey, null) ;
		else
			putDirectly(aKey, aVals);
	}
	
	@Override
	public void clear()
	{
		mMap.clear();
	}
	
	@Override
	public int size()
	{
		return mMap.size() ;
	}
	
	@Override
	public boolean isEmpty()
	{
		return mMap.isEmpty() ;
	}
	
	@Override
	public Collection<K> keySet()
	{
		return mMap.keySet() ;
	}
	
	public List<V> values()
	{
		List<V> valList = XC.arrayList() ;
		forEach((k , v)->valList.add(v)) ;
		return valList ;
	}
	
	@Override
	public SizeIter<Entry<K, V>> entrySet()
	{
		return new EntrySet() ;
	}
	
	@Override
	public void forEach(BiConsumer<K, V> aConsumer)
	{
		for(Entry<K , Object> entry : mMap.entrySet())
		{
			Object v = entry.getValue() ;
			if(v == null)
				aConsumer.accept(entry.getKey(), null);
			else if(entry.getValue() instanceof  __Handle)
			{
				aConsumer.accept(entry.getKey(), ((__Handle<V>)v).mVal) ;
			}
			else
			{
				for(V v1 : (Set<V>)entry.getValue())
				{
					aConsumer.accept(entry.getKey(), v1);
				}
			}
		}
	}
	
	@Override
	public void iterateEntry(BiIteratorPredicate<K, V> aIt)
	{
		Iterator<Entry<K , Object>> it = mMap.entrySet().iterator() ;
		Entry<K , Object> entry = null ;
		while(it.hasNext())
		{
			entry = it.next() ;
			Object v = entry.getValue() ;
			if(v instanceof __Handle)
			{
				switch(aIt.visit(entry.getKey(), ((__Handle<V>)v).mVal))
				{
				case IterateOpCode.sContinue:
					break ;
				case IterateOpCode.sBreak :
				case IterateOpCode.sInterrupted:
					return ;
				case IterateOpCode.sRemove:
					it.remove();
					break ;
				case IterateOpCode.sRemoveAndBreak:
					it.remove();
					return ;
				default:
					throw new IllegalStateException("不合法的动作码") ;
				}
			}
			else
			{
				Set<V> set = (Set<V>)entry.getValue() ;
				if(set != null)
				{
	 				Iterator<V> sit = set.iterator() ;
					V val = null ;
					while(sit.hasNext())
					{
						val = sit.next() ;
						switch(aIt.visit(entry.getKey(), val))
						{
						case IterateOpCode.sContinue:
							break ;
						case IterateOpCode.sBreak :
						case IterateOpCode.sInterrupted:
							return ;
						case IterateOpCode.sRemove:
							sit.remove();
							break ;
						case IterateOpCode.sRemoveAndBreak:
							sit.remove();
							if(set.isEmpty())
								it.remove();
							else if(set.size() == 1)
								entry.setValue(new __Handle<V>(XC.getFirst(set))) ;
							return ;
						default:
							throw new IllegalStateException("不合法的动作码") ;
						}
					}
					if(set.isEmpty())
						it.remove();
					else if(set.size() == 1)
						entry.setValue(new __Handle<V>(XC.getFirst(set))) ;
				}
			}
		}
	}
	
	private static class __Handle<V>
	{
		V mVal ;
		
		public __Handle(V aVal)
		{
			mVal = aVal ;
		}
		
		Set<V> asCollection(V aAdd)
		{
			Set<V> set = new LinkedHashSet<>() ;
			set.add(mVal) ;
			set.add(aAdd) ;
			return set ;
		}
	}
	
	class __Iterator implements Iterator<Entry<K , V>>
	{
		Iterator<Entry<K, Object>> mIt ;

		Entry<K , Object> mCurrent ;
		Iterator<Object> mListIt ;
		
		public __Iterator()
		{
			mIt = mMap.entrySet().iterator() ;
		}

		@Override
		public boolean hasNext()
		{
			if(mListIt != null && mListIt.hasNext())
				return true ;
			return mIt.hasNext() ;
		}
		
		@Override
		public void remove()
		{
			if(mListIt != null)
				mListIt.remove(); 
			else
				mIt.remove();
		}

		@Override
		public Entry<K, V> next()
		{
			if(mListIt != null && mListIt.hasNext())
			{
				return new __Entry(mCurrent.getKey() , (V)mListIt.next()) ;
			}
			else
			{
				mCurrent = mIt.next() ;
				Object v = mCurrent.getValue() ;
				if(v == null)
				{
					mListIt = null ;
					return new __Entry(mCurrent.getKey(), null) ;
				}
				else if(v instanceof __Handle)
				{
					mListIt = null ;
					return new __Entry(mCurrent.getKey() , ((__Handle<V>)v).mVal) ;
				}
				else
				{
					mListIt = new ArrayIterator<>(((Set<V>)v).toArray()).iterator()  ;
					if(mListIt.hasNext())
						return new __Entry(mCurrent.getKey(), (V)mListIt.next()) ;
					else
						return new __Entry(mCurrent.getKey(), null) ;
				}
			}
		}
		
		class __Entry implements Entry<K, V>
		{
			K mKey ;
			V mVal ;
			
			public __Entry(K aKey , V aVal)
			{
				mKey = aKey ;
				mVal = aVal ;
			}

			@Override
			public K getKey()
			{
				return mKey ;
			}

			@Override
			public V getValue()
			{
				return mVal ;
			}

			@Override
			public V setValue(V aValue)
			{
				if(JCommon.unequals(mVal, aValue))
				{
					Object r = mMap.get(mKey) ;
					if(r == null)
						 mMap.put(mKey, new __Handle<V>(aValue)) ;
					else if(r instanceof __Handle)
					{
						if(aValue == null)
							mMap.put(mKey, aValue) ;
						else
							((__Handle<V>)r).mVal = aValue ;
					}
					else
					{
						((Set<V>)r).remove(mVal) ;
						if(aValue == null)
						{
							if(((Set<V>)r).isEmpty())
								mMap.put(mKey, null) ;
						}
						else
							((Set<V>)r).add(aValue) ;
					}
				}
				return mVal ;
			}
			
		}
		
	}
	
	class EntrySet implements SizeIter<Entry<K , V>>
	{
		@Override
		public Iterator<Entry<K, V>> iterator()
		{
			return new __Iterator() ;
		}

		@Override
		public int size()
		{
			return mMap.size() ;
		}

		@Override
		public boolean isEmpty()
		{
			return mMap.isEmpty() ;
		}
		
	}

	@Override
	public SizeIter<V> removeAll(K aKey)
	{
		return asSizeIter(mMap.remove(aKey)) ;
	}

	@Override
	public IMultiMap<K, V> remove(K aKey, V aValue)
	{
		Object obj = mMap.get(aKey) ;
		if(obj == null)
		{
			if(aValue == null)
				mMap.remove(aKey) ;
		}
		else if(obj instanceof __Handle)
		{
			if(JCommon.equals(((__Handle<V>)obj).mVal , aValue))
				mMap.remove(aKey) ;
		}
		else 
		{
			if(((Set<V>)obj).remove(aValue))
			{
				if(((Set<V>)obj).isEmpty())
					mMap.remove(aKey) ;
			}
		}
		return this ;
	}

	@Override
	public boolean containsKey(K aKey)
	{
		return mMap.containsKey(aKey) ;
	}

	@Override
	public boolean containsEntry(K aKey, V aValue)
	{
		Object obj = mMap.get(aKey) ;
		if(obj == null)
		{
			if(aValue == null)
				return true ;
		}
		else if(obj instanceof __Handle)
		{
			if(JCommon.equals(((__Handle<V>)obj).mVal , aValue))
				return true ;
		}
		else
			return (((Set<V>)obj).contains(aValue)) ;
		return false ;
	}

	@Override
	public String toString()
	{
		if(isEmpty())
			return "" ;
		StringBuilder strBld = new StringBuilder() ;
		forEach((k,v)->{
			if(strBld.length() > 0)
				strBld.append('&') ;
			strBld.append(k).append('=').append(v) ;	
		});
		return strBld.toString() ;
	}
}
