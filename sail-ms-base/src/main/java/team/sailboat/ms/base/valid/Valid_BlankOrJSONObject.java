package team.sailboat.ms.base.valid;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.text.XString;

public class Valid_BlankOrJSONObject implements ConstraintValidator<BlankOrJSONObject , String>
{
	String who ;
	
	@Override
	public void initialize(BlankOrJSONObject aConstraintAnnotation)
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
			return true ;
		}
		
		try
		{
			JSONObject.of(aValue) ;
		}
		catch(Exception e)
		{
			aContext.disableDefaultConstraintViolation(); 
			aContext.buildConstraintViolationWithTemplate(who + "不是一个合法的JSONObject字符串！提示："+e.getMessage())
				.addConstraintViolation() ;
			return false ;
		}
		return true ;
	}

}
