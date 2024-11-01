package team.sailboat.aviator.collection;

import java.util.Collection;
import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorNil;
import com.googlecode.aviator.runtime.type.AviatorObject;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.Assert;

/**
 * 减去指定的集合，返回一个新集合
 *
 * @author yyl
 * @since 2023年3月2日
 */
public class Func_contains_any extends AbstractFunction
{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aArg1, AviatorObject aArg2)
	{
		Object val = aArg1.getValue(aEnv) ;
		if(val == null || (val instanceof String && ((String)val).isEmpty()))
		{
			return AviatorNil.NIL ;
		}
		Assert.isTrue(val instanceof Collection , "指定的第一个参数不是Collection，而是%s", val.getClass().getName()) ;
		Object testVals = aArg2.getValue(aEnv) ;
		if(testVals != null)
		{
			if(testVals instanceof Collection)
			{
				return AviatorBoolean.valueOf(XC.containsAny((Collection)val , (Collection)testVals)) ;
			}
			else if(testVals.getClass().isArray())
				return AviatorBoolean.valueOf(XC.containsAny((Collection)val , (Object[])testVals)) ;
			else
				return AviatorBoolean.valueOf(((Collection)val).contains(testVals)) ;
		}
		return AviatorBoolean.FALSE ;
	}

	@Override
	public String getName()
	{
		return "cs.contains_any" ;
	}

}
