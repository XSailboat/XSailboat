package team.sailboat.bd.base.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import team.sailboat.bd.base.model.ContextOutputParam;
import team.sailboat.bd.base.model.ParamSource;
import team.sailboat.bd.base.model.ParamType;
import team.sailboat.commons.fan.collection.XC;

@Schema(description = "上下文输出参数")
@Data
public class NodeOutputCtxParam
{
	@Schema(description = "参数名")
	String name ;
	
	@Schema(description = "类型，常量/变量")
	String type ;
	
	@Schema(description = "参数取值")
	String value ;
	
	@Schema(description = "描述")
	String description ;
	
	@Schema(description = "来源，auto（系统默认添加）/manual（手动添加）")
	String paramSource ;
	
	public static ArrayList<NodeOutputCtxParam> ofList(Collection<ContextOutputParam> aParamList)
	{
		ArrayList<NodeOutputCtxParam> paramList = XC.arrayList() ;
		for(ContextOutputParam param : aParamList)
			paramList.add(NodeOutputCtxParam.of(param)) ;
		return paramList ;
	}
	
	public static NodeOutputCtxParam of(ContextOutputParam aOutputParam)
	{
		NodeOutputCtxParam param = new NodeOutputCtxParam() ;
		param.setName(aOutputParam.getName()) ;
		param.setDescription(aOutputParam.getDescription()) ;
		param.setParamSource(aOutputParam.getParamSource().name()) ;
		param.setType(aOutputParam.getParamType().name()) ;
		param.setValue(aOutputParam.getValue()) ;
		return param ;
	}
	
	public static List<ContextOutputParam> asList(Collection<NodeOutputCtxParam> aParams , String aNodeId)
	{
		return XC.extractAsArrayList(aParams, (p)->as(p, aNodeId)) ;
	}
	
	public static ContextOutputParam as(NodeOutputCtxParam aParam , String aNodeId)
	{
		ContextOutputParam param = new ContextOutputParam(aParam.getName()) ;
		param.setDescription(aParam.getDescription()) ;
		param.setParamSource(ParamSource.valueOf(aParam.getParamSource())) ;
		param.setParamType(ParamType.valueOf(aParam.getType())) ;
		param.setValue(aParam.getValue()) ;
		param.setSourceNodeId(aNodeId);
		return param ;
	}
}
