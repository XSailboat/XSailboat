package team.sailboat.commons.fan.dpa.anno;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * 懒加载指定的字段
 *
 * @author yyl
 * @since 2023年7月22日
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface BLazy
{

}
