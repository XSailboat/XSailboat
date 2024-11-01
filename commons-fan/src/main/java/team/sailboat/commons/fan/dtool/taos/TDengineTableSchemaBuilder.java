package team.sailboat.commons.fan.dtool.taos;

import team.sailboat.commons.fan.dtool.ColumnSchema;
import team.sailboat.commons.fan.dtool.DBType;
import team.sailboat.commons.fan.dtool.IndexSchema;
import team.sailboat.commons.fan.dtool.TableSchema;
import team.sailboat.commons.fan.dtool.TableSchemaBuilder;

public class TDengineTableSchemaBuilder implements TableSchemaBuilder , TDengineFeatures , TDengineConst
{
	DBType mDBType ;
	TDengineTableSchema mSchema ;
	
	public TDengineTableSchemaBuilder(DBType aDBType)
	{
		mSchema = new TDengineTableSchema() ;
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
		throw new UnsupportedOperationException("不支持") ;
	}
	
	@Override
	public TableSchemaBuilder featureFor(String aFeactureName, Object aFeactureValue, DBType aDBType)
	{
		if(mDBType == aDBType)
			feature(aFeactureName, aFeactureValue) ;
		return this ;
	}

	@Override
	public TDengineColumnBuilder column(String aName)
	{
		return new TDengineColumnBuilder(this, aName , false) ;
	}
	
	@Override
	public TableSchemaBuilder withPrimaryKey(String... aColNames)
	{
		return new MySQLPrimayKeyBuilder(this , aColNames).and() ;
	}

	@Override
	public PrimaryKeyBuilder primaryKey(String... aColNames)
	{
		return new MySQLPrimayKeyBuilder(this , aColNames) ;
	}

	@Override
	public IndexBuilder index(String aName)
	{
		return new MySQLIndexBuilder(this, aName) ;
	}

	@Override
	public TableSchema build()
	{
		return mSchema ;
	}
	
	public static class TDengineColumnBuilder implements ColumnBuilder , TDengineConst
	{
		TDengineTableSchemaBuilder mTableSchemaBld ;
		TDengineColumnSchema mColSchema ;
		
		public TDengineColumnBuilder(TDengineTableSchemaBuilder aTableSchemaBld , String aName , boolean aTag)
		{
			mTableSchemaBld = aTableSchemaBld ;
			mColSchema = new TDengineColumnSchema(aName , aTag) ;
		}
		
		public TDengineColumnBuilder tag(String aTag)
		{
			mColSchema.setTag(true);
			return this ;
		}

		@Override
		public TableSchemaBuilder and()
		{
			mTableSchemaBld.mSchema.addColumnSchema(mColSchema) ;
			return mTableSchemaBld ;
		}

		@Override
		public TDengineColumnBuilder comment(String aComment)
		{
			mColSchema.setComment(aComment) ;
			return this ;
		}
		
		@Override
		public TDengineColumnBuilder dataType_vchar(int aLen)
		{
			mColSchema.setDataType(sDataType_NCHAR) ;
			mColSchema.setDataLength(aLen) ;
			return this ;
		}
		
		@Override
		public ColumnBuilder dataType_small_int()
		{
			mColSchema.setDataType(sDataType_SMALLINT);
			return this ;
		}

		@Override
		public TDengineColumnBuilder dataType_int(int aDisplayWith)
		{
			mColSchema.setDataType(sDataType_INT);
			return this ;
		}
		
		@Override
		public ColumnBuilder dataType_long()
		{
			mColSchema.setDataType(sDataType_BIGINT) ;
			return this ;
		}
		
		@Override
		public ColumnBuilder dataType_long_AutoIncrement()
		{
			mColSchema.setDataType(sDataType_BIGINT) ;
			mColSchema.putOtherProperty("AUTO_INCREMENT", ColumnSchema.sPV_Append_Directly) ;
			return this ;
		}
		
		public TDengineColumnBuilder dataType_double()
		{
			mColSchema.setDataType(sDataType_DOUBLE) ;
			return this ;
		}

		@Override
		public TDengineColumnBuilder dataType_datetime()
		{
			mColSchema.setDataType(sDataType_TIMESTAMP) ;
			mColSchema.setDataPrecision(3) ;
			return this ;
		}
		
		@Override
		public TDengineColumnBuilder dataType_datetime_autoupdate()
		{
			mColSchema.setDataType(sDataType_TIMESTAMP) ;
			return this ;
		}
		
		@Override
		public TDengineColumnBuilder dataType_datetime_autocreate()
		{
			throw new UnsupportedOperationException("不支持") ;
		}
		
		@Override
		public TDengineColumnBuilder dataType_blob()
		{
			mColSchema.setDataType(sDataType_BINARY) ;
 			return this ;
		}
		
		@Override
		public ColumnBuilder dataType_bool()
		{
			mColSchema.setDataType(sDataType_BOOL);
			return this ;
		}
		
		@Override
		public ColumnBuilder dataType_longText()
		{
			mColSchema.setDataType(sDataType_NCHAR) ;
 			return this ;
		}

		@Override
		public TDengineColumnBuilder notNull()
		{
			mColSchema.setNullable(false) ;
			return this ;
		}

		@Override
		public TDengineColumnBuilder defaultValue(Object aDefaultVal)
		{
			mColSchema.setDataDefault(aDefaultVal) ;
			return this ;
		}

		@Override
		public TDengineColumnBuilder feature(String aFeactureName, Object aFeactureValue)
		{
			mColSchema.putOtherProperty(aFeactureName, aFeactureValue) ;
			return this ;
		}
		
		@Override
		public TDengineColumnBuilder featureFor(String aFeactureName, Object aFeactureValue , DBType aDBType)
		{
			if(aDBType == DBType.MySQL)
				mColSchema.putOtherProperty(aFeactureName, aFeactureValue) ;
			return this ;
		}		
	}
	
	static class MySQLPrimayKeyBuilder implements PrimaryKeyBuilder , TDengineFeatures , TDengineConst
	{	
		TDengineTableSchemaBuilder mSchemaBuilder ;
		TDengineConstraintSchema mConstraintSchema ;
		
		public MySQLPrimayKeyBuilder(TDengineTableSchemaBuilder aSchemaBuilder , String[] aColNames)
		{
			mSchemaBuilder = aSchemaBuilder ;
			mConstraintSchema = TDengineConstraintSchema.createPrimary() ;
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
			throw new IllegalArgumentException("尚未实现") ;
		}
		
	}
	
	static class MySQLIndexBuilder implements IndexBuilder
	{
		TDengineTableSchemaBuilder mSchemaBuilder ;
		
		IndexSchema mIndexSchema ;
		
		public MySQLIndexBuilder(TDengineTableSchemaBuilder aSchemaBuilder , String aName)
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
