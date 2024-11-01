package team.sailboat.commons.fan.es.query;

import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;

/**
 * 排序
 *
 * @author yyl
 * @since 2024年4月25日
 */
public class Sorter
{
	
	JSONArray mDefine = new JSONArray() ;
	
	Sorter()
	{
	}
	
	public boolean isEmpty()
	{
		return mDefine.isEmpty() ;
	}
	
	public Sorter asc(String aField)
	{
		mDefine.put(new JSONObject().put(aField , "asc")) ;
		return this ;
	}
	
	public Sorter desc(String aField)
	{
		mDefine.put(new JSONObject().put(aField , "desc")) ;
		return this ;
	}
	
	public Sorter score()
	{
		mDefine.put("_score") ;
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
	
	public static Sorter one()
	{
		return new Sorter() ;
	}
}
