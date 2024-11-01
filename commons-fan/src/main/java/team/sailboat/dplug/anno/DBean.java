package team.sailboat.dplug.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface DBean
{
	/**
	 * 是否生成Java Bean
	 * @return
	 */
	boolean genBean() default false ;
	
	/**
	 * 是否记录创建信息			<br />
	 * 自动增加createUserId和createTime字段
	 * @return
	 */
	boolean recordCreate() default false ;
	
	/**
	 * 是否记录更新信息。		<br />
	 * 自动增加lastEditUserId和lastEditTime字段
	 * @return
	 */
	boolean recordEdit() default false ;
}
