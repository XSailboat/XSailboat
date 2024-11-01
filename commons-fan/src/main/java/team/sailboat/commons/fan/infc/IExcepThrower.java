package team.sailboat.commons.fan.infc;

public interface IExcepThrower<X extends Throwable>
{
	/**
	 * 如果此Wrapper的值不是null，就抛出异常
	 * @param exceptionSupplier
	 * @throws X
	 */
	 void orThrow() throws X ;
}
