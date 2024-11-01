package team.sailboat.base.dataset;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import team.sailboat.base.util.JacksonUtils;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dpa.anno.BForwardMethod;
import team.sailboat.commons.fan.dpa.anno.BReverseMethod;

/**
 * 数据源描述信息
 *
 * @author yyl
 * @since 2021年12月14日
 */
@JsonTypeInfo(
	    use = JsonTypeInfo.Id.NAME, // Were binding by providing a name
	    include = JsonTypeInfo.As.PROPERTY, // The name is provided in a property
	    property = "source", // Property name is type
	    visible = true // Retain the value of type after deserialisation
	)
	@JsonSubTypes({//Below, we define the names and the binding classes.
	    @JsonSubTypes.Type(value = DatasetDesc_Sql.class, name = "Sql") ,
	    @JsonSubTypes.Type(value = DatasetDesc_Api.class, name = "Api") ,
	    @JsonSubTypes.Type(value = DatasetDesc_Csv.class, name = "Csv") ,
	    @JsonSubTypes.Type(value = DatasetDesc_Tsf.class, name = "Transform")
	})
@Schema(name="DatasetDescriptor" , description="数据源连接信息，这是一个抽象基类，注意根据数据库类型选择" 
	, subTypes = {DatasetDesc_Sql.class , DatasetDesc_Api.class , DatasetDesc_Csv.class , DatasetDesc_Tsf.class})
@Data
public abstract class DatasetDescriptor implements Cloneable
{
	
	/**
	 * 前置处理
	 */
	ParamList paramList ;
	
	@Schema(description = "数据集来源")
	@JsonIgnore
	DatasetSource source ;
	
	@Schema(description = "输入参数")
	List<InParam> inParams ;
	
	@Schema(description = "输出参数")
	List<OutParam> outParams ;
	
	/**
	 * 后置处理表达式
	 */
	@Schema(description = "后置处理表达式")
	String postHandleExpr ;
	
	@Schema(hidden = true)
	@JsonIgnore
	@Setter(value = AccessLevel.NONE)
	transient Map<String, OutParam> outParamMap ;
	
	
	public DatasetDescriptor(DatasetSource aSource)
	{
		source = aSource ;
	}
	
	public void setOutParams(List<OutParam> aOutParams)
	{
		outParams = aOutParams;
		outParamMap = null ;
	}
	
	@JsonIgnore
	@Schema(hidden = false)
	public OutParam getOutParamByName(String aName)
	{
		Map<String, OutParam> map = outParamMap ;
		if(map == null)
		{
			map = XC.hashMap() ;
			List<OutParam> outParams = this.outParams ;
			if(outParams != null)
			{
				for(OutParam outParam : outParams)
				{
					map.put(outParam.getName(), outParam) ;
				}
			}
			outParamMap = map ;
		}
		return map.get(aName) ;
	}
	
	public abstract DatasetDescriptor clone() ;
	
	
	protected DatasetDescriptor initClone(DatasetDescriptor aClone)
	{
		aClone.setSource(source) ;
		if(inParams != null)
			aClone.setInParams(new ArrayList<InParam>(inParams)) ;
		if(outParams != null)
			aClone.setOutParams(new ArrayList<OutParam>(outParams)) ;
		if(paramList != null)
			aClone.paramList = paramList.clone() ;
		if(postHandleExpr != null)
			aClone.postHandleExpr = postHandleExpr ;
		return aClone ;
	}
	
	public static class DatasetDescSerDe
	{
		@BForwardMethod
		public static String forward(DatasetDescriptor aVal)
		{
			return aVal == null?null:JacksonUtils.toString(aVal) ;
		}
		
		@BReverseMethod
		public static DatasetDescriptor reverse(Object aVal)
		{
			if(aVal == null)
				return null ;
			return JacksonUtils.asBean(aVal.toString() , DatasetDescriptor.class) ;
		}
	}
}
