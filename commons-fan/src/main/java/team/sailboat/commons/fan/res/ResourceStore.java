package team.sailboat.commons.fan.res ;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.exec.AutoCleaner;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.fan.struct.Tuples;
import team.sailboat.commons.fan.text.XString;

public class ResourceStore<T extends IResourceBundle<?>> extends AutoCleaner
{	
	
	LinkedList<ResourceBean<T>> mIdleStack = new LinkedList<>() ;
	LinkedList<ResourceBean<T>> mUsingStack = new LinkedList<>() ;
	String mName ;
	IResourceCreator<T> mCreator ;
	int mIdleAmountLimit = 1 ;
	boolean mDestory = false ;
	Map<String , GetCounter<T>> mGetCounterMap = XC.concurrentHashMap() ;
	
	public ResourceStore(String aName , IResourceCreator<T> aCreator)
	{
		super(60) ;
		Assert.notNull(aCreator , "资源创建器为null") ;
 		mName = aName ;
		mCreator = aCreator ;
	}
	
	/**
	 * 检查空闲资源时，可以保有的空闲资源数量
	 * @param aIdleAmountLimit
	 */
	public void setIdleAmountLimit(int aIdleAmountLimit)
	{
		mIdleAmountLimit = aIdleAmountLimit;
	}
	
	public String getName()
	{
		return mName;
	}
	
	public synchronized Map.Entry<String , T> get(String aKey) throws Exception
	{
		if(mDestory)
			throw new IllegalStateException("ResourceStore已经销毁") ;
		
		GetCounter<T> resource = null ;
		if(XString.isNotEmpty(aKey))
			resource = mGetCounterMap.get(aKey) ;
		if(resource != null)
		{
			return Tuples.of(aKey ,  resource.add().mResource) ;
		}
		if(mIdleStack.isEmpty())
		{
			T t = mCreator.create() ;
			ResourceBean<T> rbean = new ResourceBean<T>(t) ;
			//记录创建和获取时间
			rbean.mCreateTime = System.currentTimeMillis() ;
			rbean.mGetTime = rbean.mCreateTime ;
			mUsingStack.push(rbean);
			if(!t.prepareForUse())
			{
				StreamAssist.close(t) ;
				throw new ResourceUnavailableException(String.format("资源库[%1$s]新创建的资源不可用", mName)) ;
			}
			GetCounter<T> counter = new GetCounter<T>(t) ;
			String key = UUID.randomUUID().toString() ;
			mGetCounterMap.put(key, counter) ;
 			return Tuples.of(key , t) ;
		}
		else
		{
			ResourceBean<T> bean = null ;	
			while(!mIdleStack.isEmpty())
			{
				bean = mIdleStack.pop() ;
				if(!mCreator.isAvailable(bean.mResource))
				{
					bean.mResource.destroy();
					bean = null ;
				}
				else
					break ;
			}
			if(bean == null)
				return get(aKey) ;
			
			if(!bean.mResource.prepareForUse())
			{
				StreamAssist.close(bean.mResource) ;
				throw new ResourceUnavailableException(String.format("资源库[%1$s]从空闲池中取得的资源不可用", mName)) ;
			}
			
			bean.mGetTime = System.currentTimeMillis() ;
			mUsingStack.push(bean);
			GetCounter<T> counter = new GetCounter<T>(bean.mResource) ;
			String key = UUID.randomUUID().toString() ;
			mGetCounterMap.put(key, counter) ;
			return Tuples.of(key , bean.mResource) ;
		}
	}
	
	/**
	 * 释放资源，将资源压入空闲队列
	 * @param aResource
	 * @return
	 */
	public synchronized boolean release(String aKey)
	{
		GetCounter<T> counter = mGetCounterMap.get(aKey) ;
		if(counter == null)
		{
			return false ;
		}
		if(counter.mCount == 0)
			throw new IllegalStateException("release的次数比get的次数多") ;
		if(counter.reduce().mCount == 0)
		{
			mGetCounterMap.remove(aKey) ;
			Iterator<ResourceBean<T>> it = mUsingStack.iterator() ;
			while(it.hasNext())
			{
				ResourceBean<T> bean = it.next() ;
				if(bean.mResource == counter.mResource)
				{
					it.remove();
					bean.mReleaseTime = System.currentTimeMillis() ;
					if(!bean.mDiscard)
						mIdleStack.offer(bean) ;
					else
						bean.mResource.destroy();
					return true ;
				}
			}
		}
		return false ;
	}
	
	public synchronized void discardAll()
	{
		Iterator<ResourceBean<T>> it = mUsingStack.iterator() ;
		while(it.hasNext())
		{
			ResourceBean<T> bean = it.next() ;
			bean.mDiscard = true ;
		}
		if(mIdleStack.isEmpty())
		{
			for(ResourceBean<T> res : mIdleStack)
			{
				res.mResource.destroy();
			}
			mIdleStack.clear(); 
		}
	}
	
	/**
	 * 取得正在使用的资源数量
	 * @return
	 */
	public int getUsingAmount()
	{
		return mUsingStack.size() ;
	}
	
	public synchronized boolean destroy(String aKey)
	{
		GetCounter<T> counter = mGetCounterMap.get(aKey) ;
		if(counter == null)
			return false ;
		
		counter.mResource.destroy() ;
		
		Iterator<ResourceBean<T>> it = mIdleStack.iterator() ;
		while(it.hasNext())
		{
			ResourceBean<T> bean = it.next() ;
			if(bean.mResource == counter.mResource)
			{
				it.remove();
				bean.mResource.destroy() ;
				return true ;
			}
		}
		it = mUsingStack.iterator() ;
		while(it.hasNext())
		{
			ResourceBean<T> bean = it.next() ;
			if(bean.mResource == counter.mResource)
			{
				it.remove();
				bean.mResource.destroy(); ;
				return true ;
			}
		}
		return false ;
	}
	
	/**
	 * 空闲资源过多就释放掉
	 */
	@Override
	protected synchronized void doClean()
	{
		if(mIdleStack.isEmpty())
			return ;
		if(mIdleAmountLimit<mIdleStack.size())
		{
			Iterator<ResourceBean<T>> it = mIdleStack.iterator() ;
			int maxRemoveCount = mIdleStack.size()-mIdleAmountLimit ;
			final long current = System.currentTimeMillis() ;
			final long oneMinute = TimeUnit.MINUTES.toMillis(1) ;
			int count = 0 ;
			while(it.hasNext())
			{
				ResourceBean<T> bean = it.next() ;
				//如果释放已经超过1分钟了，就可以被清理掉
				if(current-bean.mReleaseTime>oneMinute)
				{
					count++ ;
					it.remove();
					bean.mResource.destroy(); ;
					if(count==maxRemoveCount)
						break ;
				}
			}
		}
	}
}
