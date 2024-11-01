package team.sailboat.commons.ms.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

import io.swagger.v3.oas.annotations.media.Schema;
import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.infc.EFunction;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;
import team.sailboat.commons.fan.lang.XClassUtil;

public interface ISwaggerJBean extends ToJSONObject
{
	@Schema(hidden = true)
	@Override
	default JSONObject setTo(JSONObject aJSONObj)
	{
		String ckey = "ISwaggerJBean_"+getClass().getName() ;
		Map<String, Function<Object, Object>> funcMap = (Map<String, Function<Object, Object>>) AppContext.get(ckey) ;
		if(funcMap == null)
		{
			funcMap = XC.hashMap() ;
			for(Field field : XClassUtil.getAllFieldsList(getClass()))
			{
				Annotation anno = field.getAnnotation(Schema.class) ;
				if(anno != null)
				{
					// JSONObject 的键
					String key = XClassUtil.toCommonFieldName(field.getName()) ;
					// 看看有没有getter方法，有的话优先取用getter方法的 -- 暂时不做
					
					//
					field.setAccessible(true) ;
					funcMap.put(key, EFunction.silence(field::get)) ;
				}
			}
			
			for(Method method : XClassUtil.getAllMethodList(getClass()))
			{
				Annotation anno = method.getAnnotation(Schema.class) ;
				if(anno != null)
				{
					// JSONObject 的键
					String key = XClassUtil.toCommonFieldName(method.getName()) ;
					// 看看有没有getter方法，有的话优先取用getter方法的 -- 暂时不做
					
					//
					method.setAccessible(true) ;
					funcMap.put(key, EFunction.silence(method::invoke)) ;
				}
			}
			AppContext.set(ckey, funcMap) ;
		}
		funcMap.forEach((key , func)->aJSONObj.put(key, func.apply(this)));
		return aJSONObj ;
	}
}
