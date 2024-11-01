package team.sailboat.commons.fan.excep;

import team.sailboat.commons.fan.collection.XC;

public class FormatException extends CodeException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public FormatException(String aMsg , Object...aArgs)
	{
		super(0 , XC.isNotEmpty(aArgs)?String.format(aMsg, aArgs):aMsg) ;
	}
	
	public FormatException(int aCode , String aMsg)
	{
		super(aCode , aMsg) ;
	}
	
	public FormatException(Throwable aE)
	{
		super(0 , aE) ;
	}

	public FormatException(Throwable aCause , String aMessage)
	{
		super(0 , aMessage, aCause);
	}

}
