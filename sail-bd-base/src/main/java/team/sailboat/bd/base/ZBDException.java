package team.sailboat.bd.base;

import team.sailboat.commons.fan.text.XString;

public class ZBDException extends RuntimeException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ZBDException(String aMsg , Object...aArgs)
	{
		super(XString.msgFmt(aMsg, aArgs)) ;
	}

	public ZBDException(Throwable aCause , String aMessage , Object...aArgs)
	{
		super(XString.msgFmt(aMessage, aArgs) , aCause);
	}
}
