package team.sailboat.aviator.json;

import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

/**
 * 
 *
 * @author yyl
 * @since 2024年9月30日
 */
public class Func_json_object_entries extends AbstractFunction
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
		return "json.object.entries" ;
	}

}
