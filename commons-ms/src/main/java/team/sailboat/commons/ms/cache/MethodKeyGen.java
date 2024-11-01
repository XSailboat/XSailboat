package team.sailboat.commons.ms.cache;

import java.lang.reflect.Method;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import team.sailboat.commons.fan.text.XString;

@Component
public class MethodKeyGen implements KeyGenerator
{
	
	public MethodKeyGen()
	{
	}

	@Override
	public Object generate(Object aTarget, Method aMethod, Object... aParams)
	{
		String objId = getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(aTarget)) ;
		return objId +"."+aMethod.getName()+XString.toString(",", "(", ")", aParams) ;
	}

}
