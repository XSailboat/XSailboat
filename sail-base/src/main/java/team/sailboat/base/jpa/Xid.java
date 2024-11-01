package team.sailboat.base.jpa;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.hibernate.annotations.IdGeneratorType;

/**
 * 
 *
 * @author yyl
 * @since 2024年9月30日
 */
@IdGeneratorType(TRSGenerator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface Xid
{
	String category();

	String prefix();

	boolean canSpecify() default false;
}