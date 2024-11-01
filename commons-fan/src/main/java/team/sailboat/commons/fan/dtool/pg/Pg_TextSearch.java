package team.sailboat.commons.fan.dtool.pg;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface Pg_TextSearch
{
	
	String name() ;
	
	String type() default "pg_bigm" ;
}
