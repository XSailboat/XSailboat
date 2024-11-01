package team.sailboat.commons.fan.es.index;

import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;

public class IndexDefine implements ToJSONObject
{
	JSONObject mIndexDefine = new JSONObject() ;
	
	IndexDefine()
	{
	}
	
	public MappingsDefine mappings()
	{
		JSONObject jobj = mIndexDefine.optJSONObject("mappings") ;
		if(jobj == null)
		{
			jobj = new JSONObject() ;
			mIndexDefine.put("mappings", jobj) ;
		}
		return new MappingsDefine(this , jobj) ;
	}
	
	public SettingsDefine settings()
	{
		JSONObject jobj = mIndexDefine.optJSONObject("settings") ;
		if(jobj == null)
		{
			jobj = new JSONObject() ;
			mIndexDefine.put("settings", jobj) ;
		}
		return new SettingsDefine(this , jobj) ;
	}
	
	@Override
	public String toString()
	{
		return mIndexDefine.toString() ;
	}
	
	@Override
	public JSONObject toJSONObject()
	{
		return mIndexDefine ;
	}
	
//	public String toString_V6()
//	{
//		JSONObject clone = mIndexDefine.clone() ;
//		JSONObject mappings = clone.optJSONObject("mappings") ;
//		if(mappings != null)
//		{
//			Object properties = mappings.remove("properties") ;
//			if(properties != null)
//			{
//				mappings.put("_doc", new JSONObject().put("properties" , properties)) ;
//			}
//		}
//		return clone.toString() ;
//	}
	
	public static IndexDefine one()
	{
		return new IndexDefine() ;
	}

	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		throw new UnsupportedOperationException() ;
	}
	
}
