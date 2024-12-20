package team.sailboat.commons.ms;

import team.sailboat.commons.fan.app.ACKeys;

public interface ACKeys_Common extends ACKeys
{
	public static final String sServiceCfg = "ServiceCfg" ;
	
	/**
	 * 类型是String[] ，Controller包名		<br />
	 * 我们的框架会把这些包加入到Spring的扫描路径中。	<br />
	 * 如果启用了Swagger，也会将这些包下的Controller纳入到生成OpenAPI文档的范围
	 */
	public static final String sControllerPackages = "ControllerPackages" ;
	
	public static final String sPathSupport = "PathSupport" ;
	
	/**
	 * 运行期持久化记录的数据,类型是PropertiesEx
	 */
	public static final String sRunningProp = "RunningProp" ;
	
	/**
	 * IMSActivatorSupport类在各个jar中的实现类，类型是Class<?>[]
	 */
	public static final String sMSActivatorClasses = "MSActivatorClasses" ;
	
	/**
	 * 类型为Class<?>
	 */
	public static final String sSpringBootApplicationClass = "SpringBootApplicationClass" ;
	
	/**
	 * 类型为Function<String,String>		<br>
	 * 用来通过AppKey获取AppSecret
	 */
	public static final String sAppSecretGetter = "AppSecretGetter" ;
	
	/**
	 * 类型：Function<String,Set<String>>		<br>
	 * 获取指定App的权限
	 */
	public static final String sAppAuthoritiesGetter = "AppAuthoritiesGetter" ;
	
	public static final String sTaskId = "TaskId" ;
}
