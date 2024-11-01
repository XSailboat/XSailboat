package team.sailboat.commons.fan.lang;

public class First
{
	boolean mFirst = true ;
	
	public void checkAndNotFirstDo(Runnable aAction)
	{
		if(mFirst)
			mFirst = false ;
		else
			aAction.run();
	}
	
	public void checkAndFirstDo(Runnable aAction)
	{
		if(mFirst)
		{
			aAction.run();
			mFirst = false ;
		}
	}
	
	/**
	 * 如果是第一次check，将返回true，否则返回false
	 * @return
	 */
	public boolean checkDo()
	{
		if(mFirst)
		{
			mFirst = false ;
			return true ;
		}
		else
			return false ;
	}
	
	public void reset()
	{
		mFirst = true ;
	}
	
	/**
	 * 如果从未被“checkXXX”，将返回true，否则返回false
	 * @return
	 */
	public boolean test()
	{
		return mFirst ;
	}
}
