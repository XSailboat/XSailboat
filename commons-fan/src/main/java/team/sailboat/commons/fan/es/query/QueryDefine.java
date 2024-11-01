package team.sailboat.commons.fan.es.query;

import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;

public class QueryDefine extends BaseLogicDefine implements ToJSONObject
{
	
	QueryDefine()
	{
		super(null , new JSONObject()) ;
	}
	
	public boolean isEmpty()
	{
		return mDefine.isEmpty() ;
	}
	
	@Override
	public QueryDefine term(String aField, String aValue)
	{
		return (QueryDefine)super.term(aField, aValue);
	}
	
	@Override
	public QueryDefine existsField(String aFieldName)
	{
		return (QueryDefine)super.existsField(aFieldName);
	}
	
	public QueryDefine match_all()
	{
		mDefine.put("match_all", new JSONObject()) ;
		return this ;
	}
	
	@Override
	public String toString()
	{
		return mDefine.toString() ;
	}
	
	@Override
	public JSONObject toJSONObject()
	{
		return mDefine ;
	}

	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		throw new UnsupportedOperationException() ;
	}
	
	public static QueryDefine one()
	{
		return new QueryDefine() ;
	}
}
