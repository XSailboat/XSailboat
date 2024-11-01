package team.sailboat.commons.ms.access;

import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.sys.IPList;
import team.sailboat.commons.fan.text.XString;

@Order(0)
@WebFilter(filterName="IPRestrictFilter" , urlPatterns="/*")
public class IPRestrictFilter implements Filter
{
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	IPList mIPList  ;
	
	final Set<String> mUnProtectedPaths = XC.hashSet("/error_view" , "/favicon.ico") ;
	
	public IPRestrictFilter()
	{
	}

	@Override
	public void doFilter(ServletRequest aRequest, ServletResponse aResponse, FilterChain aChain)
			throws IOException, ServletException
	{
		IPList ipList = mIPList ;
		if(ipList != null && !ipList.isEmpty())
		{
			String addr = aRequest.getRemoteAddr() ;
			if(!ipList.test(addr))
			{
				String path = ((HttpServletRequest)aRequest).getServletPath() ;
				if(!mUnProtectedPaths.contains(path) && !path.startsWith("/public/"))
				{
					((HttpServletResponse)aResponse).sendError(HttpStatus.FORBIDDEN.value() 
							, XString.msgFmt("当前的访问地址[{}]不在白名单范围内！" , addr)) ;
					mLogger.warn("已拒绝来自[{}]不在IP白名单范围内的访问！访问目标：{}" , addr , path) ;
					return ;
				}
			}
		}
		aChain.doFilter(aRequest, aResponse) ;
	}
	
	public void setIPList(IPList aIPList)
	{
		mIPList = aIPList;
	}
}