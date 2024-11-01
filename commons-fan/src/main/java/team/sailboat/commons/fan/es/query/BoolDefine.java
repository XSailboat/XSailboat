package team.sailboat.commons.fan.es.query;

import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;

public class BoolDefine implements IQExprNode
{
	IExprNode mUpper ;
	JSONObject mBoolDefine ;
	
	BoolDefine(IExprNode aUpper , JSONObject aBoolDefine)
	{	
		mUpper = aUpper ;
		mBoolDefine = aBoolDefine ;
	}
	
	public MustnotDefine mustnot()
	{
		JSONArray jarray = new JSONArray() ;
		mBoolDefine.put("must_not", jarray) ;
		return new MustnotDefine(this , jarray) ;
	}
	
	public MustDefine must()
	{
		JSONArray jarray = new JSONArray() ;
		mBoolDefine.put("must", jarray) ;
		return new MustDefine(this , jarray) ;
	}
	
	public ShouldDefine should()
	{
		JSONArray jarray = new JSONArray() ;
		mBoolDefine.put("should", jarray) ;
		return new ShouldDefine(this , jarray) ;
	}

	public FilterDefine filter()
	{
		JSONObject jobj = new JSONObject() ;
		mBoolDefine.put("filter", jobj) ;
		return new FilterDefine(this, jobj) ;
	}
	
	/**
	 * 
	 * @param aApply	为false时，返回的结果不为null，但不会添加到最终的表达式中
	 * @return
	 */
	public FilterDefine filter(boolean aApply)
	{
		if(aApply)
			return filter() ;
		else
			return new FilterDefine(this, new JSONObject()) ;
	}
	
	@Override
	public IExprNode up()
	{
		return mUpper ;
	}
}
