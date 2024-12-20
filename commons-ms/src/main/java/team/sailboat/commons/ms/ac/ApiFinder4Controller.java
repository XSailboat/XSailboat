package team.sailboat.commons.ms.ac;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.ms.ACKeys_Common;

/**
 * 
 * 应用中Controller对象API的查找工具
 *
 * @author yyl
 * @since 2024年11月13日
 */
public class ApiFinder4Controller
{
	/**
	 * 获取具有指定注解的API路径集合。
	 *
	 * <p>此方法通过Spring的ApplicationContext查找所有带有@Controller或@RestController注解的bean，
	 * 然后检查这些bean的类及其方法是否包含指定的注解。如果包含，则提取对应的API路径。</p>
	 *
	 *
	 * <p>此方法依赖于AppContext中存储的Spring ApplicationContext。</p>
	 *
	 * @param aAnnoClass 要查找的注解类。这个注解应该被用于标记API路径。
	 * @return 包含所有具有指定注解的API路径的集合。如果无法从AppContext中取得Spring ApplicationContext，则返回一个空集合。
	 * @throws IllegalArgumentException 如果AppContext中的Spring ApplicationContext为null。
	 */
	public static Set<String> getApiPaths(Class<? extends Annotation> aAnnoClass)
	{
		ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) AppContext.get(ACKeys_Common.sSpringAppContext) ;
		Assert.notNull(ctx , "无法从AppContext中取得SpringAppContext！") ;
		Set<String> apiPaths = XC.hashSet() ;
		getApiPaths(aAnnoClass , ctx.getBeansWithAnnotation(Controller.class).values() , apiPaths);
		getApiPaths(aAnnoClass , ctx.getBeansWithAnnotation(RestController.class).values() , apiPaths);
		return apiPaths ;
	}
	
	static void getApiPaths(Class<? extends Annotation> aAnnoClass , Collection<Object> aObjs
			, Collection<String> aApiPaths)
	{
		if(XC.isEmpty(aObjs))
			return ;
		for(Object obj : aObjs)
		{
			getApiPaths(aAnnoClass, obj.getClass(), aApiPaths) ;
		}
	}
	
	static void getApiPaths(Class<? extends Annotation> aAnnoClass , Class<?> aClass
			, Collection<String> aApiPaths)
	{
		for(Method method : XClassUtil.getMethodsByAnnotation(aAnnoClass , aClass))
		{
			RequestMapping rm = method.getAnnotation(RequestMapping.class) ;
			if(rm != null)
			{
				XC.addAll(aApiPaths , rm.value()) ;
				continue ;
			}
			GetMapping gm = method.getAnnotation(GetMapping.class) ;
			if(gm != null)
			{
				XC.addAll(aApiPaths , gm.value()) ;
				continue ;
			}
			PostMapping pm = method.getAnnotation(PostMapping.class) ;
			if(pm != null)
			{
				XC.addAll(aApiPaths , pm.value()) ;
				continue ;
			}
			PutMapping pum = method.getAnnotation(PutMapping.class) ;
			if(pum != null)
			{
				XC.addAll(aApiPaths , pum.value()) ;
				continue ;
			}
			PatchMapping pam = method.getAnnotation(PatchMapping.class) ;
			if(pam != null)
			{
				XC.addAll(aApiPaths , pam.value()) ;
				continue ;
			}
			DeleteMapping dm = method.getAnnotation(DeleteMapping.class) ;
			if(dm != null)
			{
				XC.addAll(aApiPaths , dm.value()) ;
				continue ;
			}
		}
	}
	
	/**
	 * 获取指定注解标注的API路径集合
	 *
	 * @param aAnnoClass  需要查找的注解类型，必须是Annotation的子类
	 * @param aPackages   需要扫描的包名数组
	 * @return            返回包含所有匹配API路径的集合
	 */
	public static Set<String> getApiPaths(Class<? extends Annotation> aAnnoClass , String[] aPackages)
	{
		Set<String> apiPaths = XC.hashSet() ;
		for(String pkg : aPackages)
		{
			XClassUtil.getAllClassByAnnotation(Controller.class, pkg)
					.forEach(clazz->getApiPaths(aAnnoClass, clazz , apiPaths)) ;
			XClassUtil.getAllClassByAnnotation(RestController.class, pkg)
					.forEach(clazz->getApiPaths(aAnnoClass, clazz , apiPaths)) ;
		}
		return apiPaths ;
	}
}
