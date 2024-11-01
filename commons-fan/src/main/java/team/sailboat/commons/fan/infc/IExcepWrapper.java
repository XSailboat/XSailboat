package team.sailboat.commons.fan.infc;

import java.util.function.Supplier;

public interface IExcepWrapper
{
	/**
	 * 如果此Wrapper的值不是null，就抛出异常
	 * @param exceptionSupplier
	 * @throws X
	 */
	<X extends Throwable> void orThrow(Supplier<? extends X> exceptionSupplier) throws X ;
}
