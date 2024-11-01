package team.sailboat.aviator.json;

import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

public class Func_j_object_entries extends AbstractFunction
{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aObj)
	{
		 return AviatorRuntimeJavaType.valueOf(Func_json_object.of(aEnv, aObj).entrySet()) ;
	}

	@Override
	public String getName()
	{
		return "j.object.entries" ;
	}

}
