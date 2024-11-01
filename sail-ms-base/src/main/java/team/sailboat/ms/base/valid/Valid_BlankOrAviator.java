package team.sailboat.ms.base.valid;

import com.googlecode.aviator.AviatorEvaluator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import team.sailboat.commons.fan.text.XString;

public class Valid_BlankOrAviator implements ConstraintValidator<BlankOrAviator , String>
{
	String who ;

	@Override
	public void initialize(BlankOrAviator aConstraintAnnotation)
	{
		who = aConstraintAnnotation.message() ;
		if(XString.isNotEmpty(who))
			who += "的" ;
	}

	@Override
	public boolean isValid(String aValue, ConstraintValidatorContext aContext)
	{
		if(XString.isNotBlank(aValue))
		{
			try
			{
				AviatorEvaluator.compile(aValue) ;
			}
			catch(Exception e)
			{
				aContext.disableDefaultConstraintViolation();
				aContext.buildConstraintViolationWithTemplate("Aviator表达式不合法。提示："+e.getMessage())
					.addConstraintViolation() ;
				return false ;
			}
		}
		return true ;
	}

}
