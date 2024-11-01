package team.sailboat.commons.fan.struct;

public class XInt
{
	public int i ;
	
	public XInt()
	{
		i = 0 ;
	}
	
	public XInt(int aVal)
	{
		i = aVal ;
	}
	
	public void plus(Integer aVal)
	{
		if(aVal != null)
			i+= aVal.intValue() ;
	}
	
	public void plus(int aVal)
	{
		i+= aVal ;
	}
	
	public int plusAndGet(Integer aVal)
	{
		return aVal != null?plusAndGet(aVal.intValue()):i ;
	}
	
	public int getAndPlus(Integer aVal)
	{
		return aVal != null?getAndPlus(aVal.intValue()):i ;
	}
	
	public int plusAndGet(int aVal)
	{
		return i+= aVal ;
	}
	
	public int getAndPlus(int aVal)
	{
		int v = i ;
		i+=aVal ;
		return v ;
	}
	
	public int incrementAndGet()
	{
		return ++i ;
	}
	
	public int decrementAndGet()
	{
		return --i ;
	}
	
	public int getAndIncrement()
	{
		return i++ ;
	}
	
	public int get()
	{
		return i ;
	}
	
	public void set(int aV)
	{
		i = aV ;
	}
	
	public void set(int aV , boolean aWhen)
	{
		if(aWhen)
			i = aV ;
	}
	
	public void setWhenLarge(int aV)
	{
		if(aV > i)
			i = aV ;
	}
	
	/**
	 * 当指定值和当前值相同时，返回false ，否则设置当前值为指定值，并且返回true
	 * @param aV
	 * @return
	 */
	public boolean compareAndSet(int aV)
	{
		if(aV == i)
			return false ;
		i = aV ;
		return true ;
	}
	
	@Override
	public String toString()
	{
		return Integer.toString(i) ;
	}
}
