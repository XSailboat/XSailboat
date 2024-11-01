package team.sailboat.commons.fan.excep;

public class CodeException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public final int mCode ;
	
	public CodeException(int aCode)
	{
		super() ;
		mCode = aCode ;
	}

	public CodeException(int aCode , String aMessage, Throwable aCause)
	{
		super(aMessage, aCause);
		mCode = aCode ;
	}

	public CodeException(int aCode , String aMessage)
	{
		super(aMessage);
		mCode = aCode ;
	}

	public CodeException(int aCode , Throwable aCause)
	{
		super(aCause);
		mCode = aCode ;
	}
	
}
