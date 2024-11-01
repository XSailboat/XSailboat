package team.sailboat.aviator;

import java.io.Serializable;
import java.util.Map;

import com.googlecode.aviator.runtime.type.AviatorObject;

import team.sailboat.commons.fan.struct.Bool;

public interface IWedge extends Serializable
{
	
	/**
	 * 这类楔子的唯一Id，避免重复添加
	 * @return
	 */
	String getId() ;
	
	AviatorObject call(Map<String, Object> aEnv, AviatorObject aArg1, AviatorObject aArg2 , Bool aSupport) ;
}
