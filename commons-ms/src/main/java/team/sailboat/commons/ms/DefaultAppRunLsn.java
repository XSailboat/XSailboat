package team.sailboat.commons.ms;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.time.XTime;
import team.sailboat.commons.ms.aware.HeaderCoderHandler;

/**
 * 
 * 通过在spring.factories文件里面配置，将其注入到Spring框架中。从而实现在某些阶段进行一些初始化
 *
 * @author yyl
 * @since 2024年11月28日
 */
public class DefaultAppRunLsn implements ApplicationListener<ApplicationEvent>
{
	boolean mReady = false;

	final Logger mLogger = LoggerFactory.getLogger(getClass());

	@Override
	public void onApplicationEvent(ApplicationEvent event)
	{
		if (event instanceof ApplicationPreparedEvent)
		{
			ConfigurableApplicationContext ctx = ((ApplicationPreparedEvent) event).getApplicationContext();
			AppContext.set(ACKeys_Common.sSpringAppContext, ctx);
			if (ctx instanceof AnnotationConfigServletWebServerApplicationContext)
			{
				String[] pkgs = (String[]) AppContext.get(ACKeys_Common.sControllerPackages);
				if (XC.isNotEmpty(pkgs))
					((AnnotationConfigServletWebServerApplicationContext) ctx).scan(pkgs);
			}
		}
		else if (event instanceof ServletWebServerInitializedEvent)
		{
			ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) AppContext.get(
					ACKeys_Common.sSpringAppContext);
			RequestMappingHandlerAdapter adapter = (RequestMappingHandlerAdapter) ctx.getBean(
					"requestMappingHandlerAdapter");
			List<HandlerMethodArgumentResolver> argumentResolver = new ArrayList<>(adapter.getArgumentResolvers());
			final int len = argumentResolver.size();
			for (int i = 0; i < len; i++)
			{
				if (argumentResolver.get(i) instanceof RequestHeaderMethodArgumentResolver)
				{
					//把它替换成我们自己的
					argumentResolver.set(i, new HeaderCoderHandler(ctx.getBeanFactory()));
					adapter.setArgumentResolvers(argumentResolver);
					mLogger.info("已经把Header方法参数的解析器改成自定义的，以解决header参数中文乱码问题");
					break;
				}
			}
			
			ObjectMapper objectMapper = ctx.getBean(ObjectMapper.class);
			addMultiDateDeserializer(objectMapper);
		}
	}

	/**
	 * 
	 * 增加多种时间格式的自动适应解析
	 * 
	 * @param aObjMapper
	 */
	static void addMultiDateDeserializer(ObjectMapper aObjMapper)
	{
		SimpleModule javaTimeModule = new SimpleModule() ;
		javaTimeModule.addDeserializer(Date.class
				, new JsonDeserializer<Date>() {
		            @Override
		            public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException
		            {
		                try
						{
							return XTime.adaptiveParse(p.getText()) ;
						}
						catch (ParseException e)
						{
							WrapException.wrapThrow(e) ;
							return null ;			// dead code
						}
		            }
		        }) ;
		aObjMapper.registerModule(javaTimeModule);
	}
}
