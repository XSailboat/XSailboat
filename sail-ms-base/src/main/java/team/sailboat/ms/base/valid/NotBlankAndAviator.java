package team.sailboat.ms.base.valid;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = Valid_NotBlankAndAviator.class)
@Retention(RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface NotBlankAndAviator
{
	String value() default "" ;
	
	String message() default "";
	
	//下面这两个属性必须添加
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
