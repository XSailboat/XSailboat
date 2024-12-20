package team.sailboat.commons.ms.ac;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.security.sasl.AuthenticationException;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.lang.Assert;

/**
 * 
 * 保护当前服务的API，需要获得授权才能访问
 *
 * @author yyl
 * @since 2024年11月30日
 */
@Aspect
@Component
public class ApiAccessControlAspect
{	
	final Logger mLogger = LoggerFactory.getLogger(ApiAccessControlAspect.class) ;
	
	IApiPredicate mApiPred ;
	
	public ApiAccessControlAspect(IApiPredicate aApiPred)
	{
		mApiPred = aApiPred ;
	}
	
	@Around("@annotation(team.sailboat.commons.ms.ac.ProtectedApi)")
	public Object check(ProceedingJoinPoint aJoinpoint) throws Throwable
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication() ;
		if(auth == null)
			throw new AuthenticationException("未提供合法认证信息！") ;
		Object principal = auth.getPrincipal() ;
		if(principal == null || !(principal instanceof  String))
			throw new AuthenticationException("未提供合法认证信息！") ;
		if("anonymousUser".equals(principal))
			throw new AuthenticationException("未提供合法认证信息！") ;
		String appId = (String)auth.getDetails() ;
		
		// 检查RegisteredClient是不是有调用这个API的权限
		Class<? extends Object> controllerClass = aJoinpoint.getTarget().getClass();
		RequestMapping reqMap = controllerClass.getAnnotation(RequestMapping.class) ;
		String pathPrefix = "" ;
		if(reqMap != null)
		{
			String[] pathPrefixs = reqMap.value() ;
			if(XC.isNotEmpty(pathPrefixs))
			{
				pathPrefix = pathPrefixs[0] ;
			}
		}
//		向下强转不一定会成功，向上转型一定成功
		MethodSignature signature = (MethodSignature) aJoinpoint.getSignature();
		Method method = signature.getMethod();
		Annotation[] annos = method.getAnnotations() ;
		String[] paths = null ;
		String[] httpMethods = null ;
		if(XC.isNotEmpty(annos))
		{
			for(Annotation anno : annos)
			{
				Class<? extends Annotation> annoType = anno.annotationType() ;
				if(PostMapping.class.equals(annoType))
				{
					paths = ((PostMapping)anno).value() ;
					if(XC.isEmpty(paths))
						paths = ((PostMapping)anno).path() ;
					httpMethods = new String[] {HttpMethod.POST.name()} ;
					break ;
				}
				else if(GetMapping.class.equals(annoType))
				{
					paths = ((GetMapping)anno).value() ;
					if(XC.isEmpty(paths))
						paths = ((GetMapping)anno).path() ;
					httpMethods = new String[] {HttpMethod.GET.name()} ;
					break ;
				}
				else if(PutMapping.class.equals((annoType)))
				{
					paths = ((PutMapping)anno).value() ;
					if(XC.isEmpty(paths))
						paths = ((PutMapping)anno).path() ;
					httpMethods = new String[] {HttpMethod.PUT.name()} ;
					break ;
				}
				else if(DeleteMapping.class.equals((annoType)))
				{
					paths = ((DeleteMapping)anno).value() ;
					if(XC.isEmpty(paths))
						paths = ((DeleteMapping)anno).path() ;
					httpMethods = new String[] {HttpMethod.DELETE.name()} ;
					break ;
				}
				else if(PatchMapping.class.equals(annoType))
				{
					paths = ((PatchMapping)anno).value() ;
					if(XC.isEmpty(paths))
						paths = ((PatchMapping)anno).path() ;
					httpMethods = new String[] {HttpMethod.PATCH.name()} ;
					break ;
				}
				else if(RequestMapping.class.equals(annoType))
				{
					paths = ((RequestMapping)anno).value() ;
					if(XC.isEmpty(paths))
						paths = ((RequestMapping)anno).path() ;
					httpMethods = XC.extract(((RequestMapping)anno).method() , RequestMethod::name , String.class) ;
					break ;
				}
			}
		}
		Assert.notNull(paths , "没有在方法%1$s.%2$s上找到RequestMapping相关注解!" , controllerClass.getName() , method.getName()) ;
		boolean checked = false ;
		bp_1650:for(String path : paths)
		{
			String dpath = deflatePath(FileUtils.getPath(pathPrefix , path)) ;
			for(String httpMethod : httpMethods)
			{
				String apiName = dpath+httpMethod ;
				if(mApiPred.canInvokeApiOfClientApp(appId , apiName))
				{
					checked = true ;
					break bp_1650 ;
				}
			}
		}
		//
		if(!checked)
			throw new AuthenticationException("未获得调用此API的授权！") ;
		
		try
		{
			AppContext.setThreadLocal("ApiGuard:appId" , appId) ;
			Object result = aJoinpoint.proceed(aJoinpoint.getArgs()) ;
			return result ;
		}
		finally
		{
			AppContext.removeThreadLocal("ApiGuard:appId") ;
		}
	}
	
	public static String deflatePath(String aPath)
	{
		StringBuilder strBld = new StringBuilder() ;
		final int len = aPath.length() ;
		for(int i=0 ; i<len ; i++)
		{
			char ch = aPath.charAt(i) ;
			if(ch == '/')
			{
				if(i<len-1)
				{
					strBld.append(Character.toUpperCase(aPath.charAt(i+1))) ;
					i++ ;
				}
			}
			else
				strBld.append(ch) ;
		}
		return strBld.toString() ;
	}
}
