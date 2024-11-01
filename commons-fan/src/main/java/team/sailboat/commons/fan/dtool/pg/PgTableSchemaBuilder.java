package team.sailboat.commons.fan.dtool.pg;

import team.sailboat.commons.fan.dtool.DBType;
import team.sailboat.commons.fan.dtool.IndexSchema;
import team.sailboat.commons.fan.dtool.TableSchema;
import team.sailboat.commons.fan.dtool.TableSchemaBuilder;

public class PgTableSchemaBuilder implements TableSchemaBuilder , PgConst
{
	DBType mDBType ;
	PgTableSchema mSchema ;
	
	public PgTableSchemaBuilder()
	{
		mSchema = new PgTableSchema() ;
		mDBType = DBType.PostgreSQL ;
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
	
	public String getName()
	{
		return mSchema.getName() ;
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
//		if(TABLE__ENGINE.equalsIgnoreCase(aFeactureName))
//		{
//			mSchema.setEngine(JCommon.toString(aFeactureValue)) ;
//		}
//		else if(TABLE__CHARACTER_SET.equalsIgnoreCase(aFeactureName))
//		{
//			mSchema.setCharacterSet(JCommon.toString(aFeactureValue)) ; 
//		}
//		else if(TABLE__COLLATION.equalsIgnoreCase(aFeactureName))
//		{
//			mSchema.setTableCollation(JCommon.toString(aFeactureValue)) ;
//		}
//		else
			throw new IllegalStateException() ;
//		return this ;
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
		return new PgColumnBuilder(this, aName) ;
	}
	
	@Override
	public TableSchemaBuilder withPrimaryKey(String... aColNames)
	{
		return new PgPrimayKeyBuilder(this , aColNames).and() ;
	}

	@Override
	public PrimaryKeyBuilder primaryKey(String... aColNames)
	{
		return new PgPrimayKeyBuilder(this , aColNames) ;
	}

	@Override
	public IndexBuilder index(String aName)
	{
		return new PgIndexBuilder(this, aName) ;
	}

	@Override
	public TableSchema build()
	{
		return mSchema ;
	}
	
	static class PgColumnBuilder implements ColumnBuilder , PgConst
	{
		PgTableSchemaBuilder mTableSchemaBld ;
		PgColumnSchema mColSchema ;
		
		public PgColumnBuilder(PgTableSchemaBuilder aTableSchemaBld , String aName)
		{
			mTableSchemaBld = aTableSchemaBld ;
			mColSchema = new PgColumnSchema(aName) ;
		}

		@Override
		public TableSchemaBuilder and()
		{
			mTableSchemaBld.mSchema.addColumnSchema(mColSchema) ;
			return mTableSchemaBld ;
		}

		@Override
		public PgColumnBuilder comment(String aComment)
		{
			mColSchema.setComment(aComment) ;
			return this ;
		}
		
		@Override
		public PgColumnBuilder dataType_vchar(int aLen)
		{
			mColSchema.setDataType(sDataType_VARCHAR) ;
			mColSchema.setDataLength(aLen) ;
			return this ;
		}
		
		@Override
		public ColumnBuilder dataType_longText()
		{
			mColSchema.setDataType(sDataType_TEXT);
			return this ;
		}
		
		@Override
		public PgColumnBuilder dataType_small_int()
		{
			mColSchema.setDataType(sDataType_SMALLINT);
			return this ;
		}

		@Override
		public PgColumnBuilder dataType_int(int aDisplayWith)
		{
			mColSchema.setDataType(sDataType_INTEGER);
			return this ;
		}
		
		@Override
		public ColumnBuilder dataType_long()
		{
			mColSchema.setDataType(sDataType_BIGINT) ;
			return this;
		}
		
		@Override
		public ColumnBuilder dataType_long_AutoIncrement()
		{
			mColSchema.setDataType(sDataType_BIGSERIAL) ;
			return this;
		}
		
		public PgColumnBuilder dataType_double()
		{
			mColSchema.setDataType(sDataType_DOUBLE_PRECISION) ;
			return this ;
		}

		@Override
		public PgColumnBuilder dataType_datetime()
		{
			mColSchema.setDataType(sDataType_TIMESTAMP) ;
			mColSchema.setDataPrecision(3) ;
			return this ;
		}
		
		@Override
		public PgColumnBuilder dataType_datetime_autoupdate()
		{
			mColSchema.setDataType(sDataType_TIMESTAMP) ;
			mColSchema.setDataDefault(sCOL_FEATURE__ON_UPDATE__VAL__CURRENT_TIMESTAMP) ;
			mColSchema.setOnUpdate(sCOL_FEATURE__ON_UPDATE__VAL__CURRENT_TIMESTAMP) ;
			return this ;
		}
		
		@Override
		public PgColumnBuilder dataType_datetime_autocreate()
		{
			mColSchema.setDataType(sDataType_TIMESTAMP) ;
			mColSchema.setNullable(false) ;
			mColSchema.setDataDefault(sCOL_FEATURE__ON_UPDATE__VAL__CURRENT_TIMESTAMP) ;
			return this ;
		}
		
		@Override
		public PgColumnBuilder dataType_blob()
		{
			mColSchema.setDataType(sDataType_BYTEA) ;
 			return this ;
		}
		
		@Override
		public ColumnBuilder dataType_bool()
		{
			mColSchema.setDataType(sDataType_BOOLEAN);
			return this ;
		}

		@Override
		public PgColumnBuilder notNull()
		{
			mColSchema.setNullable(false) ;
			return this ;
		}

		@Override
		public PgColumnBuilder defaultValue(Object aDefaultVal)
		{
			mColSchema.setDataDefault(aDefaultVal) ;
			return this ;
		}

		@Override
		public PgColumnBuilder feature(String aFeactureName, Object aFeactureValue)
		{
			mColSchema.putOtherProperty(aFeactureName, aFeactureValue) ;
			return this ;
		}
		
		@Override
		public PgColumnBuilder featureFor(String aFeactureName, Object aFeactureValue , DBType aDBType)
		{
			if(aDBType == DBType.PostgreSQL)
				mColSchema.putOtherProperty(aFeactureName, aFeactureValue) ;
			return this ;
		}
		
	}
	
	static class PgPrimayKeyBuilder implements PrimaryKeyBuilder , PgConst
	{	
		PgTableSchemaBuilder mSchemaBuilder ;
		PgConstraintSchema mConstraintSchema ;
		
		public PgPrimayKeyBuilder(PgTableSchemaBuilder aSchemaBuilder , String[] aColNames)
		{
			mSchemaBuilder = aSchemaBuilder ;
			mConstraintSchema = PgConstraintSchema.createPrimary() ;
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
//			if(PRIMARYKEY__USING.equalsIgnoreCase(aFeactureName))
//				throw new IllegalStateException("尚未实现") ;
//			else
				throw new IllegalArgumentException("尚未实现") ;
		}
		
	}
	
	static class PgIndexBuilder implements IndexBuilder
	{
		PgTableSchemaBuilder mSchemaBuilder ;
		
		IndexSchema mIndexSchema ;
		
		public PgIndexBuilder(PgTableSchemaBuilder aSchemaBuilder , String aName)
		{
			mSchemaBuilder = aSchemaBuilder ;
			mIndexSchema = new IndexSchema(aName) ;
			mIndexSchema.setTableName(aSchemaBuilder.getName()) ;
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
		public IndexBuilder on(String aFeactureSQLSeg, DBType aDBType)
		{
			if(aDBType == DBType.PostgreSQL)
				mIndexSchema.setFeatureSqlSeg(aFeactureSQLSeg);
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
