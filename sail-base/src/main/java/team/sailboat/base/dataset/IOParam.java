package team.sailboat.base.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import team.sailboat.base.def.ConfigItemSource;
import team.sailboat.commons.fan.text.RegexUtils;
import team.sailboat.commons.fan.text.XString;

@JsonTypeInfo(
	    use = JsonTypeInfo.Id.NAME, // Were binding by providing a name
	    include = JsonTypeInfo.As.PROPERTY, // The name is provided in a property
	    property = "type", // Property name is type
	    visible = true // Retain the value of type after deserialisation
	)
	@JsonSubTypes({//Below, we define the names and the binding classes.
	    @JsonSubTypes.Type(value = InParam.class, name = "in") ,
	    @JsonSubTypes.Type(value = OutParam.class, name = "out")
	})
@Schema(name="Param" , description = "参数定义（抽象基类）" , subTypes = {InParam.class , OutParam.class})
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class IOParam extends Param
{	
	@Schema(description = "字段名，函数表达式，常量等，如果是字符串，应该用单引号包裹")
	String expression ;
	
	@Schema(description = "数据类型")
	String dataType ;
	
	@Schema(description = "描述")
	String description ;
	
	@Schema(description = "参数来源")
	ConfigItemSource source ;
	
	@Schema(description = "是否是表字段" , accessMode = AccessMode.READ_ONLY)
	@JsonProperty(access = Access.READ_ONLY)
	@Setter(value = AccessLevel.NONE)
	boolean isTableField = false ;
	
	/**
	 * 单表简单情形下才设置，复杂情形下不用设置这个性质
	 * 
	 */
	public void setExpression(String aExpression)
	{
		expression = aExpression;
		if(XString.isEmpty(expression)
				|| expression.charAt(0) == '\'' 
				|| expression.contains("(") 
				|| RegexUtils.isDouble(expression))
			isTableField = false ;
		else
			isTableField = true ;
	}
	
	@Override
	protected Param initClone(Param aParam)
	{
		IOParam clone = (IOParam)super.initClone(aParam) ;
		clone.dataType = dataType ;
		clone.description = description ;
		clone.expression = expression ;
		clone.isTableField = isTableField ;
		clone.source = source ;
		return aParam ;
	}
}