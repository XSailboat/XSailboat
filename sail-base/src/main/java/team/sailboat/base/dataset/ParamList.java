package team.sailboat.base.dataset;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import team.sailboat.base.util.JacksonUtils;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dpa.anno.BForwardMethod;
import team.sailboat.commons.fan.dpa.anno.BReverseMethod;

/**
 * 前置处理
 *
 * @author yyl
 * @since 2023年4月6日
 */
@Schema(description = "参数定义表")
@Data
public class ParamList implements Cloneable
{
	@Schema(description = "参数列表")
	List<Param> params ;
	
	@Override
	public ParamList clone()
	{
		ParamList clone = new ParamList() ;
		if(XC.isNotEmpty(params))
			clone.params = XC.deepCloneArrayList(params) ;
		return clone ;
	}
	
	@JsonIgnore
	public boolean isNotEmpty()
	{
		return XC.isNotEmpty(params) ;
	}
	
	@JsonIgnore
	public boolean hasAviator()
	{
		return XC.findFirst(params , p->p instanceof Param_Aviator).isPresent() ;
	}
	
	public static class SerDe
	{
		@BForwardMethod
		public static String forward(ParamList aVal)
		{
			return aVal == null?null:JacksonUtils.toString(aVal) ;
		}
		
		@BReverseMethod
		public static ParamList reverse(Object aVal)
		{
			return aVal == null?null:JacksonUtils.asBean(aVal.toString() , ParamList.class) ;
		}
	}
}
