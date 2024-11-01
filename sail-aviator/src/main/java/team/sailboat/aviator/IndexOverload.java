package team.sailboat.aviator;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.googlecode.aviator.lexer.token.OperatorType;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaElementType;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaElementType.ContainerType;

import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.struct.Bool;

import com.googlecode.aviator.runtime.type.AviatorType;

public class IndexOverload extends WedgeFunction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String getName()
	{
		return OperatorType.INDEX.getToken();
	}

	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aArg1, AviatorObject aArg2)
	{
		if (aArg1.getAviatorType() == AviatorType.JavaType)
		{
			Object obj = aArg1.getValue(aEnv);
			if (obj != null)
			{
				if (obj instanceof JSONArray)
				{
					Integer index = XClassUtil.toInteger(aArg2.getValue(aEnv));
					Assert.notNull(index, "序号不能为null。%s", aArg2.toString());
					// 负向索引
					if (index < 0)
					{
						index += ((JSONArray) obj).size() ;
					}
					return new AviatorJaRuntimeJavaType((JSONArray) obj, index);
				}
				// 对List和Array的[]重载，是为了支持负向索引。例如a[-1] 等价a[size-1]
				else if (obj instanceof List)
				{
					Integer index = XClassUtil.toInteger(aArg2.getValue(aEnv));
					Assert.notNull(index, "序号不能为null。%s", aArg2.toString());
					// 负向索引
					if (index < 0)
					{
						index += ((List) obj).size();
					}
					final Integer index0 = index;
					// 用这种形式返回，就能支持a[0] = 'hello' 这种设置方法
					return new AviatorRuntimeJavaElementType(ContainerType.List,
							obj,
							index0,
							new Callable<Object>()
							{
								@Override
								public Object call() throws Exception
								{
									return ((List<?>) obj).get(index0);
								}
							});
				}
				else if (obj.getClass().isArray())
				{
					Integer index = XClassUtil.toInteger(aArg2.getValue(aEnv));
					Assert.notNull(index, "序号不能为null。%s", aArg2.toString());
					// 负向索引
					if (index < 0)
					{
						index += Array.getLength(obj);
					}
					final Integer index0 = index;
					// 用这种形式返回，就能支持a[0] = 'hello' 这种设置方法
					return new AviatorRuntimeJavaElementType(ContainerType.Array,
							obj,
							index0,
							new Callable<Object>()
							{
								@Override
								public Object call() throws Exception
								{
									return Array.get(obj, index0);
								}
							});
				}
			}
		}
		if (wedges != null)
		{
			Bool support = new Bool(false);
			for (IWedge wedge : wedges)
			{
				AviatorObject result = wedge.call(aEnv, aArg1, aArg2, support);
				if (support.get())
					return result;
			}
		}
		return aArg1.getElement(aEnv, aArg2);
	}
}
