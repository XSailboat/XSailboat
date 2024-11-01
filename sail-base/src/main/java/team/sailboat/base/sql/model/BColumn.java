package team.sailboat.base.sql.model;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dtool.DataType;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;

public class BColumn implements ToJSONObject
{
	BTable mTable ;
	
	String mName ;
	
	DataType mDataType ;
	
	final List<BColumn> mParents = XC.arrayList() ;
	
	final List<BColumn> mChildren = XC.arrayList() ;
	
	public BColumn(BTable aTable , String aName)
	{
		mTable = aTable ;
		mName = aName ;
	}
	
	public BTable getTable()
	{
		return mTable;
	}
	
	public String getName()
	{
		return mName;
	}
	
	public String getPathName()
	{
		return mTable.getName().toString()+"."+mName ;
	}
	
	private void addChild(BColumn aCol)
	{
		if(!mChildren.contains(aCol))
			mChildren.add(aCol) ;
	}
	
	private void addParent(BColumn aCol)
	{
		if(!mParents.contains(aCol))
			mParents.add(aCol) ;
	}
	
	/**
	 * 
	 * @return			返回结果必不为null
	 */
	public List<BColumn> getParents()
	{
		return mParents;
	}
	
	/**
	 * 
	 * @return			返回结果必不为null
	 */
	public List<BColumn> getChildren()
	{
		return mChildren;
	}
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return aJSONObj.put("name" , mName)
				.put("dataType", mDataType)
				.put("parents" , new JSONArray(mParents.stream().map(BColumn::getPathName).collect(Collectors.toList())))
				.put("children" , new JSONArray(mChildren.stream().map(BColumn::getPathName).collect(Collectors.toList())))
				;
	}
	
	@Override
	public String toString()
	{
		return toJSONString() ;
	}
	
	public static void relate(Collection<BColumn> aParents , BColumn aChild)
	{
		if(XC.isNotEmpty(aParents))
		{
			for(BColumn parent : aParents)
			{
				parent.addChild(aChild) ;
				aChild.addParent(parent) ;
			}
		}
	}
}
