package team.sailboat.commons.ms.error ;

import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.AccessDeniedException;
import java.util.Date;
import java.util.Map;

import javax.security.sasl.AuthenticationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.http.HttpConst;
import team.sailboat.commons.fan.http.IdentityTrace;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.md5.MD5;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.fan.time.XTime;
import team.sailboat.commons.ms.exceps.RestException;

/**
 * 作为服务端，存在客户端在循环内高频次调用接口，每次调用都异常，但仍是没有中断循环的情况		<br />
 * 服务端避免一下，相同异常日志，高频次记录的情况，以减少日志量。
 *
 * @author yyl
 * @since 2024年8月1日
 */
@RestControllerAdvice
public class ControllerExcepHandler
{
	final Logger mLogger = LoggerFactory.getLogger(ControllerExcepHandler.class) ; 
	final Map<Class<?>, ExcepRecord> mExcepRcdMap = XC.concurrentHashMap() ;

	public ControllerExcepHandler()
	{
	}

	/**
     * 应用到所有@RequestMapping注解方法，在其执行之前初始化数据绑定器
     * @param binder
     */
    @InitBinder
    public void initBinder(WebDataBinder binder)
    {
//        System.out.println("请求有参数才进来");
    }

    /**
     * 把值绑定到Model中，使全局@RequestMapping可以获取到该值
     * @param model
     */
    @ModelAttribute
    public void addAttributes(Model model) {
//       
    }
    @ExceptionHandler(Exception.class)
//    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public Object handleException(Exception e , HttpServletRequest req)
    {
    	if(e instanceof UndeclaredThrowableException && e.getCause() != null)
    		e = (Exception)e.getCause() ;
    	String path = req.getServletPath() ;
    	StringBuilder msgBld = new StringBuilder(XString.msgFmt("来自[{}]的用户{} {} 请求[{}]出错" , req.getRemoteAddr() 
    			, IdentityTrace.get(req) , req.getMethod() , path))
    		.append("，查询参数").append(req.getQueryString())
    		.append(XString.sLineSeparator) ;
    	
    	Class<?> excepCls = e.getClass() ;
    	boolean needLog = true ;
    	ExcepRecord rcd = mExcepRcdMap.get(excepCls) ;
    	if(rcd == null)
    	{
    		rcd = new ExcepRecord(excepCls , MD5.calMd5(msgBld)) ;
    		mExcepRcdMap.put(excepCls, rcd) ;
    	}
    	else if(System.currentTimeMillis() -  rcd.tsMs < 5000)
    	{
    		// 5秒内发生多次，看看它的请求是否相同
    		String digest = MD5.calMd5(msgBld) ;
    		if(digest.equals(rcd.digest))
    		{
    			rcd.times++ ;
    			needLog = false ;
    		}
    		else
    		{
    			mLogger.info("抑制了 {} 次异常日志" , rcd.times-1);
    			rcd.digest = digest ;
    			rcd.times = 1 ;
    		}
    	}
    	else
    	{
    		mLogger.info("抑制了 {} 次异常日志" , rcd.times-1);
    		rcd.tsMs = System.currentTimeMillis() ;
    		rcd.times = 1 ;
    	}
    	
    	
    	Throwable e1 = WrapException.unwrap(e) ;
    	while(true)
    	{
	    	String stackTrace = ExceptionAssist.getStackTrace(e1) ;
	    	
	    	int i = stackTrace.indexOf("at org.springframework.cglib.proxy.MethodProxy") ;
	    	if(i == -1)
	    		i = stackTrace.indexOf("at org.springframework.web.method.support.InvocableHandlerMethod") ;
	    	if(i != -1)
	    	{
	    		msgBld.append(stackTrace.substring(0, i)).append("【此处省略】") ;
	    	}
	    	else
	    		msgBld.append(stackTrace) ;
	    	e1 = e1.getCause() ;
	    	if(e1 == null)
	    		break ;
	    	msgBld.append('\n') ;
    	}
    	if(needLog)
    		mLogger.error(msgBld.toString()) ;
    	//使用HttpServletRequest中的header检测请求是否为ajax, 如果是ajax则返回json, 如果为非ajax则返回view(即ModelAndView)
    	String contentTypeHeader = req.getHeader(HttpConst.sHeaderName_ContentType);
    	String acceptHeader = req.getHeader(HttpConst.sHeaderName_Accept);
    	String xRequestedWith = req.getHeader(HttpConst.sHeaderName_X_Requested_With);
    	String referer = req.getHeader(HttpConst.sHeaderName_referer) ;
    	int httpStatus = getHttpStatusFromException(e , 500) ;
    	if ((contentTypeHeader != null && contentTypeHeader.contains("application/json"))
              || (acceptHeader != null && acceptHeader.contains("application/json"))
              || "XMLHttpRequest".equalsIgnoreCase(xRequestedWith)
              || HttpConst.sHeaderValue_UserAgent_x_HttpClient.equalsIgnoreCase(req.getHeader(HttpConst.sHeaderName_UserAgent))
              || (referer != null && (referer.endsWith("swagger-ui.html") || referer.endsWith("/swagger-ui/index.html")))) 
    	{
    		Date current = new Date() ;
    		HttpHeaders httpHeaders = new HttpHeaders() ;
    		httpHeaders.set(HttpConst.sHeaderName_ContentType, MediaType.APPLICATION_JSON_VALUE) ;
    		return new ResponseEntity<>(new JSONObject().put("timestamp" , System.currentTimeMillis())
    				.put("status" , httpStatus)
    				.put("statusReason" , HttpStatus.valueOf(httpStatus).getReasonPhrase())
    				.put("message" , JCommon.defaultIfEmpty(ExceptionAssist.getRootMessage(e) , "异常消息为空"))
    				.put("path" , path)
    				.put("timestamp" , current.getTime())
    				.put("datetime" , XTime.format$yyyyMMddHHmmssSSS(current , ""))
    				.put("rootExceptionClass" , ExceptionAssist.getRootException(e).getClass().getName())
    				.toString() 
    			, httpHeaders
    			, HttpStatus.valueOf(httpStatus)) ;
    	} else {
    		ModelAndView modelAndView = new ModelAndView();
    		modelAndView.addObject("msg", JCommon.defaultIfEmpty(ExceptionAssist.getRootMessage(e) , "异常消息为空"));
    		modelAndView.addObject("url", req.getPathInfo());
    		modelAndView.addObject("http-status", httpStatus) ;
    		modelAndView.addObject("stackTrace", e.getStackTrace());
    		modelAndView.setViewName(XString.msgFmt("redirect:{}/error_view" , req.getContextPath())) ;
    		modelAndView.setStatus(HttpStatus.valueOf(httpStatus));
    		return modelAndView;
    	}
    }
    
    int getHttpStatusFromException(Exception aExecp , int aDefaultHttpStatus)
    {
    	if(aExecp instanceof IllegalArgumentException)
    		return 400 ;
    	else if(aExecp instanceof RestException)
    		return ((RestException)aExecp).getHttpStatus(aDefaultHttpStatus) ;
    	else if(aExecp instanceof AccessDeniedException || aExecp instanceof AuthenticationException
    			|| "AccessDeniedException".equals(aExecp.getClass().getSimpleName()))
    		return 403 ;
    	else
    		return aDefaultHttpStatus ;
    }
    
    static class ExcepRecord
    {
    	/**
    	 * 异常类
    	 */
    	Class<?> excepClass ;
    	
    	/**
    	 * 上次记录的异常时间
    	 */
    	long tsMs ;
    	
    	/**
    	 * 异常次数
    	 */
    	int times ;
    	
    	String digest ;
    	
    	public ExcepRecord()
    	{
    	}
    	
    	public ExcepRecord(Class<?> aClass , String aDigest)
    	{
    		excepClass = aClass ;
    		tsMs = System.currentTimeMillis() ;
    		times = 1 ;
    		digest = aDigest ;
    	}
    }
}
