package team.sailboat.aviator ;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.lexer.token.OperatorType;

public class AviatorExtend
{
	static boolean sInited = false ;
	
	public static void init()
	{
		if(!sInited)
		{
			synchronized (AviatorExtend.class)
			{
				if(!sInited)
				{
					// 防止表达式缓存过多
					AviatorEvaluator.getInstance().useLRUExpressionCache(512) ;
					AviatorEvaluator.addFunctionLoader(new SailFunctionLoader().packages("com.cimstech.sailboat.aviator"));
					// 编译表达式时，保存表达式
//					AviatorEvaluator.getInstance().setOption(Options.TRACE_EVAL , true) ;
					overload_json_index();
					sInited = true ;
				}
			}			
		}
	}
	
	static void overload_json_index()
	{
		AviatorEvaluator.getInstance().addOpFunction(OperatorType.INDEX , new IndexOverload()) ;
		AviatorEvaluator.getInstance().addOpFunction(OperatorType.ADD , new AddOverload()) ; 
		AviatorEvaluator.getInstance().addOpFunction(OperatorType.SUB , new SubtractOverload()) ; 
	}
}
