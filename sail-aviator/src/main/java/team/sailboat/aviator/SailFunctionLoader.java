package team.sailboat.aviator;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.googlecode.aviator.FunctionLoader;
import com.googlecode.aviator.runtime.type.AviatorFunction;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.text.XString;

public class SailFunctionLoader implements FunctionLoader
{
	final Map<String , AviatorFunction> mFuncMap = XC.concurrentHashMap() ;
	
	final Set<String> mPackages = XC.hashSet() ;
	
	final Object mMutex = new Object() ;
	
	public SailFunctionLoader()
	{
	}
	
	public SailFunctionLoader packages(String... aPackages)
	{
		if(XC.isEmpty(aPackages))
			return this ;
		for(String pkg : aPackages)
		{
			if(XString.isNotEmpty(pkg) && mPackages.add(pkg))
			{
				List<Class<?>> classList = XClassUtil.getClasses(pkg , getClass().getClassLoader()) ;
				if(XC.isNotEmpty(classList))
				{
					for(Class<?> clazz : classList)
					{
						int md = clazz.getModifiers() ;
						if(AviatorFunction.class.isAssignableFrom(clazz)
								&& !Modifier.isAbstract(md)
								&& Modifier.isPublic(md))
						{
							if(!Modifier.isPublic(clazz.getModifiers()))
								continue ;
							try
							{
								AviatorFunction func = (AviatorFunction)clazz
										.getConstructor()
										.newInstance() ;
								mFuncMap.putIfAbsent(func.getName() , func) ;
							}
							catch (Exception e)
							{
								System.out.println(clazz.getName()) ;
								e.printStackTrace();
							}
						}
						
					}
				}
			}
		}
		return this ;
	}

	@Override
	public AviatorFunction onFunctionNotFound(String aName)
	{
		return mFuncMap.get(aName) ;
	}

}
