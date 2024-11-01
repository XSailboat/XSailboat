package team.sailboat.commons.ms.ac_api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 公开的API，不进行权限控制
 *
 * @author yyl
 * @since 2021年11月2日
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.METHOD })
public @interface PublicApi
{

}
