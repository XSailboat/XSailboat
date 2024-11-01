package team.sailboat.commons.fan.dtool;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;

public class TableSchema implements Cloneable , ToJSONObject
{
	String mOwner ;
	
	/**
	 * 表名
	 */
	String mName ;
	
	/**
	 * 表注释
	 */
	String mComment ;
	
	/**
	 * 列模式
	 */
	List<ColumnSchema> mColumnSchemas ;
	
	/**
	 * 索引模式
	 */
	List<IndexSchema> mIndexSchemas ;
	
	/**
	 * 约束条件
	 */
	List<ConstraintSchema> mConstraintSchemas = new ArrayList<>() ;
	
	public TableSchema()
	{
	}
	
	public TableSchema(String aOwner , String aName)
	{
		mOwner = aOwner ;
		mName = aName ;
	}
	
	public TableSchema(String aOwner , String aName , String aComment)
	{
		mOwner = aOwner ;
		mName = aName ;
		mComment = aComment ;
	}
	
	public String getName()
	{
		return mName;
	}
	
	public void setName(String aName)
	{
		mName = aName;
	}
	
	public String getFullName()
	{
		return DBHelper.getTableFullName(mOwner, mName) ;
	}
	
	public String getOwner()
	{
		return mOwner;
	}
	
	public void setOwner(String aOwner)
	{
		mOwner = aOwner ;
	}
	
	public void setComment(String aComment)
	{
		mComment = aComment;
	}
	
	public String getComment()
	{
		return mComment;
	}
	
	public void addColumnSchema(ColumnSchema aColSchema)
	{
		if(mColumnSchemas == null)
			mColumnSchemas = new ArrayList<>() ;
		mColumnSchemas.add(aColSchema) ;
	}
	
	public void setColumnSchemas(ColumnSchema... aColumnSchemas)
	{
		mColumnSchemas = XC.arrayList(aColumnSchemas) ;
	}
	
	/**
	 * 添加约束定义
	 * @param aConstraintSchema
	 */
	public void addConstraintSchema(ConstraintSchema aConstraintSchema)
	{
		if(aConstraintSchema == null)
			return ;
		if(mConstraintSchemas == null)
			mConstraintSchemas = new ArrayList<>() ;
		mConstraintSchemas.add(aConstraintSchema) ;
	}
	
	public void addIndexSchema(IndexSchema aIndexSchema)
	{
		if(mIndexSchemas == null)
			mIndexSchemas = new ArrayList<>() ;
		mIndexSchemas.add(aIndexSchema) ;
	}
	
	public void setIndexSchemas(IndexSchema... aIndexSchemas)
	{
		mIndexSchemas = XC.arrayList(aIndexSchemas) ;
	}
	
	public ConstraintSchema getConstraintOnlyFor(final String aColumnName)
	{
		return XC.findFirst(mConstraintSchemas
				, (constraintSchema)->constraintSchema.isOnlyFor(aColumnName)).orElse(null) ;
	}
	
	public List<ColumnSchema> getColumnSchemas()
	{
		return mColumnSchemas;
	}
	
	public List<ConstraintSchema> getForeignKeyConstraintSchema()
	{
		return mConstraintSchemas.stream()
				.filter(ConstraintSchema::isForeign)
				.collect(Collectors.toList()) ;
		
	}
	
	/**
	 * 主键约束			<br>
	 * @return		返回的结果必然不为null
	 */
	public List<ConstraintSchema> getPrimaryKeyConstraintSchema(boolean aSingleCol)
	{
		return XC.extract(mConstraintSchemas, (cons)->cons.isPrimary() && (!aSingleCol || !cons.isMultiColumns())) ;		
	}
	
	/**
	 * 获取涉及到多列的约束
	 * @return
	 */
	public List<ConstraintSchema> getMultiColsConstraintSchema()
	{
		return mConstraintSchemas.stream()
				.filter(ConstraintSchema::isMultiColumns)
				.collect(Collectors.toList()) ;
	}
	
	public List<IndexSchema> getIndexSchemas()
	{
		return mIndexSchemas;
	}
	
	protected void initClone(TableSchema aClone)
	{
		aClone.mComment = mComment ;
		aClone.mColumnSchemas = XC.deepCloneArrayList(mColumnSchemas) ;
		aClone.mIndexSchemas = XC.deepCloneArrayList(mIndexSchemas) ;
		XC.deepClone(aClone.mConstraintSchemas, mConstraintSchemas) ;
	}
	
	@Override
	public TableSchema clone()
	{
		try
		{
			TableSchema clone = getClass().getConstructor().newInstance();
			initClone(clone) ;
			
			return clone ;
		}
		catch (Exception e)
		{
			throw new IllegalStateException(e) ;
		}
	}
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		aJSONObj.put("owner", mOwner)
			.put("name", mName)
			.put("comment", mComment)
			.put("columns", new JSONArray(mColumnSchemas)) ;
		return aJSONObj ;
	}
	
}
