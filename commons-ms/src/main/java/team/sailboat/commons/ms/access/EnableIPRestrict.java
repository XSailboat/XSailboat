package team.sailboat.commons.ms.access;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

@Documented
@Retention(RUNTIME)
@Target({ TYPE })
@Import(IPRestrictFilter.class)
public @interface EnableIPRestrict
{

}
