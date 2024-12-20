package team.sailboat.aviator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import team.sailboat.commons.fan.excep.WrapException;

/**
 * 
 * 对象字段通过[]索引
 *
 * @author yyl
 * @since 2024年11月29日
 */
public class ObjectFieldIndexRuntimeJavaType extends AviatorRuntimeJavaType
{

private static final long serialVersionUID = 1L;
	
	private final String mFieldName ;
	private final Method mGetterMethod ;
	private final Method mSetterMethod ;

	
	/**
	 * 
	 * @param aContainer
	 * @param aFieldName
	 * @param aGetterMethod
	 * @param aSetterMethod
	 */
	public ObjectFieldIndexRuntimeJavaType(final Object aContainer , String aFieldName
			, final Method aGetterMethod
			, final Method aSetterMethod)
	{
		super(null);
		object = aContainer;
		mFieldName = aFieldName ;
		mGetterMethod = aGetterMethod ;
		mSetterMethod = aSetterMethod ;
		callable = this::get;
	}

	Object get() throws IllegalAccessException, InvocationTargetException
	{
		return mGetterMethod.invoke(object) ;
	}
	
	/**
	 * 支持 obj['field'] = "a" ; 这种赋值方式
	 */
	@Override
	public AviatorObject setValue(final AviatorObject aValue, final Map<String, Object> aEnv)
	{
		if(mSetterMethod != null)
		{
			try
			{
				mSetterMethod.invoke(object , aValue.getValue(aEnv)) ;
				return AviatorRuntimeJavaType.valueOf(object) ;
			}
			catch (IllegalAccessException | InvocationTargetException e)
			{
				WrapException.wrapThrow(e) ;
			}
		}
		throw new IllegalStateException("类型 "+object.getClass()+" 没有字段 " + mFieldName + " 的set方法！") ;
	}
}
