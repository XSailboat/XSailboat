package team.sailboat.aviator.json;

import java.util.List;
import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.text.XString;

public class Func_json_array extends AbstractFunction
{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aJa)
	{
		return AviatorRuntimeJavaType.valueOf(of(aEnv, aJa)) ;
	}

	@Override
	public String getName()
	{
		return "json.array" ;
	}
	
	public static List<Object> of(Map<String, Object> aEnv, AviatorObject aJa)
	{
		Object jaO = aJa.getValue(aEnv) ;
		List<Object> list = XC.arrayList() ;
		String jaStr = XClassUtil.toString(jaO) ;
		if(XString.isNotEmpty(jaStr))
			new JSONArray(jaStr).toList(list) ;
		return list ;
	}

}
