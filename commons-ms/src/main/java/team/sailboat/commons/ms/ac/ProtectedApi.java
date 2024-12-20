package team.sailboat.commons.ms.ac;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 受安全访问控制保护的API
 *
 * @author yyl
 * @since 2021年11月2日
 */
@Documented
@Retention(RUNTIME)
@Target({ ElementType.METHOD })
public @interface ProtectedApi
{

}
