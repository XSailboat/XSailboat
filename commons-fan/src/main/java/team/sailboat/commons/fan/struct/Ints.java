package team.sailboat.commons.fan.struct;

import java.util.Arrays;

public class Ints
{
	public int[] mData ;
	public int mSize ;
	
	public Ints()
	{
		mData = new int[8] ;
		mSize = 0 ;
	}
	
	public Ints(int aCapacity)
	{
		if(aCapacity<=0)
			throw new IllegalArgumentException() ;
		mData = new int[aCapacity] ;
		mSize = 0 ;
	}
	
	public boolean isEmpty()
	{
		return mSize == 0 ;
	}
	
	public boolean isNotEmpty()
	{
		return mSize > 0 ;
	}
	
	public void add(int aValue)
	{
		ensureCapacity(mSize+1) ;
		mData[mSize++] = aValue ;
	}
	
	public void add(int...aValues)
	{
		if(aValues.length>0)
		{
			mSize += aValues.length ;
			ensureCapacity(mSize) ;
			System.arraycopy(aValues, 0, mData, mSize-aValues.length, aValues.length) ;
		}
	}
	
	public void add(int[] aValues , int aOffset , int aLen)
	{
		if(aValues != null && aValues.length>=aOffset+aLen)
		{
			mSize += aLen ;
			ensureCapacity(mSize) ;
			System.arraycopy(aValues, aOffset, mData, mSize-aLen, aLen) ;
		}
		else
			throw new IllegalArgumentException() ;
	}
	
	void ensureCapacity(int aMinCapicity)
	{
		if(mData.length<aMinCapicity)
			mData = Arrays.copyOf(mData, Math.max(mData.length*3/2+1 , aMinCapicity)) ;
	}
	
	/**
	 * 如果数组是满的，返回数组本身；<br>
	 * 如果数组不是满的，将等效toArray()
	 * @return
	 */
	public int[] getArray()
	{
		if(mData.length == mSize)
			return mData ;
		else
			return toArray() ;
	}
	
	public int indexOf(int aVal)
	{
		for(int i=0 ; i<mSize ; i++)
			if(mData[i] == aVal)
				return i ;
		return -1 ;
	}
	
	public boolean contains(int aVal)
	{
		return indexOf(aVal) != -1 ;
	}
	
	public boolean remove(int aIndex)
	{
		if(aIndex>=0 && aIndex<mSize)
		{
			for(int i=aIndex+1 ; i<mSize ; i++)
				mData[i-1] = mData[i] ;
			mSize-- ;
			return true ;
		}
		return false ;
	}
	
	public void clear()
	{
		mSize = 0 ;
	}
	
	/**
	 * 返回紧缩后的数组,即数组大小是实际数量
	 * @return
	 */
	public int[] toArray()
	{
		return Arrays.copyOf(mData, mSize) ;
	}
}
