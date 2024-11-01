package team.sailboat.commons.fan.dtool;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.infc.EConsumer;
import team.sailboat.commons.fan.infc.EPredicate;
import team.sailboat.commons.fan.jquery.JSqlBuilder;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.First;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.struct.Wrapper;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.fan.text.XStringReader;

public interface IDBTool
{	
	static final Pattern sPtnOffsetAndLimit = Pattern.compile(" limit +\\d+ *, *\\d+") ;
	
	public default List<ColumnInfo> getColumnInfos(ResultSetMetaData aRsmd
			, List<ColumnInfo> aColInfos) throws SQLException
	{
		List<ColumnInfo> colInfoList = aColInfos== null?XC.arrayList():aColInfos ;
		int len = aRsmd.getColumnCount() ;
		for(int i=1 ; i<=len ; i++)
		{
			colInfoList.add(getColumnInfo(aRsmd, i)) ;
		}
		return colInfoList ;
	}
	
	/**
	 * 
	 * @param aRsmd
	 * @param aColIndex			从1开始
	 * @return
	 * @throws SQLException
	 */
	public default ColumnInfo getColumnInfo(ResultSetMetaData aRsmd , int aColIndex) throws SQLException
	{
		return new ColumnInfo(getDBSchemaName(aRsmd , aColIndex)
				, aRsmd.getTableName(aColIndex) 
				, aRsmd.getColumnName(aColIndex)
				, aRsmd.getColumnLabel(aColIndex)
				, aColIndex-1
				, aRsmd.getColumnType(aColIndex)) ;
	}
	
	/**
	 * 根据字段类型名取得标准类型名
	 * @param aRawTypeName
	 * @return
	 */
	String getCSN(String aRawTypeName) ;
	
	default String buildDeleteSql(String aTableName , String[] aColumnNames)
	{
		StringBuilder sqlBld = new StringBuilder("DELETE FROM ")
				.append(aTableName).append(" WHERE ") ;
		First first = new First() ;
		for(String columnName : aColumnNames)
		{
			first.checkAndNotFirstDo(()->sqlBld.append(" AND ") );
			sqlBld.append(columnName).append(" = ?") ;
		}
		return sqlBld.toString() ;
	}
	
	String buildUpdateOrInsertKitSql(String aTableName , String[] aColumnNames , int[] aPKColIndexes) ;
	
	String buildInsertSql(String aTableFullName , String[] aColumnNames) ;
	
	String buildUpdateSql(String aTableName , String[] aColumnNames , int... aPKColIndexes ) ;
	
	default String buildUpdateSql(String aTableName , String[] aColumnNames , String[] aPKCols )
	{
		StringBuilder strBld = new StringBuilder() ;
		strBld.append("UPDATE ").append(aTableName).append(" SET ") ;
		First first = new First() ;
		for(int i=0 ; i<aColumnNames.length ; i++)
		{
			first.checkAndNotFirstDo(()->strBld.append(" , ")) ;
			strBld.append(aColumnNames[i]).append(" = ?") ;
		}
		strBld.append(" WHERE ") ;
		for(int i=0 ; i<aPKCols.length ; i++)
		{
			if(i>0)
				strBld.append(" AND ") ;
			strBld.append(aPKCols[i]).append(" = ?") ;
		}
		return strBld.toString() ;
	}
	
	String buildInsertOrIgnoreSql(String aTableName , String[] aColumnNames , int[] aPKColIndexes)  ;
	
	default void iterate(Connection aConn , String aSql , EConsumer<ResultSet , SQLException> aConsumer 
			, int aFetchSize , Object...aArgs) throws SQLException
	{
		query(aConn, aSql, (rs)->{
			while(rs.next())
				aConsumer.accept(rs) ;
		}, aFetchSize, aArgs);
	}
	
	default void iterate(Connection aConn , String aSql , EPredicate<ResultSet , SQLException> aPred 
			, int aFetchSize , Object...aArgs) throws SQLException
	{
		query(aConn, aSql, (rs)->{
			while(rs.next())
			{
				if(!aPred.test(rs))
					return ;
			}
		}, aFetchSize, aArgs);
	}
	
	/**
	 * 
	 * @param <X>
	 * @param aConn
	 * @param aSql
	 * @param aConsumer				需要Consumer内部自己迭代ResultSet
	 * @param aFetchSize			数据库连接一次抓取的记录数量
	 * @param aArgs
	 * @throws X
	 * @throws SQLException
	 */
	default <X extends Throwable> void query(Connection aConn , String aSql , EConsumer<ResultSet , X> aConsumer 
			, int aFetchSize , Object...aArgs) throws X, SQLException
	{
		_query(aConn, aSql, aConsumer, aFetchSize , aArgs) ;
	}
	
	/**
	 * 如果SQL中有limit，那么指定的Limit将不会生效
	 * @param <X>
	 * @param aConn
	 * @param aSql
	 * @param aConsumer
	 * @param aFetchSize
	 * @param aOffset
	 * @param aLimit
	 * @param aArgs
	 * @throws X
	 * @throws SQLException
	 */
	default <X extends Throwable> void queryLimit(Connection aConn , String aSql , EConsumer<ResultSet , X> aConsumer 
			, int aFetchSize , int aOffset , int aLimit , Object...aArgs) throws X, SQLException
	{
		_query(aConn, buildSqlLimit(aSql , aOffset, aLimit) , aConsumer, aFetchSize , aArgs) ;
	}
	
	default String buildSqlLimit(String aSql , int aOffset , int aLimit)
	{
		aSql = aSql.trim() ;
		if(aSql.endsWith(";"))
		{
			aSql = aSql.substring(0, aSql.length() -1) ;
		}
		
		String cleanText = wipeBrackets(aSql) ;
		boolean haveLimit = cleanText.contains(" limit ") ;
		boolean needOffset = aOffset>0 && !cleanText.contains(" offset ") ;
		if(needOffset && haveLimit)
		{
			Matcher matcher = sPtnOffsetAndLimit.matcher(cleanText) ;
			if(matcher.find())
				needOffset = false ;
		}
		if(!haveLimit && needOffset)
			return aSql + " LIMIT " + aLimit + " OFFSET " + aOffset  ;
		else if(needOffset)
			return aSql + " OFFSET " + aOffset  ;
		else if(!haveLimit)
			return aSql + " LIMIT " + aLimit ;
		else
			return aSql ;
	}
	
	/**
	 * 擦除括号及括号中的内容		成对才擦除
	 *
	 * @author yyl
	 * @throws IOException 
	 * @since 2024年3月14日
	 */
	static String wipeBrackets(String aText)
	{
		try(XStringReader reader = new XStringReader(aText.replace('\n' , ' ')))
		{
			StringBuilder sqlBld = new StringBuilder() ;
			int lastPos = 0  ;
			while(true)
			{
				Entry<Boolean, String> entry = reader.readUntil('(' , true , false) ;
				if(entry.getKey())
				{
					sqlBld.append(entry.getValue()) ;
					lastPos = reader.getPointer() ;
					entry = reader.readUntilWithPair(')', true , false , '(') ;
					if(!entry.getKey())
					{
						sqlBld.append('(') ;			// 不成对仍然保留
						reader.skipTo(++lastPos) ;
						break ;
					}
				}
				else
				{
					if(entry.getValue() != null)
						sqlBld.append(entry.getValue()) ;
					break ;
				}
			}
			return sqlBld.toString().toLowerCase() ;
		}
		catch (IOException e)
		{
			WrapException.wrapThrow(e) ;
			return null ;			// dead code
		}
	}
	
	default <X extends Throwable> void query(Connection aConn , JSqlBuilder aJSqlBld , EConsumer<ResultSet , X> aConsumer 
			, int aFetchSize) throws X , SQLException
	{
		_query(aConn, aJSqlBld.getSql(), aConsumer, aFetchSize , aJSqlBld.getArgs()) ;
	}
	
	default <X extends Throwable> void queryCanScroll(Connection aConn , String aSql , EConsumer<ResultSet , X> aConsumer 
			, int aFetchSize , Object...aArgs) throws X, SQLException
	{
		_query(aConn, aSql, aConsumer, aFetchSize, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY , aArgs);
	}
	
	DBType getDBType() ;
	
	Date getDBCurrentDateTime(Statement aStm) throws SQLException ;
	
	Date getDBCurrentDateTime(Connection aConn) throws SQLException ;
	
	TableSchemaBuilder builder_tableSchema() ;
	
	/**
	 * 取得所有表名
	 * @param aSchemaName
	 * @return
	 */
	String[] getTableNames(Connection aConn , String aSchemaName) throws SQLException ;
	
	/**
	 * 取得指定表的模式
	 * @param aConn
	 * @param aSchemaName
	 * @param aTableName
	 * @return
	 */
	TableSchema getTableSchema(Connection aConn , String aSchemaName , String aTableName) throws SQLException ;
	
	/**
	 * 
	 * @param aConn
	 * @param aSchemaName
	 * @param aTableNames			如果没有设置表名，则查询指定模式下的所有表
	 * @return						按表名升序排列
	 * @throws SQLException
	 */
	Map<String, String> getTablesComment(Connection aConn , String aSchemaName , String...aTableNames) throws SQLException ;
	
	default Map<String , String> getTableNameAndComments_default(Connection aConn) throws SQLException
	{
		try(ResultSet rs = aConn.getMetaData().getTables(DBHelper.getCatalog(aConn) 
				, DBHelper.getSchema(aConn) , null , null))
		{
			Map<String, String> map = XC.linkedHashMap() ;
			while(rs.next())
			{
				map.put(rs.getString("TABLE_NAME") , rs.getString("REMARKS")) ;
			}
			return map ;
		}
	}
	
	/**
	 * 取得的信息，参考DBEaver里面点击模式所显示的表信息		<br>
	 * 不同的数据库，显示的东西是不一样的，但table_name和table_comment总是有的
	 * @return
	 */
	default JSONArray getTablesInfos(Connection aConn , String aDBName) throws SQLException
	{
		throw new IllegalStateException("未实现") ;
	}
	
	/**
	 * 指定用户下是否存在指定的表				<br>
	 * 
	 * @param aStm
	 * @param aTableName
	 * @param aOwner			表所属用户。如果aOwner为null，表示是当前用户。						
	 * @return
	 */
	boolean isTableExists(Statement aStm , String aTableName , String aOwner) throws SQLException ;
	
	default boolean isTableExists(Connection aConn , String aTableName , String aOwner) throws SQLException
	{
		try(Statement stm = aConn.createStatement())
		{
			return isTableExists(stm, aTableName, aOwner) ;
		}
	}
	
	/**
	 * 数据库表中是否为空<br>
	 * 表为空，即表中没有记录
	 * @param aStm
	 * @param aOwner
	 * @param aTableName
	 * @return
	 * @throws SQLException
	 */
	boolean isTableEmpty(Statement aStm , String aOwner , String aTableName) throws SQLException ;
	
	/**
	 * 删除数据库表
	 * @param aConn
	 * @param aTableFullNames		数据库表全名。如果不是当前模式，需要用"模式名."作为前缀
	 * @throws SQLException
	 */
	void dropTables(Connection aConn , String...aTableFullNames) throws SQLException ;
	
	void dropTables(Statement aStm , String...aTableFullNames) throws SQLException ;
	
	/**
	 * 删除数据库表的索引
	 * @param aConn
	 * @param aOwner
	 * @param aTableName
	 * @param aIndexNames
	 */
	int[] dropTableIndexes(Connection aConn , String aOwner , String aTableName , String...aIndexNames)
			 throws SQLException ;
	
	/**
	 * 删除数据库
	 * @param aConn
	 * @param aDatabaseName
	 * @throws SQLException
	 */
	void dropDatabase(Connection aConn , String aDatabaseName) throws SQLException ;
	
	/**
	 *
	 * truncate清空表
	 * @param aStm
	 * @param aTableFullNames
	 * @throws SQLException
	 */
	void truncateTables(Statement aStm , String...aTableFullNames) throws SQLException ;
	
	
	void renameTable(Connection aConn , String aOldName , String aNewName) throws SQLException ;
	
	/**
	 * 重命名表
	 * @param aStm
	 * @param aOldName
	 * @param aNewName
	 * @throws SQLException
	 */
	void renameTable(Statement aStm , String aOldName , String aNewName) throws SQLException ;
	
	/**
	 * 
	 * @param aConn
	 * @param aSchemaName		null表示当前模式
	 * @return
	 */
	TableSchema[] getTableSchemas(Connection aConn , String aSchemaName) throws SQLException ;
	
	/**
	 * 取得表的列模式
	 * @param aConn
	 * @param aSchemaName
	 * @param aTableName
	 * @return
	 * @throws SQLException
	 */
	ColumnSchema[] getColumnSchemas(Connection aConn , String aSchemaName , String aTableName) throws SQLException ;
	
	/**
	 * 
	 * @param aConn
	 * @param aSchemaName
	 * @param aTableName
	 * @return
	 * @throws SQLException
	 */
	IndexSchema[] getIndexSchemas(Connection aConn , String aSchemaName , String aTableName) throws SQLException ;
	
	/**
	 * 
	 * @param aConn
	 * @param aIndexSchema
	 */
	void createIndex(Connection aConn , IndexSchema aIndexSchema) throws SQLException ;
	
	/**
	 * 取得指定表的主键约束
	 * @param aConn
	 * @param aSchemaName
	 * @param aTableName
	 * @return
	 * @throws SQLException
	 */
	ConstraintSchema getPrimaryKey(Connection aConn ,String aSchemaName , String aTableName) throws SQLException ;
	
	/**
	 * 获取主键列
	 * @param aConn
	 * @param aSchemaName
	 * @param aTableName
	 * @return
	 * @throws SQLException
	 */
	default List<String> getPrimaryKeyColumnNames(Connection aConn , String aSchemaName , String aTableName) throws SQLException
	{
		ConstraintSchema cst = getPrimaryKey(aConn, aSchemaName, aTableName) ;
		if(cst == null)
			return Collections.emptyList() ;
		else
			return cst.getColumnNames() ;
	}
	
	/**
	 * 创建数据库表
	 * @param aConn
	 * @param aTblSchemas
	 * @return
	 * @throws SQLException
	 */
	void createTables(Connection aConn , TableSchema... aTblSchemas)  throws SQLException ;
	
	/**
	 * 获取指定表的建表语句
	 * @param aConn
	 * @param aDBName
	 * @param aTableName
	 * @return
	 */
	String getCreateTableSql(Connection aConn , String aDBName , String aTableName)
			 throws SQLException ;
	
	/**
	 * 创建数据库表
	 * @param aStm
	 * @param aTblSchemas
	 * @throws SQLException
	 */
	void createTables(Statement aStm , TableSchema... aTblSchemas)  throws SQLException ;
	
	/**
	 * 创建一个数据库表
	 * 
	 * @param aStm
	 * @param aTblSchema
	 * @throws SQLException
	 */
	void createTable(Statement aStm , TableSchema aTblSchema)  throws SQLException ;
	
	/**
	 * 修改表名
	 * @param aConn
	 * @param aSchemaName
	 * @param aTableName
	 * @param aNewTableName
	 * @throws SQLException
	 */
	void alterTableName(Connection aConn , String aSchemaName , String aTableName , String aNewTableName) throws SQLException ;

	/**
	 * 创建主键约束
	 * @param aName
	 * @param aTableName
	 * @param aColumnName
	 * @return
	 */
	ConstraintSchema createConstraint_PrimaryKey(String aName , String aTableName , String aColumnName) ;
	
	default UpdateOrInsertKit createUpdateOrInsertKit(String aTableName , String[] aColumnNames , String[] aColumnTypes
			, String... aPKColNames)
	{
		int[] indexes = new int[aPKColNames.length] ;
		int i=0 ;
		for(String colName : aPKColNames)
		{
			indexes[i] = XC.indexOf(aColumnNames , colName) ;
			Assert.isTrue(indexes[i] != -1 , "未在指定列名范围内的主键列：%s" , colName) ;
		}
		return createUpdateOrInsertKit(aTableName, aColumnNames, aColumnTypes, indexes) ;
	}
	
	default ICommitKit createDeleteKit(String aTableName , String[] aColumnNames , String[] aColumnTypes)
	{
		return new SqlParamsCommitKit(buildDeleteSql(aTableName, aColumnNames), aColumnTypes)  ;
	}
	
	
	UpdateOrInsertKit createUpdateOrInsertKit(String aTableName , String[] aColumnNames , String[] aColumnTypes
			, int... aPKColIndexes) ;
	
	UpdateOrInsertKit createUpdateOrInsertKit(String aTableName , String[] aColumnNames , int[] aColumnTypes
			, int... aPKColIndexes) ;
	
	UpdateOrInsertKit createInsertOrIgnoreKit(String aTableName , String[] aColumnNames , String[] aColumnTypes
			, int... aPKColIndexes) ;
	
	default UpdateOrInsertKit createInsertOrIgnoreKit(String aTableName , String[] aColumnNames , String[] aColumnTypes
			, String... aPKColNames)
	{
		int[] indexes = new int[aPKColNames.length] ;
		int i=0 ;
		for(String colName : aPKColNames)
		{
			indexes[i] = XC.indexOf(aColumnNames , colName) ;
			Assert.isTrue(indexes[i] != -1 , "未在指定列名范围内的主键列：%s" , colName) ;
		}
		return createInsertOrIgnoreKit(aTableName, aColumnNames, aColumnTypes, indexes) ;
	}
	
	default UpdateOrInsertKit createInsertOrIgnoreKit(String aTableFullName ,ColumnSchema[] aColSchemas
			, String... aPKColNames) 
	{
		String[] colNames = XC.extract(aColSchemas, ColumnSchema::getColumnName , String.class) ;
		String[] colTypes = XC.extract(aColSchemas, (colSchema)->colSchema.getDataType0().getCommonType() 
				, String.class) ;
		int[] pkColIndexes = new int[aPKColNames.length] ;
		int i = 0 ;
		for(String colName : aPKColNames)
		{
			int j=0 ;
			for(ColumnSchema colSchema : aColSchemas)
			{
				if(colName.equals(colSchema.getColumnName()))
				{
					pkColIndexes[i] = j ;
					break ;
				}
				j++ ;
			}
			Assert.isTrue(j<aColSchemas.length , "主键列[%s]在指定的列中不存在！" , colName) ;
			i++ ;
		}
		return createInsertOrIgnoreKit(aTableFullName, colNames, colTypes, pkColIndexes) ;
	}
	
	default UpdateOrInsertKit createUpdateOrInsertKit(String aTableFullName ,ColumnSchema[] aColSchemas
			, String... aPKColNames) 
	{
		String[] colNames = XC.extract(aColSchemas, ColumnSchema::getColumnName , String.class) ;
		String[] colTypes = XC.extract(aColSchemas, (colSchema)->colSchema.getDataType0().getCommonType() 
				, String.class) ;
		int[] pkColIndexes = new int[aPKColNames.length] ;
		int i = 0 ;
		for(String colName : aPKColNames)
		{
			int j=0 ;
			for(ColumnSchema colSchema : aColSchemas)
			{
				if(colName.equals(colSchema.getColumnName()))
				{
					pkColIndexes[i] = j ;
					break ;
				}
				j++ ;
			}
			Assert.isTrue(j<aColSchemas.length , "主键列[%s]在指定的列中不存在！" , colName) ;
			i++ ;
		}
		return createUpdateOrInsertKit(aTableFullName, colNames, colTypes, pkColIndexes) ;
	}
	
	/**
	 * 创建一个基于单列主键表的数据更新操作
	 * @param aTableName			表名
	 * @param aPKColName			主键名称
	 * @param aPKColType			主键类型
	 * @param aColumnNames			列名称
	 * @param aColumnTypes			列类型
	 * @return
	 */
	UpdateOrInsertKit createUpdateKit(String aTableName , String[] aColumnNames , String[] aColumnTypes
			, int...aPKColIndexes) ;
	
	UpdateOrInsertKit createUpdateKit(String aTableName , String[] aColumnNames , int[] aColumnTypes
			, int...aPKColIndexes) ;
	
	/**
	 * 创建一个数据插入操作（没有主键）
	 * @param aTableFullName		表名
	 * @param aColumnNames			列名称
	 * @param aColumnTypes			列类型，取XClassUtils.sCSN_*
	 * @return
	 */
	UpdateOrInsertKit createInsertKit(String aTableFullName
			, String[] aColumnNames , String[] aColumnTypes) ;
	
	
	
	
	/**
	 * 创建一个数据插入操作（没有主键）
	 * @param aTableFullName
	 * @param aColSchemas
	 * @return
	 */
	default UpdateOrInsertKit createInsertKit(String aTableFullName , ColumnSchema... aColSchemas)
	{
		String[] colNames = XC.extract(aColSchemas, ColumnSchema::getColumnName , String.class) ;
		String[] colTypes = XC.extract(aColSchemas, (colSchema)->colSchema.getDataType0().getCommonType() 
				, String.class) ;
		return createInsertKit(aTableFullName, colNames, colTypes) ;
	}
	
	/**
	 * 
	 * @param aTableFullName
	 * @param aColumnNames
	 * @param aColumnTypes			java.sql.Types.*
	 * @return
	 */
	UpdateOrInsertKit createInsertKit(String aTableFullName
			, String[] aColumnNames , int[] aColumnTypes) ;
	
	
	/**
	 * 数据库模式名
	 * @return
	 */
	String getSchemaName(Connection aConn) throws SQLException ;
	
	/**
	 * 数据库模式名
	 * @return
	 */
	String getDBSchemaName(ResultSetMetaData aRsmd , int aIndex) throws SQLException ;
	
	/**
	 * 取得所有数据库模式
	 * @return
	 * @throws SQLException
	 */
	List<String> getAllSchemaNames(Connection aConn) throws SQLException ;
	
	/**
	 * 创建数据库
	 * @param aConn
	 * @throws SQLException
	 */
	void createDatabase(Connection aConn , String aSchemaName) throws SQLException ;
	
	/**
	 * 授予这个数据库的必要权限（连接、读、写等）
	 * @param aSchemaName
	 * @param aUser
	 * @throws SQLException
	 */
	void grantSchemaPrivileges(Connection aConn , String aSchemaName , String aUser) throws SQLException ;
	
	public static String convertTypeToCSN(int aDBDataType)
	{
		switch(aDBDataType)
		{
		case Types.VARCHAR:
		case Types.NVARCHAR:
		case Types.CHAR:
		case Types.NCHAR:
			return XClassUtil.sCSN_String;
		case Types.NUMERIC:
		case Types.DECIMAL:
		case Types.DOUBLE:
			return XClassUtil.sCSN_Double ;
		case Types.BIGINT:
			return XClassUtil.sCSN_Long ;
		case Types.INTEGER:
		case Types.SMALLINT:
			return XClassUtil.sCSN_Integer ;
		case Types.DATE:
		case Types.TIMESTAMP:
			return XClassUtil.sCSN_DateTime ;
		case Types.BOOLEAN:
		case Types.BIT:
		case Types.TINYINT:
			return XClassUtil.sCSN_Bool ;
		case Types.FLOAT:
			return XClassUtil.sCSN_Float ;
		default:
			return "UNKNOW["+aDBDataType+"]" ;
		}
	}
	
	static <X extends Throwable> void _query(Connection aConn,
			String aSql,
			EConsumer<ResultSet, X> aConsumer,
			int aFetchSize,
			Object... aArgs) throws X , SQLException
	{
		_query(aConn, aSql, aConsumer, aFetchSize, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, aArgs);
	}
	
	static <X extends Throwable> void _query(Connection aConn,
			String aSql,
			EConsumer<ResultSet, X> aConsumer,
			int aFetchSize,
			int aResultType , 
			int aConcurrency ,
			Object... aArgs) throws X , SQLException
	{
		try(PreparedStatement pstm = aConn.prepareStatement(aSql , aResultType , aConcurrency))
		{
			if(XC.isNotEmpty(aArgs))
			{
				if(aArgs.length == 1 && aArgs[0] == null && !aSql.contains("?"))
				{
					//do nothing
				}
				else
				{
					for(int i=1 ; i<=aArgs.length ; i++)
					{
						Object arg = aArgs[i-1] ;
						if(arg instanceof Timestamp)
							pstm.setTimestamp(i, (Timestamp) arg);
						else if(arg instanceof Date)
							pstm.setTimestamp(i, new Timestamp(((Date) arg).getTime()));
						else
							pstm.setObject(i, arg) ;
					}
				}
			}
			pstm.setFetchSize(aFetchSize);
			try(ResultSet rs = pstm.executeQuery())
			{
				aConsumer.accept(rs) ;
			}
		}
	}
	
	default <X extends SQLException> void queryPage(Connection aConn , String aSql , int aPageSize , int aPage 
			, EConsumer<ResultSet , X> aResultSetConsumer , Wrapper<JSONObject> aResultMeta , Object...aParamVals) throws SQLException
	{
		queryPage(aConn, aSql, aPageSize, aPage, null, aResultSetConsumer, aResultMeta, true , aParamVals);
	}
	
	default <X extends SQLException> void queryPage(Connection aConn , String aSql , int aPageSize , int aPage 
			, EConsumer<ResultSetMetaData, X> aResultMetaConsumer
			, EConsumer<ResultSet , X> aResultSetConsumer , Wrapper<JSONObject> aResultMeta , Object...aParamVals) throws SQLException
	{
		queryPage(aConn, aSql, aPageSize, aPage, aResultMetaConsumer, aResultSetConsumer, aResultMeta, true, aParamVals);
	}
	
	/**
	 * 
	 * @param <X>
	 * @param aConn
	 * @param aSql
	 * @param aPageSize
	 * @param aPage
	 * @param aResultMetaConsumer
	 * @param aResultSetConsumer
	 * @param aResultMeta
	 * @param aCareTotalAmount		是否需要查询总条目数，页数。如果为false,将设置上hasMore字段，以表明是否有更多的数据
	 * @param aParamVals
	 * @throws SQLException
	 */
	public <X extends SQLException> void queryPage(Connection aConn ,
			String aSql,
			int aPageSize,
			int aPage,
			EConsumer<ResultSetMetaData, X> aResultMetaConsumer,
			EConsumer<ResultSet, X> aResultSetConsumer,
			Wrapper<JSONObject> aResultMeta,
			boolean aCareTotalAmount ,
			Object... aParamVals) throws SQLException ;
	
	
	
	default long getRowCount(Connection aConn , String aSchemaName , String aTableName) throws SQLException
	{
		AtomicLong result = new AtomicLong(0) ;
		query(aConn, JSqlBuilder.one("SELECT COUNT(1) FROM ")
				.append(XString.isEmpty(aSchemaName)?"":(aSchemaName+"."))
				.append(aTableName) , (rs)->{
					if(rs.next())
						result.set(rs.getLong(1)) ;
				}, 1);
		return result.get() ;
	}
	
	/**
	 * 转义指定的字符
	 * @param aStr
	 * @param aChs
	 * @return
	 */
	String escape(String aStr , char...aChs) ;
	
	default boolean isRSMDSupportColumnSourceTable()
	{
		return true ;
	}
}
