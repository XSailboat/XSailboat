package team.sailboat.aviator.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorNil;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import team.sailboat.commons.fan.lang.Assert;

/**
 * 减去指定的集合，返回一个新集合
 *
 * @author yyl
 * @since 2023年3月2日
 */
public class Func_subtract extends AbstractFunction
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
		List<Object> list = new ArrayList<Object>((Collection)val) ;
		Object removeVals = aArg2.getValue(aEnv) ;
		if(removeVals != null)
		{
			if(removeVals instanceof Collection)
			{
				list.removeAll((Collection)removeVals) ;
			}
			else
				list.remove(removeVals) ;
		}
		return AviatorRuntimeJavaType.valueOf(list) ;
	}

	@Override
	public String getName()
	{
		return "cs.subtract" ;
	}

}
