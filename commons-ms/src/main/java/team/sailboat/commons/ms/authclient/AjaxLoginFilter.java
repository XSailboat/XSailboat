package team.sailboat.commons.ms.authclient;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

import org.springframework.http.HttpStatus;
import org.springframework.security.web.util.ThrowableAnalyzer;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import team.sailboat.commons.fan.http.HttpConst;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.ms.util.WebUtils;

public class AjaxLoginFilter implements Filter
{
	
	private ThrowableAnalyzer throwableAnalyzer = new DefaultThrowableAnalyzer();
	
	String mBlankLoginPath ;
	
	public AjaxLoginFilter(String aBlankLoginPath)
	{
		mBlankLoginPath = aBlankLoginPath ;
	}

	@Override
	public void doFilter(ServletRequest aRequest, ServletResponse aResponse, FilterChain aChain)
			throws IOException, ServletException
	{
		try
		{
			aChain.doFilter(aRequest, aResponse) ;
		}
		catch (IOException e)
		{
			throw e ;
		}
		catch(Exception e)
		{
			Throwable[] causeChain = this.throwableAnalyzer.determineCauseChain(e);
			AccessDeniedException securityException = (AccessDeniedException) this.throwableAnalyzer
						.getFirstThrowableOfType(AccessDeniedException.class, causeChain);
			if (securityException != null)
			{
				// 如果是ajax
				HttpServletRequest httpReq = (HttpServletRequest)aRequest ;
				String contentTypeHeader = httpReq.getHeader(HttpConst.sHeaderName_ContentType);
		    	String acceptHeader = httpReq.getHeader(HttpConst.sHeaderName_Accept);
		    	String xRequestedWith = httpReq.getHeader(HttpConst.sHeaderName_X_Requested_With);
		    	String referer = httpReq.getHeader(HttpConst.sHeaderName_referer) ;
		    	if ((contentTypeHeader != null && contentTypeHeader.contains("application/json"))
			              || (acceptHeader != null && acceptHeader.contains("application/json"))
			              || "XMLHttpRequest".equalsIgnoreCase(xRequestedWith)
			              || HttpConst.sHeaderValue_UserAgent_x_HttpClient.equalsIgnoreCase(httpReq.getHeader(HttpConst.sHeaderName_UserAgent))
			              || (referer != null && referer.endsWith("swagger-ui.html"))) 
			    {
		    		WebUtils.sendErrorInJSON(httpReq, ((HttpServletResponse)aResponse) , HttpStatus.UNAUTHORIZED 
		    				, new JSONObject().put("path",  httpReq.getContextPath()+mBlankLoginPath).put("target" , "_blank")) ;
			  		return ;
			    }
			}
	    	throw e ;
		}
	}
	
	private static final class DefaultThrowableAnalyzer extends ThrowableAnalyzer {

		/**
		 * @see org.springframework.security.web.util.ThrowableAnalyzer#initExtractorMap()
		 */
		@Override
		protected void initExtractorMap() {
			super.initExtractorMap();
			registerExtractor(ServletException.class, (throwable) -> {
				ThrowableAnalyzer.verifyThrowableHierarchy(throwable, ServletException.class);
				return ((ServletException) throwable).getRootCause();
			});
		}

	}

}
