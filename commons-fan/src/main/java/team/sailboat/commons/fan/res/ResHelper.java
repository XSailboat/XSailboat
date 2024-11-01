package team.sailboat.commons.fan.res;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.XClassUtil;

public class ResHelper
{
	static final Map<Class<?>, Object> sIsClosedMethodMap = XC.concurrentHashMap() ;
	static final Object sNoMethod = new Object() ;
	
	public static boolean isClosed(Object aRes)
	{
		Assert.notNull(aRes) ;
		Object method_0 = sIsClosedMethodMap.get(aRes.getClass()) ;
		try
		{
			if(method_0 == null)
			{
				Method method = XClassUtil.getMethod0(aRes.getClass(), "isClosed") ;
				if(method != null)
				{
					if(!method.canAccess(aRes))
					{
						method.setAccessible(true) ;
					}
					sIsClosedMethodMap.put(aRes.getClass() , method) ;
					method_0 = method ;
				}
				else
				{
					sIsClosedMethodMap.put(aRes.getClass() , sNoMethod) ;
					method_0 = sNoMethod ;
				}
			}
			if(method_0 == sNoMethod)
				throw new UnsupportedOperationException("类["+aRes.getClass().getName()+"]没有isClosed方法！") ;
					
			Object val = ((Method)method_0).invoke(aRes) ;
			if(val == null)
				throw new IllegalStateException("类["+aRes.getClass().getName()+"]的isClosed方法返回了null ") ;
			return (Boolean)val ;
		}
		catch (SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{
			WrapException.wrapThrow(e) ;
			return false ;			// dead code
		}
	}
	
	public static boolean isClosed(Object aRes , boolean aDefaultValue)
	{
		Assert.notNull(aRes) ;
		Object method_0 = sIsClosedMethodMap.get(aRes.getClass()) ;
		try
		{
			if(method_0 == null)
			{
				Method method = XClassUtil.getMethod0(aRes.getClass(), "isClosed") ;
				if(method != null)
				{
					if(!method.canAccess(aRes))
					{
						method.setAccessible(true) ;
					}
					sIsClosedMethodMap.put(aRes.getClass() , method) ;
					method_0 = method ;
				}
				else
				{
					sIsClosedMethodMap.put(aRes.getClass() , sNoMethod) ;
					method_0 = sNoMethod ;
				}
			}
			if(method_0 == sNoMethod)
				return aDefaultValue ;
					
			Object val = ((Method)method_0).invoke(aRes) ;
			if(val == null)
				return aDefaultValue ;
			return (Boolean)val ;
		}
		catch (SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e)
		{
			WrapException.wrapThrow(e) ;
			return false ;			// dead code
		}
	}
}
