package team.sailboat.base.sql;

import java.util.List;

import team.sailboat.base.sql.model.BName;
import team.sailboat.base.sql.model.BTable;
import team.sailboat.commons.fan.dtool.DBType;

public interface ISqlBloodEngine
{
	/**
	 * 表有别名
	 */
	public static final int sCheckAction_TableHasAlias = 0b1 ; 
	
	/**
	 * 引用表的字段必需得有别名修饰
	 */
	public static final int sCheckAction_TableAliasPrefixField = 0b10 ;
	
	List<BTable> getTable(String aTableName) ;
	
	
	public default BTable getTable(String aDbName, String aTableName)
	{
		return getTable(new BName(aDbName, aTableName)) ;
	}
	
	BTable getTable(BName aTableName) ;
	
	/**
	 * 
	 * @param aCurrentDBName		当前的数据库名
	 * @param aSql
	 * @return
	 */
	List<BTable> parse(String aCurrentDBName , String aSql) ;
	
	default List<BTable> parse(String aSql)
	{
		return parse(null, aSql) ;
	}
	
	public static ISqlBloodEngine ofHive(String aDefaultDbName)
	{
		return new HQLBloodEngine(aDefaultDbName) ;
	}
	
	public static ISqlBloodEngine ofMySQL(String aDefaultDbName)
	{
		return new MySQLBloodEngine(aDefaultDbName) ;
	}
	
	public static ISqlBloodEngine ofPostgreSql(String aDefaultDbName)
	{
		return new PgBloodEngine(aDefaultDbName) ;
	}
	
	public static ISqlBloodEngine ofTDengine(String aDefaultDbName)
	{
		return new TDengineSQLBloodEngine(aDefaultDbName) ;
	}
	
	public static ISqlBloodEngine of(DBType aDBType , String aDefaultDbName)
	{
		switch(aDBType)
		{
		case Hive:
			return ofHive(aDefaultDbName) ;
		case MySQL:
		case MySQL5:
			return ofMySQL(aDefaultDbName) ;
		case PostgreSQL:
			return ofPostgreSql(aDefaultDbName) ;
		case TDengine:
			return ofTDengine(aDefaultDbName) ;
		default:
			throw new IllegalStateException("ISqlBloodEngine未支持："+aDBType.name()) ;
		}
	}
}
