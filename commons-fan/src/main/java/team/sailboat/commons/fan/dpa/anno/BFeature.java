package team.sailboat.commons.fan.dpa.anno;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import team.sailboat.commons.fan.dtool.DBType;

@Documented
@Retention(RUNTIME)
public @interface BFeature
{
	String name() ;
	
	String value() ;
	
	DBType type() ;
}
