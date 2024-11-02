package team.sailboat.bd.base.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import team.sailboat.base.util.IWSDBeanIdHelper;
import team.sailboat.bd.base.ZBDException;
import team.sailboat.bd.base.beanch.IFlowDiscovery;
import team.sailboat.bd.base.model.ContextInputParam;
import team.sailboat.bd.base.model.ContextOutputParam;
import team.sailboat.bd.base.model.IFlowNode;
import team.sailboat.bd.base.model.IFlowValve;
import team.sailboat.bd.base.model.ParamIOContext;
import team.sailboat.bd.base.model.ParamSource;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.Assert;

@Schema(description = "上下文输入参数")
@Data
public class NodeInputCtxParam
{
	
	@Schema(description = "参数名")
	String name ;
	
	@Schema(description = "取值来源，格式：工作空间名.阀id:原参数名")
	String source ;
	
	@Schema(description = "描述")
	String description ;
	
	@Schema(description = "父节点id")
	String parentNodeId ;
	
	@Schema(description = "来源")
	String paramSource ;
	
	public static ArrayList<NodeInputCtxParam> ofList(Collection<ContextInputParam> aParams , IFlowDiscovery aDiscovery)
			throws ZBDException
	{
		ArrayList<NodeInputCtxParam> paramList = XC.arrayList() ;
		for(ContextInputParam param : aParams)
			paramList.add(of(param, aDiscovery)) ;
		return paramList ;
	}
	
	public static NodeInputCtxParam of(ContextInputParam aParam , IFlowDiscovery aDiscovery)
			throws ZBDException
	{
		NodeInputCtxParam param = new NodeInputCtxParam() ;
		param.setName(aParam.getName()) ;
		param.setParamSource(aParam.getParamSource().name()) ;
		String valveId = aParam.getSourceValveId() ;
		IFlowValve valve = aDiscovery.getFlowValve(valveId) ;
		param.setParentNodeId(valve.getSourceNodeId()) ;
		// 得取出它的输出定义，才能取得描述
		IFlowNode parent = aDiscovery.getFlowNode(valve.getSourceNodeId()) ;
		ParamIOContext ctx = parent.getParamIOContext() ;
		boolean setted = false ;
		if(ctx != null)
		{
			ContextOutputParam outParam = ctx.getOutputParam(aParam.getOriginalName()) ;
			if(outParam != null)
			{
				param.setDescription(outParam.getDescription()) ;
				setted = true ;
			}
		}
		if(!setted)
		{
			param.setDescription("<! 无法取得此节点上下文输入参数所引用的输出参数 >");
		}
		String wsId = IWSDBeanIdHelper.getWsIdFromDBeanId(valveId) ;
		String wsName = aDiscovery.getWorkspaceName(wsId) ;
 		param.setSource(wsName+"."+valveId+":"+aParam.getOriginalName()) ;
		return param ;
	}
	
	public static List<ContextInputParam> asList(Collection<NodeInputCtxParam> aParams)
	{
		return XC.extractAsArrayList(aParams, NodeInputCtxParam::as) ;
	}
	
	public static ContextInputParam as(NodeInputCtxParam aParam)
	{
		String source = aParam.getSource() ;
		Assert.notEmpty(source , "取值来源不能为空") ;
		int i = source.indexOf('.') ;
		Assert.isTrue(i>0 , "取值来源格式不正确！") ;
		int j = source.indexOf(':' , i+1) ;
		Assert.isTrue(j>0 , "取值来源格式不正确！") ;
		String originalParamName = source.substring(j+1) ;
		String valveId = source.substring(i+1, j) ;
		ContextInputParam param = new ContextInputParam() ;
		param.setName(aParam.getName()) ;
		param.setOriginalName(originalParamName) ;
		param.setParamSource(ParamSource.valueOf(aParam.getParamSource())) ;
		param.setSourceValveId(valveId);
		return param ;
	}
}
