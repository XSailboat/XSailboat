package team.sailboat.aviator.json;

import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.json.JSONException;
import team.sailboat.commons.fan.json.JSONObject;

/**
 * 
 *
 * @author yyl
 * @since 2024年9月30日
 */
public class Func_json_object extends AbstractFunction
{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv)
	{
		return AviatorRuntimeJavaType.valueOf(XC.linkedHashMap()) ;
	}
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aObj)
	{
		return AviatorRuntimeJavaType.valueOf(of(aEnv, aObj)) ;
	}

	@Override
	public String getName()
	{
		return "json.object" ;
	}
	
	public static Map<String , Object> of(Map<String, Object> aEnv, AviatorObject aObj)
	{
		Object obj = aObj.getValue(aEnv) ;
		Map<String, Object> map = XC.linkedHashMap() ;
		if(obj != null)
		{
			String str = obj.toString() ;
			try
			{
				new JSONObject(str).toMap(map) ;
			}
			catch(JSONException je)
			{
				throw new IllegalStateException(ExceptionAssist.getRootException(je) + "\tJSON字符串解析失败："+str) ;
			}
		}
		return map ;
	}

}
