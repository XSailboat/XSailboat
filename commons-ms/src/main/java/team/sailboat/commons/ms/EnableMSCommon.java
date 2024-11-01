package team.sailboat.commons.ms;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import team.sailboat.commons.ms.aware.EnableCommonConfigurer;
import team.sailboat.commons.ms.cors.EnableCORS;
import team.sailboat.commons.ms.error.EnableCommonErrorHandler;
import team.sailboat.commons.ms.json.EnableMyJSONSerializer;
import team.sailboat.commons.ms.swagger.EnableSwagger;

@Documented
@Retention(RUNTIME)
@Target({ TYPE })
@EnableCORS
@EnableSwagger
@EnableCommonConfigurer
@EnableCommonErrorHandler
@EnableMyJSONSerializer
public @interface EnableMSCommon
{

}
