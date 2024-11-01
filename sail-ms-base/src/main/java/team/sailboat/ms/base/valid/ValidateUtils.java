package team.sailboat.ms.base.valid;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import team.sailboat.commons.fan.collection.XC;

/**
 * Validator辅助工具
 *
 * @author yyl
 * @since 2024年10月17日
 */
public class ValidateUtils
{
	public static <T> void validateAndThrow(Validator aValidator , T aObj)
	{
		// 检查
		Set<ConstraintViolation<T>> valids= aValidator.validate(aObj) ;
		if(XC.isNotEmpty(valids))
		{
			for(ConstraintViolation<T> constraint : valids)
			{
				throw new IllegalArgumentException(constraint.getMessage()) ;
			}
		}
	}
}
