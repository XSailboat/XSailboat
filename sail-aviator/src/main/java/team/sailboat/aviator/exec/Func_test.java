package team.sailboat.aviator.exec;

import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;

import team.sailboat.commons.fan.lang.Assert;

public class Func_test extends AbstractFunction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv , AviatorObject aFunc , AviatorObject aEle1)
	{
		Assert.isTrue(aFunc instanceof AviatorFunction , "第1个参数需是一个函数") ;
		return ((AviatorFunction)aFunc).call(aEnv, aEle1) ;
	}
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv , AviatorObject aFunc , AviatorObject aEle1
			 , AviatorObject aEle2)
	{
		Assert.isTrue(aFunc instanceof AviatorFunction , "第1个参数需是一个函数") ;
		return ((AviatorFunction)aFunc).call(aEnv, aEle1 , aEle2) ;
	}

	@Override
	public String getName()
	{
		return "test" ;
	}

}
