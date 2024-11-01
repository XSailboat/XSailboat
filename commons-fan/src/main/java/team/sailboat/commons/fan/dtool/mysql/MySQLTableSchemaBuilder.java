package team.sailboat.commons.fan.dtool.mysql;

import team.sailboat.commons.fan.dtool.ColumnSchema;
import team.sailboat.commons.fan.dtool.DBType;
import team.sailboat.commons.fan.dtool.IndexSchema;
import team.sailboat.commons.fan.dtool.TableSchema;
import team.sailboat.commons.fan.dtool.TableSchemaBuilder;
import team.sailboat.commons.fan.lang.JCommon;

public class MySQLTableSchemaBuilder implements TableSchemaBuilder , MySQLFeatures , MySQLConst
{
	DBType mDBType ;
	MySQLTableSchema mSchema ;
	
	public MySQLTableSchemaBuilder(DBType aDBType)
	{
		mSchema = new MySQLTableSchema() ;
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
		if(TABLE__ENGINE.equalsIgnoreCase(aFeactureName))
		{
			mSchema.setEngine(JCommon.toString(aFeactureValue)) ;
		}
		else if(TABLE__CHARACTER_SET.equalsIgnoreCase(aFeactureName))
		{
			mSchema.setCharacterSet(JCommon.toString(aFeactureValue)) ; 
		}
		else if(TABLE__COLLATION.equalsIgnoreCase(aFeactureName))
		{
			mSchema.setTableCollation(JCommon.toString(aFeactureValue)) ;
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
		return new MySQLColumnBuilder(this, aName) ;
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
	
	static class MySQLColumnBuilder implements ColumnBuilder , MySQLConst
	{
		MySQLTableSchemaBuilder mTableSchemaBld ;
		MySQLColumnSchema mColSchema ;
		
		public MySQLColumnBuilder(MySQLTableSchemaBuilder aTableSchemaBld , String aName)
		{
			mTableSchemaBld = aTableSchemaBld ;
			mColSchema = new MySQLColumnSchema(aName) ;
		}

		@Override
		public TableSchemaBuilder and()
		{
			mTableSchemaBld.mSchema.addColumnSchema(mColSchema) ;
			return mTableSchemaBld ;
		}

		@Override
		public MySQLColumnBuilder comment(String aComment)
		{
			mColSchema.setComment(aComment) ;
			return this ;
		}
		
		@Override
		public MySQLColumnBuilder dataType_vchar(int aLen)
		{
			// 超过10K,将使用中等文本获取
			if(aLen >= 10240)
			{
				mColSchema.setDataType(sDataType_MEDIUMTEXT) ;
			}
			else
			{
				mColSchema.setDataType(sDataType_VARCHAR) ;
				mColSchema.setDataLength(aLen) ;
			}
			return this ;
		}
		
		@Override
		public ColumnBuilder dataType_small_int()
		{
			mColSchema.setDataType(sDataType_SMALLINT);
			return this ;
		}

		@Override
		public MySQLColumnBuilder dataType_int(int aDisplayWith)
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
		
		public MySQLColumnBuilder dataType_double()
		{
			mColSchema.setDataType(sDataType_DOUBLE) ;
			return this ;
		}

		@Override
		public MySQLColumnBuilder dataType_datetime()
		{
			mColSchema.setDataType(sDataType_TIMESTAMP) ;
			mColSchema.setDataPrecision(3) ;
			return this ;
		}
		
		@Override
		public MySQLColumnBuilder dataType_datetime_autoupdate()
		{
			mColSchema.setDataType(sDataType_TIMESTAMP) ;
//			mColSchema.setDataPrecision(3) ;		// MySQL CURRENT_TIMESTAMP不能设置为TIMESTAMP(?)类型
			mColSchema.setDataDefault(sCOL_FEATURE__ON_UPDATE__VAL__CURRENT_TIMESTAMP) ;
			mColSchema.setOnUpdate(sCOL_FEATURE__ON_UPDATE__VAL__CURRENT_TIMESTAMP) ;
			return this ;
		}
		
		@Override
		public MySQLColumnBuilder dataType_datetime_autocreate()
		{
			mColSchema.setDataType(sDataType_TIMESTAMP) ;
//			mColSchema.setDataPrecision(3) ;		// MySQL CURRENT_TIMESTAMP不能设置为TIMESTAMP(?)类型
			mColSchema.setDataDefault(sCOL_FEATURE__ON_UPDATE__VAL__CURRENT_TIMESTAMP) ;
			return this ;
		}
		
		@Override
		public MySQLColumnBuilder dataType_blob()
		{
			mColSchema.setDataType(sDataType_BLOB) ;
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
			mColSchema.setDataType(sDataType_TEXT) ;
 			return this ;
		}

		@Override
		public MySQLColumnBuilder notNull()
		{
			mColSchema.setNullable(false) ;
			return this ;
		}

		@Override
		public MySQLColumnBuilder defaultValue(Object aDefaultVal)
		{
			mColSchema.setDataDefault(aDefaultVal) ;
			return this ;
		}

		@Override
		public MySQLColumnBuilder feature(String aFeactureName, Object aFeactureValue)
		{
			mColSchema.putOtherProperty(aFeactureName, aFeactureValue) ;
			return this ;
		}
		
		@Override
		public MySQLColumnBuilder featureFor(String aFeactureName, Object aFeactureValue , DBType aDBType)
		{
			if(aDBType == DBType.MySQL)
				mColSchema.putOtherProperty(aFeactureName, aFeactureValue) ;
			return this ;
		}		
	}
	
	static class MySQLPrimayKeyBuilder implements PrimaryKeyBuilder , MySQLFeatures , MySQLConst
	{	
		MySQLTableSchemaBuilder mSchemaBuilder ;
		MySQLConstraintSchema mConstraintSchema ;
		
		public MySQLPrimayKeyBuilder(MySQLTableSchemaBuilder aSchemaBuilder , String[] aColNames)
		{
			mSchemaBuilder = aSchemaBuilder ;
			mConstraintSchema = MySQLConstraintSchema.createPrimary() ;
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
			if(PRIMARYKEY__USING.equalsIgnoreCase(aFeactureName))
				throw new IllegalStateException("尚未实现") ;
			else
				throw new IllegalArgumentException("尚未实现") ;
		}
		
	}
	
	static class MySQLIndexBuilder implements IndexBuilder
	{
		MySQLTableSchemaBuilder mSchemaBuilder ;
		
		IndexSchema mIndexSchema ;
		
		public MySQLIndexBuilder(MySQLTableSchemaBuilder aSchemaBuilder , String aName)
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
