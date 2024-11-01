package team.sailboat.ms.base.valid;

import com.googlecode.aviator.AviatorEvaluator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import team.sailboat.commons.fan.text.XString;

public class Valid_NotBlankAndAviator implements ConstraintValidator<NotBlankAndAviator , String>
{
	String who ;
	
	@Override
	public void initialize(NotBlankAndAviator aConstraintAnnotation)
	{
		who = aConstraintAnnotation.value() ;
		if(XString.isNotEmpty(who))
			who += "的" ;
	}

	@Override
	public boolean isValid(String aValue, ConstraintValidatorContext aContext)
	{
		if(XString.isBlank(aValue))
		{
			aContext.disableDefaultConstraintViolation(); 
			aContext.buildConstraintViolationWithTemplate(who + "Aviator表达式不能为空！")
				.addConstraintViolation() ;
			return false ;
		}
		
		try
		{
			AviatorEvaluator.compile(aValue) ;
		}
		catch(Exception e)
		{
			aContext.disableDefaultConstraintViolation(); 
			aContext.buildConstraintViolationWithTemplate(who + "Aviator表达式不合法！提示："+e.getMessage())
				.addConstraintViolation() ;
			return false ;
		}
		return true ;
	}

}
