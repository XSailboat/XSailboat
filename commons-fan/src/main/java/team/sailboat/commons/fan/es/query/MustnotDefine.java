package team.sailboat.commons.fan.es.query;

import team.sailboat.commons.fan.json.JSONArray;

public class MustnotDefine extends BaseLogicalCombination
{

	protected MustnotDefine(BoolDefine aUpper , JSONArray aDefine)
	{
		super(aUpper , aDefine);
	}
}
