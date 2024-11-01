package team.sailboat.commons.fan.es.query;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;

public abstract class BaseLogicalCombination extends BaseExprNode implements ILogicalCombination , ILogicalCondition
			, IQExprNode
{
	protected JSONArray mDefine ;
	
	protected BaseLogicalCombination(BoolDefine aUpper , JSONArray aDefine)
	{
		super(aUpper) ;
		mDefine = aDefine ;
	}

	@Override
	public BoolDefine up()
	{
		return (BoolDefine)super.up();
	}
	
	@Override
	public BaseLogicalCombination existsField(String aFieldName)
	{
		mDefine.put(new JSONObject().put("exists" , new JSONObject().put("field" , aFieldName)));
		return this ;
	}

	@Override
	public BaseLogicalCombination term(String aField, String aValue)
	{
		return term(aField, aValue , true) ;
	}
	
	@Override
	public BaseLogicalCombination term(String aField, String aValue, boolean aApply)
	{
		if(aApply)
			mDefine.put(new JSONObject().put("term", new JSONObject().put(aField, aValue)));
		return this ;
	}
	
	@Override
	public BaseLogicalCombination terms(String aField, String... aValues)
	{
		mDefine.put(new JSONObject().put("terms", new JSONObject().put(aField, new JSONArray(aValues))));
		return this ;
	}

	@Override
	public BaseLogicalCombination query_string(String aQuery, String... aFields)
	{
		mDefine.put(new JSONObject().put("query_string" , new JSONObject()
				.put("query", aQuery)
				.put("fields" , XC.isNotEmpty(aFields)?new JSONArray(aFields):null))) ;
		return this ;
	}
	
	@Override
	public IExprNode match(JSONObject aExample)
	{
		mDefine.put(new JSONObject().put("match" , aExample)) ;
		return this ;
	}

}
