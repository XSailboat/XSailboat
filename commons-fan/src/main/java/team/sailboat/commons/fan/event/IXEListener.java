package team.sailboat.commons.fan.event;


public interface IXEListener extends IXListener
{
	/**
	 * 如果抛出中断处理异常，则中断处理
	 * @param aE
	 * @throws BreakException
	 */
	void dealException(Exception aE) throws BreakException ;
	
	public static class BreakException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}
}
