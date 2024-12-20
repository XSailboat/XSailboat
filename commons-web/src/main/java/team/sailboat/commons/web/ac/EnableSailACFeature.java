package team.sailboat.commons.web.ac;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

@Documented
@Retention(RUNTIME)
@Target({ TYPE })
@EnableResId
@Import({SailboatProviderConfigurer.class , OAuthClientConf.class})
public @interface EnableSailACFeature
{

}
