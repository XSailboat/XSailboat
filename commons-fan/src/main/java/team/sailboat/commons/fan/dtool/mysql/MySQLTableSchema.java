package team.sailboat.commons.fan.dtool.mysql;

import team.sailboat.commons.fan.dtool.TableSchema;

public class MySQLTableSchema extends TableSchema
{	
	String mEngine ;
	String mTableCollation ;
	String mCharset ;
	String mRowFormat ;

	public MySQLTableSchema()
	{
		super();
	}

	public MySQLTableSchema(String aSchema, String aName, String aComment)
	{
		super(aSchema, aName, aComment);
	}

	public MySQLTableSchema(String aSchema, String aName)
	{
		super(aSchema, aName);
	}
	
	public MySQLTableSchema(String aSchema, String aName, String aComment , String aEngine , String aTableCollation)
	{
		super(aSchema, aName, aComment);
		mEngine = aEngine ;
		mTableCollation = aTableCollation ;
	}
	
	public void setTableCollation(String aTableCollation)
	{
		mTableCollation = aTableCollation;
	}
	
	public String getTableCollation()
	{
		return mTableCollation;
	}
	
	public void setCharacterSet(String aCharset)
	{
		mCharset = aCharset ;
	}
	
	public String getCharacterSet()
	{
		return mCharset ;
	}
	
	public void setEngine(String aEngine)
	{
		mEngine = aEngine;
	}
	
	public String getEngine()
	{
		return mEngine;
	}
	
	public void setRowFormat(String aRowFormat)
	{
		mRowFormat = aRowFormat;
	}
	
	public String getRowFormat()
	{
		return mRowFormat;
	}
	
	@Override
	protected void initClone(TableSchema aClone)
	{
		super.initClone(aClone);
		((MySQLTableSchema)aClone).mEngine = mEngine ;
		((MySQLTableSchema)aClone).mTableCollation = mTableCollation ;
		((MySQLTableSchema)aClone).mCharset = mCharset ;
		((MySQLTableSchema)aClone).mRowFormat = mRowFormat ;
	}
}
