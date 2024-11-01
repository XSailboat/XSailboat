package team.sailboat.commons.fan.sys;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;

public class JEnvKit
{
	public static float getJavaVersion()
	{
		String version = System.getProperty("java.version") ;
		String shortVersion = JCommon.defaultIfNull(XString.substringLeft(version, '.', true, 1) , version) ;
		return Float.parseFloat(shortVersion) ;
	}
	
	public static String getJavaHome()
	{
		return System.getProperty("java.home") ;
	}
	
	public static String getRunDir()
	{
		return System.getProperty("user.dir") ;
	}
	
	public static String getTempDir()
	{
		return System.getProperty("java.io.tmpdir") ;
	}
	
    /**
     * 获得进程ID
     * @return
     */
    public static int getPID()
    {
    	RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        String name = runtime.getName();
        return Integer.parseInt(name.substring(0, name.indexOf("@")));
    }
    
    /**
     * 取得可用的处理器数量
     * @return
     */
    public static int getAvailableProcessorNum()
    {
    	return Runtime.getRuntime().availableProcessors() ;
    }
    
    @SuppressWarnings("unchecked")
	public static void setEnv(Map<String, String> aEnvs) throws Exception
	{
		try
		{
			Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
			Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
			theEnvironmentField.setAccessible(true);
			Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null) ;
			env.putAll(aEnvs);
			Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField(
					"theCaseInsensitiveEnvironment");
			theCaseInsensitiveEnvironmentField.setAccessible(true);
			Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
			cienv.putAll(aEnvs);
		}
		catch (NoSuchFieldException e)
		{
			Class<?>[] classes = Collections.class.getDeclaredClasses();
			Map<String, String> env = System.getenv();
			for (Class<?> cl : classes)
			{
				if ("java.util.Collections$UnmodifiableMap".equals(cl.getName()))
				{
					Field field = cl.getDeclaredField("m");
					field.setAccessible(true);
					Object obj = field.get(env);
					Map<String, String> map = (Map<String, String>) obj;
					map.clear();
					map.putAll(aEnvs);
				}
			}
		}
	}
    
    @Deprecated
    public static void setSysEnv(String[] aArgs)
    {
    	int k = XC.indexOf(aArgs, "-sys_env") ;
		String sysEnv = "prod" ;
		if(k != -1)
		{
			Assert.isTrue(k<aArgs.length-1 , "没有指定参数sys_env的参数值") ;
			sysEnv = aArgs[k+1] ;
			Assert.isIn(sysEnv , "sys_env的参数值["+sysEnv+"]不合法", "prod" , "dev" , "test");
		}
		System.setProperty("sys_env", sysEnv) ;
		JCommon.cout("当前的系统环境是："+sysEnv) ;
    }
    
    public static String getSysEnv()
    {
    	String sysEnv = System.getProperty("sys_env") ;
    	Assert.notEmpty(sysEnv , "system.properties中没有设置sys_env") ;
    	return sysEnv ;
    }
    
    public static void coutSystemProperties()
    {
    	Properties prop = System.getProperties() ;
		for(Entry<Object, Object> entry : prop.entrySet())
		{
			System.out.println(entry.getKey()+"\t"+entry.getValue());
		}
    }
	
	public static void main(String[] args)
	{
		coutSystemProperties(); 
	}
}
