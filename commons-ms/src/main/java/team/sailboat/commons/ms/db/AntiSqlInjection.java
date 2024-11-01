package team.sailboat.commons.ms.db;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * 防SQL注入
 *
 * @author yyl
 * @since Feb 18, 2021
 */
@Documented
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface AntiSqlInjection
{

}
