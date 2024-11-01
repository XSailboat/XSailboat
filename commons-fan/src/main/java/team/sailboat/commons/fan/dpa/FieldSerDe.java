package team.sailboat.commons.fan.dpa;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import team.sailboat.commons.fan.dpa.anno.BForwardMethod;
import team.sailboat.commons.fan.dpa.anno.BReverseMethod;
import team.sailboat.commons.fan.excep.WrapException;

public class FieldSerDe implements IFieldSerDe
{
	
	Method mSerMethod ;
	
	Method mDeMethod ;
	
	Class<?> mFieldType ;
	
	boolean mUseFieldType = false ;
	
	public FieldSerDe()
	{
	}
	
	public FieldSerDe(Class<?> aFieldType , Class<?> aSerClass , Class<?> aDeClass , Class<?> aSerDeClass)
	{
		mFieldType = aFieldType ;
		if(Object.class.equals(aSerClass))
			aSerClass = aSerDeClass ;
		if(!Object.class.equals(aSerClass) && aSerClass != null)
		{
			Method[] methods = aSerClass.getDeclaredMethods() ;
			for(Method method : methods)
			{
				if(Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers())
						&& method.getAnnotation(BForwardMethod.class) != null)
				{
					mSerMethod = method ;
					break ; 
				}
			}
		}
		if(Object.class.equals(aDeClass))
			aDeClass = aSerDeClass ;
		if(!Object.class.equals(aDeClass) && aDeClass != null)
		{
			Method[] methods = aDeClass.getDeclaredMethods() ;
			for(Method method : methods)
			{
				if(Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers())
						&& method.getAnnotation(BReverseMethod.class) != null)
				{
					mDeMethod = method ;
					if(mDeMethod.getParameterCount() == 2)
						mUseFieldType = true ;
					break ; 
				}
			}
		}
		
	}
	
	@Override
	public Object forward(Object aObj)
	{
		if(mSerMethod == null)
			return aObj ;
		else
		{
			try
			{
				return mSerMethod.invoke(null, aObj) ;
			}
			catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				WrapException.wrapThrow(e) ;
				return null ;			// dead code
			}
		}
	}
	
	@Override
	public Object reverse(Object aObj)
	{
		if(mDeMethod == null)
			return aObj ;
		else
		{
			try
			{
				return mUseFieldType?mDeMethod.invoke(null, aObj , mFieldType):mDeMethod.invoke(null, aObj) ;
			}
			catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				WrapException.wrapThrow(e) ;
				return null ;			// dead code
			}
		}
	}
	
}
