package team.sailboat.base.dataset;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import team.sailboat.base.def.ParamType;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;

@Schema(description = "API的调用参数")
@Data
public class ApiArg implements Cloneable
{
	@Schema(description = "参数名")
	String name ;
	
	@Schema(description = "值")
	String value ;
	
	@Schema(description = "参数类型")
	ParamType type ;
	
	@Schema(description = "值中的表达式")
	@JsonIgnore
	@Setter(value = AccessLevel.NONE)
	@Getter(value = AccessLevel.NONE)
	Map<String , Expression> mExprMap = null ;
	
	@JsonIgnore
	public boolean hasParams()
	{
		return getExprs().size() > 0 ;
	}
	
	@JsonIgnore
	public Map<String , Expression> getExprs()
	{
		Map<String , Expression> exprMap = mExprMap ;
		if(exprMap == null)
		{
			Set<String> exprStrs = XString.extractParamNames(value) ;
			if(exprStrs.isEmpty())
				exprMap = Collections.emptyMap() ;
			else
			{
				exprMap = XC.hashMap() ;
				for(String exprStr : exprStrs)
				{
					exprMap.put(exprStr , AviatorEvaluator.compile(exprStr)) ;
				}
			}
			mExprMap = exprMap ;
		}
		return exprMap ;
	}
	
	public void setValue(String aValue)
	{
		if(JCommon.unequals(value, aValue))
		{
			value = aValue ;
			mExprMap = null ;
		}
	}
	
	@Override
	public ApiArg clone()
	{
		ApiArg clone = new ApiArg() ;
		clone.name = name ;
		clone.value = value ;
		clone.type = type ;
		return clone ;
	}
	
}
