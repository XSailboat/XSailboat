package team.sailboat.commons.fan.es.index;

import team.sailboat.commons.fan.json.JSONObject;

public class SettingsDefine
{
	IndexDefine mUp ;
	JSONObject mSettingsDefine ;
	
	SettingsDefine(IndexDefine aUp , JSONObject aSettingsDefine)
	{
		mSettingsDefine = aSettingsDefine ;
		mUp = aUp ;
	}
	
	public SettingsDefine number_of_shards(int aNum)
	{
		mSettingsDefine.put("number_of_shards", aNum) ;
		return this ;
	}
	
	public SettingsDefine number_of_replicas(int aNum)
	{
		mSettingsDefine.put("number_of_replicas", aNum) ;
		return this ;
	}
	
	public IndexDefine up()
	{
		return mUp ;
	}
	
}
