package team.sailboat.commons.ms.db;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import team.sailboat.commons.fan.http.HttpStatus;
import team.sailboat.commons.fan.text.XString;

public class AntiSqlInjectionInterceptor implements HandlerInterceptor
{
	
	static Pattern sPtn = Pattern.compile("\\b(and|exec|insert|select|drop|grant|alter|delete|update|updatexml|count|chr|mid|master|truncate|char|declare|or)\\b|(\\*|;|\\+|'|%)") ;
	
	final Logger mLogger = LoggerFactory.getLogger(AntiSqlInjectionInterceptor.class) ;

	public AntiSqlInjectionInterceptor()
	{
	}
	
	@Override
	public boolean preHandle(HttpServletRequest aRequest, HttpServletResponse aResponse, Object aHandler)
			throws Exception
	{
		if (!(aHandler instanceof HandlerMethod))
		{
            mLogger.warn("UnSupport handler：{}" , aHandler.getClass().getName()) ;
            return true ;
        }
        List<String> list = getParamNames((HandlerMethod) aHandler);
        for (String paramName : list)
        {
            String paramValue = aRequest.getParameter(paramName) ;
            if (XString.isNotEmpty(paramValue) && sPtn.matcher(paramValue.toLowerCase()).find())
            {
            	aResponse.setStatus(HttpStatus.BAD_REQUEST.value()) ;
                aResponse.setHeader("Content-type", MediaType.TEXT_PLAIN_VALUE);
                aResponse.setHeader("Access-Control-Allow-Origin", "*");//跨域
                aResponse.getWriter().write(XString.msgFmt("不合法的参数[{}]：{}" , paramName , paramValue)) ;
                return false;
            }
        }
        return true;
	}
	
	/**
	 * 获取使用了该注解的参数名称
	 */
	private List<String> getParamNames(HandlerMethod aHandlerMethod)
	{
	    Parameter[] parameters = aHandlerMethod.getMethod().getParameters();
	    List<String> list = new ArrayList<>();
	    for (Parameter parameter : parameters)
	    {
	        //判断这个参数时候被加入了 ParamsNotNull. 的注解
	        //.isAnnotationPresent()  这个方法可以看一下
	        if(parameter.isAnnotationPresent(AntiSqlInjection.class))
	        {
	        	Annotation rpAnno = parameter.getAnnotation(RequestParam.class) ;
	        	if(rpAnno != null)
	        	{
	        		list.add(((RequestParam)rpAnno).name()) ;
	        	}
	        	else
	        		mLogger.warn("参数[{}]没有用RequestParam修饰，AntiSqlInjection注解不起作用" , parameter.getName()) ;
	        }
	    }
	    return list;
	}

}
