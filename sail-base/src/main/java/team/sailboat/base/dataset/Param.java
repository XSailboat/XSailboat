package team.sailboat.base.dataset;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import team.sailboat.commons.fan.excep.WrapException;

@JsonTypeInfo(
	    use = JsonTypeInfo.Id.NAME, // Were binding by providing a name
	    include = JsonTypeInfo.As.PROPERTY, // The name is provided in a property
	    property = "type", // Property name is type
	    visible = true // Retain the value of type after deserialisation
	)
	@JsonSubTypes({//Below, we define the names and the binding classes.
	    @JsonSubTypes.Type(value = InParam.class, name = "in") ,
	    @JsonSubTypes.Type(value = OutParam.class, name = "out") ,
	    @JsonSubTypes.Type(value = Param_InvokeApi.class , name = "InvokeApi") ,
	    @JsonSubTypes.Type(value = Param_Aviator.class , name = "Aviator")
	})
@Schema(name="Param" , description = "参数定义（抽象基类）" 
		, subTypes = {InParam.class , OutParam.class , Param_Aviator.class , Param_InvokeApi.class})
@Data
public abstract class Param implements Cloneable
{	
	@Schema(description = "参数名称")
	String name ;
	
	public Param()
	{
	}
	
	@Override
	public Param clone()
	{
		Param clone = null ;
		try
		{
			clone = getClass().getConstructor().newInstance();
		}
		catch (Exception e)
		{
			WrapException.wrapThrow(e) ;
			return null ;		//dead code
		}
		clone.name = name ;
		return initClone(clone);
	}
	
	protected Param initClone(Param aClone)
	{
		aClone.name = name ;
		return aClone ;
	}
}