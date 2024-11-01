package team.sailboat.commons.fan.exec;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import team.sailboat.commons.fan.lang.XClassUtil;

public class MethodRunner implements CRun
{

	Method mMethod ;
	Object mSource ;
	Object[] mArgs ;
	
	public MethodRunner(Method aMethod , Object aSource , Object...aArgs)
	{
		mMethod = aMethod ;
		mSource = aSource ;
		mArgs = aArgs ;
	}
	
	public MethodRunner(Object aObj , String aMethodName , Object...aArgs)
	{
		mSource = aObj ;
		mArgs = aArgs ;
		Class<?>[] clses = null ;
		if(aArgs != null)
		{
			clses = new Class<?>[aArgs.length] ;
			for(int i=0 ; i<aArgs.length ; i++)
				clses[i] = aArgs[i].getClass() ;
		}
		try
		{
			mMethod = XClassUtil.getMethod(aObj.getClass() , aMethodName, clses) ;
			mMethod.setAccessible(true) ;
		}
		catch (NoSuchMethodException e)
		{
			String argTypes = null ;
			if(aArgs != null && aArgs.length>0)
			{
				StringBuilder strBld = new StringBuilder() ;
				strBld.append("(") ;
				boolean first = true ;
				for(Object arg : aArgs)
				{
					if(first)
						first = false ;
					else
						strBld.append(" , ") ;
					strBld.append(arg.getClass().getName()) ;
				}
				strBld.append(")") ;
				argTypes = strBld.toString() ;
			}
			throw new IllegalStateException("类型"+aObj.getClass().getName()+"没有名为"+aMethodName
					+(argTypes != null?" , 参数为"+argTypes:"")+"的方法") ;
		}
		catch (SecurityException e)
		{
			throw new IllegalStateException(e) ;
		}
	}
	
	@Override
	public void run()
	{
		if(isClosing())
			return ;
		try
		{
			mMethod.invoke(mSource, mArgs) ;
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
	}

}
