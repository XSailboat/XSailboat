package team.sailboat.commons.fan.struct;

import java.util.function.Supplier;

import team.sailboat.commons.fan.infc.IExcepWrapper;

public class Wrapper<T> implements Supplier<T> , IExcepWrapper
{
	T mVal ;
	
	public Wrapper(T aVal)
	{
		mVal = aVal ;
	}
	
	public Wrapper()
	{
	}
	
	public boolean isNull()
	{
		return mVal == null ;
	}
	
	@Override
	public T get()
	{
		return mVal ;
	}
	
	/**
	 * 
	 * @param aNewVal
	 * @return			返回旧值
	 */
	public T set(T aNewVal)
	{
		T oldVal = mVal ;
		mVal = aNewVal ;
		return oldVal ;
	}
	
	/**
	 * 如果此Wrapper的值为null就抛出指定异常
	 * @param exceptionSupplier
	 * @return
	 * @throws X
	 */
	public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X
	{
		if (mVal != null)
			return mVal;
		else
			throw exceptionSupplier.get();
	}
	
	/**
	 * 如果此Wrapper的值不是null，就抛出异常
	 * @param exceptionSupplier
	 * @throws X
	 */
	@Override
	public <X extends Throwable> void orThrow(Supplier<? extends X> exceptionSupplier) throws X
	{
		if (mVal != null)
			throw exceptionSupplier.get() ;
	}

	public static boolean doWhen(boolean aCnd , Runnable aRun)
	{
		if(aCnd)
			aRun.run();
		return aCnd ;
	}
	
	/**
	 * 如果aEle为null，就返回aDefaultVal；否则返回aEle
	 * @param aEle
	 * @param aDefaultVal
	 * @return
	 */
	public static <T> T get(T aEle , T aDefaultVal)
	{
		return aEle == null?aDefaultVal:aEle ;
	}
	
	public static <T> Wrapper<T> of(T aEle)
	{
		return new Wrapper<>(aEle) ;
	}
	
	public static <T> Wrapper<T> ofNull()
	{
		return new Wrapper<T>() ;
	}
}
