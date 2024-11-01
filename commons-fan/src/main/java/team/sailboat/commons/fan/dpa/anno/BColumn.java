package team.sailboat.commons.fan.dpa.anno;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface BColumn
{
	String name() ;
	
	String comment() default "" ;
	
	BDataType dataType() ;
	
	int seq() ;
	
	boolean primary() default false ;
	
	Class<?> deserClass() default Object.class ;
	
	Class<?> serClass() default Object.class ;
	
	Class<?> serDeClass() default Object.class ;
	
	String defaultValue() default "" ;
}
