package team.sailboat.base.msg;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.text.XString;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextPattern
{
	
	TextPatternType type ;
	
	String expr ;
	
	boolean negative ;
	
	@JsonIgnore
	public boolean isEmpty()
	{
		return type == null || (type != TextPatternType.NotEmpty && XString.isEmpty(expr)) ;
	}
	
	public static Predicate<String> toPredicate(TextPattern aPtn)
	{
		if(aPtn == null || aPtn.type == null || XString.isEmpty(aPtn.expr))
			return null ;
		switch(aPtn.type)
		{
		case Contains:
			return (text)->text==null?false:(aPtn.negative?!text.contains(aPtn.expr):text.contains(aPtn.expr)) ;
		case RawRegex:
			return new PatternPred(Pattern.compile(aPtn.expr) , aPtn.negative) ;
		case SqlLike:
			String regex = Pattern.quote(aPtn.expr).replace("%" , ".*") ;
			return new PatternPred(Pattern.compile(regex) , aPtn.negative) ;
		case NotEmpty:
			return text->{
				boolean notEmpty = text != null && text.length() > 0 ;
				return aPtn.negative?!notEmpty:notEmpty ;
			} ;
		case ContainsAny:
			String[] eles = new JSONArray(aPtn.expr).toStringArray() ;
			return text->{
				for(String ele : eles)
				{
					if(text.contains(ele))
					{
						return !aPtn.negative ;
					}
				}
				return aPtn.negative ;
			} ;
		default:
			throw new IllegalArgumentException() ;
		}
	}
	
	static class PatternPred implements Predicate<String>
	{
		Pattern mPattern ;
		boolean mNegative ;
		
		public PatternPred(Pattern aPtn , boolean aNegative)
		{
			mPattern = aPtn ;
			mNegative = aNegative ;
		}
		
		@Override
		public boolean test(String aT)
		{
			if(aT == null)
				return mNegative ;
			return mNegative?!mPattern.matcher(aT).matches():mPattern.matcher(aT).matches() ;
		}
		
	}
	
	public static String toAviator(TextPattern aPtn , String aField)
	{
		if(aPtn == null || aPtn.type == null || XString.isEmpty(aPtn.expr))
			return null ;
		StringBuilder scriptBld = new StringBuilder() ;
		switch(aPtn.type)
		{
		case Contains:
		{
			scriptBld.append(aField).append(" == nil?false:") ;
			if(aPtn.negative)
				scriptBld.append("!") ;
			scriptBld.append("string.contains(").append(aField).append(" , '").append(aPtn.getExpr().replace("'", "\'")).append("')") ;
			return scriptBld.toString()  ;
		}
		case RawRegex:
		{
			if(aPtn.negative)
				scriptBld.append("!(") ;
			scriptBld.append(aField).append(" =~ /").append(aPtn.expr).append("/") ;
			if(aPtn.negative)
				scriptBld.append(")") ;
			return scriptBld.toString() ;
		}
		case SqlLike:
		{
			String regex = Pattern.quote(aPtn.expr).replace("%" , ".*") ;
			if(aPtn.negative)
				scriptBld.append("!(") ;
			scriptBld.append(aField).append(" =~ /").append(regex).append("/") ;
			if(aPtn.negative)
				scriptBld.append(")") ;
			return scriptBld.toString() ;
		}
		case NotEmpty:
		{
			if(aPtn.negative)
				scriptBld.append("xstring.isBlank(").append(aField).append(")") ;
			else
				scriptBld.append("xstring.isNotBlank(").append(aField).append(")") ;
			return scriptBld.toString() ;
		}
		case ContainsAny:
			String[] eles = new JSONArray(aPtn.expr).toStringArray() ;
			if(eles.length == 0)
				return null ;
			scriptBld.append(aField).append(" == nil?false:(") ;
			if(aPtn.negative)
			{
				boolean first = true ;
				for(String ele : eles)
				{
					if(first)
						first = false ;
					else
						scriptBld.append(" && ") ;
					scriptBld.append("!string.contains(").append(aField).append(" , '").append(ele.replace("'", "\'")).append("')") ;
				}
			}
			else
			{
				boolean first = true ;
				for(String ele : eles)
				{
					if(first)
						first = false ;
					else
						scriptBld.append(" || ") ;
					scriptBld.append("string.contains(").append(aField).append(" , '").append(ele.replace("'", "\'")).append("')") ;
				}
			}
			scriptBld.append(")") ;
			return scriptBld.toString()  ;
		default:
			throw new IllegalArgumentException() ;
		}
	}
}
