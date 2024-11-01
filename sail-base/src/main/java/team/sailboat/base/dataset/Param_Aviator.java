package team.sailboat.base.dataset;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 调用API
 *
 * @author yyl
 * @since 2023年4月6日
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Aviator表达式前置处理器")
public class Param_Aviator extends Param
{
	@Schema(description = "Aviator表达式")
	String expr ;

	@Override
	protected Param_Aviator initClone(Param aClone)
	{
		Param_Aviator clone = (Param_Aviator)super.initClone(aClone) ;
		clone.expr = expr ;
		return clone ;
	}
}
