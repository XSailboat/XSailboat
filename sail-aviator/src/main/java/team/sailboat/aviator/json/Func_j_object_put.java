package team.sailboat.aviator.json;

import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.XClassUtil;

public class Func_j_object_put extends AbstractFunction
{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aJa , AviatorObject aKey
			, AviatorObject aValue)
	{
		Object obj = aJa.getValue(aEnv) ;
		JSONObject jo = null ;
		if(obj == null)
			jo = new JSONObject() ;
		else if(obj instanceof JSONObject)
			jo = (JSONObject)obj ;
		else
			jo = Func_j_object.of(aEnv, aJa) ;
		jo.put(XClassUtil.toString(aKey.getValue(aEnv)) , aValue.getValue(aEnv)) ;
		return AviatorRuntimeJavaType.valueOf(jo) ;
	}

	@Override
	public String getName()
	{
		return "j.object.put" ;
	}

}
