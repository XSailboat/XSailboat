package team.sailboat.commons.ms.ac_api;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

@Documented
@Retention(RUNTIME)
@Target({ ElementType.TYPE })
@Import(ApiAccessControlAspect.class)
public @interface EnableApiGuard
{

}
