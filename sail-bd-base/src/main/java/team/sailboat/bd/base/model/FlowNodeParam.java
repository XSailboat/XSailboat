package team.sailboat.bd.base.model;

import java.util.Collection;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import team.sailboat.base.def.ConfigItemSource;
import team.sailboat.bd.base.def.FlowNodeParamType;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dpa.anno.BForwardMethod;
import team.sailboat.commons.fan.dpa.anno.BReverseMethod;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.text.XString;

/**
 * 流程节点的参数
 *
 * @author yyl
 * @since 2023年5月20日
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlowNodeParam
{
	@Schema(description = "参数名")
	String name ;
	
	@Schema(description = "参数类型")
	FlowNodeParamType type ;

	@Schema(description = "参数值/表达式。只有自定义参数有效，其它类型参数无效")
	String expr ;
	
	@Schema(description = "配置项来源")
	ConfigItemSource source ;
	
	static FlowNodeParam buildFromJSON(JSONObject aJo)
	{
		if(aJo == null || aJo.isEmpty())
			return null ;
		return  new FlowNodeParam(aJo.optString("name")
				, aJo.optEnum("type" , FlowNodeParamType.class)
				, aJo.optString("expr")
				, aJo.optEnum("source" , ConfigItemSource.class))
				;
	}
	
	public static class SerDe_List
	{
		@BForwardMethod
		public static String forward(Collection<FlowNodeParam> aParams)
		{
			if(XC.isEmpty(aParams))
				return null ;
			return new JSONArray(aParams)
					.toJSONString() ;
		}
		
		@BReverseMethod
		public static List<FlowNodeParam> reverse(Object aSource)
		{
			if(aSource == null)
				return null ;
			JSONArray ja = null ;
			if(aSource instanceof JSONArray)
				ja = (JSONArray)aSource ;
			else
			{
				String jaStr = aSource.toString() ;
				if(XString.isEmpty(jaStr))
					return null ;
				ja = new JSONArray(jaStr) ;
			}
			List<FlowNodeParam> list = XC.arrayList() ;
	 		ja.forEachJSONObject(jo->{
	 			list.add(FlowNodeParam.buildFromJSON(jo)) ;
	 		}) ;
			return list ;
		}
	}
	
//	public static class SerDe_Map
//	{
//		@BForwardMethod
//		public static String forward(Map<String, FlowNodeParam> aParamsMap)
//		{
//			if(XCollections.isEmpty(aParamsMap))
//				return null ;
//			return new JSONArray(aParamsMap.values())
//					.toJSONString() ;
//		}
//		
//		@BReverseMethod
//		public static Map<String, FlowNodeParam> reverse(Object aSource)
//		{
//			if(aSource == null)
//				return null ;
//			JSONArray ja = null ;
//			if(aSource instanceof JSONArray)
//				ja = (JSONArray)aSource ;
//			else
//			{
//				String jaStr = aSource.toString() ;
//				if(XString.isEmpty(jaStr))
//					return null ;
//				ja = new JSONArray(jaStr) ;
//			}
//			Map<String, FlowNodeParam> map = CS.linkedHashMap() ;
//	 		ja.forEachJSONObject(jo->{
//	 			FlowNodeParam param = FlowNodeParam.buildFromJSON(jo) ;
//	 			map.put(param.mName , param) ;
//	 		}) ;
//			return map ;
//		}
//	}
}
