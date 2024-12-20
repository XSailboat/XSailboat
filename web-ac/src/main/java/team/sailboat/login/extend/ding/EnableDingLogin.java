package team.sailboat.login.extend.ding;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

@Documented
@Retention(RUNTIME)
@Target({ElementType.TYPE})
@Import({DingLoginController.class , DingLoginComponent.class})
public @interface EnableDingLogin
{

}
