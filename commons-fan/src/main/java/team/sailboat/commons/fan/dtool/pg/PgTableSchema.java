package team.sailboat.commons.fan.dtool.pg;

import team.sailboat.commons.fan.dtool.TableSchema;

public class PgTableSchema extends TableSchema
{	
	String mTableCollation ;
	String mCharset ;
	String mRowFormat ;

	public PgTableSchema()
	{
		super();
	}

	public PgTableSchema(String aSchema, String aName, String aComment)
	{
		super(aSchema, aName, aComment);
	}

	public PgTableSchema(String aSchema, String aName)
	{
		super(aSchema, aName);
	}
	
	public PgTableSchema(String aSchema, String aName, String aComment , String aTableCollation)
	{
		super(aSchema, aName, aComment);
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
		((PgTableSchema)aClone).mTableCollation = mTableCollation ;
		((PgTableSchema)aClone).mCharset = mCharset ;
		((PgTableSchema)aClone).mRowFormat = mRowFormat ;
	}
}
