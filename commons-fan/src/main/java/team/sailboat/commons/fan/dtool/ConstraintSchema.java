package team.sailboat.commons.fan.dtool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONException;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.JSONString;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.text.XString;

/**
 * 约束
 *
 * @author yyl
 * @since 2017年10月24日
 */
public abstract class ConstraintSchema implements Cloneable , JSONString
{
	/**
	 * 约束名称
	 */
	String mName ;
	
	/**
	 * 约束所属，一般是表名
	 */
	String mOwner ;
	
	/**
	 * 是否可用
	 */
	boolean mEnabled = true ;
	
	/**
	 * 约束的列
	 */
	List<String> mColumnNames ;
	
	Map<String , Object> mOtherProps = new HashMap<>() ;
	
	public ConstraintSchema()
	{
	}
	
	public ConstraintSchema(String aName)
	{
		mName = aName ;
	}
	
	public void setName(String aName)
	{
		mName = aName;
	}
	
	public boolean isOnlyFor(String aColumnName)
	{
		if(XC.count(mColumnNames) == 1)
			return XString.equalsStrIgnoreCase(aColumnName, mColumnNames.get(0)) ;
		return false ;
	}
	
	public boolean isEnabled()
	{
		return mEnabled;
	}
	
	public String getName()
	{
		return mName;
	}
	
	public String getOwner()
	{
		return mOwner;
	}
	
	public void setOwner(String aOwner)
	{
		mOwner = aOwner;
	}
	
	
	
	public List<String> getColumnNames()
	{
		return mColumnNames;
	}
	
	public void setColumnNames(String...aColumnNames)
	{
		mColumnNames = XC.arrayList(aColumnNames) ;
	}
	
	/**
	 * 单约束的列大于1时，返回true
	 * @return
	 */
	public boolean isMultiColumns()
	{
		return XC.count(mColumnNames)>1 ;
	}
	
	public void putOtherProperty(String aKey , Object aVal)
	{
		mOtherProps.put(aKey , aVal) ;
	}
	
	public Object getOtherProperty(String aKey)
	{
		return mOtherProps.get(aKey) ;
	}
	
	/**
	 * 是否是外键
	 * @return
	 */
	public abstract boolean isForeign() ;
	
	protected void toJSON(JSONObject aJObj)
	{
		aJObj.put("name", mName)
				.put("owner", mOwner)
				.put("enabled", mEnabled)
				.put("columns", mColumnNames) ;
		if(!mOtherProps.isEmpty())
		{
			for(Entry<String, Object> entry : mOtherProps.entrySet())
			{
				aJObj.put(entry.getKey(), JCommon.toString(entry.getValue())) ;
			}
		}
	}
	
	public abstract String getSqlText() ;
	
	@Override
	public String toString()
	{
		return toJSONString() ;
	}
	
	@Override
	public String toJSONString()
	{
		try
		{
			JSONObject jobj = new JSONObject() ;
			toJSON(jobj) ;
			return jobj.toString() ;
		}
		catch (JSONException e)
		{
			throw new IllegalStateException(e) ;
		}
	}
	
	/**
	 * 是否是主键约束
	 * @return
	 */
	public abstract boolean isPrimary() ;
	
	/**
	 * 是否是唯一性约束
	 * @return
	 */
	public abstract boolean isUnique() ;
	
	public abstract ConstraintSchema clone() ;
	
	protected void initClone(ConstraintSchema aClone)
	{
		aClone.mName = mName ;
		aClone.mOwner = mOwner ;
		aClone.mEnabled = mEnabled ;
		if(mColumnNames != null)
			aClone.mColumnNames = new ArrayList<>(mColumnNames) ;
		if(!mOtherProps.isEmpty())
			aClone.mOtherProps.putAll(mOtherProps) ;
	}
}
