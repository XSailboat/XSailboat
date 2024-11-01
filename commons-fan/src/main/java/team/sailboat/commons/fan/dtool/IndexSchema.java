package team.sailboat.commons.fan.dtool;

import java.util.LinkedHashMap;
import java.util.Map;

import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;
import team.sailboat.commons.fan.lang.JCommon;

public class IndexSchema implements Cloneable , ToJSONObject
{
	String mName ;
	String mTableName ;
	
	Map<String , Boolean> mColumnMap = new LinkedHashMap<>() ;
	
	boolean mUnique ;
	
	String mFeatureSqlSeg ;
	
	public IndexSchema(String aName)
	{
		mName = aName ;
	}
	
	public String getName()
	{
		return mName;
	}
	
	public String getTableName()
	{
		return mTableName;
	}
	
	public void setTableName(String aTableName)
	{
		mTableName = aTableName;
	}
	
	public void setUnique(boolean aUnique)
	{
		mUnique = aUnique;
	}
	
	public boolean isUnique()
	{
		return mUnique;
	}
	
	public void addColumn(String aColumnName , Boolean aASC)
	{
		mColumnMap.put(aColumnName, aASC) ;
	}
	
	public Map<String, Boolean> getColumnMap()
	{
		return mColumnMap;
	}
	
	public String[] getColumnNames()
	{
		return mColumnMap.keySet().toArray(JCommon.sEmptyStringArray);
	}
	
	public void setFeatureSqlSeg(String aFeatureSqlSeg)
	{
		mFeatureSqlSeg = aFeatureSqlSeg;
	}
	public String getFeatureSqlSeg()
	{
		return mFeatureSqlSeg;
	}
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		JSONArray colsJa = new JSONArray() ;
		mColumnMap.forEach((colName , asc)->{
			colsJa.put(new JSONObject().put("column" , colName)
					.put("asc" , asc)) ;
		}) ;
		return aJSONObj.put("name" , mName)
				.put("tableName" , mTableName)
				.put("unique" , mUnique)
				.put("columns" , colsJa)
				;
	}
	
	public IndexSchema clone()
	{
		IndexSchema clone = new IndexSchema(mName) ;
		clone.mTableName = mTableName ;
		clone.mUnique = mUnique ;
		clone.mColumnMap.putAll(mColumnMap);
		clone.mFeatureSqlSeg = mFeatureSqlSeg ;
		return clone ;
	}
}
