package team.sailboat.base.sql.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import team.sailboat.commons.fan.collection.HashMultiMap;
import team.sailboat.commons.fan.collection.IMultiMap;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;
import team.sailboat.commons.fan.lang.Assert;

public class BTable implements ToJSONObject
{
	static int sVirtualTableCount = 1 ;
	
	BName mName ;
	
	/**
	 * 是不是虚表，虚表意味着它只是一个SELECT语句，并没有一个确定的表名
	 */
	boolean mVirtual ;
	
	/**
	 * 键是列的名称
	 */
	final Map<String, BColumn> mColMap = XC.linkedHashMap() ;
	
	/**
	 * 引入所有列的表
	 */
	Set<BName> mTablesOfIncludeAllCols ;
	
	public BTable()
	{}
	
	public BTable(String aDbName , String aTableName , boolean aVirtual)
	{
		mName = new BName(aDbName, aTableName) ;
		mVirtual = aVirtual ;
	}
	
	public BName getName()
	{
		return mName;
	}
	
	public boolean isVirtual()
	{
		return mVirtual;
	}
	
	public void addTablesOfIncludeAllCols(BName...aTableNames)
	{
		if(XC.isEmpty(aTableNames))
			return ;
		// 只有虚表才可以
		Assert.isTrue(mVirtual , "不是虚表，不能调用此方法！") ;
		if(mTablesOfIncludeAllCols == null)
			mTablesOfIncludeAllCols = XC.linkedHashSet() ;
		for(BName tableName : aTableNames)
		{
			mTablesOfIncludeAllCols.add(tableName) ;
		}
	}
	
	public BColumn addColumn(String aColName)
	{
		BColumn col = mColMap.get(aColName) ;
		Assert.isNull(col, "表[%1$s]已经存在列[%2$s]！" , aColName) ;
		col = new BColumn(this , aColName) ;
		mColMap.put(aColName, col) ;
		return col ;
	}
	
	public BColumn addColumnIfAbsent(String aColName)
	{
		BColumn col = mColMap.get(aColName) ;
		if(col == null)
		{
			col = new BColumn(this , aColName) ;
			mColMap.put(aColName, col) ;
		}
		return col ;
	}
	
	public Collection<BColumn> getColumns()
	{
		return mColMap.values() ;
	}
	
	public List<String> getColumnNames()
	{
		return mColMap.values().stream().map(BColumn::getName).collect(Collectors.toList()) ;
	}
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return aJSONObj.put("name", mName)
				.put("virtual" , mVirtual)
				.put("columns" , new JSONObject(mColMap))
				.put("tableNamesOfIncludeAllCols" , mTablesOfIncludeAllCols == null?null:new JSONArray(mTablesOfIncludeAllCols))
				;
	}
	
	@Override
	public String toString()
	{
		return toJSONString() ;
	}
	
	/**
	 * 键是列名，值是此字段值来源表的表名
	 * @return
	 */
	public IMultiMap<String, BName> getColumnsSourceTables()
	{
		IMultiMap<String, BName> map = new HashMultiMap<String, BName>() ;
		for(BColumn col : mColMap.values())
		{
			map.putAll(col.getName() , col.getParents().stream()
					.map(BColumn::getTable)
					.map(BTable::getName)
					.collect(Collectors.toList())) ;
		}
		return map ;
	}
	
	public static BTable ofSelectVirtualTable(String aDbName)
	{
		return new BTable(aDbName, "select_"+sVirtualTableCount++ , true) ;
	}
	
	public static BTable ofTable(BName aName)
	{
		return new BTable(aName.getPrefix(), aName.getLocalName(), false) ;
	}
}
