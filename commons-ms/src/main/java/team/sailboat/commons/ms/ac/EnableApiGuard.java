package team.sailboat.commons.ms.ac;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * 
 * 启用保护当前服务的API的能力。要求调用指定的受保护的API，必需获得授权			<br />
 * 
 * 这个能力一般用在WebApp，需要提供接口给ClientApp调用的情形
 * 
 * @author yyl
 * @since 2024年11月30日
 */
@Documented
@Retention(RUNTIME)
@Target({ ElementType.TYPE })
@Import(ApiAccessControlAspect.class)
public @interface EnableApiGuard
{

}
