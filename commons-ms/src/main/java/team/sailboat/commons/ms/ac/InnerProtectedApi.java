package team.sailboat.commons.ms.ac;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法内部自己进行安全检查的API，无需外部再额外保护
 *
 * @author yyl
 * @since 2021年11月2日
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface InnerProtectedApi
{

}
