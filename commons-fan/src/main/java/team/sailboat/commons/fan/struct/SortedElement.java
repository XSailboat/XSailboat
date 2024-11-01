package team.sailboat.commons.fan.struct;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class SortedElement<T> implements ISortedElement<T>
{

	protected T mValue ;
	protected PropertyChangeListener mLsn ;
	protected int mIndex ;
	
	public SortedElement(T aValue)
	{
		mValue = aValue ;
	}
	
	@Override
	public void setPropertyChangeListener(PropertyChangeListener aLsn)
	{
		mLsn = aLsn ;
	}
	
	/**
	 * 
	 * @param aProp				发生变化的性质名称
	 * @param aOldValue			旧值
	 * @param aNewValue			新值
	 */
	protected void notifySortedPropertyChange(String aProp , Object aOldValue , Object aNewValue)
	{
		if(mLsn != null)
			mLsn.propertyChange(new PropertyChangeEvent(this, aProp , aOldValue, aNewValue));
	}

	@Override
	public T getValue()
	{
		return mValue ;
	}
	
	@Override
	public void setIndex(int aIndex)
	{
		mIndex = aIndex ;
	}
	
	@Override
	public int getIndex()
	{
		return mIndex ;
	}

}
