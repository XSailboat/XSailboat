package team.sailboat.commons.fan.dtool.dm;

import team.sailboat.commons.fan.dtool.DBType;
import team.sailboat.commons.fan.dtool.IndexSchema;
import team.sailboat.commons.fan.dtool.TableSchema;
import team.sailboat.commons.fan.dtool.TableSchemaBuilder;
import team.sailboat.commons.fan.dtool.TableSchemaBuilder.ColumnBuilder;
import team.sailboat.commons.fan.dtool.TableSchemaBuilder.IndexBuilder;
import team.sailboat.commons.fan.dtool.TableSchemaBuilder.PrimaryKeyBuilder;
import team.sailboat.commons.fan.lang.JCommon;

public class DMTableSchemaBuilder implements TableSchemaBuilder , DMFeatures
{
	DBType mDBType ;
	DMTableSchema mSchema ;
	
	public DMTableSchemaBuilder(DBType aDBType)
	{
		mSchema = new DMTableSchema() ;
		mDBType = aDBType ;
	}
	
	@Override
	public TableSchemaBuilder owner(String aOwner)
	{
		mSchema.setOwner(aOwner);
		return this ;
	}

	@Override
	public TableSchemaBuilder name(String aName)
	{
		mSchema.setName(aName) ;
		return this ;
	}

	@Override
	public TableSchemaBuilder comment(String aComment)
	{
		mSchema.setComment(aComment) ;
		return this ;
	}

	@Override
	public TableSchemaBuilder feature(String aFeactureName, Object aFeactureValue)
	{
		if(TABLE__TABLESPACE.equalsIgnoreCase(aFeactureName))
		{
			mSchema.setTableSpace(JCommon.toString(aFeactureValue)) ;
		}
		else
			throw new IllegalStateException() ;
		return this ;
	}
	
	@Override
	public TableSchemaBuilder featureFor(String aFeactureName, Object aFeactureValue, DBType aDBType)
	{
		if(mDBType == aDBType)
			feature(aFeactureName, aFeactureValue) ;
		return this ;
	}

	@Override
	public ColumnBuilder column(String aName)
	{
		return new DMColumnBuilder(this, aName) ;
	}
	
	@Override
	public TableSchemaBuilder withPrimaryKey(String... aColNames)
	{
		return new DMPrimayKeyBuilder(this , aColNames).and() ;
	}

	@Override
	public PrimaryKeyBuilder primaryKey(String... aColNames)
	{
		return new DMPrimayKeyBuilder(this , aColNames) ;
	}

	@Override
	public IndexBuilder index(String aName)
	{
		return new DMIndexBuilder(this, aName) ;
	}

	@Override
	public TableSchema build()
	{
		return mSchema ;
	}
	
	static class DMColumnBuilder implements ColumnBuilder , DMConst
	{
		DMTableSchemaBuilder mTableSchemaBld ;
		DMColumnSchema mColSchema ;
		
		public DMColumnBuilder(DMTableSchemaBuilder aTableSchemaBld , String aName)
		{
			mTableSchemaBld = aTableSchemaBld ;
			mColSchema = new DMColumnSchema(aName) ;
		}

		@Override
		public TableSchemaBuilder and()
		{
			mTableSchemaBld.mSchema.addColumnSchema(mColSchema) ;
			return mTableSchemaBld ;
		}
		
		@Override
		public ColumnBuilder comment(String aComment)
		{
			mColSchema.setComment(aComment) ;
			return this ;
		}

		@Override
		public ColumnBuilder dataType_vchar(int aLen)
		{
			mColSchema.setDataType(sDataType_VARCHAR2) ;
			mColSchema.setDataLength(aLen) ;
			return this ;
		}
		
		@Override
		public ColumnBuilder dataType_longText()
		{
			mColSchema.setDataType(sDataType_CLOB);
			return this ;
		}
		
		@Override
		public ColumnBuilder dataType_small_int()
		{
			mColSchema.setDataType(sDataType_SMALLINT);
			return this ;
		}

		@Override
		public ColumnBuilder dataType_int(int aDisplayWith)
		{
			mColSchema.setDataType(sDataType_INTEGER);
			return this ;
		}
		
		@Override
		public ColumnBuilder dataType_long()
		{
			mColSchema.setDataType(sDataType_INTEGER);
			return this ;
		}
		
		@Override
		public ColumnBuilder dataType_long_AutoIncrement()
		{
			throw new IllegalStateException("未实现！") ;
		}

		@Override
		public ColumnBuilder dataType_datetime()
		{
			mColSchema.setDataType(sDataType_DATE) ;
			return this ;
		}

		@Override
		public ColumnBuilder dataType_datetime_autoupdate()
		{
			mColSchema.setDataType(sDataType_DATE);
			mColSchema.setDataDefault(sSYSDATE) ;
			String triggerName = "tg_upd_t_"+mColSchema.getColumnName()+"_"
					+mTableSchemaBld.mSchema.getName()+"_"+mTableSchemaBld.mSchema.getOwner() ;
			if(triggerName.length()>32)
				triggerName = triggerName.substring(0, 32) ;
			mTableSchemaBld.mSchema.addSql_trigger("CREATE OR REPLACE TRIGGER "+triggerName+" BEFORE INSERT OR UPDATE ON "
					+ mTableSchemaBld.mSchema.getFullName()+" FOR EACH ROW BEGIN SELECT sysdate INTO:NEW."
					+ mColSchema.getColumnName() 
					+ "FROM DUAL ; END ;") ;
			return this ;
		}
		
		@Override
		public ColumnBuilder dataType_datetime_autocreate()
		{
			mColSchema.setDataType(sDataType_DATE);
			mColSchema.setDataDefault(sSYSDATE) ;
			String triggerName = "tg_upd_t_"+mColSchema.getColumnName()+"_"
					+mTableSchemaBld.mSchema.getName()+"_"+mTableSchemaBld.mSchema.getOwner() ;
			if(triggerName.length()>32)
				triggerName = triggerName.substring(0, 32) ;
			mTableSchemaBld.mSchema.addSql_trigger("CREATE OR REPLACE TRIGGER "+triggerName+" BEFORE INSERT ON "
					+ mTableSchemaBld.mSchema.getFullName()+" FOR EACH ROW BEGIN SELECT sysdate INTO:NEW."
					+ mColSchema.getColumnName() 
					+ "FROM DUAL ; END ;") ;
			return this ;
		}
		
		@Override
		public ColumnBuilder dataType_blob()
		{
			mColSchema.setDataType(sDataType_BLOB) ;
			return this ;
		}
		
		@Override
		public ColumnBuilder dataType_bool()
		{
			mColSchema.setDataType(sDataType_NUMBER) ;
			mColSchema.setDataLength(1) ;
			mColSchema.setDataPrecision(0) ;
			return this ;
		}
		
		@Override
		public ColumnBuilder dataType_double()
		{
			mColSchema.setDataType(sDataType_REAL) ;
			return this ;
		}

		@Override
		public ColumnBuilder notNull()
		{
			mColSchema.setNullable(false) ;
			return this ;
		}

		@Override
		public ColumnBuilder defaultValue(Object aDefaultVal)
		{
			mColSchema.setDataDefault(aDefaultVal) ;
			return this ;
		}

		@Override
		public ColumnBuilder feature(String aFeactureName, Object aFeactureValue)
		{
			throw new IllegalStateException("尚未实现") ;
		}
		
		@Override
		public ColumnBuilder featureFor(String aFeactureName, Object aFeactureValue, DBType aDBType)
		{
			throw new IllegalStateException("尚未实现") ;
		}

	}
	
	static class DMPrimayKeyBuilder implements PrimaryKeyBuilder , DMFeatures
	{	
		DMTableSchemaBuilder mSchemaBuilder ;
		DMConstraintSchema mConstraintSchema ;
		
		public DMPrimayKeyBuilder(DMTableSchemaBuilder aSchemaBuilder , String[] aColNames)
		{
			mSchemaBuilder = aSchemaBuilder ;
			mConstraintSchema = new DMConstraintSchema() ;
			mConstraintSchema.setColumnNames(aColNames);
		}

		@Override
		public TableSchemaBuilder and()
		{
			mSchemaBuilder.mSchema.addConstraintSchema(mConstraintSchema) ;
			return mSchemaBuilder ;
		}

		@Override
		public PrimaryKeyBuilder feature(String aFeactureName, Object aFeactureValue)
		{
			if(PRIMARYKEY__NAME.equalsIgnoreCase(aFeactureName))
				mConstraintSchema.setName(JCommon.toString(aFeactureValue)) ;
			else
				throw new IllegalArgumentException() ;
			return this ;
		}
		
	}
	
	static class DMIndexBuilder implements IndexBuilder
	{
		DMTableSchemaBuilder mSchemaBuilder ;
		
		IndexSchema mIndexSchema ;
		
		public DMIndexBuilder(DMTableSchemaBuilder aSchemaBuilder , String aName)
		{
			mSchemaBuilder = aSchemaBuilder ;
			mIndexSchema = new IndexSchema(aName) ;
		}

		@Override
		public TableSchemaBuilder and()
		{
			mSchemaBuilder.mSchema.addIndexSchema(mIndexSchema) ;
			return mSchemaBuilder ;
		}

		@Override
		public IndexBuilder unique()
		{
			mIndexSchema.setUnique(true) ;
			return this ;
		}
		
		@Override
		public IndexBuilder on(String aColName, boolean aASC)
		{
			mIndexSchema.addColumn(aColName, aASC); 
			return this ;
		}
		
		@Override
		public IndexBuilder on(String... aColNames)
		{
			for(String colName : aColNames)
				mIndexSchema.addColumn(colName, true) ;
			return this ;
		}
		
		@Override
		public IndexBuilder onOfDesc(String... aColNames)
		{
			for(String colName : aColNames)
				mIndexSchema.addColumn(colName, false) ;
			return this ;
		}
		
	}

}
