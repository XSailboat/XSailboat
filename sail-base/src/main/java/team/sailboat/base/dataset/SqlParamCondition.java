package team.sailboat.base.dataset;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import team.sailboat.base.def.SqlOper;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;

@Schema(description = "Sql的参数条件")
public class SqlParamCondition implements ToJSONObject
{
	/**
	 * 源字段名称
	 */
	String mSource ;
	
	/**
	 * 操作符
	 */
	SqlOper mOper ;
	
	/**
	 * 参数名
	 */
	String mValue ;
	
	@Schema(description = "源字段名")
	public String getSource()
	{
		return mSource;
	}
	public void setSource(String aSource)
	{
		mSource = aSource;
	}
	
	@Schema(description = "操作")
	public SqlOper getOper()
	{
		return mOper;
	}
	public void setOper(SqlOper aOper)
	{
		mOper = aOper;
	}
	
	@Schema(description = "参数名")
	public String getValue()
	{
		return mValue;
	}
	public void setValue(String aValue)
	{
		mValue = aValue;
	}
	
	@Schema(hidden = true)
	@JsonIgnore
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return aJSONObj.put("source", mSource)
				.put("oper", mOper == null? null : mOper.name())
				.put("value" , mValue)
				;
	}
	
	public static SqlParamCondition build(JSONObject aJo)
	{
		SqlParamCondition cnd = new SqlParamCondition() ;
		cnd.mSource = aJo.optString("source") ;
		String v = aJo.optString("oper") ;
		if(v != null)
			cnd.mOper = SqlOper.valueOf(v) ;
		cnd.mValue = aJo.optString("value") ;
		return cnd ;
	}
}
