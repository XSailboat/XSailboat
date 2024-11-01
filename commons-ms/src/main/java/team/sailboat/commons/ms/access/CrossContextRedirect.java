package team.sailboat.commons.ms.access;

import java.io.IOException;
import java.util.Map;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.mapper.MappingData;
import org.apache.catalina.startup.Tomcat;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;

import jakarta.servlet.ServletException;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.text.XString;

public class CrossContextRedirect
{
	ServletWebServerApplicationContext mWebCtx ;
	
	Engine mEngine ;
	
	final Map<String , String> mRedirctPathMap = XC.hashMap() ;
	
	public CrossContextRedirect(ServletWebServerApplicationContext aWebCtx)
	{
		mWebCtx = aWebCtx ;
		WebServer webServ = mWebCtx.getWebServer() ;
		if(webServ instanceof TomcatWebServer)
		{
			Tomcat tomcat = ((TomcatWebServer) webServ).getTomcat() ;
			mEngine = tomcat.getService().getContainer() ;
			mEngine.getPipeline().addValve(new RedirectValve()) ;
		}
	}
	
	public void addRediectItem(String aRequestPath , String aTargetPath)
	{
		mRedirctPathMap.put(aRequestPath, aTargetPath) ;
	}
	
	
	class RedirectValve implements Valve
	{
		Valve mNext ;
		
		@Override
		public void setNext(Valve aValve)
		{
			mNext = aValve ;
		}
		
		@Override
		public boolean isAsyncSupported()
		{
			return false;
		}
		
		@Override
		public void invoke(Request aRequest, Response aResponse) throws IOException, ServletException
		{
			String reqPath = aRequest.getRequestURI() ;
			String targetPath = mRedirctPathMap.get(reqPath) ;
			if(targetPath != null)
			{
				// 需要重定向
				MappingData md = aRequest.getMappingData() ;
				if(md.context == null)
				{
					if(mEngine instanceof StandardEngine)
					{
						String path = targetPath ;
						int i=0 ;
						bp_1747: while(path != null)
						{
							Container[] ctns = ((StandardEngine)mEngine).findChildren() ;
							for(Container ctn : ctns)
							{
								StandardHost host = (StandardHost)ctn ;
								Context ctx = (Context)host.findChild(path) ;
								if(ctx != null)
								{
									md.context = ctx ;
									break bp_1747 ;
								}
							}
							path = XString.substringLeft(path, '/', false , i++) ;
						}
					}
				}
				if(md.context != null)
				{
					aResponse.sendRedirect(targetPath) ;
					return ;
				}
			}
			mNext.invoke(aRequest, aResponse) ;
		}
		
		@Override
		public Valve getNext()
		{
			return mNext ;
		}
		
		@Override
		public void backgroundProcess()
		{
		}
	}
}
