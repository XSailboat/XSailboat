package team.sailboat.commons.fan.lang;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import team.sailboat.commons.fan.adapter.ITypeAdapter;
import team.sailboat.commons.fan.adapter.TA_Boolean;
import team.sailboat.commons.fan.adapter.TA_Bytes;
import team.sailboat.commons.fan.adapter.TA_Date;
import team.sailboat.commons.fan.adapter.TA_Double;
import team.sailboat.commons.fan.adapter.TA_Integer;
import team.sailboat.commons.fan.adapter.TA_Long;
import team.sailboat.commons.fan.adapter.TA_String;
import team.sailboat.commons.fan.adapter.TA_Strings;
import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.infc.EFunction;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.JSONString;
import team.sailboat.commons.fan.log.Log;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.fan.struct.Bits;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.fan.time.XTime;

/**
 * 
 * Java Class类型相关的操作接口
 *
 * @author yyl
 * @since 2024年11月22日
 */
public class XClassUtil
{
	static final DecimalFormat sDFmt = new DecimalFormat(",#.#");
	
	/**
	 * CSN(Class Simple Name)类型简写<br>
	 * String的类型简写是string
	 */
	public static final String sCSN_String = "string" ;
	
	/**
	 * CSN(Class Simple Name)类型简写<br>
	 * Double的类型简写是double
	 */
	public static final String sCSN_Double = "double" ;
	
	/**
	 * CSN(Class Simple Name)类型简写<br>
	 * Integer的类型简写是int
	 */
	public static final String sCSN_Integer = "int" ;
	
	/**
	 * CSN(Class Simple Name)类型简写<br>
	 * Float的类型简写是float
	 */
	public static final String sCSN_Float = "float" ;
	
	/**
	 * CSN(Class Simple Name)类型简写<br>
	 * Long的类型简写是long
	 */
	public static final String sCSN_Long = "long" ;
	
	/**
	 * CSN(Class Simple Name)类型简写<br>
	 * Date的类型的简写是datetime
	 */
	public static final String sCSN_DateTime = "datetime" ;
	
	/**
	 * CSN(Class Simple Name)类型简写<br>
	 * byte[]的类型的简写是bytes
	 */
	public static final String sCSN_Bytes = "bytes" ;
	
	/**
	 * CSN(Class Simple Name)类型简写<br>
	 * boolean的类型的简写是bool
	 */
	public static final String sCSN_Bool = "bool" ;
	
	private static final Map<String , Class<?>> sCSNMap = new HashMap<>() ;
	static Map<Class<?> , ITypeAdapter<?>> sTypeAdapters = new HashMap<>() ;
	static final Map<String, String> sClassCSNMap = XC.hashMap() ;
	
	/**
	 * 键是类，值是将字符串转成指定类型的方法
	 */
	static final Map<Class<?> , EFunction<String, Object , Exception>> sStrCvtToFuncs = XC.hashMap() ;
	static
	{
		sCSNMap.put(sCSN_String, String.class) ;
		sCSNMap.put(sCSN_Double, Double.class) ;
		sCSNMap.put(sCSN_Integer, Integer.class) ;
		sCSNMap.put(sCSN_Float, Float.class) ;
		sCSNMap.put(sCSN_Long, Long.class) ;
		sCSNMap.put(sCSN_DateTime, Date.class) ;
		sCSNMap.put(sCSN_Bytes, byte[].class) ;
		sCSNMap.put(sCSN_Bool , Boolean.class) ;
		
		sClassCSNMap.put(String.class.getName() , sCSN_String) ;
		sClassCSNMap.put(Double.class.getName() , sCSN_Double) ;
		sClassCSNMap.put(Integer.class.getName() , sCSN_Integer) ;
		sClassCSNMap.put(Float.class.getName() , sCSN_Float) ;
		sClassCSNMap.put(Long.class.getName() , sCSN_Long) ;
		sClassCSNMap.put(Date.class.getName() , sCSN_DateTime) ;
		sClassCSNMap.put(Boolean.class.getName() , sCSN_Bool) ;
		sClassCSNMap.put(byte[].class.getName() , sCSN_Bytes) ;
		
		sTypeAdapters.put(Integer.class , new TA_Integer()) ;
		sTypeAdapters.put(Integer.TYPE , new TA_Integer(0)) ;
		sTypeAdapters.put(Long.class , new TA_Long()) ;
		sTypeAdapters.put(Long.TYPE , new TA_Long(0)) ;
		sTypeAdapters.put(Double.class , new TA_Double()) ;
		sTypeAdapters.put(Double.TYPE , new TA_Double(0)) ;
		sTypeAdapters.put(Boolean.class , new TA_Boolean()) ;
		sTypeAdapters.put(Boolean.TYPE , new TA_Boolean(false)) ;
		sTypeAdapters.put(Date.class , new TA_Date()) ;
		sTypeAdapters.put(String.class , new TA_String()) ;
		sTypeAdapters.put(String[].class , new TA_Strings()) ;
		sTypeAdapters.put(byte[].class, new TA_Bytes()) ;
		
		sStrCvtToFuncs.put(String.class , str->str) ;
		sStrCvtToFuncs.put(Integer.class , Integer::valueOf) ;
		sStrCvtToFuncs.put(Integer.TYPE , Integer::valueOf) ;
		sStrCvtToFuncs.put(Boolean.class , Boolean::valueOf) ;
		sStrCvtToFuncs.put(Boolean.TYPE , Boolean::valueOf) ;
		sStrCvtToFuncs.put(Character.class , str->str.charAt(0)) ;
		sStrCvtToFuncs.put(Character.TYPE , str->str.charAt(0)) ;
		sStrCvtToFuncs.put(Byte.class , Byte::valueOf) ;
		sStrCvtToFuncs.put(Byte.TYPE , Byte::valueOf) ;
		sStrCvtToFuncs.put(Short.class , Short::valueOf) ;
		sStrCvtToFuncs.put(Short.TYPE , Short::valueOf) ;
		sStrCvtToFuncs.put(Long.class , Long::valueOf) ;
		sStrCvtToFuncs.put(Long.TYPE , Long::valueOf) ;
		sStrCvtToFuncs.put(Float.class , Float::valueOf) ;
		sStrCvtToFuncs.put(Float.TYPE , Float::valueOf) ;
		sStrCvtToFuncs.put(Double.class , Double::valueOf) ;
		sStrCvtToFuncs.put(Double.TYPE , Double::valueOf) ;
		sStrCvtToFuncs.put(Date.class , XTime::adaptiveParse) ;
	}

	static Class<?>[] sBasicJavaDataType = {Integer.class , Boolean.class , Character.class
			, Byte.class , Short.class , Long.class , Float.class , Double.class
			, String.class , Date.class , Class.class} ;
	
	static final Set<String> sNumericTypes = XC.hashSet(sCSN_Double , sCSN_Float
			, sCSN_Integer , sCSN_Long) ;
	
	
	/**
	 * 将给定的方法名或变量名转换为常见的字段名格式
	 * @param aName 输入的方法名或变量名
	 * @return 转换后的字段名
	 * 
	 * 转换规则：
	 * 1. 如果名称长度大于1，以"m"开头且第二个字符为大写，则将第二个字符转为小写并返回。
	 * 2. 如果名称长度大于3，以"get"或"set"开头且第四个字符为大写，则将第四个字符转为小写并返回。
	 * 3. 如果名称长度大于2，以"is"开头且第三个字符为大写，则将第三个字符转为小写并返回。
	 * 4. 如果不满足上述条件，则直接返回原名称。
	 */
	public static String toCommonFieldName(String aName)
	{
		if(aName.length()>1 && aName.startsWith("m") && Character.isUpperCase(aName.charAt(1)))
			return Character.toLowerCase(aName.charAt(1))+aName.substring(2) ;
		else if(aName.length()>3 && (aName.startsWith("get") || aName.startsWith("set")) && Character.isUpperCase(aName.charAt(3)))
			return  Character.toLowerCase(aName.charAt(3))+aName.substring(4) ;
		else if(aName.length()>2 && aName.startsWith("is") && Character.isUpperCase(aName.charAt(2)))
			return  Character.toLowerCase(aName.charAt(2))+aName.substring(3) ;
		else
			return aName ;
	}
	
	/**
	 * 检查给定的类是否有对应的基本公共类型（CSN），如果不存在则抛出异常
	 * @param aClazz 要检查的类
	 * @return 该类的基本公共类型字符串
	 * @throws IllegalArgumentException 如果aClazz为null，或者该类没有对应的基本公共类型
	 */
	public static String checkCSN(Class<?> aClazz)
	{
		Assert.notNull(aClazz) ;
		String csn = sClassCSNMap.get(aClazz.getName()) ;
		Assert.notNull(csn , "类 %s 没有对应的基本公共类型！" , aClazz.getName()) ;
		return csn ;
	}
	
	/**
	 * 获取给定类的基本公共类型（CSN），如果不存在则返回null
	 * @param aClazz 要查询的类
	 * @return 该类的基本公共类型字符串，如果不存在则返回null
	 */
	public static String getCSN(Class<?> aClazz)
	{
		return aClazz != null?sClassCSNMap.get(aClazz.getName()):null ;
	}
	
	/**
	 * 根据前缀和名称生成方法名
	 * @param aPreffix 方法前缀，如"get"、"set"
	 * @param aName 原始名称
	 * @return 生成的方法名
	 * 
	 * 生成规则：
	 * 1. 如果名称长度大于1，以"m"开头且第二个字符为大写，则直接添加前缀并返回。
	 * 2. 如果名称长度大于2，以"is"开头且第三个字符为大写，且前缀为"set"，则将"is"替换为"set"并返回；否则直接返回原名称。
	 * 3. 如果名称长度大于3，以前缀开头且第四个字符为大写，则直接返回原名称。
	 * 4. 其他情况，将名称的首字母转为大写，并添加前缀后返回。
	 */
	public static String getMethodName(String aPreffix , String aName)
	{
		if(aName.length()>1 && aName.startsWith("m") && Character.isUpperCase(aName.charAt(1)))
		{
			return aPreffix+aName.substring(1) ;
		}
		else if(aName.length()>2 && aName.startsWith("is") && Character.isUpperCase(aName.charAt(2)))
		{
			if("set".equals(aPreffix))
				return "set"+aName.substring(2) ;
			return aName ;
		}
		else if(aName.length()>3 && aName.substring(0, 3).equals(aPreffix) && Character.isUpperCase(aName.charAt(3)))
		{
			return aName ;
		}
		else
		{
			return aPreffix+Character.toUpperCase(aName.charAt(0))+aName.substring(1) ;
		}
	}
	
	/**
	 * 尝试获取给定类中指定名称和参数类型的方法，如果不存在则返回null
	 * @param aClass 要查询的类
	 * @param aName 方法名称
	 * @param aParameterTypes 方法参数类型
	 * @return 找到的方法对象，如果不存在则返回null
	 */
	public static Method getMethod0(Class<?> aClass , String aName, Class<?>... aParameterTypes)
	{
		try
		{
			return getMethod(aClass, aName, aParameterTypes) ;
		}
		catch(NoSuchMethodException e)
		{
			return null ;
		}
	}
	
	/**
	 * 
	 * 在指定类中查找指定名称的，并且与指定的参数类型兼容的方法	<br />
	 * 尝试获取给定类中指定名称和参数类型的方法。如果找不到完全匹配的方法，会尝试进行参数类型的宽松匹配，
	 * 并在当前类及其父类中递归查找。
	 *
	 * @param aClass 要查询的类
	 * @param aName 方法名称
	 * @param aParameterTypes 方法参数类型
	 * @return 找到的方法对象
	 * @throws NoSuchMethodException 如果找不到指定的方法
	 */
	public static Method getMethod(Class<?> aClass , String aName, Class<?>... aParameterTypes) throws NoSuchMethodException
	{
		try
		{
			return aClass.getDeclaredMethod(aName, aParameterTypes) ;
		}
		catch (NoSuchMethodException e)
		{
			if(XC.isNotEmpty(aParameterTypes))
			{
				Method[] methods = aClass.getDeclaredMethods() ;
				if(methods != null)
				{
					for(Method method : methods)
					{
						if(aName.equals(method.getName()))
						{
							Class<?>[] classes = method.getParameterTypes() ;
							if(classes != null && classes.length == aParameterTypes.length)
							{
								boolean fit = true ;
								for(int i=0 ; i<classes.length ; i++)
								{
									if(!classes[i].isAssignableFrom(aParameterTypes[i]))
									{
										fit = false ;
										break ;
									}
								}
								if(fit)
									return method ;
							}
						}
					}
				}
			}
			Class<?> superClass = aClass.getSuperclass() ;
			if(superClass != null)
			{
				try
				{
					return getMethod(superClass, aName, aParameterTypes) ;
				}
				catch(Exception e2)
				{}
			}
			throw e ;
		}
	}
	
	/**
	 * 根据给定的类和对应的构造参数创建该类的一个新实例。
	 *
	 * @param aClass 要实例化的类
	 * @param aArgs 构造函数的参数
	 * @return 新创建的实例对象
	 * @throws Exception 如果实例化过程中发生异常
	 */
	public static Object newInstance(Class<?> aClass , Object...aArgs) throws Exception
	{
		if(aArgs == null || aArgs.length == 0)
		{
			Constructor<?> c = aClass.getDeclaredConstructor() ;
			c.setAccessible(true) ;
			return c.newInstance() ;
		}
		else
		{
			Class<?>[] ptypes = new Class<?>[aArgs.length] ;
			for(int i=0 ; i<aArgs.length ; i++)
				ptypes[i] = aArgs[i].getClass() ;
			Constructor<?>[] cons = aClass.getConstructors() ;
			for(Constructor<?> con : cons)
			{
				Class<?>[] ptypes0 = con.getParameterTypes() ;
				if(XC.count(ptypes0) == aArgs.length)
				{
					boolean fit = true ;
					for(int i=0 ; i<aArgs.length ; i++)
					{
						if(!ptypes0[i].isAssignableFrom(ptypes[i]))
						{
							fit = false ;
							break ;
						}
					}
					if(fit)
						return con.newInstance(aArgs) ;
				}
			}
			return null ;
		}
	}
	
	/**
	 * 判断给定的方法是否为一个setter方法（即以"set"开头的方法）。
	 *
	 * @param aMethod 要判断的方法
	 * @return 如果该方法为setter方法，则返回true；否则返回false
	 */
	public static boolean isSetterMethod(Method aMethod)
	{
		return aMethod.getName().startsWith("set") ;
	}
	
	/**
	 * 根据给定的getter或is方法，获取对应的setter方法。
	 *
	 * @param aMethod 给定的getter或is方法
	 * @return 对应的setter方法
	 * @throws Exception 如果找不到对应的setter方法或发生其他异常
	 */
	public static Method getSetterMethod(Method aMethod) throws Exception
	{
		if(isSetterMethod(aMethod)) return aMethod ;
		else
		{
			String name = aMethod.getName().replaceFirst("get|(is)", "set") ;
			return aMethod.getDeclaringClass().getMethod(name, aMethod.getReturnType()) ;
		}
	}
	
	/**
	 * 根据给定的字段名生成对应的getter方法名。
	 *
	 * @param aName 字段名
	 * @return 生成的getter方法名
	 */
	public static String getGetterMethodName(String aName)
	{
		return getMethodName("get", aName) ;
	}
	
	/**
	 * 根据给定的字段名生成对应的setter方法名。
	 *
	 * @param aName 字段名
	 * @return 生成的setter方法名
	 */
	public static String getSetterMethodName(String aName)
	{
		return getMethodName("set", aName) ;
	}
	
	/**
	 * 通过反射调用对象的setter方法，为指定字段设置值。
	 *
	 * @param aObj 要操作的对象
	 * @param aName 字段名
	 * @param aVals 要设置的值（可变参数，支持多个值的情况，但通常只设置一个值）
	 * @throws Exception 如果调用过程中发生异常
	 */
	public static void setValue(Object aObj , String aName , Object...aVals) throws Exception
	{
		Class<?>[] classes = null ;
		if(aVals != null)
		{
			classes = new Class<?>[aVals.length] ;
			for(int i=0 ; i<aVals.length ; i++)
				classes[i] = aVals[i].getClass() ;
		}
		Method method = null ;
		try
		{
			method = aObj.getClass().getMethod(getSetterMethodName(aName), classes) ;
		}
		catch(Exception e)
		{
			String methodName = getSetterMethodName(aName) ; 
			for(Method method0 : aObj.getClass().getMethods())
				if(method0.getName().equals(methodName))
				{
					method = method0 ;
					break ;
				}
			if(method == null)
				throw e ;
		}
		method.invoke(aObj, aVals) ;
	}
	
	/**
	 * 调用对象的getter方法，并返回其值。
	 * 
	 * @param aObj 要调用getter方法的对象
	 * @param aName 属性名称，用于构造getter方法名（例如，属性名为"name"，则getter方法名为"getName"）
	 * @return getter方法的返回值
	 * @throws Exception 如果方法调用过程中发生异常
	 */
	public static Object invokeGetterMethod(Object aObj , String aName) throws Exception
	{
		Method method = aObj.getClass().getMethod(getGetterMethodName(aName)) ;
		return method.invoke(aObj) ;
	}
	
	/**
	 * 调用对象的指定方法，并返回其值。该方法可以是无参的，也可以是有参的。
	 * 
	 * @param aSource 要调用方法的对象
	 * @param aMethod 方法名称
	 * @param aArgs 方法参数（可变参数）
	 * @return 方法的返回值
	 * @throws NoSuchMethodException 如果找不到指定的方法，或者方法是静态的但尝试以非静态方式调用
	 */
	public static Object invokeMethod(Object aSource , String aMethod , Object...aArgs) throws NoSuchMethodException
	{
		Class<?> clazz = aSource.getClass() ;
		if(XC.isEmpty(aArgs))
		{
			try
			{
				Method method  =clazz.getDeclaredMethod(aMethod) ;
				if(Modifier.isStatic(method.getModifiers()))
					throw new NoSuchMethodException(String.format("类%1$s中有无参的%2$s方法，但却是静态的")) ;
				if(!method.canAccess(aSource))
					method.setAccessible(true);
				return method.invoke(aSource) ;
			}
			catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				throw new WrapException(e) ;
			}
		}
		else
		{
			Class<?>[] classes = new Class[aArgs.length] ;
			for(int i=0 ; i<aArgs.length ; i++)
				classes[i] = aArgs[i].getClass() ;
			Method method = getMethod(clazz, aMethod , classes) ;
			if(Modifier.isStatic(method.getModifiers()))
				throw new NoSuchMethodException(String.format("类%1$s中有无参的%2$s方法，但却是静态的")) ;
			if(!method.canAccess(aSource))
				method.setAccessible(true);
			try
			{
				return method.invoke(aSource, aArgs) ;
			}
			catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				throw new WrapException(e) ;
			}
		}
	}
	
	/**
	 * 调用指定类的静态方法，并返回其值。该方法可以是无参的，也可以是有参的。
	 * 
	 * @param aClass 要调用方法的类
	 * @param aMethod 方法名称
	 * @param aArgs 方法参数（可变参数）
	 * @return 方法的返回值
	 * @throws NoSuchMethodException 如果找不到指定的方法，或者方法不是静态的但尝试以静态方式调用
	 */
	public static Object invokeStaticMethod(Class<?> aClass , String aMethod , Object...aArgs) throws NoSuchMethodException
	{
		if(XC.isEmpty(aArgs))
		{
			try
			{
				Method method = aClass.getDeclaredMethod(aMethod) ;
				if(!Modifier.isStatic(method.getModifiers()))
					throw new NoSuchMethodException(String.format("类%1$s中有无参的%2$s方法，但却不是静态的")) ;
				if(!method.canAccess(null))
					method.setAccessible(true);
				return method.invoke(null) ;
			}
			catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				throw new WrapException(e) ;
			}
		}
		else
		{
			Class<?>[] classes = new Class[aArgs.length] ;
			for(int i=0 ; i<aArgs.length ; i++)
				classes[i] = aArgs[i].getClass() ;
			Method method = getMethod(aClass, aMethod , classes) ;
			if(!Modifier.isStatic(method.getModifiers()))
				throw new NoSuchMethodException(String.format("类%1$s中有无参的%2$s方法，但却不是静态的")) ;
			if(!method.canAccess(null))
				method.setAccessible(true);
			try
			{
				return method.invoke(null, aArgs) ;
			}
			catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				throw new WrapException(e) ;
			}
		}
	}

	/**
	 * 调用对象的is方法（通常用于布尔类型的属性），并返回其值。
	 * 
	 * @param aObj 要调用is方法的对象
	 * @param aName 属性名称，用于构造is方法名（例如，属性名为"active"，则is方法名为"isActive"）
	 * @return is方法的返回值
	 * @throws Exception 如果方法调用过程中发生异常
	 */
	public static Object invokeIsMethod(Object aObj , String aName) throws Exception
	{
		Method method = aObj.getClass().getMethod(getMethodName("is", aName)) ;
		return method.invoke(aObj) ;
	}
	
	/**
	 * 根据getter方法获取对应的setter方法。
	 * 
	 * @param aClass 包含getter方法的类
	 * @param aGetter getter方法
	 * @param aPTypes setter方法的参数类型（可变参数）
	 * @return 对应的setter方法，如果不存在则返回null
	 */
	public static Method getSetterByGetter(Class<?> aClass , Method aGetter , Class<?>...aPTypes)
	{
		try
		{
			if(aGetter.getName().startsWith("get"))
			{
				String methodName = getSetterMethodName(aGetter.getName().substring(3)) ;
				return aClass.getMethod(methodName, aPTypes) ;
			}
			else if(aGetter.getName().startsWith("is"))
			{
				String methodName = getSetterMethodName(aGetter.getName().substring(2)) ;
				return aClass.getMethod(methodName, aPTypes) ;
			}
		}
		catch(Exception e)
		{}
		return null ;
	}
	
	/**
	 * 原子类型，8种基础数据类型，以及String和java.util.Date
	 * @param aClass
	 * @return
	 */
	public static boolean isBasicJavaDataType(Class<?> aClass)
	{
		if(aClass.isPrimitive()) return true ;
		for(Class<?> clazz : sBasicJavaDataType)
			if(clazz.equals(aClass)) return true ;
		return false ;
	}
	
	public static boolean isPrimitiveBox(Class<?> aClass)
	{
		for(int i=0 ; i<8 ; i++)
			if(sBasicJavaDataType[i].equals(aClass)) return true ;
		return false ;
	}
	
	public static Object convertToJavaBasicType(Class<?> aClass , String aValue) throws Exception
	{
		return convertToJavaBasicType(aClass, aValue, null) ;
	}
	
	public static Object convertToJavaBasicType(Class<?> aClass , String aValue
			, ClassLoader aClassLoader) throws Exception
	{
		EFunction<String , Object, Exception> func = sStrCvtToFuncs.get(aClass) ;
		if(func != null)
			return func.apply(aValue) ;
		else  if(Class.class.isAssignableFrom(aClass))
		{
			String classname = aValue.replace("class", "").trim() ;
			if(aClassLoader == null)
				aClassLoader = JCommon.getYClassLoader() ;
			return aClassLoader.loadClass(classname) ;
		}
		return null ;
	}
	
	public static Object assertJavaBasicType(Class<?> aTargetType , String aValue) throws Exception
	{
		Object result = convertToJavaBasicType(aTargetType, aValue) ;
		if(result == null)
			throw new UnsupportedOperationException("不支持将String转化成 "+aTargetType.getName()+" 类型") ;
		return result ;
	}
	
	public static <T> T convert(Object aValue , Function<Object , T> aFunc)
	{
		return aFunc.apply(aValue) ;
	}
	
	public static Object[] toJavaBasicArray(Class<?> aClass , Object aValue)
	{
		if(aValue != null)
		{
			if(aClass.isPrimitive())
			{
				if(Integer.TYPE.equals(aClass))
				{
					int[] objs = (int[])aValue ;
					Integer[] results = new Integer[objs.length] ;
					int i= 0 ;
					for(int obj : objs)
						results[i++] = obj ;
					return results ;
				}
				else if(Float.TYPE.equals(aClass))
				{
					float[] objs = (float[])aValue ;
					Float[] results = new Float[objs.length] ;
					int i= 0 ;
					for(float obj : objs)
						results[i++] = obj ;
					return results ;
				}
				else if(Double.TYPE.equals(aClass))
				{
					double[] objs = (double[])aValue ;
					Double[] results = new Double[objs.length] ;
					int i= 0 ;
					for(double obj : objs)
						results[i++] = obj ;
					return results ;
				}
				else if(Boolean.TYPE.equals(aClass))
				{
					boolean[] objs = (boolean[])aValue ;
					Boolean[] results = new Boolean[objs.length] ;
					int i= 0 ;
					for(boolean obj : objs)
						results[i++] = obj ;
					return results ;
				}
				else if(Long.TYPE.equals(aClass))
				{
					long[] objs = (long[])aValue ;
					Long[] results = new Long[objs.length] ;
					int i= 0 ;
					for(long obj : objs)
						results[i++] = obj ;
					return results ;
				}
			}
			else if(Integer.class.isAssignableFrom(aClass))
				return (Integer[])aValue ;
			else if(Float.class.isAssignableFrom(aClass))
				return (Float[])aValue ;
			else if(Double.class.isAssignableFrom(aClass))
				return (Double[])aValue ;
			else if(Boolean.class.isAssignableFrom(aClass))
				return (Boolean[])aValue ;
			else if(Long.class.isAssignableFrom(aClass))
				return (Long[])aValue ;
			else if(Date.class.isAssignableFrom(aClass))
				return ((Date[])aValue) ;
		}
		return new Object[0] ;
	}
	
	/**
	 * 数组、集合或者Map
	 * @param aClass
	 * @return
	 */
	public static boolean isJavaComplexType(Class<?> aClass)
	{
		return aClass.isArray()
				|| Collection.class.isAssignableFrom(aClass)
				|| Map.class.isAssignableFrom(aClass) ;
	}
	
	public static boolean isNumericType(String aDataType)
	{
		return sNumericTypes.contains(aDataType) ;
	}
	
	/**
	 * 
	 * @param aClass
	 * @param aFieldName	可以带m也可以不带m
	 * @return
	 */
	public static Field getField(Class<?> aClass , String aFieldName)
	{
		String[] fieldNames = null ;
		if(aFieldName.startsWith("m") && Character.isUpperCase(aFieldName.charAt(1)))
			fieldNames = new String[]{aFieldName} ;
		else
		{
			String fn = "m"+Character.toUpperCase(aFieldName.charAt(0))+aFieldName.substring(1) ;
			fieldNames = new String[]{aFieldName , fn} ;
		}
		for(Field field : aClass.getDeclaredFields())
		{
			String aName = field.getName() ;
			for(String fn : fieldNames)
			{
				if(fn.equals(aName))
					return field ;
			}
		}
		if(!Object.class.equals(aClass.getSuperclass()))
			return getField(aClass.getSuperclass(), aFieldName) ;
		return null ;
	}
	
	public static List<Field> getAllFieldsList(final Class<?> cls)
	{
		final List<Field> allFields = new ArrayList<Field>();
		Class<?> currentClass = cls;
		while (currentClass != null)
		{
			final Field[] declaredFields = currentClass.getDeclaredFields();
			for (final Field field : declaredFields)
			{
				allFields.add(field);
			}
			currentClass = currentClass.getSuperclass();
		}
		return allFields;
	}
	
	public static List<Method> getAllMethodList(final Class<?> cls)
	{
		final List<Method> allMethods = XC.arrayList() ;
		Class<?> currentClass = cls;
		while (currentClass != null)
		{
			final Method[] declaredMethods = currentClass.getDeclaredMethods() ;
			for (final Method method : declaredMethods)
			{
				allMethods.add(method);
			}
			currentClass = currentClass.getSuperclass();
		}
		return allMethods;
	}
	
	/**
	 * 指定类及其祖先类所有的字段
	 * @param cls
	 * @return
	 */
	public static Field[] getAllFields(final Class<?> cls)
	{
		final List<Field> allFieldsList = getAllFieldsList(cls);
		return allFieldsList.toArray(new Field[allFieldsList.size()]);
	}
	
	/** 
	* 从包package中获取所有的Class 
	* @param pack 
	* @return 
	*/
	public static List<Class<?>> getClasses(ClassLoader aClassLoader, String packageName)
	{

		//第一个class类的集合  
		List<Class<?>> classes = new ArrayList<Class<?>>();
		//是否循环迭代  
		boolean recursive = true;
		//获取包的名字 并进行替换  
		String packageDirName = packageName.replace('.', '/');
		//定义一个枚举的集合 并进行循环来处理这个目录下的things  
		Enumeration<URL> dirs;
		try
		{
			dirs = aClassLoader.getResources(packageDirName);
			//循环迭代下去  
			while (dirs.hasMoreElements())
			{
				//获取下一个元素  
				URL url = dirs.nextElement();
				//得到协议的名称  
				String protocol = url.getProtocol();
				//如果是以文件的形式保存在服务器上
				if ("bundleresource".equals(protocol))
				{
					url = toFileURL_BundleResource(url);
					protocol = url.getProtocol();
				}
				if ("file".equals(protocol))
				{
					//获取包的物理路径  
					String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
					//以文件的方式扫描整个包下的文件 并添加到集合中  
					findAndAddClassesInPackageByFile(aClassLoader, packageName, filePath, recursive, classes);
				}
				else if ("jar".equals(protocol))
				{
					//如果是jar包文件   
					//定义一个JarFile  
					JarFile jar;
					try
					{
						//获取jar  
						jar = ((JarURLConnection) url.openConnection()).getJarFile();
						//从此jar包 得到一个枚举类  
						Enumeration<JarEntry> entries = jar.entries();
						//同样的进行循环迭代  
						while (entries.hasMoreElements())
						{
							//获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件  
							JarEntry entry = entries.nextElement();
							String name = entry.getName();
							//如果是以/开头的  
							if (name.charAt(0) == '/')
							{
								//获取后面的字符串  
								name = name.substring(1);
							}
							//如果前半部分和定义的包名相同  
							if (name.startsWith(packageDirName))
							{
								int idx = name.lastIndexOf('/');
								//如果以"/"结尾 是一个包  
								if (idx != -1)
								{
									//获取包名 把"/"替换成"."  
									packageName = name.substring(0, idx).replace('/', '.');
								}
								//如果可以迭代下去 并且是一个包  
								if ((idx != -1) || recursive)
								{
									//如果是一个.class文件 而且不是目录  
									if (name.endsWith(".class") && !entry.isDirectory())
									{
										//去掉后面的".class" 获取真正的类名  
										String className = name.substring(packageName.length() + 1, name.length() - 6);
										try
										{
											//添加到classes  
											classes.add(aClassLoader.loadClass(packageName + '.' + className));
										}
										catch (ClassNotFoundException e)
										{
											e.printStackTrace();
										}
									}
								}
							}
						}
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return classes;
	}

	/** 
	 * 以文件的形式来获取包下的所有Class 
	 * @param packageName 
	 * @param packagePath 
	 * @param recursive 
	 * @param classes 
	 */
	public static void findAndAddClassesInPackageByFile(ClassLoader aClassLoader, String packageName,
			String packagePath,
			final boolean recursive,
			List<Class<?>> classes)
	{
		//获取此包的目录 建立一个File  
		File dir = new File(packagePath);
		//如果不存在或者 也不是目录就直接返回  
		if (!dir.exists() || !dir.isDirectory())
		{
			return;
		}
		//如果存在 就获取包下的所有文件 包括目录  
		File[] dirfiles = dir.listFiles(new FileFilter()
		{
			//自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)  
			public boolean accept(File file)
			{
				return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
			}
		});
		//循环所有文件  
		for (File file : dirfiles)
		{
			//如果是目录 则继续扫描  
			if (file.isDirectory())
			{
				findAndAddClassesInPackageByFile(aClassLoader, packageName + "." + file.getName(),
										file.getAbsolutePath(),
										recursive,
										classes);
			}
			else
			{
				//如果是java类文件 去掉后面的.class 只留下类名  
				String className = file.getName().substring(0, file.getName().length() - 6);
				try
				{
					//添加到集合中去  
					classes.add(aClassLoader.loadClass(packageName + '.' + className));
				}
				catch (ClassNotFoundException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public static List<Class<?>> getAllClassByInterface(Class<?> c)
	{
		return getAllClassByInterface(c, JCommon.getYClassLoader()) ;
	}

	/**
	 * 取得指定接口所在包下所有实现这个接口的类
	 * */
	public static List<Class<?>> getAllClassByInterface(Class<?> c , ClassLoader aClassLoader)
	{
		List<Class<?>> returnClassList = null;
		if (c.isInterface())
		{
			// 获取当前的包名
			String packageName = c.getPackage().getName();
			// 获取当前包下以及子包下所以的类
			List<Class<?>> allClass = getClasses(packageName , aClassLoader);
			if (allClass != null)
			{
				returnClassList = new ArrayList<Class<?>>();
				for (Class<?> classes : allClass)
				{
					// 判断是否是同一个接口
					if (c.isAssignableFrom(classes))
					{
						// 本身不加入进去
						if (!c.equals(classes))
						{
							returnClassList.add(classes);
						}
					}
				}
			}
		}

		return returnClassList;
	}
	
	public static List<Class<?>> getAllClassByInterface(Class<?> c , String aPackageName)
	{
		return getAllClassByInterface(c, aPackageName , null) ;
	}
	
	public static List<Class<?>> getAllClassByInterface(Class<?> c , String aPackageName
			, ClassLoader aClassLoader)
	{
		List<Class<?>> returnClassList = null;
		if (c.isInterface())
		{
			// 获取当前包下以及子包下所以的类
			List<Class<?>> allClass = getClasses(aPackageName , aClassLoader);
			if (allClass != null)
			{
				returnClassList = new ArrayList<Class<?>>();
				for (Class<?> classes : allClass)
				{
					// 判断是否是同一个接口
					if (c.isAssignableFrom(classes))
					{
						// 本身不加入进去
						if (!c.equals(classes))
						{
							returnClassList.add(classes);
						}
					}
				}
			}
		}

		return returnClassList;
	}
	
	public static List<Class<?>> getAllClassByAnnotation(Class<? extends Annotation> c , String aPackageName)
	{
		return getAllClassByAnnotation(c, aPackageName, null) ;
	}
	
	public static List<Class<?>> getAllClassByAnnotation(Class<? extends Annotation> c , String aPackageName
			, ClassLoader aClassLoader)
	{
		List<Class<?>> returnClassList = null;
		if (c.isAnnotation())
		{
			// 获取当前包下以及子包下所以的类
			List<Class<?>> allClass = getClasses(aPackageName , aClassLoader);
			if (allClass != null)
			{
				returnClassList = new ArrayList<Class<?>>();
				for (Class<?> clazz : allClass)
				{
					// 判断是否是同一个接口
					if (clazz.getAnnotation(c) != null)
					{
						returnClassList.add(clazz);
					}
				}
			}
		}

		return returnClassList;
	}
	
	/**
	 * 获取指定类中具有指定注解的所有方法。
	 *
	 * @param aAnnoClass 指定要查找的注解类型，必须是Annotation的子类。
	 * @param aClass      要查找方法的类。
	 * @return 返回一个包含所有具有指定注解的方法的List集合。
	 *         如果该类中没有方法具有指定的注解，则返回一个空列表。
	 *         注意：返回的列表是ArrayList类型，但方法签名中使用了List接口，
	 *         以提供更大的灵活性。
	 */
	public static List<Method> getMethodsByAnnotation(Class<? extends Annotation> aAnnoClass , Class<?> aClass)
	{
		return XC.extractAsArrayList(aClass.getDeclaredMethods()
				, method->method.isAnnotationPresent(aAnnoClass)
				, method->method
				, true) ;
	}
	
	public static List<Class<?>> getAllSubClass(Class<?> c , String aPackageName 
			, boolean aIncludeAbstractInterface , ClassLoader aClassLoader)
	{
		List<Class<?>> returnClassList = null;
		// 获取当前包下以及子包下所以的类
		List<Class<?>> allClass = getClasses(aPackageName , aClassLoader);
		if (allClass != null)
		{
			returnClassList = new ArrayList<Class<?>>();
			for (Class<?> clazz : allClass)
			{
				// 判断是否是同一个接口
				if (c.isAssignableFrom(clazz) && !c.equals(clazz)
						&& (aIncludeAbstractInterface || (Bits.hit(clazz.getModifiers(), Modifier.PUBLIC)
								&& !Bits.hit(clazz.getModifiers(), Modifier.ABSTRACT)
								&& !clazz.isInterface())))
				{
					returnClassList.add(clazz);
				}
			}
		}

		return returnClassList;
	}

	/*
	 * 取得某一类所在包的所有类名 不含迭代
	 */
	public static String[] getPackageAllClassName(String aClassLocation, String packageName)
	{
		//将packageName分解
		String[] packagePathSplit = packageName.split("[.]");
		String realClassLocation = aClassLocation;
		int packageLength = packagePathSplit.length;
		for (int i = 0; i < packageLength; i++)
		{
			realClassLocation = realClassLocation + File.separator + packagePathSplit[i];
		}
		File packeageDir = new File(realClassLocation);
		if (packeageDir.isDirectory())
		{
			String[] allClassName = packeageDir.list();
			return allClassName;
		}
		return null;
	}
	
	public static Enumeration<URL> getPackage(String aPackageName) throws IOException
	{
		//获取包的名字 并进行替换
		String packageDirName = aPackageName.replace('.', '/');
		//定义一个枚举的集合 并进行循环来处理这个目录下的things
		return JCommon.getYClassLoader().getResources(packageDirName);
	}
	
	/**
	 * 获取指定包及其子包下面的类
	 * @param aPackageName
	 * @return
	 */
	public static List<Class<?>> getClasses(String aPackageName)
	{
		return getClasses(aPackageName, Thread.currentThread().getContextClassLoader() , true) ;
 	}
	
	public static List<Class<?>> getClasses(String aPackageName , boolean aRecursive)
	{
		return getClasses(aPackageName, Thread.currentThread().getContextClassLoader() , aRecursive) ;
 	}
	
	/**
	 * 
	 * @param aPackageName			包名
	 * @param aClassLoader
	 * @param aRecursive			是否深入到子文件夹
	 * @return
	 */
	public static List<String> getClassNames(String aPackageName , ClassLoader aClassLoader
			, boolean aRecursive)
	{
		if(aClassLoader == null)
			aClassLoader = JCommon.getYClassLoader() ;
		//第一个class类的集合
		List<String> classNames = XC.arrayList() ;
		//获取包的名字 并进行替换
		String packageDirName = aPackageName.replace('.', '/');
		//定义一个枚举的集合 并进行循环来处理这个目录下的things
		Enumeration<URL> dirs;
		try
		{
			dirs = aClassLoader.getResources(packageDirName);
			if(dirs == null)
				return classNames ;
			//循环迭代下去
			while (dirs.hasMoreElements())
			{
				//获取下一个元素
				URL url = dirs.nextElement();
				//得到协议的名称
				String protocol = url.getProtocol();
				//如果是以文件的形式保存在服务器上
				if ("bundleresource".equals(protocol))
				{
					url = toFileURL_BundleResource(url);
					protocol = url.getProtocol();
				}
				if ("file".equals(protocol))
				{
					//获取包的物理路径
					String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
					//以文件的方式扫描整个包下的文件 并添加到集合中
					findAndAddClassNamesInPackageByFile(aPackageName, filePath, aRecursive, classNames);
				}
				else if ("jar".equals(protocol))
				{
					//如果是jar包文件 
					//定义一个JarFile
					JarFile jar;
					try
					{
						//获取jar
						jar = ((JarURLConnection) url.openConnection()).getJarFile();
						//从此jar包 得到一个枚举类
						Enumeration<JarEntry> entries = jar.entries();
						//同样的进行循环迭代
						while (entries.hasMoreElements())
						{
							//获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
							JarEntry entry = entries.nextElement();
							String name = entry.getName();
							//如果是以/开头的
							if (name.charAt(0) == '/')
							{
								//获取后面的字符串
								name = name.substring(1);
							}
							//如果前半部分和定义的包名相同
							if (name.startsWith(packageDirName))
							{
								int idx = name.lastIndexOf('/');
								//如果以"/"结尾 是一个包
								if (idx != -1)
								{
									//获取包名 把"/"替换成"."
									aPackageName = name.substring(0, idx).replace('/', '.');
								}
								//如果可以迭代下去 并且是一个包
								if ((idx != -1) || aRecursive)
								{
									//如果是一个.class文件 而且不是目录
									if (name.endsWith(".class") && !entry.isDirectory())
									{
										//去掉后面的".class" 获取真正的类名
										String classSimpleName = name.substring(aPackageName.length() + 1, name.length() - 6);
										classNames.add(aPackageName + '.' + classSimpleName) ;
									}
								}
							}
						}
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}

		return classNames ;
	}

	/**
	 * 获取指定包及其子包下的类
	 * @param aPackageName
	 * @param aClassLoader
	 * @return
	 */
	public static List<Class<?>> getClasses(String aPackageName , ClassLoader aClassLoader)
	{
		return getClasses(aPackageName, aClassLoader, true) ;
	}
	
	/**
	 * 从包package中获取所有的Class
	 * @param pack
	 * @return
	 */
	public static List<Class<?>> getClasses(String aPackageName , ClassLoader aClassLoader
			, boolean aRecursive)
	{
		if(aClassLoader == null)
			aClassLoader = JCommon.getYClassLoader() ;
		List<String> classNames = getClassNames(aPackageName , aClassLoader , aRecursive) ;
		if(XC.isNotEmpty(classNames))
		{
			List<Class<?>> classList = XC.arrayList() ;
			for(String className : classNames)
			{
				try
				{
					//添加到classes
					classList.add(aClassLoader.loadClass(className));
				}
				catch (ClassNotFoundException e)
				{
					Log.error(XClassUtil.class , e);
				}
			}
			return classList ;
		}
		return Collections.emptyList() ;
	}
	
	/**
	 * 以文件的形式来获取包下的所有Class
	 * @param packageName
	 * @param packagePath
	 * @param recursive
	 * @param classes
	 */
	public static void findAndAddClassNamesInPackageByFile(String aPackageName,
			String aPackagePath,
			final boolean aRecursive,
			List<String> aClassNames)
	{
		//获取此包的目录 建立一个File
		File dir = new File(aPackagePath);
		//如果不存在或者 也不是目录就直接返回
		if (!dir.exists() || !dir.isDirectory())
		{
			return;
		}
		//如果存在 就获取包下的所有文件 包括目录
		File[] dirfiles = dir.listFiles(new FileFilter()
		{
			//自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
			public boolean accept(File file)
			{
				return (aRecursive && file.isDirectory()) || (file.getName().endsWith(".class"));
			}
		});
		//循环所有文件
		for (File file : dirfiles)
		{
			//如果是目录 则继续扫描
			if (file.isDirectory())
			{
				findAndAddClassNamesInPackageByFile(aPackageName + "." + file.getName(),
											file.getAbsolutePath(),
											aRecursive,
											aClassNames);
			}
			else
			{
				//如果是java类文件 去掉后面的.class 只留下类名
				String classSimpleName = file.getName().substring(0, file.getName().length() - 6);
				
				//添加到集合中去
				String className = aPackageName + '.' + classSimpleName ;
				aClassNames.add(className) ;
			}
		}
	}

	/**
	 * 以文件的形式来获取包下的所有Class
	 * @param packageName
	 * @param packagePath
	 * @param recursive
	 * @param classes
	 */
	public static void findAndAddClassesInPackageByFile(String packageName,
			String packagePath,
			final boolean recursive,
			List<Class<?>> classes)
	{
		//获取此包的目录 建立一个File
		File dir = new File(packagePath);
		//如果不存在或者 也不是目录就直接返回
		if (!dir.exists() || !dir.isDirectory())
		{
			return;
		}
		//如果存在 就获取包下的所有文件 包括目录
		File[] dirfiles = dir.listFiles(new FileFilter()
		{
			//自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
			public boolean accept(File file)
			{
				return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
			}
		});
		//循环所有文件
		for (File file : dirfiles)
		{
			//如果是目录 则继续扫描
			if (file.isDirectory())
			{
				findAndAddClassesInPackageByFile(packageName + "." + file.getName(),
											file.getAbsolutePath(),
											recursive,
											classes);
			}
			else
			{
				//如果是java类文件 去掉后面的.class 只留下类名
				String className = file.getName().substring(0, file.getName().length() - 6);
				try
				{
					//添加到集合中去
					String classPath = packageName + '.' + className ;
					Class<?> clazz = JCommon.getYClassLoader().loadClass(classPath) ;
					if(clazz != null)
						classes.add(clazz);
					else
						System.out.println("无法加载类："+classPath);
				}
				catch (ClassNotFoundException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public static boolean isDeprecated(Class<?> aClass)
	{
		Annotation[] annos = aClass.getAnnotations() ;
		if(annos != null && annos.length>0)
		{
			for(Annotation anno : annos)
				if(Deprecated.class.equals(anno))
					return true ;
		}
		return false ;
	}
	
	public static Object clone(Object aObj)
	{
		if(aObj != null)
		{
			if(aObj instanceof Cloneable)
				return clone0(aObj) ;
			else if(XClassUtil.isBasicJavaDataType(aObj.getClass()))
				return aObj ;
		}
		return null ;
	}
	
	static Object clone0(Object aObj)
	{
		if(aObj.getClass().isArray())
		{
			if(Cloneable.class.isAssignableFrom(aObj.getClass().getComponentType()))
			{
				int len = Array.getLength(aObj) ;
				Object array = Array.newInstance(aObj.getClass().getComponentType(), len) ;
				for(int i=0 ;i<len ; i++)
					Array.set(array, i, clone0(Array.get(aObj, i))) ;
				return array ;
			}
		}
		else
		{
			try
			{
				Method method = aObj.getClass().getMethod("clone") ;
				if((method.getModifiers()&Modifier.PUBLIC) == Modifier.PUBLIC
						&& (method.getModifiers()&Modifier.STATIC) != Modifier.STATIC)
				{
					return method.invoke(aObj) ;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace() ;
			}
		}
		return null ;
	}
	
	/**
	 * 取得这个类包括基类和接口的所有修饰符等于或包含aModifiers的字段
	 * @param aClass
	 * @param aModifiers
	 * @return
	 */
	public static Field[] getFields(Class<?> aClass , int aModifiers)
	{
		return getFields(aClass, aModifiers, null) ;
	}
	
	public static Field[] getFields(Class<?> aClass , int aModifiers , Predicate<Field> aFieldPred)
	{
		List<Field> fieldList = new ArrayList<>() ;
		getFields(aClass, aModifiers, aFieldPred , fieldList);
		Class<?>[] classes = aClass.getInterfaces() ;
		if(classes != null && classes.length>0)
		{
			for(Class<?> clazz : classes)
				getFields(clazz, aModifiers ,aFieldPred , fieldList);
		}
		return fieldList.toArray(new Field[0]) ;
	}
	
	private static void getFields(Class<?> aClass , int aModifiers , Predicate<Field> aFieldPred , List<Field> aList)
	{
		if(aClass != null && !Object.class.equals(aClass))
		{
			Field[] fields = aClass.getDeclaredFields() ;
			if(fields != null)
			{
				for(Field field : fields)
				{
					if((aModifiers ==0 || Bits.hit(field.getModifiers(),aModifiers))
							&& (aFieldPred == null || aFieldPred.test(field)))
						aList.add(field) ;
				}
			}
			if(!aClass.isInterface())
				getFields(aClass.getSuperclass(), aModifiers , aFieldPred , aList);
		}
	}
	
	public static String toString(Object aVal)
	{
		return toString(aVal, null) ;
	}
	
	public static String toString(Object aVal , String aDefaultVal)
	{
		if(aVal == null)
			return aDefaultVal ;
		if(aVal instanceof JSONString)
			return ((JSONString)aVal).toJSONString() ;
		if(aVal instanceof Date)
			return XTime.format$yyyyMMddHHmmssSSS((Date)aVal , aDefaultVal) ;
		if(aVal instanceof Map)
			return new JSONObject(aVal).toJSONString() ;
		if(aVal instanceof Iterable<?> || aVal.getClass().isArray())
			return new JSONArray(aVal).toJSONString() ;
		return aVal.toString() ;
	}
	
	public static char toChar(Object aVal , char aDefault)
	{
		if(aVal == null)
			return aDefault ;
		if(aVal instanceof Character)
			return ((Character)aVal).charValue() ;
		if(aVal instanceof String && ((String)aVal).length() == 1)
			return ((String)aVal).charAt(0) ;
		if(aVal instanceof Boolean)
		{
			return ((Boolean)aVal).booleanValue()?'Y':'N' ;
		}
		Integer v = toInteger(aVal) ;
		if(v != null && v>0 && v<65536)
			return (char)v.intValue() ;
		String vs = aVal.toString() ;
		if(vs.length() == 1)
			return vs.charAt(0) ;
		return aDefault ;
	}
	
	public static Character toCharacter(Object aVal)
	{
		if(aVal == null)
			return null ;
		if(aVal instanceof Character)
			return ((Character)aVal).charValue() ;
		if(aVal instanceof String && ((String)aVal).length() == 1)
			return ((String)aVal).charAt(0) ;
		if(aVal instanceof Boolean)
		{
			return ((Boolean)aVal).booleanValue()?'Y':'N' ;
		}
		Integer v = toInteger(aVal) ;
		if(v != null && v>0 && v<65536)
			return (char)v.intValue() ;
		String vs = aVal.toString() ;
		if(vs.length() == 1)
			return vs.charAt(0) ;
		return null ;
	}
	
	public static Boolean toBoolean(Object aVal)
	{
		return toBoolean_0(aVal , true) ;
	}
	
	public static Boolean assetBoolean(Object aVal)
	{
		return toBoolean_0(aVal , false) ;
	}
	
	static Boolean toBoolean_0(Object aVal , boolean aQuietly)
	{
		if(aVal == null)
			return null ;
		if(aVal instanceof Boolean)
			return (Boolean)aVal ;
		if(aVal instanceof String)
		{
			if(((String)aVal).isEmpty())
				return null ;
			return "true".equalsIgnoreCase((String)aVal) ;
		}
		if(aVal instanceof Number)
			return ((Number)aVal).intValue() > 0 ;
		if(aQuietly)
			return null ;
		throw new IllegalArgumentException(String.format("不支持将%s类型的对象转化成Boolean" 
				, aVal.getClass().getName())) ;
	}
	
	public static boolean toBoolean(Object aVal , boolean aDefultVal)
	{
		Boolean val = toBoolean(aVal) ;
		return val == null?aDefultVal:val.booleanValue() ;
	}
	
	static Integer _toInteger(Object aVal , boolean aQuietly)
	{
		if(aVal == null)
			return null ;
		if(aVal instanceof Integer)
			return (Integer)aVal ;
		if(aVal instanceof String)
		{
			if(((String)aVal).isEmpty())
				return null ;
			else
				return Integer.valueOf((String)aVal) ;
		}
		if(aVal instanceof Number)
			return ((Number)aVal).intValue() ;
		if(aVal instanceof Boolean)
			return ((Boolean)aVal).booleanValue()?1:0 ;
		if(aQuietly)
			return null ;
		throw new IllegalArgumentException(String.format("不支持将%s类型的对象转化成Integer" 
				, aVal.getClass().getName())) ;
	}
	
	public static Integer assetInteger(Object aVal)
	{
		return _toInteger(aVal, false) ;
	}
	
	public static Integer toInteger(Object aVal)
	{
		return _toInteger(aVal, true) ;
	}
	
	public static int toInteger(Object aVal , int aDefaultVal)
	{
		Integer val = toInteger(aVal) ;
		return val==null?aDefaultVal:val.intValue() ;
	}
	
	public static Long toLong(Object aVal)
	{
		return _toLong(aVal, true) ;
	}
	
	public static Long assetLong(Object aVal)
	{
		return _toLong(aVal, false) ;
	}
	
	public static long toLong(Object aVal , long aDefaultVal)
	{
		Long val = toLong(aVal) ;
		return val==null?aDefaultVal:val.longValue() ;
	}
	
	static Long _toLong(Object aVal , boolean aQuietly)
	{
		if(aVal == null)
			return null ;
		if(aVal instanceof Long)
			return (Long)aVal ;
		if(aVal instanceof String)
		{
			String str = (String)aVal ;
			if(str.isEmpty())
				return null ;
			try
			{
			if(str.length()<=20)
			{
				int i = str.indexOf('.') ;
				if(i > 0)
					return Long.valueOf(str.substring(0 , i)) ;
			}
			return Long.valueOf((String)aVal) ;
			}
			catch(NumberFormatException e)
			{
				if(aQuietly)
					return null ;
				throw e ;
			}
		}
		if(aVal instanceof Number)
			return ((Number)aVal).longValue() ;
		if(aVal instanceof Date)
			return ((Date)aVal).getTime() ;
		if(aQuietly)
			return null ;
		throw new IllegalArgumentException(String.format("不支持将%s类型的对象转化成Long" 
				, aVal.getClass().getName())) ;
	}
	
	public static Double toDouble(Object aVal)
	{
		return _toDouble(aVal, true) ;
	}
	
	public static Double assetDouble(Object aVal)
	{
		return _toDouble(aVal, false) ;
	}
	
	static Double _toDouble(Object aVal , boolean aQuietly)
	{
		if(aVal == null)
			return null ;
		if(aVal instanceof Double)
			return (Double)aVal ;
		if(aVal instanceof String)
		{
			if(((String)aVal).isEmpty())
				return null ;
			int i = ((String) aVal).indexOf(',') ;
			try
			{
				return i == -1?Double.valueOf((String)aVal):sDFmt.parse((String)aVal).doubleValue() ;
			}
			catch (NumberFormatException | ParseException e)
			{
				throw new IllegalStateException(e) ;
			}
		}
		if(aVal instanceof Number)
			return ((Number)aVal).doubleValue() ;
		if(aVal instanceof Date)
			return Double.valueOf(((Date)aVal).getTime()) ;
		if(aQuietly)
			return null ;
		throw new IllegalArgumentException(String.format("不支持将%s类型的对象转化成Double" 
				, aVal.getClass().getName())) ;
	}
	
	public static Date toDate(Object aVal)
	{
		if(aVal == null)
			return null ;
		if(aVal instanceof Date)
			return (Date)aVal ;
		if(aVal instanceof String)
		{
			try
			{
				return XTime.adaptiveParse((String)aVal) ;
			}
			catch (ParseException e)
			{
				throw new IllegalStateException(e.getMessage()) ;
			}
		}
		if(aVal instanceof Number)
		{
			return new Date(((Number)aVal).longValue()) ;
		}
		throw new IllegalArgumentException(String.format("不支持将%s类型的对象转化成Date" 
				, aVal.getClass().getName())) ;
	}
	
	public static java.sql.Date toSqlDate(Object aVal)
	{
		if(aVal == null)
			return null ;
		if(aVal instanceof java.sql.Date)
			return (java.sql.Date)aVal ;
		if(aVal instanceof Date)
			return new java.sql.Date(((Date)aVal).getTime()) ;
		if(aVal instanceof String)
		{
			try
			{
				return new java.sql.Date(XTime.adaptiveParse((String)aVal).getTime()) ;
			}
			catch (ParseException e)
			{
				throw new IllegalStateException(e.getMessage()) ;
			}
		}
		throw new IllegalArgumentException(String.format("不支持将%s类型的对象转化成Date" 
				, aVal.getClass().getName())) ;
	}
	
	public static java.sql.Timestamp toSqlDateTime(Object aVal)
	{
		if(aVal == null)
			return null ;
		if(aVal instanceof java.sql.Timestamp)
			return (java.sql.Timestamp)aVal ;
		if(aVal instanceof Date)
			return new java.sql.Timestamp(((Date)aVal).getTime()) ;
		if(aVal instanceof java.sql.Date)
			return new java.sql.Timestamp(((java.sql.Date)aVal).getTime()) ;
		if(aVal instanceof String)
		{
			if(((String)aVal).isEmpty())
				return null ;
			try
			{
				return new java.sql.Timestamp(XTime.adaptiveParse((String)aVal).getTime()) ;
			}
			catch (ParseException e)
			{
				throw new IllegalStateException(e.getMessage()) ;
			}
		}
		throw new IllegalArgumentException(String.format("不支持将%s类型的对象转化成Date" 
				, aVal.getClass().getName())) ;
	}
	
	public static double toDouble(Object aVal , double aDefaultVal)
	{
		Double val = toDouble(aVal) ;
		return val==null?aDefaultVal:val.doubleValue() ;
	}
	
	public static Float assetFloat(Object aVal)
	{
		return _toFloat(aVal, false) ;
	}
	
	public static Float toFloat(Object aVal)
	{
		return _toFloat(aVal, true) ;
	}
	
	public static Float _toFloat(Object aVal , boolean aQuietly)
	{
		if(aVal == null)
			return null ;
		if(aVal instanceof Float)
			return (Float)aVal ;
		if(aVal instanceof String)
		{
			if(((String)aVal).isEmpty())
				return null ;
			return Float.valueOf((String)aVal) ;
		}
		if(aVal instanceof Number)
			return ((Number)aVal).floatValue() ;
		if(aQuietly)
			return null ;
		throw new IllegalArgumentException(String.format("不支持将%s类型的对象转化成Float" 
				, aVal.getClass().getName())) ;
	}
	
	public static float toFloat(Object aVal , float aDefaultVal)
	{
		Float val = _toFloat(aVal , true) ;
		return val==null?aDefaultVal:val.floatValue() ;
	}
	
	public static Byte toByte(Object aVal)
	{
		if(aVal == null)
			return null ;
		if(aVal instanceof Byte)
			return (Byte)aVal ;
		if(aVal instanceof String)
		{
			if(((String)aVal).isEmpty())
				return null ;
			return Byte.valueOf((String)aVal) ;
		}
		if(aVal instanceof Integer)
			return ((Integer)aVal).byteValue() ;
		if(aVal instanceof Long)
			return ((Long)aVal).byteValue() ;
		if(aVal instanceof Short)
			return ((Short)aVal).byteValue() ;
		throw new IllegalArgumentException(String.format("不支持将%s类型的对象转化成Byte" 
				, aVal.getClass().getName())) ;
	}
	
	public static byte toByte(Object aVal , byte aDefaultVal)
	{
		Byte val = toByte(aVal) ;
		return val == null?aDefaultVal:val.byteValue() ;
	}
	
	public static Short toShort(Object aVal)
	{
		if(aVal == null)
			return null ;
		if(aVal instanceof Short)
			return (Short)aVal ;
		if(aVal instanceof String)
		{
			if(((String)aVal).isEmpty())
				return null ;
			return Short.valueOf((String)aVal) ;
		}
		if(aVal instanceof Integer)
			return ((Integer)aVal).shortValue() ;
		if(aVal instanceof Long)
			return ((Long)aVal).shortValue() ;
		if(aVal instanceof Byte)
			return ((Byte)aVal).shortValue() ;
		throw new IllegalArgumentException(String.format("不支持将%s类型的对象转化成Byte" 
				, aVal.getClass().getName())) ;
	}
	
	public static short toShort(Object aVal , short aDefaultVal)
	{
		Short val = toShort(aVal, aDefaultVal) ;
		return val==null?aDefaultVal:val.shortValue() ;
	}
	
	public static byte[] toBytes(Object aVal)
	{
		if(aVal == null)
			return null ;
		if(aVal instanceof byte[])
			return (byte[])aVal ;
		throw new IllegalArgumentException(String.format("不支持将%s类型的对象转化成byte[]" 
				, aVal.getClass().getName())) ;
	}
	
	/**
	 * 取得类型名称简写所对应的Class<?>
	 * @param aCSN
	 * @return
	 */
	public static Class<?> getClassOfCSN(String aCSN)
	{
		return sCSNMap.get(aCSN) ;
	}
	
	/**
	 * 给aVal拆箱			<br>
	 * 如果aVal == null ，将抛出异常
	 * @param aVal
	 * @param aMsg
	 * @param aArgs
	 * @return
	 */
	public static int uncrate(Integer aVal , String aMsg , Object...aArgs)
	{
		Assert.notNull(aVal, aMsg, aArgs) ;
		return aVal.intValue() ;
	}
	
	static URL toFileURL_BundleResource(URL aUrl)
	{
		final String fileLocaltorClsName = "org.eclipse.core.runtime.FileLocator" ;
		try
		{
			Class<?> clazz = JCommon.getYClassLoader().loadClass(fileLocaltorClsName) ;
			return (URL)invokeStaticMethod(clazz, "toFileURL" , null, aUrl) ;
		}
		catch (ClassNotFoundException e)
		{
			throw new IllegalStateException(XString.splice("无法加载类：" , fileLocaltorClsName)) ;
		}
		catch (NoSuchMethodException e)
		{
			throw new WrapException(e) ;
		}
	}
	
	public static Collection<String> getResourceNamesUnder(URL aUrl) throws IOException
	{
		String urlStr = aUrl.toString() ;
		if(urlStr.startsWith("file:/"))
		{
			File file = new File(aUrl.getFile()) ;
			if(file.isDirectory())
			{
				File[] files = file.listFiles() ;
				List<String> nameList = new ArrayList<>() ;
				if(XC.isNotEmpty(files))
				{
					for(File file_0 : files)
					{
						nameList.add(file_0.getName()) ;
					}
				}
				return nameList ;
			}
		}
		else if(urlStr.startsWith("jar:file:/"))
		{
			JarURLConnection jarConn = (JarURLConnection)aUrl.openConnection() ;
			String name = jarConn.getJarEntry().getName() ;
			final int nameLen = name.length() ;
			List<String> nameList = new ArrayList<>() ;
			jarConn.getJarFile().stream().collect(()->nameList , (nameList_0 , jarEntry)->{
				String name_0 = jarEntry.getName() ;
				if(name_0.startsWith(name) && name_0.length()>nameLen)
				{
					String resName= name_0.substring(name_0.charAt(nameLen)=='/'?nameLen+1:nameLen) ; ;
					if(XString.count(resName, '/', 0) == 0)
						nameList_0.add(resName) ;
				}
			} , (jarEntryList_0 , jarEntryList_1)->{}) ;
			return nameList ;
		}
		return null ;
	}
	
	/**
	 * 
	 * @param aResourceName		取得aClass所在工程(源码工程或jar)下的资源的输入流，不要以“/”开头
	 * @return
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public static InputStream getProjectResourceAsStream(String aResourceName , Class<?> aClass)
	{
		if(JCommon.isEclipseApp())
			return aClass.getResourceAsStream("/"+aResourceName) ;
		else
		{
			String classPath = String.format("/%1$s.class" , aClass.getName().replace('.' , '/')) ;
			URL resURL = aClass.getResource(classPath) ;
			String resURLStr = resURL.toString() ;
			try
			{
				if(resURLStr.startsWith("file:/"))
				{
					String path = resURLStr.replace(classPath, "") ;
					File dir = new File(URI.create(path).toURL().getFile()) ;
					File file = new File(dir, aResourceName) ;
					if(file.exists())
						return new FileInputStream(file) ;
					else
					{
						file = new File(dir.getParent() , aResourceName) ;
						if(file.exists())
							return new FileInputStream(file) ;
					}
					return null ;
				}
				else
				{
					if(!aResourceName.startsWith("/"))
						aResourceName = "/"+aResourceName ;
					URL url = aClass.getResource(aResourceName) ;
					if(url != null)
					{
						String preSeg = resURLStr.replace(classPath, "") ;
						if(url.toString().startsWith(preSeg))
							return url.openStream() ;
						else
						{
							return URI.create(preSeg+aResourceName).toURL().openStream() ;
						}
					}
				}
			}
			catch (IOException e)
			{
			}
			return null ;
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T typeAdapt(Object aVal , Class<T> aTargetType)
	{
		if(aVal == null || aVal.getClass() == aTargetType || aTargetType.isAssignableFrom(aVal.getClass()))
			return (T)aVal ;
		ITypeAdapter<T> adapter = (ITypeAdapter<T>) sTypeAdapters.get(aTargetType) ;
		if(adapter != null)
			return adapter.apply(aVal) ;
		if(aTargetType.isEnum())
			return (T) Enum.valueOf((Class)aTargetType, aVal.toString()) ;
		throw new ClassCastException(aVal.getClass().getName()+"类型的对象不支持转成类型"+aTargetType.getName()) ;
	}
	
	public static Object typeAdapt(Object aVal , String aCSN)
	{
		return typeAdapt(aVal, sCSNMap.get(aCSN)) ;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> ITypeAdapter<T> getTypeAdapter(Class<T> aTargetType)
	{
		return (ITypeAdapter<T>) sTypeAdapters.get(aTargetType) ;
	}
	
	public static PropertiesEx loadManifest(Class<?> aClazz) throws MalformedURLException, IOException
	{
		String path = aClazz.getResource("").toString() ;
		return StreamAssist.loadMF(StreamAssist.getProjectRootResource("META-INF/MANIFEST.MF" 
				, FileUtils.getAncestorPath(path , XString.count(aClazz.getName() , '.' , 0)))) ;
	}
	
	public static String getSimpleName(String aClassName)
	{
		if(XString.isNotEmpty(aClassName))
		{
			int i = aClassName.lastIndexOf('.') ;
			if(i != -1)
				return aClassName.substring(i+1) ;
		}
		return aClassName ;
	}
}
