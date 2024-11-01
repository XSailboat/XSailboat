package team.sailboat.ms.base.valid;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 
 * 为空或者是一个Aviator表达式
 *
 * @author yyl
 * @since 2024年10月17日
 */
@Constraint(validatedBy = Valid_BlankOrAviator.class)
@Retention(RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface BlankOrAviator
{	
	
	String value() default "" ;
	
	String message() default "";
	
	//下面这两个属性必须添加
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
