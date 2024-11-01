package team.sailboat.commons.fan.dataframe;

import team.sailboat.commons.fan.json.JSONArray;

public interface FilterExp extends Exp
{
	
	abstract Boolean eval(JSONArray aRow) ;

}
