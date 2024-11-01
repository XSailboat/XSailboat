package team.sailboat.base.dataset;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import team.sailboat.commons.fan.dpa.SQLOrdering;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;

@Schema(name="ColumnOrdering" , description="列排序方法")
public class ColumnOrdering implements Cloneable , ToJSONObject
{
	String mName ;
	
	SQLOrdering mOrdering ;
	
	public ColumnOrdering()
	{
	}

	public ColumnOrdering(String aName, SQLOrdering aOrdering)
	{
		super();
		mName = aName;
		mOrdering = aOrdering;
	}
	
	@Schema(description = "列名")
	public String getName()
	{
		return mName;
	}
	public void setName(String aName)
	{
		mName = aName;
	}
	
	@Schema(description = "排序方法")
	public SQLOrdering getOrdering()
	{
		return mOrdering;
	}
	public void setOrdering(SQLOrdering aOrdering)
	{
		mOrdering = aOrdering;
	}
	
	public ColumnOrdering clone()
	{
		return new ColumnOrdering(mName, mOrdering) ;
	}
	
	@Schema(hidden = true)
	@JsonIgnore
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return aJSONObj.put("name", mName)
				.put("ordering", mOrdering)
				;
	}
}
