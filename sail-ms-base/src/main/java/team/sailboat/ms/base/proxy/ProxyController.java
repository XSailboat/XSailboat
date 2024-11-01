package team.sailboat.ms.base.proxy;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class ProxyController
{
	
	@RequestMapping(value="/proxy/**" , method= {RequestMethod.GET,RequestMethod.POST})
	public void proxy(HttpServletRequest aRequest , HttpServletResponse aResp) throws Exception
	{
		new HttpRequestProxy(aRequest, aResp).doRequest(); 
	}
	
	@RequestMapping(value="/redirect/**" , method= {RequestMethod.GET,RequestMethod.POST})
	public void redirect(HttpServletRequest aRequest , HttpServletResponse aResp) throws Exception
	{
		new HttpRequestProxy(aRequest, aResp).doRequest(); 
	}
}
