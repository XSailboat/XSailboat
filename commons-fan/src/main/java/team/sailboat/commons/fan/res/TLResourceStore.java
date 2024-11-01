package team.sailboat.commons.fan.res ;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import team.sailboat.commons.fan.exec.AutoCleaner;
import team.sailboat.commons.fan.lang.Assert;

public class TLResourceStore<T extends IResourceBundle<?>> extends AutoCleaner
{	
	
	LinkedList<ResourceBean<T>> mIdleStack = new LinkedList<>() ;
	LinkedList<ResourceBean<T>> mUsingStack = new LinkedList<>() ;
	String mName ;
	IResourceCreator<T> mCreator ;
	int mIdleAmountLimit = 1 ;
	boolean mDestory = false ;
	ThreadLocal<GetCounter<T>> mTL = new ThreadLocal<>() ;
	
	public TLResourceStore(String aName , IResourceCreator<T> aCreator)
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
	
//	public void destory()
//	{
//		mDestory = true ;
//		discardAll();
//	}
	
	public String getName()
	{
		return mName;
	}
	
	public synchronized T get() throws Exception
	{
		if(mDestory)
			throw new IllegalStateException("ResourceStore已经销毁") ;
		GetCounter<T> resource = mTL.get() ;
		if(resource != null)
		{
//			if(!mCreator.isAvailable(resource.mResource))
//				System.out.println("ThreadLocal里面的不可用");
			return resource.add().mResource  ;
		}
		if(mIdleStack.isEmpty())
		{
			T t = mCreator.create() ;
			ResourceBean<T> rbean = new ResourceBean<T>(t) ;
			//记录创建和获取时间
			rbean.mCreateTime = System.currentTimeMillis() ;
			rbean.mGetTime = rbean.mCreateTime ;
			mUsingStack.push(rbean);
			mTL.set(new GetCounter<T>(t));
			if(!t.prepareForUse())
				throw new ResourceUnavailableException(String.format("资源库[%1$s]新创建的资源不可用", mName)) ;
 			return t ;
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
				return get() ;
			bean.mGetTime = System.currentTimeMillis() ;
			mUsingStack.push(bean);
			mTL.set(new GetCounter<T>(bean.mResource)) ;
			if(!bean.mResource.prepareForUse())
				throw new ResourceUnavailableException(String.format("资源库[%1$s]从空闲池中取得的资源不可用", mName)) ;
			return bean.mResource ;
		}
	}
	
	/**
	 * 释放资源，将资源压入空闲队列
	 * @param aResource
	 * @return
	 */
	public synchronized boolean release(T aResource)
	{
		GetCounter<T> counter = mTL.get() ;
		if(counter == null)
		{
			//可能已经被destroy掉了。有时可能存在资源被多次关闭的情形，所以这是正常的
//			throw new IllegalStateException("release时ThreadLocal不能取得数据") ;
			return false ;
		}
		if(counter.mResource != aResource)
			throw new IllegalStateException("要release的资源的与ThreadLocal中存储的不同") ;
		if(counter.mCount == 0)
			throw new IllegalStateException("release的次数比get的次数多") ;
		if(counter.reduce().mCount == 0)
		{
			mTL.remove();
			Iterator<ResourceBean<T>> it = mUsingStack.iterator() ;
			while(it.hasNext())
			{
				ResourceBean<T> bean = it.next() ;
				if(bean.mResource == aResource)
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
	
	public synchronized boolean destroy(T aResource)
	{
		GetCounter<T> counter = mTL.get() ;
		if(counter != null)
		{
			if(counter.mResource != aResource)
				throw new IllegalStateException("destroy的资源和ThreadLocal中取得的不同") ;
			else
				counter.mResource.destroy(); ;
		}
		Iterator<ResourceBean<T>> it = mIdleStack.iterator() ;
		while(it.hasNext())
		{
			ResourceBean<T> bean = it.next() ;
			if(bean.mResource == aResource)
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
			if(bean.mResource == aResource)
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
			long current = System.currentTimeMillis() ;
			long oneMinute = TimeUnit.MINUTES.toMillis(1) ;
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
