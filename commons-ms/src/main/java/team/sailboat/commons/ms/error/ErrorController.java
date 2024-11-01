package team.sailboat.commons.ms.error;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ErrorController extends ACommonErrorHandler
{	
	public ErrorController()
	{
	}
	
	@RequestMapping("/error_view")
	public ModelAndView errorView(Model aModel
			, @RequestParam("msg")String aMsg
			, @RequestParam("url")String aUrl
			, @RequestParam("http-status")int aStatusCode
			, HttpServletRequest aRequest)
	{
		ModelAndView mv = errorView(aRequest, aMsg, aUrl, aStatusCode) ;
		if(mv == null)
		{
			aModel.addAttribute("status" , aStatusCode) ;
			return new ModelAndView("error", aModel.asMap()) ;
		}
		return mv ;
	}
	
}
