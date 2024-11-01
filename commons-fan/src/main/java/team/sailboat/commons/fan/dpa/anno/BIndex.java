package team.sailboat.commons.fan.dpa.anno;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

@Documented
@Retention(RUNTIME)
public @interface BIndex
{
	
	String name() ;
	
	String[] columns() ;
	
	String type() default "BTree" ;
	
	boolean unique() default false ;
}
