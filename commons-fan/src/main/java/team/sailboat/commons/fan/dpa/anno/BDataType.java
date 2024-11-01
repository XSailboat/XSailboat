package team.sailboat.commons.fan.dpa.anno;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

@Documented
@Retention(RUNTIME)
public @interface BDataType
{
	String name() ;
	
	int length() default 0 ;
	
	boolean dateTimeOnCreate() default false ;
	
	boolean dateTimeOnUpdate() default false ;
	
}
