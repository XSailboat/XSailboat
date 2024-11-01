package team.sailboat.commons.fan.dtool;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.lang.XClassUtil;

public interface TableSchemaBuilder
{
	TableSchemaBuilder owner(String aOwner) ;
	TableSchemaBuilder name(String aName) ;
	TableSchemaBuilder comment(String aComment) ;
	TableSchemaBuilder feature(String aFeactureName , Object aFeactureValue) ;
	TableSchemaBuilder featureFor(String aFeactureName , Object aFeactureValue , DBType aDBType) ;
	
	ColumnBuilder column(String aName) ;
	
	TableSchemaBuilder withPrimaryKey(String... aColNames) ;
	
	PrimaryKeyBuilder primaryKey(String... aColNames) ;
	
	IndexBuilder index(String aName) ;
	
	TableSchema build() ;
	
	public static interface ColumnBuilder
	{
		TableSchemaBuilder and() ;
		
		ColumnBuilder comment(String aComment) ;
		ColumnBuilder dataType_vchar(int aLen) ;
		ColumnBuilder dataType_small_int() ;
		ColumnBuilder dataType_int(int aDisplayWith) ;
		ColumnBuilder dataType_long() ;
		ColumnBuilder dataType_long_AutoIncrement() ;
		ColumnBuilder dataType_datetime() ;
		/**
		 * 时间类型，精度为秒，创建或更新时自动更新
		 * @return
		 */
		ColumnBuilder dataType_datetime_autoupdate() ;
		/**
		 * 时间类型，精度为秒，创建时自动更新
		 * @return
		 */
		ColumnBuilder dataType_datetime_autocreate() ;
		
		/**
		 * 
		 * @param aType 取值为XClassUtil.sCSN_*
		 * @return
		 */
		default ColumnBuilder dataType(String aType , Object... aParams)
		{
			switch(aType)
			{
			case XClassUtil.sCSN_String :
				dataType_vchar((Integer)JCommon.defaultIfNull(XC.get(aParams, 0), 32)) ;
				break ;
			case XClassUtil.sCSN_DateTime:
				if(XC.isEmpty(aParams))
					dataType_datetime() ;
				else if("on_create".equalsIgnoreCase(aParams[0].toString()))
					dataType_datetime_autocreate() ;
				else if("on_update".equalsIgnoreCase(aParams[0].toString()))
					dataType_datetime_autoupdate() ;
				else
					dataType_datetime() ;
				break ;
			case XClassUtil.sCSN_Double:
			case XClassUtil.sCSN_Float:
				dataType_double() ;
				break ;
			case XClassUtil.sCSN_Long:
				dataType_long() ;
				break ;
			case XClassUtil.sCSN_Integer:
				dataType_int((Integer)JCommon.defaultIfNull(XC.get(aParams, 0), 11)) ;
				break ;
			case XClassUtil.sCSN_Bytes:
				dataType_blob() ;
				break ;
			case XClassUtil.sCSN_Bool:
				dataType_bool() ;
				break ;
			default:
				throw new IllegalArgumentException("未知的dataType："+aType) ;
			}
			return this ;
		}
		
		ColumnBuilder dataType_blob() ;
		ColumnBuilder dataType_bool() ;
		ColumnBuilder dataType_double() ;
		ColumnBuilder dataType_longText() ;
		ColumnBuilder notNull() ;
		ColumnBuilder defaultValue(Object aDefaultVal) ;
		ColumnBuilder feature(String aFeactureName , Object aFeactureValue) ;
		ColumnBuilder featureFor(String aFeactureName , Object aFeactureValue , DBType aDBType) ;
	}
	
	public static interface IndexBuilder
	{
		TableSchemaBuilder and() ;
		IndexBuilder unique() ;
		IndexBuilder on(String aColName , boolean aASC) ;
		default IndexBuilder on(String aFeactureSQLSeg , DBType aDBType)
		{
			throw new IllegalStateException("不支持的特性！") ;
		}
		IndexBuilder on(String... aColNames) ;
		IndexBuilder onOfDesc(String... aColNames) ;
	}
	
	public static interface PrimaryKeyBuilder
	{
		TableSchemaBuilder and() ;
		
		PrimaryKeyBuilder feature(String aFeactureName , Object aFeactureValue) ;
	}
}
