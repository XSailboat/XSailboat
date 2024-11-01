package team.sailboat.commons.ms.authclient;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * 资源Id提取		<br>
 *
 * @author yyl
 * @since 2024年10月17日
 */
@Documented
@Retention(RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface ResId
{
	/**
	 * 提取方法实例的键。提取方法实例存储在AppContext中			<br />
	 * 
	 * 提取方法类应该实现Function<Object , String>接口，方法返回资源id		<br />
	 * 
	 * 如果不设置value的值，说明此注解修饰的参数的值就是资源id
	 * 
	 * @return
	 */
	String value() default "" ;
}
