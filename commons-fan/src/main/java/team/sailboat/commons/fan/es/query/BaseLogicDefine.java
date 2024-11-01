package team.sailboat.commons.fan.es.query;

import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;

public class BaseLogicDefine implements LogicDefine , ToJSONObject
{
	
	protected JSONObject mDefine ;
	protected IExprNode mUpper ;
	
	
	protected BaseLogicDefine(IExprNode aUpper , JSONObject aDefine)
	{
		mUpper = aUpper ;
		mDefine = aDefine ;
	}
	
	@Override
	public BaseLogicDefine existsField(String aFieldName)
	{
		mDefine.put("exists", new JSONObject().put("field", aFieldName)) ;
		return this ;
	}
	
	@Override
	public BaseLogicDefine term(String aField , String aValue)
	{
		mDefine.put("term" , new JSONObject().put(aField , aValue)) ;
		return this ;
	}
	
	@Override
	public LogicDefine match(JSONObject aExample)
	{
		mDefine.put("match" , aExample) ;
		return this ;
	}

	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		throw new UnsupportedOperationException() ;
	}

	@Override
	public JSONObject toJSONObject()
	{
		return mDefine ;
	}

	@Override
	public IExprNode up()
	{
		return mUpper ;
	}
	
	@Override
	public BoolDefine bool()
	{
		JSONObject jobj = new JSONObject() ;
		mDefine.put("bool" , jobj) ;
		return new BoolDefine(this , jobj) ;
	}
}
