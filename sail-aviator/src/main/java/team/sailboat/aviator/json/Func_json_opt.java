package team.sailboat.aviator.json;

import java.util.List;
import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.text.XString;

public class Func_json_opt extends AbstractFunction
{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aJa , AviatorObject aValue)
	{
		Object obj = aJa.getValue(aEnv) ;
		Assert.notNull(obj , "第1个参数不能为null") ;
		if(obj instanceof JSONArray)
		{
			Integer index = XClassUtil.toInteger(aValue.getValue(aEnv)) ;
			Assert.notNull(index , "第2个参数为JSONArry的元素序号，不能为null！") ;
			return AviatorRuntimeJavaType.valueOf(((JSONArray)obj).opt(index.intValue())) ;
		}
		else if(obj instanceof JSONObject)
		{
			String key = XClassUtil.toString(aValue.getValue(aEnv)) ;
			Assert.notNull(key , "第2个参数为JSONObject的键，不能为null！") ;
			return AviatorRuntimeJavaType.valueOf(((JSONObject)obj).opt(key)) ;
		}
		else if(obj instanceof List)
		{
			Integer index = XClassUtil.toInteger(aValue.getValue(aEnv)) ;
			Assert.notNull(index , "第2个参数为List的元素序号，不能为null！") ;
			return AviatorRuntimeJavaType.valueOf(((List)obj).get(index)) ;
		}
		else if(obj instanceof Map)
		{
			Object key = aValue.getValue(aEnv) ;
			Assert.notNull(key , "第2个参数为Map的键，不能为null！") ;
			return AviatorRuntimeJavaType.valueOf(((Map)obj).get(key)) ;
		}
		else if(obj instanceof String)
		{
			String text = ((String)obj).toString().trim() ;
			Assert.notEmpty(text , "第1个参数不能是空字符串！") ;
			switch(text.charAt(0))
			{
			case '{':
			{
				String key = XClassUtil.toString(aValue.getValue(aEnv)) ;
				Assert.notNull(key , "第2个参数为JSONObject的键，不能为null！") ;
				JSONObject jo = new JSONObject(text) ;
				return AviatorRuntimeJavaType.valueOf(jo.opt(key)) ;
			}
			case '[' :
			{
				Integer index = XClassUtil.toInteger(aValue.getValue(aEnv)) ;
				Assert.notNull(index , "第2个参数为JSONArry的元素序号，不能为null！") ;
				JSONArray ja = new JSONArray(text) ;
				return AviatorRuntimeJavaType.valueOf(ja.opt(index.intValue())) ;
			}
			default:
				throw new IllegalArgumentException("无法解析成JSONObject或者JSONArray的字符串："+text) ;
			}
		}
		else
			throw new IllegalArgumentException("未支持的类型："+obj.getClass().getName()) ;
	}
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aJa , AviatorObject aValue
			, AviatorObject aDefaultValue)
	{
		Object obj = aJa.getValue(aEnv) ;
		Object result = null ;
		if(obj == null)
		{
			
		}
		else if(obj instanceof JSONArray)
		{
			Integer index = XClassUtil.toInteger(aValue.getValue(aEnv)) ;
			if(index != null)
				result = ((JSONArray)obj).opt(index.intValue()) ;
		}
		else if(obj instanceof JSONObject)
		{
			String key = XClassUtil.toString(aValue.getValue(aEnv)) ;
			if(XString.isNotEmpty(key))
				result = ((JSONObject)obj).opt(key) ;
		}
		else if(obj instanceof List)
		{
			Integer index = XClassUtil.toInteger(aValue.getValue(aEnv)) ;
			if(index != null)
				result = ((List)obj).get(index) ;
		}
		else if(obj instanceof Map)
		{
			Object key = aValue.getValue(aEnv) ;
			if(key != null)
				result = ((Map)obj).get(key) ;
		}
		else if(obj instanceof String)
		{
			String text = ((String)obj).toString().trim() ;
			if(!text.isEmpty())
			{
				switch(text.charAt(0))
				{
				case '{':
				{
					String key = XClassUtil.toString(aValue.getValue(aEnv)) ;
					if(XString.isNotEmpty(key))
					{
						JSONObject jo = new JSONObject(text) ;
						result = jo.opt(key) ;
					}
					break ;
				}
				case '[' :
				{
					Integer index = XClassUtil.toInteger(aValue.getValue(aEnv)) ;
					if(index != null)
					{
						JSONArray ja = new JSONArray(text) ;
						result = ja.opt(index.intValue()) ;
					}
					break ;
				}
				default:
				}
			}
		}
		return result == null?aDefaultValue:AviatorRuntimeJavaType.valueOf(result) ;
 	}

	@Override
	public String getName()
	{
		return "json.opt" ;
	}

}
