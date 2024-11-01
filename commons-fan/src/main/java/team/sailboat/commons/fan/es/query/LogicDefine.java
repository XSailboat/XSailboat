package team.sailboat.commons.fan.es.query;

import team.sailboat.commons.fan.json.JSONObject;

public interface LogicDefine extends IQExprNode
{
	LogicDefine existsField(String aFieldName) ;
	
	LogicDefine term(String aField, String aValue) ;
	
	LogicDefine match(JSONObject aExample) ;
	
	BoolDefine bool() ;
	
}
