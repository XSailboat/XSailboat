package team.sailboat.commons.fan.dpa.anno;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import team.sailboat.commons.fan.dpa.DBeanFactory;
import team.sailboat.commons.fan.dpa.IDBeanFactory;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface BTable
{
	String name() ;
	
	String comment() default "" ;
	
	BFeature[] features() default {} ;
	
	BIndex[] indexes() default {} ;
	
	Class<? extends IDBeanFactory> factory() default DBeanFactory.class ;
	
	String id_category() default "" ;
	
	String id_prefix() default "" ;
	
	/**
	 * 优先级高于在列上指定
	 */
	String[] primaryKeys() default {} ;
}
