package team.sailboat.commons.fan.es.agg;

import java.util.List;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.es.query.BaseExprNode;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;
import team.sailboat.commons.fan.lang.Assert;

public class AggsDefine extends BaseExprNode implements IAggExprNode , ToJSONObject
{
	protected JSONObject mDefine ;
	
	int mBreadth = 0 ;
	final int mDeepth ;
	
	final List<String> mAggPath ;
	
	AggsDefine(int aDeepth)
	{
		super(null) ;
		mDefine = new JSONObject() ;
		mDeepth = aDeepth ;
		mAggPath = XC.arrayList() ;
	}
	
	AggsDefine(AggDefine aUpper , JSONObject aDefJo
			, int aDeepth
			, List<String> aHitPath)
	{
		super(aUpper) ;
		mDefine = aDefJo ;
		mDeepth = aDeepth ;
		mAggPath = aHitPath ;
	}
	
	
	public AggDefine agg(String aName)
	{
		/**
		 * 当超过限制情形时，数据结构是怎样的，怎么提取数据并返回，缺少用例，所以做这样的限制
		 * yyl@2024-05-30
		 */
		Assert.isTrue(mBreadth == 0 , "暂不支持同一层agg的数量超过1，如有需要联系开发人员扩展！") ;
		Assert.isTrue(mDeepth < 2 , "暂不支持agg的深度超过2，如有需要联系开发人员扩展！") ;
		JSONObject defJo = new JSONObject() ;
		mDefine.put(aName, defJo) ;
		mBreadth += 1 ;
		mAggPath.add(aName) ;
		return new AggDefine(this , defJo , mDeepth + 1 , mAggPath) ;
	}
	
	public List<String> getAggPath()
	{
		return mAggPath ;
	}

	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return aJSONObj ;
	}
	
	@Override
	public JSONObject toJSONObject()
	{
		return mDefine ;
	}
	
	public static AggsDefine one()
	{
		return new AggsDefine(0) ;
	}
}
