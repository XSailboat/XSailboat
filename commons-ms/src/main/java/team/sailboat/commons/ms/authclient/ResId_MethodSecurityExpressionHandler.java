package team.sailboat.commons.ms.authclient;

import java.lang.reflect.Parameter;
import java.util.function.Function;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.core.Authentication;

import team.sailboat.commons.fan.app.AppContext;

/**
 * 在原来的基础上，增加对ResId的支持。		<br>
 * 
 * 获取ResId注解修饰的参数，从中提取出_resId_，将它注入到EvaluationContext中
 *
 * @author yyl
 * @since 2024年10月17日
 */
public class ResId_MethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler
{
	@Override
	public StandardEvaluationContext createEvaluationContextInternal(Authentication aAuth, MethodInvocation aMi)
	{
		StandardEvaluationContext ctx = super.createEvaluationContextInternal(aAuth, aMi);
		Parameter[] parameters = aMi.getMethod().getParameters() ;
		if(parameters != null && parameters.length > 0)
		{
			for(int i=0 ; i<parameters.length ; i++)
			{
				ResId resId = parameters[i].getAnnotation(ResId.class) ;
				if(resId != null)
				{
					String funcKey = resId.value() ;
					if("".equals(funcKey))
						ctx.setVariable("_resId_", aMi.getArguments()[i]) ;
					else
					{
						Function<Object, String> func = (Function<Object, String>) AppContext.get(funcKey) ;
						if(func == null)
						{
							throw new IllegalStateException("无效的资源id提取器键：" + funcKey) ;
						}
						ctx.setVariable("_resId_", func.apply(aMi.getArguments()[i])) ;
					}
					break ;
				}
			}
		}
		return ctx ;
	}
}
