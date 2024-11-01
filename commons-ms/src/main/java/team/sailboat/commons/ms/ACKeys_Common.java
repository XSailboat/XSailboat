package team.sailboat.commons.ms;

import team.sailboat.commons.fan.app.ACKeys;

public interface ACKeys_Common extends ACKeys
{
	public static final String sServiceCfg = "ServiceCfg" ;
	
	/**
	 * 类型是String[] ，Service包名
	 */
	public static final String sServicePackages = "ServicePackages" ;
	
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
