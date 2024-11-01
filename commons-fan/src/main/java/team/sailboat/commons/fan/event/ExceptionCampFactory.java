package team.sailboat.commons.fan.event;

import team.sailboat.commons.fan.collection.XC;

public class ExceptionCampFactory
{
	static Class<? extends ExceptionCamp> sClass ;
	static ExceptionCamp sDefault ;
	
	static IExceptionHandler[] sCommonHandlers ;
	
	public static void setExceptionCampClass(Class<? extends ExceptionCamp> aClass)
	{
		if(sClass != null)
			sClass = aClass ;
	}
	
	public static ExceptionCamp getDefault()
	{
		if(sDefault == null)
		{
			if(sClass == null)
				sDefault = new ExceptionCamp() ;
			else
				try
				{
					sDefault = sClass.getConstructor().newInstance() ;
				}
				catch (Exception e)
				{
					throw new IllegalStateException(sClass.getName()+"必须有公开的缺省构造函数") ;
				}
			if(sCommonHandlers != null && sCommonHandlers.length>0)
			{
				for(IExceptionHandler handler : sCommonHandlers)
					sDefault.addExceptionHandler(handler) ;
			}
		}
		return sDefault ;
	}
	
	public static ExceptionCamp newExceptionCamp()
	{
		ExceptionCamp camp = null ;
		if(sClass == null)
			camp = new ExceptionCamp() ;
		else
			try
			{
				camp = sClass.getConstructor().newInstance() ;
			}
			catch (Exception e)
			{
				throw new IllegalStateException(sClass.getName()+"必须有公开的缺省构造函数") ;
			}
		if(sCommonHandlers != null && sCommonHandlers.length>0)
		{
			for(IExceptionHandler handler : sCommonHandlers)
				camp.addExceptionHandler(handler) ;
		}
		return camp ;
	}
	
	public static void addCommonExceptionHandler(IExceptionHandler aHandler)
	{
		if(sCommonHandlers == null)
		{
			sCommonHandlers = new IExceptionHandler[]{aHandler} ;
			if(sDefault != null)
				sDefault.addExceptionHandler(aHandler);
		}
		else if(!XC.contains(sCommonHandlers, aHandler))
		{
			sCommonHandlers = XC.merge(sCommonHandlers, aHandler) ;
			if(sDefault != null)
				sDefault.addExceptionHandler(aHandler);
		}
	}
}
