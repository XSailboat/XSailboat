package team.sailboat.commons.fan.es.query;

import team.sailboat.commons.fan.json.JSONArray;

/**
 * 指定列
 *
 * @author yyl
 * @since 2024年4月25日
 */
public class Fields
{
	
	JSONArray mDefine = new JSONArray() ;
	
	Fields()
	{
	}
	
	public boolean isEmpty()
	{
		return mDefine.isEmpty() ;
	}
	
	public Fields oneField(String aField)
	{
		mDefine.put(aField) ;
		return this ;
	}
	
	@Override
	public String toString()
	{
		return mDefine.toString() ;
	}
	
	public JSONArray toJSONArray()
	{
		return mDefine ;
	}
	
	public static Fields one()
	{
		return new Fields() ;
	}
}
