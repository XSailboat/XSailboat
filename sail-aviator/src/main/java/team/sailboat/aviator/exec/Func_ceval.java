package team.sailboat.aviator.exec;

import java.util.Map;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.log.Log;
import team.sailboat.commons.fan.text.XString;

public class Func_ceval extends AbstractFunction
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aArg1 , AviatorObject aDefaultValue)
	{
		String exprStr = XClassUtil.toString(aArg1.getValue(aEnv)) ;
		if(XString.isBlank(exprStr))
			return aDefaultValue ;
		Expression expr = AviatorEvaluator.compile(exprStr , true) ;
		Object result = null ;
		try
		{
			result = expr.execute(aEnv) ;
		}
		catch(Exception e)
		{
			Log.info(ExceptionAssist.getClearMessage(getClass(), e)) ;
			result = aDefaultValue.getValue(aEnv)  ;
		}
		return AviatorRuntimeJavaType.valueOf(result) ;
	}
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aArg1 , AviatorObject aDefaultValue
			, AviatorObject aCacheKey)
	{
		String exprStr = XClassUtil.toString(aArg1.getValue(aEnv)) ;
		if(XString.isBlank(exprStr))
			return aDefaultValue ;
		Object cacheKey = aCacheKey.getValue(aEnv) ;
		Expression expr = cacheKey == null?AviatorEvaluator.compile(exprStr , true)
				: AviatorEvaluator.getInstance().compile(cacheKey.toString() , exprStr, true) ;
		Object result = null ;
		try
		{
			result = expr.execute(aEnv) ;
		}
		catch(Exception e)
		{
			Log.info(ExceptionAssist.getClearMessage(getClass(), e)) ;
			result = aDefaultValue.getValue(aEnv)  ;
		}
		return AviatorRuntimeJavaType.valueOf(result) ;
	}

	@Override
	public String getName()
	{
		return "ceval" ;
	}

}
