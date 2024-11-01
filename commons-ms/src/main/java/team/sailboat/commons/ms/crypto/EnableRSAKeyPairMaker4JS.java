package team.sailboat.commons.ms.crypto;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

@Documented
@Retention(RUNTIME)
@Target({ TYPE })
@Import(RSAKeyPairMaker4JSConfigurer.class)
public @interface EnableRSAKeyPairMaker4JS
{

}
