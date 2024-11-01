package team.sailboat.aviator.collection;

import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

import team.sailboat.commons.fan.collection.XC;

public class Func_tree_map extends AbstractFunction
{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv)
	{
		return AviatorRuntimeJavaType.valueOf(XC.treeMap()) ;
	}

	@Override
	public String getName()
	{
		return "cs.tree_map" ;
	}

}