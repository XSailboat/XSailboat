package team.sailboat.commons.fan.struct;

import java.util.function.BiConsumer;

import team.sailboat.commons.fan.lang.JCommon;

public class XDataBag implements IXDataBag
{
	Object mData ;
	boolean mKeyedData = true ;
	
	int mSize = 0 ;
	
	/**
	 * 相当于setData(null , aData)
	 */
	public void setData(Object aData)
	{
		if(mKeyedData)
		{
			if(mData != aData)
			{
				mData = aData ;
				mSize = mData != null?1:0 ;
			}
		}
		else
		{
			if(((Object[])mData)[0] != aData)
			{
				if(((Object[])mData)[0] == null)
					mSize++ ;
				else if(aData == null)
					mSize-- ;
				((Object[])mData)[0] = aData ;
			}
		}
	}
	
	/**
	 * 相当于getData(null)
	 */
	public Object getData()
	{
		if(mKeyedData)
			return mData ;
		else return ((Object[])mData)[0] ;
	}
	
	public void setData(Object aKey , Object aData)
	{
		if(aKey == null)
			setData(aData) ;
		else
		{
			if(mKeyedData)
			{
				mKeyedData = false ;
				Object[] table = new Object[3] ;
				table[0] = mData ;
				table[1] = aKey ;
				table[2] = aData ;
				mData = table ;
				mSize++ ;
			}
			else
			{
				Object[] table = (Object[])mData ;
				int i = 1 ;
				int nullPos = -1 ;
				for(; i<table.length ; i+=2)
				{
					if(JCommon.equals(table[i] , aKey))
						break ;
					else if(nullPos == -1 && table[i] == null)
						nullPos = i ;
				}
				if(i<table.length)
					table[i+1] = aData ;
				else
				{
					mSize++ ;
					if(nullPos == -1)
					{
						table = new Object[i+2] ;
						System.arraycopy(mData, 0, table, 0, i) ;
						table[i] = aKey ;
						table[i+1] = aData ;
						mData = table ;
					}
					else
					{
						table[i] = aKey ;
						table[i+1] = aData ;
					}
				}
			}
		}
	}
	
	@Override
	public Object getData(Object aKey)
	{
		if(mKeyedData)
		{
			return aKey==null?mData:null ;
		}
		else
		{
			Object[] table = (Object[])mData ;
			if(aKey == null)
				return table[0] ;
			else
			{
				for(int i=1 ; i<table.length ; i+=2)
					if(JCommon.equals(table[i], aKey))
						return table[i+1] ;
				return null ;
			}
		}
	}
	
	public Object removeData(Object aKey)
	{
		Object r = null ;
		if(!mKeyedData)
		{
			Object[] table = (Object[])mData ;
			if(aKey == null)
			{
				r = table[0] ;
				table[0] = null ;
				if(r != null)
					mSize -- ;
			}
			else
			{
				for(int i=1 ; i<table.length ; i+=2)
				{
					if(JCommon.equals(table[i], aKey))
					{
						table[i] = null ;
						r = table[i+1] ;
						table[i+1] = null ;
						mSize-- ;
						break ;
					}
				}
			}
		}
		else if(aKey == null)
		{
			r = mData ;
			mData = null ;
			if(r != null)
				mSize-- ;
		}
		return r ;
	}
	
	@Override
	public int getDataEntryAmount()
	{
		return mSize ;
	}
	
	public void clearBag()
	{
		mData = null ;
		mKeyedData = true ;
		mSize = 0 ;
	}
	
	public void forEach(BiConsumer<Object, Object> aConsumer)
	{
		if(mKeyedData)
			aConsumer.accept(null, mData) ;
		else
		{
			Object[] datas = (Object[])mData ;
			if(datas[0] != null)
				aConsumer.accept(null, datas[0]) ;
			for(int i=1 ; i<datas.length ; i+=2)
			{
				if(datas[i] != null)
					aConsumer.accept(datas[i], datas[i+1]) ;
			}
		}
	}
}
