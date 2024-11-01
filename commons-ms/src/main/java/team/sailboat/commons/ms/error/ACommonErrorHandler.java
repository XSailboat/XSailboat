package team.sailboat.commons.ms.error;

import org.springframework.boot.autoconfigure.template.TemplateAvailabilityProvider;
import org.springframework.boot.autoconfigure.template.TemplateAvailabilityProviders;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.ms.ACKeys_Common;

public abstract class ACommonErrorHandler
{
	
	final TemplateAvailabilityProviders tplPvds ;
	final ApplicationContext appCtx ;
	
	public ACommonErrorHandler()
	{
		appCtx = (ApplicationContext) AppContext.get(ACKeys_Common.sSpringAppContext) ;
		tplPvds = new TemplateAvailabilityProviders(appCtx);
	}

	public ModelAndView errorView(HttpServletRequest aRequest,
			String aMsg , String aUrl , int aStatusCode)
	{
		if(XString.isEmpty(aMsg))
			aMsg = (String)aRequest.getAttribute("javax.servlet.error.message") ;
		
		String errorViewName = null ;
		switch(aStatusCode)
		{
		case 403:
		case 404:
		case 500:
			errorViewName = "errorPage/"+aStatusCode ;
			break ;
		default:
			errorViewName = "errorPage/t_error" ;
			break ;
		}
		
		TemplateAvailabilityProvider tplPvd = tplPvds.getProvider(errorViewName, appCtx) ;
		if(tplPvd != null)
		{
			ModelAndView modelAndView = new ModelAndView();

			modelAndView.addObject("msg", aMsg);
			modelAndView.addObject("url", aUrl);
			modelAndView.addObject("http-status", aStatusCode) ;
	//		modelAndView.addObject("stackTrace", e.getStackTrace());
			modelAndView.setViewName(errorViewName) ;
			modelAndView.setStatus(HttpStatus.valueOf(aStatusCode)) ;
			return modelAndView ;
		}
		
		return null ;
	}

}
