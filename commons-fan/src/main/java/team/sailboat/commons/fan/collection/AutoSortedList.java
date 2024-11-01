package team.sailboat.commons.fan.collection;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import team.sailboat.commons.fan.struct.CountElement;
import team.sailboat.commons.fan.struct.ISortedElement;
import team.sailboat.commons.fan.struct.TimeStampElement;

public class AutoSortedList<Y extends ISortedElement<?>> extends IndexedList<Y> implements PropertyChangeListener
{
	Comparator<Y> mComp ;
	Object[] mMirror ;

	protected AutoSortedList(Comparator<Y> aComp) throws NoSuchMethodException, SecurityException, NoSuchFieldException
	{
		super(sType_Method , "index");
		mComp = aComp ;
	}

	protected AutoSortedList(Comparator<Y> aComp , int aInitCap)
			throws NoSuchMethodException, SecurityException, NoSuchFieldException
	{
		super(sType_Method , "index", aInitCap);
		mComp = aComp ;
	}
	
	/**
	 * 在不能确保插入位置能确保顺序合理的时候不要使用
	 */
	@Override
	public void add(int aIndex, Y aE)
	{
		aE.setPropertyChangeListener(this);
		super.add(aIndex, aE); 
	}
	
	@Override
	public boolean add(Y aE)
	{
		int i = Collections.binarySearch(mEles, aE, mComp) ;
		add(i , aE);
		return true ;
	}

	@Override
	public boolean addAll(Collection<? extends Y> aC)
	{
		if(XC.isEmpty(aC))
			return true ;
		else
		{
			for(Y ele : aC)
				add(ele) ;
			return true ;
		}
	}
	
	/**
	 * 不要调用
	 */
	@Override
	public boolean addAll(int aIndex, Collection<? extends Y> aC)
	{
		for(ISortedElement<?> ele : aC)
			ele.setPropertyChangeListener(this) ;
		return super.addAll(aIndex, aC);
	}

	@Override
	public void propertyChange(PropertyChangeEvent aEvt)
	{
		mWriteLock.lock(); 
		try
		{
			ISortedElement<?> ele = (ISortedElement<?>)aEvt.getSource() ;
			int index = ele.getIndex() ;
			if(leftCompare(index) || rightCompare(index))
			{
				mMirror = null ;
			}
		}
		finally
		{
			mWriteLock.unlock(); 
		}
	}
	
	@Override
	public Object[] toArray()
	{
		if(mMirror == null)
		{
			mReadLock.lock();
			try
			{
				mMirror = mEles.toArray() ;
			}
			finally
			{
				mReadLock.unlock();
			}
		}
		return mMirror ;
	}
	
	protected boolean leftCompare(int aIndex)
	{
		if(aIndex>0)
		{
			Y ele = mEles.get(aIndex) ;
			Y ele_1 = mEles.get(aIndex-1) ;
			if(mComp.compare(ele_1, ele)>0)
			{
				Collections.swap(mEles , aIndex , aIndex-1);
				leftCompare(aIndex-1) ;
				return true ;
			}
		}
		return false ;
	}
	
	protected boolean rightCompare(int aIndex)
	{
		if(aIndex<mEles.size()-1)
		{
			Y ele = mEles.get(aIndex) ;
			Y ele_1 = mEles.get(aIndex+1) ;
			if(mComp.compare(ele, ele_1)>0)
			{
				Collections.swap(mEles , aIndex , aIndex+1);
				rightCompare(aIndex+1) ;
				return true ;
			}
		}
		return false ;
	}
	
	
	public static <Y extends ISortedElement<?>> AutoSortedList<Y> create(Comparator<Y> aComp) throws NoSuchMethodException, SecurityException, NoSuchFieldException
	{
		return new AutoSortedList<>(aComp) ;
	}
	
	public static <T> AutoSortedList<CountElement<T>> createCountSortList()
	{
		try
		{
			return new AutoSortedList<>(CountElement.createComparator()) ;
		}
		catch (NoSuchMethodException | SecurityException | NoSuchFieldException e)
		{
			throw new IllegalStateException(e) ;
		}
	}
	
	public static <T> AutoSortedList<TimeStampElement<T>> createTimeSortList()
	{
		try
		{
			return new AutoSortedList<>(TimeStampElement.createComparator()) ;
		}
		catch (NoSuchMethodException | SecurityException | NoSuchFieldException e)
		{
			throw new IllegalStateException(e) ;
		}
	}
}
