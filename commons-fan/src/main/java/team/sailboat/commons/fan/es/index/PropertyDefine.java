package team.sailboat.commons.fan.es.index;

import team.sailboat.commons.fan.json.JSONObject;

public class PropertyDefine
{
	MappingsDefine mUp ;
	protected JSONObject mPropertyDefine ;
	
	protected PropertyDefine(MappingsDefine aUp , JSONObject aPropertyDefine)
	{
		mUp = aUp ;
		mPropertyDefine = aPropertyDefine ;
	}
	
	public MappingsDefine up()
	{
		return mUp ;
	}
}
