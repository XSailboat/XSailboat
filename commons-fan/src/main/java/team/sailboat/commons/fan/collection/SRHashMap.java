package team.sailboat.commons.fan.collection;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.struct.Tuples;

public class SRHashMap<K , V> extends AbstractMap<K, V>
{

	Map<K, SoftReference<V>> mMap = new HashMap<>() ;
	Map<SoftReference<V> , K> mInvMap = new HashMap<>() ;
	ReferenceQueue<V> mRefQueue = new ReferenceQueue<>() ;
	
	@Override
	public V put(K aKey, V aValue)
	{
		Assert.notNull(aKey, "键为null") ;
		Assert.notNull(aValue, "值为null") ;
		clearIdleSR(); 
		SoftReference<V> sr0 = new SoftReference<>(aValue , mRefQueue) ;
		SoftReference<V> sr = mMap.put(aKey, sr0) ;
		if(sr != null)
		{
			mInvMap.remove(sr) ;
			mInvMap.put(sr0, aKey) ;
			return sr.get() != null?sr.get():null ;
		}
		else
			mInvMap.put(sr0, aKey) ;
		return null ;
	}
	
	@Override
	public V remove(Object aKey)
	{
		SoftReference<V> sr =  mMap.remove(aKey) ;
		if(sr != null)
		{
			mInvMap.remove(sr) ;
			return sr.get() ;
		}
		return null ;
	}
	
	@Override
	public V get(Object aKey)
	{
		clearIdleSR();
		SoftReference<V> sr = mMap.get(aKey) ;
		return sr!=null?sr.get():null ;
	}
	
	/**
	 * 清除限制的SoftReference
	 */
	private void clearIdleSR()
	{
		Reference<? extends V> ref = null ;
		while((ref=mRefQueue.poll())!=null)
		{
			K key = mInvMap.remove(ref) ;
			mMap.remove(key) ;
		}
	}
	
	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet()
	{
		return new EntrySet() ;
	}
	
	@Override
	public void clear()
	{
		mMap.clear(); 
		mInvMap.clear(); 
		mRefQueue = new ReferenceQueue<>() ;
	}
	
	private Iterator<Map.Entry<K, V>> newEntryIterator()
	{
		return new _Iterator() ;
	}
	
	class _Iterator implements Iterator<Map.Entry<K,V>>
	{
		Iterator<Entry<K, SoftReference<V>>> mIt ;
		Entry<K , V> mNext ;

		public _Iterator()
		{
			mIt = mMap.entrySet().iterator() ;
		}
		
		@Override
		public boolean hasNext()
		{
			if(mNext == null)
			{
				Entry<K, SoftReference<V>> next = null ;
				while(mIt.hasNext())
				{
					next = mIt.next() ;
					V value = next.getValue().get() ;
					if(value != null)
					{
						mNext = Tuples.of(next.getKey(), value) ;
						break ;
					}
				}
			}
			return mNext != null ;
		}

		@Override
		public java.util.Map.Entry<K, V> next()
		{
			if(mNext != null)
			{
				Entry<K , V> current = mNext ;
				mNext = null ;
				return current ;
			}
			return null ;
		}

		@Override
		public void remove()
		{
			throw new IllegalStateException("不支持此方法") ;
		}
    
	}
	
	class EntrySet extends AbstractSet<Map.Entry<K,V>>
	{
        public Iterator<Map.Entry<K,V>> iterator()
        {
            return newEntryIterator();
        }

		public boolean contains(Object o)
		{
			if (!(o instanceof Map.Entry))
				return false;
			@SuppressWarnings("unchecked")
			Map.Entry<K, V> e = (Map.Entry<K, V>) o;
			V candidate = get(e.getKey());
			return candidate != null && candidate.equals(e.getValue());
		}
        
        public boolean remove(Object o)
        {
        	if (mMap.size()  == 0 || !(o instanceof Map.Entry))
        		return false ;
        	@SuppressWarnings("unchecked")
			Map.Entry<K, V> e = (Map.Entry<K, V>) o;
        	SoftReference<V> sr = mMap.remove(e.getKey()) ;
        	if(sr != null)
        	{
	        	mInvMap.remove(sr) ;
	        	return true ;
        	}
        	return false ;
        }
        
        public int size()
        {
        	clearIdleSR();
            return mMap.size() ;
        }
        
        public void clear()
        {
            clear();
        }
    }
	
	

}
