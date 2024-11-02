package team.sailboat.bd.base.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dpa.anno.BForwardMethod;
import team.sailboat.commons.fan.dpa.anno.BReverseMethod;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;
import team.sailboat.commons.fan.lang.JCommon;

/**
 * 节点上下文，参数输入/输出
 *
 * @author yyl
 * @since 2021年6月17日
 */
public class ParamIOContext implements ToJSONObject
{
	List<ContextInputParam> mInputParams ;
	
	/**
	 * 键是输出参数的名
	 */
	Map<String , ContextOutputParam> mOutputParamMap ;
	
	public Collection<ContextOutputParam> getOutputParams()
	{
		return mOutputParamMap == null?null:mOutputParamMap.values() ;
	}
	
	public ContextOutputParam getOutputParam(String aName)
	{
		return mOutputParamMap == null?null:mOutputParamMap.get(aName) ;
	}
	
	/**
	 * 发生变化，返回true
	 * @param aParams
	 * @return
	 */
	public boolean setOutputParam(Collection<ContextOutputParam> aParams)
	{
		if(XC.isNotEmpty(aParams))
		{
			if(mOutputParamMap == null)
			{
				mOutputParamMap = XC.linkedHashMap() ;
			}
			else if(!mOutputParamMap.isEmpty())
			{
				ContextOutputParam[] oldParams = mOutputParamMap.values().toArray(new ContextOutputParam[0]) ;
				ContextOutputParam[] newParams = aParams.toArray(new ContextOutputParam[0]) ;
				if(JCommon.equals(oldParams, newParams))
					return false ;
				mOutputParamMap.clear() ;
			}
			for(ContextOutputParam param : aParams)
			{
				mOutputParamMap.put(param.getName() , param) ;
			}
			return true ;
		}
		else if(XC.isNotEmpty(mOutputParamMap))
		{
			mOutputParamMap = null ;
			return true ;
		}
		return false ;
	}
	
	public boolean hasOutputParam()
	{
		return XC.isNotEmpty(mOutputParamMap) ;
	}
	
	public Collection<ContextInputParam> getInputParams()
	{
		return mInputParams ;
	}
	/**
	 * 发生改变，返回true
	 * @param aParams
	 * @return
	 */
	public boolean setInputParams(List<ContextInputParam> aParams)
	{
		if(XC.isNotEmpty(aParams))
		{
			if(mInputParams == null)
			{
				mInputParams = XC.arrayList() ;
			}
			else if(!mInputParams.isEmpty())
			{
				ContextInputParam[] oldParams = mInputParams.toArray(new ContextInputParam[0]) ;
				ContextInputParam[] newParams = aParams.toArray(new ContextInputParam[0]) ;
				if(JCommon.equals(oldParams, newParams))
					return false ;
				mInputParams.clear() ;
			}
			mInputParams.addAll(aParams) ;
			return true ;
		}
		else if(XC.isNotEmpty(mInputParams))
		{
			mInputParams = null ;
			return true ;
		}
		return false ;
	}
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return aJSONObj.putIf(mInputParams != null , "inputParams" , new JSONArray(mInputParams))
				.putIf(XC.isNotEmpty(mOutputParamMap) , "outputParams" 
						, new JSONArray(mOutputParamMap == null ? null :mOutputParamMap.values()));
	}
	
	@Override
	public String toString()
	{
		return toJSONString() ;
	}
	
	public static ParamIOContext parse(JSONObject aJo)
	{
		ParamIOContext ctx = new ParamIOContext() ;
		JSONArray ja = aJo.optJSONArray("inputParams") ;
		if(ja != null)
		{
			List<ContextInputParam> params = XC.arrayList() ;
			ja.forEach((obj)->params.add(ContextInputParam.parse((JSONObject)obj))) ;
			ctx.setInputParams(params) ; 
		}
		ja = aJo.optJSONArray("outputParams") ;
		if(ja != null)
		{
			List<ContextOutputParam> params = XC.arrayList() ;
			ja.forEach((obj)->params.add(ContextOutputParam.parse((JSONObject)obj))) ;
			ctx.setOutputParam(params) ;
		}
		return ctx ;
	}
	
	public static class SerDe
	{
		@BForwardMethod
		public static String forward(ParamIOContext aIoCtx)
		{
			return aIoCtx==null?null:aIoCtx.toJSONString() ;
		}
		
		@BReverseMethod
		public static ParamIOContext reverse(Object aSource)
		{
			return aSource == null?null:parse(new JSONObject(aSource.toString())) ;
		}
	}
	
}
