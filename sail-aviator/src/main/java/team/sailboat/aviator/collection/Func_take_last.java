package team.sailboat.aviator.collection;

import java.util.List;
import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorNil;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONArray;

public class Func_take_last extends AbstractFunction
{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aArg1)
	{
		Object val = aArg1.getValue(aEnv) ;
		if(val == null || (val instanceof String && ((String)val).isEmpty()))
		{
			return AviatorNil.NIL ;
		}
		if(val instanceof List)
		{
			Object result = XC.removeLast((List)val) ;
			return result == null? AviatorNil.NIL:AviatorRuntimeJavaType.valueOf(result) ;
		}
		else if(val instanceof JSONArray)
		{
			JSONArray ja = (JSONArray)val ;
			Object result = ja.removeLast() ;
			return result == null? AviatorNil.NIL:AviatorRuntimeJavaType.valueOf(result) ;
		}
		else
			throw new IllegalArgumentException("指定的第一个参数不是List，而是"+val.getClass().getName()) ;
		
	}

	@Override
	public String getName()
	{
		return "cs.take_last" ;
	}

}