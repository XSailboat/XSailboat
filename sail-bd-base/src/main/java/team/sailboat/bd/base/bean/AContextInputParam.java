package team.sailboat.bd.base.bean;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import team.sailboat.base.util.IWSDBeanIdHelper;
import team.sailboat.bd.base.model.ContextInputParam;
import team.sailboat.bd.base.model.ContextOutputParam;
import team.sailboat.commons.fan.collection.XC;

@Schema(name = "AContextInputParam" , description = "节点上下文输入参数" )
@Data
public class AContextInputParam
{

	@Schema(description = "参数名称")
	String name ;
	
	@Schema(description = "输出此参数的节点的缺省阀id")
	String sourceValveId ;
	
	@Schema(description = "此参数的原始名称")
	String originaName ;

	@Schema(description = "描述，此参数的原始描述")
	String description ;
	
	@Schema(description = "不是此参数的原始来源，而是作为当前节点的输入参数，是手动添加的，还是系统自动添加的")
	String paramSource ;
	
	/**
	 * 用来显示的数据来源信息。<br />
	 * 格式：工作空间名称.阀id:原参数名称
	 */
	@Schema(description = "用来显示的“取值来源”")
	String displaySource ;
	
	/**
	 * 
	 * @param aParam
	 * @param aWsNamePvd
	 * @param aOutParamPvd			通过阀id和参数名，取得上下文输出参数
	 * @return
	 */
	public static AContextInputParam of(ContextInputParam aParam , Function<String, String> aWsNamePvd
			, BiFunction<String, String , ContextOutputParam> aOutParamPvd)
	{
		if(aParam == null)
			return null ;
		AContextInputParam param = new AContextInputParam() ;
		param.setName(aParam.getName()) ;
		param.setOriginaName(aParam.getOriginalName()) ;
		param.setParamSource(aParam.getParamSource().name()) ;
		param.setSourceValveId(aParam.getSourceValveId()) ;
		// 设置displaysource，格式：工作空间名称.阀id:原参数名称
		String wsId = IWSDBeanIdHelper.getWsIdFromDBeanId(aParam.getSourceValveId()) ;
		param.setDisplaySource(aWsNamePvd.apply(wsId)+"."+aParam.getSourceValveId()+":"+aParam.getOriginalName()) ;
		// 设置description，需要从定义这个参数的ContextOutputParam中获取 
		// 通过阀id，取得节点
		ContextOutputParam outParam = aOutParamPvd.apply(aParam.getSourceValveId() , aParam.getOriginalName()) ;
		param.setDescription(outParam != null? outParam.getDescription():"<! 无法取得此输入节点所引用的输出节点 >") ;
		
		return param ;
	}
	
	public static List<AContextInputParam> ofList(Collection<ContextInputParam> aParams
			, Function<String, String> aWsNamePvd 
			, BiFunction<String, String , ContextOutputParam> aOutParamPvd)
	{
		if(XC.isEmpty(aParams))
			return null ;
		List<AContextInputParam> paramList = XC.arrayList() ;
		for(ContextInputParam param : aParams)
			paramList.add(of(param , aWsNamePvd , aOutParamPvd)) ;
		return paramList ;
	}
}
