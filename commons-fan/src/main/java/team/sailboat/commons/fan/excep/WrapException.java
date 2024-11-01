package team.sailboat.commons.fan.excep;

import team.sailboat.commons.fan.text.XString;

/**
 * 
 *
 * @author yyl
 * @since 2017年8月22日
 */
public class WrapException extends RuntimeException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String mExtendMsg ;
	
	public WrapException(Throwable aCause)
	{
		super(aCause) ;
	}
	
	public WrapException(Throwable aCause , String aMsg)
	{
		super(aMsg , aCause) ;
	}
	
	@Override
	public String getMessage()
	{
		if(XString.isNotEmpty(mExtendMsg))
		{
			return super.getMessage()+"  "+mExtendMsg ;
		}
		else
			return super.getMessage() ;
	}

	public static void wrapThrow(Throwable e)
	{
		if(e instanceof WrapException)
			throw (WrapException)e ;
		else
			throw new WrapException(e) ;
 	}
	
	public static void wrapThrow(Throwable e , String aMsg , Object...aArgs)
	{
		if(e instanceof WrapException)
		{
			((WrapException)e).mExtendMsg = XString.msgFmt(aMsg , aArgs) ;
			throw (WrapException)e ;
		}
		else
			throw new WrapException(e , XString.msgFmt(aMsg , aArgs)) ;
 	}
	
	public static Throwable unwrap(Throwable e)
	{
		if(e instanceof WrapException)
			return e.getCause() ;
		return e ;
	}
}
