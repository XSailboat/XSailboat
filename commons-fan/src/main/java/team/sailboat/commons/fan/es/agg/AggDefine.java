package team.sailboat.commons.fan.es.agg;

import java.util.List;

import team.sailboat.commons.fan.es.query.BaseExprNode;
import team.sailboat.commons.fan.es.query.Sorter;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;

public class AggDefine extends BaseExprNode implements IAggExprNode , ToJSONObject
{
	protected JSONObject mDefine ;
	int mDeepth ;
	
	final List<String> mAggPath ;
	
	AggDefine(AggsDefine aUpper , JSONObject aDefine , int aDeepth
			, List<String> aAggPath)
	{
		super(aUpper) ;
		mDefine = aDefine ;
		mDeepth = aDeepth ;
		mAggPath = aAggPath ;
	}
	
	
	public AggsDefine aggs()
	{
		JSONObject defJo = new JSONObject() ;
		mDefine.put("aggs" , defJo) ;
		return new AggsDefine(this, defJo , mDeepth
				, mAggPath) ;
	}
	
	public AggDefine terms(String aField , int aSize)
	{
		JSONObject jo = new JSONObject()
				.put("field" , aField)
				.put("size", aSize) ;
//		if(aSorter != null)
//		{
//			JSONObject orderJo = new JSONObject() ;
//			JSONArray ja = aSorter.toJSONArray() ;
//			ja.forEachJSONObject(jo0->{
//				jo0.forEach((field, d)->{
//					orderJo.put(field, d) ;
//				}) ;
//			}) ;
//			jo.put("order" , orderJo) ;
// 		}
		mDefine.put("terms" , jo) ;
		return this ;
	}
	
	public AggDefine top_hits(int aSize , Sorter aSorter)
	{
		JSONObject jo = new JSONObject()
				.put("size", aSize)
				;
		if(aSorter != null)
		{
			JSONArray ja = aSorter.toJSONArray() ;
			ja.forEachJSONObject(jo0->{
				for(String field : jo0.keyArray())
				{
					jo0.put(field , new JSONObject().put("order" , jo0.optString(field))) ;
				}
			}) ;
			jo.put("sort" , ja) ;
 		}
		mDefine.put("top_hits" , jo) ;
		return this ;
	}

	@Override
	public JSONObject toJSONObject()
	{
		return mDefine ;
	}
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return aJSONObj ;
	}
}
