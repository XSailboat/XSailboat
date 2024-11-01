package team.sailboat.commons.fan.es.index;

import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;

public class TemplateDefine implements ToJSONObject
{
	JSONObject mTplDefine = new JSONObject() ;
	
	TemplateDefine()
	{
	}
	
	public IndexDefine indexDefine()
	{
		IndexDefine index = IndexDefine.one() ;
		mTplDefine.put("template" , index.toJSONObject()) ;
		return index ;
	}
	
	public TemplateDefine priority(int aPriority)
	{
		mTplDefine.put("priority" , aPriority) ;
		return this ;
	}
	
	/**
	 * 
	 * @param aPatterns		用*来匹配任意多个字符
	 * @return
	 */
	public TemplateDefine indexPattern(String...aPatterns)
	{
		mTplDefine.put("index_patterns" , aPatterns) ;
		return this ;
	}
	

	@Override
	public JSONObject toJSONObject()
	{
		return mTplDefine ;
	}
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		throw new UnsupportedOperationException() ;
	}
	
	
	public static TemplateDefine one()
	{
		return new TemplateDefine() ;
	}
}
