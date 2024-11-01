package team.sailboat.commons.ms.error;

import java.util.Map;

import org.springframework.boot.autoconfigure.template.TemplateAvailabilityProviders;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.ms.ACKeys_Common;

public class CommonErrorViewResolver extends ACommonErrorHandler  implements ErrorViewResolver
{
	
	final TemplateAvailabilityProviders tplPvds ;
	final ApplicationContext appCtx ;
	
	public CommonErrorViewResolver()
	{
		appCtx = (ApplicationContext) AppContext.get(ACKeys_Common.sSpringAppContext) ;
		tplPvds = new TemplateAvailabilityProviders(appCtx);
	}

	@Override
	public ModelAndView resolveErrorView(HttpServletRequest aRequest, HttpStatus aStatus, Map<String, Object> aModel)
	{
		String msg = (String)aModel.get("message") ;
		String path = (String) aModel.get("path") ;
		
		return errorView(aRequest, msg, path, aStatus.value()) ;
	}

}
