package team.sailboat.aviator.json;

import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.XClassUtil;

/**
 * 
 *
 * @author yyl
 * @since 2024年9月30日
 */
public class Func_j_array_put extends AbstractFunction
{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aJa , AviatorObject aValue)
	{
		Object obj = aJa.getValue(aEnv) ;
		JSONArray ja = null ;
		if(obj == null)
			ja = new JSONArray() ;
		else if(obj instanceof JSONArray)
			ja = (JSONArray)obj ;
		else
			ja = Func_j_array.of(aEnv, aJa) ;
		ja.put(aValue.getValue(aEnv)) ;
		return AviatorRuntimeJavaType.valueOf(ja) ;
	}
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aJa
			, AviatorObject aValue
			, AviatorObject aIndex)
	{	
		Object obj = aIndex.getValue(aEnv) ;
		Integer index = XClassUtil.toInteger(obj) ;
		Assert.notNull(index , "第2个参数是序号，应该是整数，不能是%s！" , obj) ;
		
		obj = aJa.getValue(aEnv) ;
		JSONArray ja = null ;
		if(obj == null)
			ja = new JSONArray() ;
		else if(obj instanceof JSONArray)
			ja = (JSONArray)obj ;
		else
			ja = Func_j_array.of(aEnv, aJa) ;
		ja.put(index , aValue.getValue(aEnv)) ;
		return AviatorRuntimeJavaType.valueOf(ja) ;
	}

	@Override
	public String getName()
	{
		return "j.array.put" ;
	}

}
