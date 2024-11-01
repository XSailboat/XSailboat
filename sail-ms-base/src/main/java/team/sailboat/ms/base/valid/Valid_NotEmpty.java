package team.sailboat.ms.base.valid;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.text.XString;

public class Valid_NotEmpty implements ConstraintValidator<NotEmpty , Object>
{
	String who ;
	
	@Override
	public void initialize(NotEmpty aConstraintAnnotation)
	{
		who = aConstraintAnnotation.value() ;
		if(XString.isNotEmpty(who))
			who += "的" ;
	}

	@Override
	public boolean isValid(Object aValue, ConstraintValidatorContext aContext)
	{
		if(aValue == null)
		{
			aContext.disableDefaultConstraintViolation(); 
			aContext.buildConstraintViolationWithTemplate(who + "不能为空！")
				.addConstraintViolation() ;
			return false ;
		}
		if(aValue instanceof Collection)
		{
			if(XC.isEmpty((Collection)aValue))
			{
				aContext.disableDefaultConstraintViolation(); 
				aContext.buildConstraintViolationWithTemplate(who + "不能为空！")
					.addConstraintViolation() ;
				return false ;
			}
			return true ;
		}
		if(aValue instanceof Map)
		{
			if(XC.isEmpty((Map)aValue))
			{
				aContext.disableDefaultConstraintViolation(); 
				aContext.buildConstraintViolationWithTemplate(who + "不能为空！")
					.addConstraintViolation() ;
				return false ;
			}
			return true ;
		}
		if(aValue.getClass().isArray())
		{
			if(Array.getLength(aValue) == 0)
			{
				aContext.disableDefaultConstraintViolation(); 
				aContext.buildConstraintViolationWithTemplate(who + "不能为空！")
					.addConstraintViolation() ;
				return false ;
			}
			return true ;
		}
		if(aValue instanceof String)
		{
			if(XString.isEmpty((String)aValue))
			{
				aContext.disableDefaultConstraintViolation(); 
				aContext.buildConstraintViolationWithTemplate(who + "不能为空！")
					.addConstraintViolation() ;
				return false ;
			}
			return true ;
		}
		return true ;
	}

}
