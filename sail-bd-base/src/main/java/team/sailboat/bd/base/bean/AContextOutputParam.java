package team.sailboat.bd.base.bean;

import java.util.Collection;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import team.sailboat.bd.base.model.ContextOutputParam;
import team.sailboat.commons.fan.collection.XC;

@Schema(description = "节点上下文输出参数\"")
@Data
public class AContextOutputParam
{
	
	@Schema(description = "参数名称")
	String name ;
	
	@Schema(description = "输出此参数的节点id")
	String sourceNodeId ;
	
	@Schema(description = "参数类型" , allowableValues = {"常量","变量"})
	String paramType ;
	
	@Schema(description = "参数取值")
	String value ;
	
	@Schema(description = "描述")
	String description ;
	
	/**
	 * 参数来源，手动添加、自动添加
	 */
	@Schema(description = "参数来源" , allowableValues = { "manual","auto"})
	String paramSource ;
	
	public static AContextOutputParam of(ContextOutputParam aParam)
	{
		if(aParam == null)
			return null ;
		AContextOutputParam param = new AContextOutputParam() ;
		return param ;
	}
	
	public static List<AContextOutputParam> ofList(Collection<ContextOutputParam> aParams)
	{
		if(XC.isEmpty(aParams))
			return null ;
		List<AContextOutputParam> paramList = XC.arrayList() ;
		for(ContextOutputParam param : aParams)
			paramList.add(of(param)) ;
		return paramList ;
	}
}
