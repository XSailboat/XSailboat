package team.sailboat.commons.ms.access;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.ms.ACKeys_Common;
import team.sailboat.commons.ms.MSApp;

public class RedirectInterceptor implements HandlerInterceptor
{
	Map<String, String> mUrlMap = new HashMap<>() ;
	
	public RedirectInterceptor()
	{
		ConfigurableApplicationContext ctx = (ConfigurableApplicationContext)AppContext.get(ACKeys_Common.sSpringAppContext) ;
		Object obj = XC.getFirst(ctx.getBeansWithAnnotation(SpringBootApplication.class).values()) ;
		if(obj != null)
		{
			String name = obj.getClass().getName() ;
			int i = name.indexOf("$$") ;
			if(i != -1)
				name = name.substring(0, i) ;
			try(InputStream ins = XClassUtil.getProjectResourceAsStream("/redirect.properties", getClass().getClassLoader().loadClass(name)))
			{
				loadUrlMap(ins);
			}
			catch(Throwable e)
			{
				e.printStackTrace();
			}
		}
		Class<?>[] classes = (Class<?>[])AppContext.get(ACKeys_Common.sMSActivatorClasses) ;
		if(XC.isNotEmpty(classes))
		{
			for(Class<?> clazz : classes)
			{
				try(InputStream ins = XClassUtil.getProjectResourceAsStream("/redirect.properties", clazz))
				{
					loadUrlMap(ins);
				}
				catch(Throwable e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	void loadUrlMap(InputStream aIns) throws UnsupportedEncodingException, IOException
	{
		if(aIns == null)
			return ;
		PropertiesEx prop = PropertiesEx.loadFromReader(new InputStreamReader(aIns , "UTF-8")) ;
		for(String key : prop.stringPropertyNames())
		{
			String val = prop.getString(key) ;
			mUrlMap.put(key.startsWith("/")?key:"/"+key , MSApp.realPath(val)) ;
		}
	}
	
	@Override
	public boolean preHandle(HttpServletRequest aRequest, HttpServletResponse aResponse, Object aHandler)
			throws Exception
	{
		String newPath = mUrlMap.get(aRequest.getServletPath()) ;
		if(newPath != null)
		{
			aResponse.sendRedirect(newPath);
			return false ;
		}
		return true ;
	}
}
