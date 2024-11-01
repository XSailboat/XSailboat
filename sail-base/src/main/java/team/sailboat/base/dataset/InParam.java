package team.sailboat.base.dataset;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

 @Schema(name="InParam" , description = "输入参数定义")
 @Data
 @EqualsAndHashCode(callSuper = true)
 @JsonInclude(value = Include.NON_NULL)
public class InParam extends IOParam
{	
	@Schema(description = "是否必填")
	boolean required ;
	
	@Schema(description = "缺省值")
	String defaultValue ;
	
	@Schema(description = "示例")
	String example ;
	
	public InParam()
	{
	}
	
	public InParam(String aName , String aDataType , String aExample)
	{
		super() ;
		name = aName ;
		dataType = aDataType ;
		example = aExample ;
	}
	
	@Override
	public InParam clone()
	{
		return (InParam) initClone(new InParam());
	}
	
	@Override
	protected Param initClone(Param aParam)
	{
		InParam clone = (InParam)super.initClone(aParam);
		clone.required = required ;
		clone.defaultValue = defaultValue ;
		clone.example = example ;
		return clone ;
	}
}