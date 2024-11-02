package team.sailboat.bd.base.model;

import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;

public class ContextOutputParam implements ToJSONObject
{
	/**
	 * 参数名
	 */
	String mName ;
	
	/**
	 * 输出这个参数的节点ids
	 */
	String mSourceNodeId ;
	
	/**
	 * 参数类型，常量、变量等
	 */
	ParamType mParamType ;
	
	/**
	 * 取值
	 */
	String mValue ;
	
	/**
	 * 描述
	 */
	String mDescription ;
	
	/**
	 * 参数来源，手动添加、自动添加
	 */
	ParamSource mParamSource ;
	
	public ContextOutputParam()
	{
	}
	
	public ContextOutputParam(String aName)
	{
		mName = aName ;
	}

	public String getName()
	{
		return mName;
	}

	public void setName(String aName)
	{
		mName = aName;
	}

	public String getSourceNodeId()
	{
		return mSourceNodeId;
	}

	public void setSourceNodeId(String aSourceNodeId)
	{
		mSourceNodeId = aSourceNodeId;
	}

	public ParamType getParamType()
	{
		return mParamType;
	}

	public void setParamType(ParamType aParamType)
	{
		mParamType = aParamType;
	}

	public String getValue()
	{
		return mValue;
	}

	public void setValue(String aValue)
	{
		mValue = aValue;
	}

	public String getDescription()
	{
		return mDescription;
	}

	public void setDescription(String aDescription)
	{
		mDescription = aDescription;
	}

	public ParamSource getParamSource()
	{
		return mParamSource;
	}

	public void setParamSource(ParamSource aParamSource)
	{
		mParamSource = aParamSource;
	}
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return aJSONObj.put("name" , mName)
				.put("sourceNodeId" , mSourceNodeId)
				.put("paramType" , mParamType.name())
				.put("value" , mValue)
				.put("description" , mDescription)
				.put("paramSource" , mParamSource.name()) ;
	}
	
	public static ContextOutputParam parse(JSONObject aJObj)
	{
		ContextOutputParam outParam = new ContextOutputParam() ;
		outParam.setName(aJObj.optString("name")) ;
		outParam.setSourceNodeId(aJObj.optString("sourceNodeId")) ;
		outParam.setParamType(ParamType.valueOf(aJObj.optString("paramType"))) ;
		outParam.setValue(aJObj.optString("value")) ;
		outParam.setDescription(aJObj.optString("description")) ;
		outParam.setParamSource(ParamSource.valueOf(aJObj.optString("paramSource"))) ;
		return outParam ;
	}
	
}
