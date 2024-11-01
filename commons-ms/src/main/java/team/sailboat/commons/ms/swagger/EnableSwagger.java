package team.sailboat.commons.ms.swagger;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

@Documented
@Retention(RUNTIME)
@Target({ TYPE })
//@EnableAPISpringEL
@Import(Swagger3.class)
public @interface EnableSwagger
{

}
