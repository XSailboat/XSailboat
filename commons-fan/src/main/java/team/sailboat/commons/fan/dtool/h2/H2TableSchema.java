package team.sailboat.commons.fan.dtool.h2;

import team.sailboat.commons.fan.dtool.TableSchema;

public class H2TableSchema extends TableSchema
{	
	String mEngine ;
	String mTableCollation ;
	String mCharset ;
	String mRowFormat ;

	public H2TableSchema()
	{
		super();
	}

	public H2TableSchema(String aSchema, String aName, String aComment)
	{
		super(aSchema, aName, aComment);
	}

	public H2TableSchema(String aSchema, String aName)
	{
		super(aSchema, aName);
	}
	
	public H2TableSchema(String aSchema, String aName, String aComment , String aEngine , String aTableCollation)
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
		((H2TableSchema)aClone).mEngine = mEngine ;
		((H2TableSchema)aClone).mTableCollation = mTableCollation ;
		((H2TableSchema)aClone).mCharset = mCharset ;
		((H2TableSchema)aClone).mRowFormat = mRowFormat ;
	}
}
