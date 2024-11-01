package team.sailboat.base.dataset;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name="OutParam" , description = "输出参数定义" )
@JsonInclude(value = Include.NON_NULL)
public class OutParam extends IOParam
{

	public OutParam()
	{
		super();
	}
	
	public OutParam(String aName , String aDataType)
	{
		this() ;
		name = aName ;
		dataType = aDataType ;
	}
	
	public OutParam(String aName , String aDataType , String aDescription)
	{
		this() ;
		name = aName ;
		dataType = aDataType ;
		description = aDescription ;
	}
	
	@Override
	public OutParam clone()
	{
		return initClone(new OutParam()) ;
	}
	
	@Override
	protected OutParam initClone(Param aParam)
	{
		return (OutParam) super.initClone(aParam);
	}
}
