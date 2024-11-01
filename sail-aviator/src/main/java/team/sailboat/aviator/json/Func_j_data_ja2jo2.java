package team.sailboat.aviator.json;

import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;

import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;

public class Func_j_data_ja2jo2 extends AbstractFunction
{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv, AviatorObject aJa)
	{
		Object obj = aJa.getValue(aEnv) ;
		Assert.notNull(obj , "第1个参数不能为null") ;
		Assert.isTrue(obj instanceof JSONObject , "第1个参数必需是JSONObject，不能是：%s" , obj.getClass().getName()) ;
		JSONObject jo = (JSONObject)obj ;
		JSONObject colsJo = jo.optJSONObject("columns") ;
		if(colsJo != null)
		{
			JSONArray dataJa = jo.optJSONArray("data") ;
			if(dataJa != null && !dataJa.isEmpty())
			{
				final int len = colsJo.size() ;
				String[] cols = new String[len] ;
				for(String key : colsJo.keySet())
				{
					int ind = colsJo.optJSONObject(key).getInt("index") ;
					cols[ind] = key ;
				}
				
				JSONArray ja = new JSONArray() ;
				dataJa.forEachJSONArray(ja_0->{
					JSONObject jo_1 = new JSONObject() ;
					for(int i=0 ; i<len ; i++)
					{
						jo_1.put(cols[i], ja_0.opt(i)) ;
					}
					ja.put(jo_1) ;
				}) ;
				jo.put("data" , ja) ;
			}
		}
		return aJa ;
	}
	
	@Override
	public String getName()
	{
		return "j.data.ja2jo" ;
	}

}