package team.sailboat.commons.fan.es.query;

import team.sailboat.commons.fan.json.JSONObject;

public interface ILogicalCondition
{
	IExprNode existsField(String aFieldName) ;

	IExprNode term(String aField, String aValue) ;
	
	IExprNode term(String aField , String aValue , boolean aApply) ;
	
	IExprNode terms(String aField, String...aValues) ;
	
	IExprNode query_string(String aQuery  , String...aFields) ;
	
	IExprNode match(JSONObject aExample) ;
}
