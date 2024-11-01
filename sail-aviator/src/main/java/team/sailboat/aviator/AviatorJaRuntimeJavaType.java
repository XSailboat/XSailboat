package team.sailboat.aviator;

import java.util.Map;

import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import team.sailboat.commons.fan.json.JSONArray;

public class AviatorJaRuntimeJavaType extends AviatorRuntimeJavaType
{

	private static final long serialVersionUID = 1L;
	
	private final int mIndex;

	
	public AviatorJaRuntimeJavaType(final JSONArray aContainer, final int aIndex)
	{
		super(null);
		object = aContainer;
		mIndex = aIndex;
		callable = this::get;
	}

	Object get()
	{
		return ((JSONArray) object).get(mIndex);
	}

	/**
	 * a[0] = -1 这种赋值语法会调用此方法
	 */
	@Override
	public AviatorObject setValue(final AviatorObject aValue, final Map<String, Object> aEnv)
	{
		return AviatorRuntimeJavaType.valueOf(((JSONArray) object).put(mIndex, aValue.getValue(aEnv)));
	}
}
