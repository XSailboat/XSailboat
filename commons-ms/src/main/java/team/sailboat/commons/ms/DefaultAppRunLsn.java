package team.sailboat.commons.ms;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.method.annotation.RequestHeaderMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.ms.aware.HeaderCoderHandler;

public class DefaultAppRunLsn implements ApplicationListener<ApplicationEvent>
{	
	boolean mReady = false ;

	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	@Override
	public void onApplicationEvent(ApplicationEvent event)
	{
		if(event instanceof ApplicationPreparedEvent)
		{
			ConfigurableApplicationContext ctx = ((ApplicationPreparedEvent)event).getApplicationContext() ;
			AppContext.set(ACKeys_Common.sSpringAppContext, ctx) ;
			if(ctx instanceof AnnotationConfigServletWebServerApplicationContext)
			{
				String[] pkgs = (String[])AppContext.get(ACKeys_Common.sServicePackages) ;
				if(XC.isNotEmpty(pkgs))
					((AnnotationConfigServletWebServerApplicationContext)ctx).scan(pkgs);
			}
		}
		else if(event instanceof ServletWebServerInitializedEvent)
		{
			ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) AppContext.get(ACKeys_Common.sSpringAppContext) ;
			RequestMappingHandlerAdapter adapter = (RequestMappingHandlerAdapter) ctx.getBean("requestMappingHandlerAdapter") ;
			List<HandlerMethodArgumentResolver> argumentResolver = new ArrayList<>(adapter.getArgumentResolvers()) ;
			final int len = argumentResolver.size() ;
			for(int i=0 ; i<len ; i++)
			{
				if(argumentResolver.get(i) instanceof RequestHeaderMethodArgumentResolver)
				{
					//把它替换成我们自己的
					argumentResolver.set(i, new HeaderCoderHandler(ctx.getBeanFactory())) ;
					adapter.setArgumentResolvers(argumentResolver) ;
					mLogger.info("已经把Header方法参数的解析器改成自定义的，以解决header参数中文乱码问题");
					break ;
				}
			}
		}
	}
}
