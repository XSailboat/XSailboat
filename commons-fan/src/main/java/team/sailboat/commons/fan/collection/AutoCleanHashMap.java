package team.sailboat.commons.fan.collection;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import team.sailboat.commons.fan.exec.AutoCleaner;
import team.sailboat.commons.fan.exec.DefaultAutoCleaner;
import team.sailboat.commons.fan.infc.StatusCloseable;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.fan.struct.Tuples;
import team.sailboat.commons.fan.time.XTime;
import team.sailboat.commons.fan.time.XTimeUnit;

/**
 * <p>
 * 超过指定时间没有被取用过，就会移除。如果get过，那么将从取用时间开始重新计时
 *
 * @author yyl
 * @since 2016年9月29日
 */
public class AutoCleanHashMap<K , V> extends AbstractMap<K, V> 
{
	Map<K, Bean<V>> mMap = Collections.synchronizedMap(new SRHashMap<>()) ;
	int mIdleTimeInMinutes ;
	int mCreatedTimeInMinutes ;
	AutoCleaner mCleaner ;
	boolean mCloseAutoCloseable = false ;
	
	/**
	 * 
	 * @param aCleanTimeInMinutes
	 * @param aCloseAutoCloseable			只有在被自动清理掉的时候才起作用，主动remove或clear不会去调用close
	 */
	@Deprecated
	public AutoCleanHashMap(int aIdleTimeInMinutes , boolean aCloseAutoCloseable)
	{
		this(0 , aIdleTimeInMinutes , aCloseAutoCloseable) ;
	}
	
	@Deprecated
	public AutoCleanHashMap(int aIdleTimeInMinutes)
	{
		this(0 , aIdleTimeInMinutes , false) ;
	}
	
	/**
	 * 单位，分钟
	 * @param aCleanTimeInMinutes   必须>=5
	 */
	public AutoCleanHashMap(int aCreatedTimeInMinutes , int aIdleTimeInMinutes , boolean aCloseAutoCloseable)
	{
		Assert.isTrue(aCreatedTimeInMinutes>0 || aIdleTimeInMinutes>0 
				, "从创建时间开始计数的过期时间和从最近一次取用时间开始计数的过期时间，要求两者至少有一个大于0");
		mCreatedTimeInMinutes = Math.max(aCreatedTimeInMinutes , 0) ;
		mIdleTimeInMinutes = Math.max(aIdleTimeInMinutes , 0) ; ;
		mCloseAutoCloseable = aCloseAutoCloseable ;
		
		mCleaner = new DefaultAutoCleaner(60 /*1分钟*/ , ()-> {
			if(mMap.isEmpty())
				return ;
			Object[] array = mMap.keySet().toArray() ;
			for(int i=0 ; i<array.length ; i++)
			{
				Object key = array[i] ;
				Bean<V> entry = mMap.get(key) ;
				if(entry.getValue() instanceof StatusCloseable 
						&& ((StatusCloseable)entry.getValue()).isClosed())
				{
					mMap.remove(key) ;
					continue ;
				}
				if(mIdleTimeInMinutes>0)
				{
					if(XTime.pass(entry.mLastGetTime , mIdleTimeInMinutes , XTimeUnit.MINUTE))
					{
						mMap.remove(key) ;
						if(mCloseAutoCloseable && entry.getValue() != null
								&& entry.getValue() instanceof AutoCloseable)
						{
							StreamAssist.close((AutoCloseable)entry.getValue()) ;
						}
						continue ;
					}
				}
				if(mCreatedTimeInMinutes>0)
				{
					if(XTime.pass(entry.mCreatedTime , mCreatedTimeInMinutes , XTimeUnit.MINUTE))
					{
						mMap.remove(key) ;
						if(mCloseAutoCloseable && entry.getValue() != null
								&& entry.getValue() instanceof AutoCloseable)
						{
							StreamAssist.close((AutoCloseable)entry.getValue()) ;
						}
						continue ;
					}
				}
			}
		}) ;
	}
	
	@Override
	public V put(K aKey, V aValue)
	{
		Bean<V> bean = mMap.put(aKey, new Bean<>(aValue)) ;
		return bean==null?null:bean.getValue() ;
	}
	
	@Override
	public V remove(Object aKey)
	{
		Bean<V> entry =  mMap.remove(aKey) ;
		return entry == null?null:entry.getValue() ;
	}
	
	@Override
	public V get(Object aKey)
	{
		Bean<V> entry = mMap.get(aKey) ;
		if(entry == null)
			return null ;
		else
		{
			entry.updateGetTime() ;
			return entry.getValue() ;
		}
	}
	
	public Bean<V> getBean(Object aKey)
	{
		return mMap.get(aKey) ;
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
	}
	
	private Iterator<Map.Entry<K, V>> newEntryIterator()
	{
		return new _Iterator() ;
	}
	
	class _Iterator implements Iterator<Map.Entry<K,V>>
	{
		Iterator<Entry<K, Bean<V>>> mIt ;
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
				Entry<K , Bean<V>> next = null ;
				while(mIt.hasNext())
				{
					next = mIt.next() ;
					V value = next.getValue().getValue() ;
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
        	return mMap.remove(e.getKey()) != null ;
        }
        
        public int size()
        {
            return mMap.size() ;
        }
        
        public void clear()
        {
            clear();
        }
    }
	
	public static class Bean<E>
	{
		long mCreatedTime ;
		long mLastGetTime ;
		
		E mEle ;
		
		public Bean(E aEle)
		{
			mEle = aEle ;
			mCreatedTime = System.currentTimeMillis() ;
			mLastGetTime = mCreatedTime ;
		}
		
		public E getEle()
		{
			return mEle;
		}
		
		public long getCreatedTime()
		{
			return mCreatedTime;
		}
		
		void updateGetTime()
		{
			mLastGetTime = System.currentTimeMillis() ;
		}
		
		E getValue()
		{
			return mEle ;
		}
	}
	
	public static <K , V> AutoCleanHashMap<K, V> withExpired_Idle(int aIdleTimeInMinutes , boolean aCloseAutoCloseable)
	{
		return new AutoCleanHashMap<>(0 , aIdleTimeInMinutes , aCloseAutoCloseable) ;
	}
	
	public static <K , V> AutoCleanHashMap<K, V> withExpired_Idle(int aIdleTimeInMinutes)
	{
		return new AutoCleanHashMap<>(0 , aIdleTimeInMinutes , false) ;
	}
	
	public static <K , V> AutoCleanHashMap<K, V> withExpired_Created(int aCreatedTimeInMinutes)
	{
		return new AutoCleanHashMap<>(aCreatedTimeInMinutes, 0 , false) ;
	}
}
