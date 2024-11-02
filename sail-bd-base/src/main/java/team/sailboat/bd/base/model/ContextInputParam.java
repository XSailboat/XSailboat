package team.sailboat.bd.base.model;

import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;

/**
 * 上下文输入参数，引用了前置节点的上下文输出参数
 *
 * @author yyl
 * @since 2021年6月18日
 */
public class ContextInputParam implements ToJSONObject
{
	/**
	 * 参数名
	 */
	String mName ;
	
	/**
	 * 输出这个参数的阀id
	 */
	String mSourceValveId ;
	
	/**
	 * 原来的参数名
	 */
	String mOriginalName ;
	
	/**
	 * 参数来源，手动添加、自动添加
	 */
	ParamSource mParamSource ;
	
	public ContextInputParam()
	{
	}
	
	public ContextInputParam(String aName)
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

	public String getSourceValveId()
	{
		return mSourceValveId;
	}

	public void setSourceValveId(String aSourceValveId)
	{
		mSourceValveId = aSourceValveId;
	}

	public String getOriginalName()
	{
		return mOriginalName;
	}

	public void setOriginalName(String aOriginaName)
	{
		mOriginalName = aOriginaName;
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
				.put("sourceValveId" , mSourceValveId)
				.put("originalName", mOriginalName)
				.put("paramSource" , mParamSource.name()) ;
	}
	
	public static ContextInputParam parse(JSONObject aJo)
	{
		ContextInputParam inputParam = new ContextInputParam() ;
		inputParam.setName(aJo.optString("name")) ;
		inputParam.setSourceValveId(aJo.optString("sourceValveId")) ;
		inputParam.setOriginalName(aJo.optString("originalName")) ;
		inputParam.setParamSource(ParamSource.valueOf(aJo.optString("paramSource"))) ;
		return inputParam ;
	}
}
