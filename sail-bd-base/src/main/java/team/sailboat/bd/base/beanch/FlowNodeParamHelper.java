package team.sailboat.bd.base.beanch;

import java.util.Collection;
import java.util.Map;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;

import team.sailboat.bd.base.def.FlowNodeParamType;
import team.sailboat.bd.base.def.FlowNodeSysParams;
import team.sailboat.bd.base.model.FlowNodeParam;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.text.XString;

public class FlowNodeParamHelper
{
	public static JSONObject buildArgumentsJo(Collection<FlowNodeParam> aParams 
			, JSONObject aSysParamValJo)
	{
		JSONObject paramValueJo = aSysParamValJo.clone() ;
		if(XC.isNotEmpty(aParams))
		{
			Map<String , Object> ctxMap = paramValueJo.toMap() ;
			for(FlowNodeParam param : aParams)
			{
				if(param.getType() == FlowNodeParamType.CustomParam)
				{
					String expr = param.getExpr() ;
					if(XString.isEmpty(expr))
					{
						paramValueJo.put(param.getName() , null , true) ;
					}
					else
					{
						Expression cexpr = AviatorEvaluator.compile(expr) ;
						paramValueJo.put(param.getName() , cexpr.execute(ctxMap) , true) ;
					}
				}
			}
		}
		
		return paramValueJo ;
	}
	
	/**
	 * 注入系统参数
	 * @param aParamValJo
	 * @return
	 */
	public static JSONObject injectSysParamValues(JSONObject aParamValJo 
			, boolean aOverrideExists
			, String aBizdate
			, String aFlowId , String aNodeId , String aTaskId)
	{
		if(aParamValJo == null)
			aParamValJo = new JSONObject() ;
		if(aOverrideExists)
			aParamValJo.put(FlowNodeSysParams.sBizdate , aBizdate) ;
		else
			aParamValJo.putIfAbsent(FlowNodeSysParams.sBizdate , aBizdate) ;
		aParamValJo.put(FlowNodeSysParams.sFlowId , aFlowId)
			.put(FlowNodeSysParams.sNodeId , aNodeId)
			.put(FlowNodeSysParams.sTaskId , aTaskId)
			;
		return aParamValJo ;
	}
}
