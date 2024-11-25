package team.sailboat.ms.crane;


import org.springframework.stereotype.Component;

import lombok.Data;
import team.sailboat.commons.fan.collection.PropertiesEx;

/**
 * App的ini配置		<br />
 * 
 * 当键中有中文时，使用@ConfigurationProperties这种方法加载会有问题。中文字符会丢失，这与Spring的对象化解析注入，对键的按类字段规范化处理有关。
 * 不是Properties的问题。		<br />
 * 
 * 这里改成在MainApplication的程序里面自己加载注入
 *
 * @author yyl
 * @since 2024年10月11日
 */
@Data
@Component
//@PropertySource(value={"file:${app.config.path}"} , ignoreResourceNotFound=false
//		, encoding="utf-8" , name="app-config" , factory = PropertiesExSourceFactory.class)
//@ConfigurationProperties
public class AppConfig
{
	
	
	/**
	 * 里面的键是不带sys_params.这个前缀的
	 */
	PropertiesEx sys_params ;
	
}
