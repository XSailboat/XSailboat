package team.sailboat.commons.fan.dtool.oracle;

import java.util.ArrayList;
import java.util.List;

import team.sailboat.commons.fan.dtool.TableSchema;

public class OracleTableSchema extends TableSchema
{
	/**
	 * 表空间
	 */
	String mTableSpace ;
	
	List<String> mSqls_trigger ;
	
	public OracleTableSchema()
	{
		super();
	}

	public OracleTableSchema(String aSchema, String aName, String aComment)
	{
		super(aSchema, aName, aComment);
	}

	public OracleTableSchema(String aSchema, String aName)
	{
		super(aSchema, aName);
	}
	
	public OracleTableSchema(String aSchema, String aName, String aComment , String aTableSpace)
	{
		super(aSchema, aName, aComment);
		mTableSpace = aTableSpace ;
	}
	
	public String getTableSpace()
	{
		return mTableSpace;
	}
	
	public void setTableSpace(String aTableSpace)
	{
		mTableSpace = aTableSpace;
	}
	
	@Override
	protected void initClone(TableSchema aClone)
	{
		super.initClone(aClone);
		((OracleTableSchema)aClone).mTableSpace = mTableSpace ;
	}
	
	public void addSql_trigger(String aSql)
	{
		if(mSqls_trigger == null)
			mSqls_trigger = new ArrayList<>() ;
		mSqls_trigger.add(aSql) ;
	}
	
	public List<String> getSqls_trigger()
	{
		return mSqls_trigger;
	}
}
