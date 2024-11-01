package team.sailboat.commons.fan.dtool.taos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dtool.ColumnSchema;
import team.sailboat.commons.fan.dtool.ConstraintSchema;
import team.sailboat.commons.fan.dtool.DBHelper;
import team.sailboat.commons.fan.dtool.DBTool;
import team.sailboat.commons.fan.dtool.DBType;
import team.sailboat.commons.fan.dtool.IDBTool;
import team.sailboat.commons.fan.dtool.IndexSchema;
import team.sailboat.commons.fan.dtool.TableSchema;
import team.sailboat.commons.fan.dtool.TableSchemaBuilder;
import team.sailboat.commons.fan.dtool.UpdateOrInsertKit;
import team.sailboat.commons.fan.infc.EConsumer;
import team.sailboat.commons.fan.jquery.JSqlBuilder;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.First;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.struct.Wrapper;
import team.sailboat.commons.fan.text.XString;

/**
 * 涛思2.6.x		<br />
 * 表不支持注释
 *
 * @author yyl
 * @since 2023年10月12日
 */
public class TDengineTool extends DBTool implements TDengineConst
{
	static final String sSQL_IsTableExists = "SELECT COUNT(*) FROM  information_schema.tables WHERE TABLE_NAME='%s'" ;
	static final String sSQL_IsTableExists_Onwer = "SELECT COUNT(*) FROM  information_schema.tables WHERE TABLE_SCHEMA='%1$s' AND TABLE_NAME='%2$s'" ;
	static final String sSQL_GetTableNames = "SELECT TABLE_NAME FROM  information_schema.tables where TABLE_SCHEMA='%s'" ;
	static final String sSQL_GetFirst = "SELECT * FROM %s LIMIT 1" ; 
	static final String sSQL_GetTablesComment = "SELECT TABLE_NAME , TABLE_COMMENT FROM information_schema.tables WHERE TABLE_SCHEMA='%1$s' AND TABLE_NAME IN (%2$s)" ;
	static final String sSQL_GetColumnSchemas = "DESCRIBE {}.{}" ;
	static final String sSQL_GetIndexCols = "SELECT * FROM information_schema.STATISTICS where TABLE_SCHEMA='%1$s' and TABLE_NAME='%2$s' ORDER BY SEQ_IN_INDEX ASC" ;
	static final String sSQL_GetPrimaryKey = "SELECT * from information_schema.KEY_COLUMN_USAGE kcu WHERE TABLE_SCHEMA=? AND TABLE_NAME=? AND CONSTRAINT_NAME ='PRIMARY' ORDER BY ORDINAL_POSITION ASC" ;
	
	final DBType mDBType = DBType.TDengine ;
	
	/**
	 * 数据库的字段类型名 到标准类型名的映射
	 */
	static Map<String, String> sDataTypeMap = XC.hashMap(
			sDataType_INT , XClassUtil.sCSN_Integer
			, sDataType_BIGINT , XClassUtil.sCSN_Long
			, sDataType_NCHAR , XClassUtil.sCSN_String
			, sDataType_TIMESTAMP , XClassUtil.sCSN_DateTime
			, sDataType_JSON , XClassUtil.sCSN_String
			, sDataType_TINYINT , XClassUtil.sCSN_Integer
			, sDataType_DOUBLE , XClassUtil.sCSN_Double
			, sDataType_BOOL , XClassUtil.sCSN_Bool
			) ;
	
	static final Set<String> sKeyWords = XC.hashSet("limit" , "partition") ;
	
	static String checkSqlField(String aFieldName)
	{
		if(sKeyWords.contains(aFieldName.toLowerCase()))
			return "`"+ aFieldName + "`" ;
		return aFieldName ;
	}
	
	@Override
	public String getCSN(String aRawTypeName)
	{
		return aRawTypeName == null?null:sDataTypeMap.get(aRawTypeName.toUpperCase());
	}
	
	public TDengineTool()
	{
	}
	
	
	@Override
	public String getSchemaName(Connection aConn) throws SQLException
	{
		return aConn.getCatalog() ;
	}
	
	@Override
	public String getDBSchemaName(ResultSetMetaData aRsmd, int aIndex) throws SQLException
	{
		return aRsmd.getCatalogName(aIndex) ;
	}
	
	@Override
	public <X extends Throwable> void query(Connection aConn,
			String aSql,
			EConsumer<ResultSet, X> aConsumer,
			int aFetchSize,
			Object... aArgs) throws X , SQLException
	{
		IDBTool._query(aConn, aSql, aConsumer, aFetchSize , ResultSet.TYPE_FORWARD_ONLY , ResultSet.CONCUR_READ_ONLY , aArgs) ;
	}
	
	@Override
	public DBType getDBType()
	{
		return mDBType ;
	}
	
	@Override
	public TableSchemaBuilder builder_tableSchema()
	{
		return new TDengineTableSchemaBuilder(getDBType()) ;
	}
	
	@Override
	protected String getSQL_IsTableExists()
	{
		return sSQL_IsTableExists ;
	}
	
	@Override
	protected String getSQL_IsTableExists_Owner()
	{
		return sSQL_IsTableExists_Onwer ;
	}
	
	@Override
	protected String getSQL_GetTableNames()
	{
		return sSQL_GetTableNames ;
	}
	
	@Override
	protected String getSQL_GetFirst()
	{
		return sSQL_GetFirst ;
	}
	
	@Override
	protected String getSQL_GetCurrentDateTime()
	{
		return "SELECT now()" ;
	}
	
	@Override
	public String buildInsertSql(String aTableFullName , String[] aColumnNames)
	{
		StringBuilder strBld = new StringBuilder() ;
		strBld.append("INSERT INTO ").append(aTableFullName).append(" ( ") ;
		First first = new First() ;
		for(int i=0 ; i<aColumnNames.length ; i++)
		{
			first.checkAndNotFirstDo(()->strBld.append(" , ")) ;
			strBld.append(checkSqlField(aColumnNames[i])) ;
		}
		strBld.append(" ) VALUES ( ") ;
		first.reset();
		for(int i=0 ; i<aColumnNames.length ; i++)
		{
			first.checkAndNotFirstDo(()->strBld.append(" , ")) ;
			strBld.append("?") ;
		}
		strBld.append(")") ;
		return strBld.toString() ;
	}
	
//	@Override
//	protected String processUpdateOrInsertKitSQL(String aTableName , String[] aPKColNames
//			, String[] aColumnNames)
//	{
//		StringBuilder strBld = new StringBuilder() ;
//		strBld.append("INSERT INTO ").append(aTableName).append("(") 
//				.append(XString.toString(" , ", aPKColNames))
//				.append(" , ")
//				.append(XString.toString(" , ", aColumnNames))
//				.append(") VALUES(") ;
//		for(int i=0 ; i<aColumnNames.length ; i++)
//		{
//			if(i>0)
//				strBld.append(" , ") ;
//			strBld.append("?") ;
//		}
//		strBld.append(") ON DUPLICATE KEY UPDATE ") ;
//		for(int i=0 ; i<aColumnNames.length ; i++)
//		{
//			if(i > 0)
//				strBld.append(" , ") ;
//			strBld.append(aColumnNames[i]).append("=VALUES(").append(aColumnNames[i]).append(")") ;
//		}
//		return strBld.toString() ;
//	}
	
	@Override
	public String buildUpdateOrInsertKitSql(String aTableName
			, String[] aColumnNames , int[] aPKColIndexes)
	{
		String[] safeFieldNames = new String[aColumnNames.length] ;
		for(int i=0 ; i<aColumnNames.length ; i++)
		{
			safeFieldNames[i] = checkSqlField(aColumnNames[i]) ;
		}
		
		StringBuilder strBld = new StringBuilder() ;
		if(aColumnNames.length == aPKColIndexes.length)
			strBld.append("INSERT IGNORE INTO ") ;
		else
			strBld.append("INSERT INTO ") ; 
		strBld.append(aTableName).append("(")
				.append(XString.toString(" , ", safeFieldNames))
				.append(") VALUES(") ;
		for(int i=0 ; i<aColumnNames.length ; i++)
		{
			if(i>0)
				strBld.append(" , ") ;
			strBld.append("?") ;
		}
		strBld.append(')') ;
		if(aColumnNames.length != aPKColIndexes.length)
		{
			strBld.append(" ON DUPLICATE KEY UPDATE ") ;
			boolean first = true ;
			for(int i=0 ; i<aColumnNames.length ; i++)
			{
				if(XC.contains(aPKColIndexes, i))
					continue ;
				if(first)
					first = false ;
				else
					strBld.append(" , ") ;
				strBld.append(safeFieldNames[i]).append("=VALUES(").append(safeFieldNames[i]).append(")") ;
			}
		}
		return strBld.toString() ;
	}
	
	@Override
	public String buildInsertOrIgnoreSql(String aTableFullName, String[] aColumnNames, int[] aPKColIndexes)
	{
		StringBuilder strBld = new StringBuilder() ;
		strBld.append("INSERT IGNORE INTO ").append(aTableFullName).append(" ( ") ;
		First first = new First() ;
		for(int i=0 ; i<aColumnNames.length ; i++)
		{
			first.checkAndNotFirstDo(()->strBld.append(" , ")) ;
			strBld.append(checkSqlField(aColumnNames[i])) ;
		}
		strBld.append(" ) VALUES ( ") ;
		first.reset();
		for(int i=0 ; i<aColumnNames.length ; i++)
		{
			first.checkAndNotFirstDo(()->strBld.append(" , ")) ;
			strBld.append("?") ;
		}
		strBld.append(")") ;
		return strBld.toString() ;
	}
	
	@Override
	public TableSchema[] getTableSchemas(Connection aConn, String aSchemaName) throws SQLException
	{
		throw new UnsupportedOperationException("未实现") ;
	}
	
	@Override
	public String getCreateTableSql(Connection aConn, String aDBName, String aTableName) throws SQLException
	{
		try(Statement stm = aConn.createStatement())
		{
			ResultSet rs = stm.executeQuery("SHOW CREATE TABLE "+(XString.isEmpty(aDBName)?"":aDBName+".")+"`"
					+aTableName+"`") ;
			if(rs.next())
			{
				// 第1列是表名，第2列是建表语句
				return rs.getString(2) ;
			}
			else
				return null ;
		}
	}
	
	@Override
	public TableSchema getTableSchema(Connection aConn, String aSchemaName, String aTableName) throws SQLException
	{
		TDengineTableSchema tblSchema = new TDengineTableSchema(aSchemaName, aTableName) ;
		tblSchema.setColumnSchemas(getColumnSchemas(aConn, aSchemaName, tblSchema.getName())) ;
		return tblSchema ;
	}
	
	@Override
	public Map<String, String> getTablesComment(Connection aConn, String aSchemaName, String... aTableNames)
			throws SQLException
	{
		if(XC.isEmpty(aTableNames))
			return Collections.emptyMap() ;
		if(XString.isEmpty(aSchemaName))
			aSchemaName = aConn.getCatalog() ;
		
		String sql = XString.msgFmt(sSQL_GetTablesComment, aSchemaName , XString.toString(",", "'" ,aTableNames)) ;
		Map<String, String> map = new HashMap<>() ;
		DBHelper.executeQuery(aConn, sql, (ResultSet rs)->map.put(rs.getString(1), rs.getString(2))) ;
		return map ;
	}
	
	public Map<String , String> getTableNameAndComments_default(Connection aConn) throws SQLException
	{
		Map<String, String> map = XC.linkedHashMap() ;
		query(aConn, "SHOW STABLES", rs->{
			while(rs.next())
				map.put(rs.getString("name") , "") ;			// TDengine2.6表不能设置注释信息
		}, 500);
		return map ;
	}
	
	@Override
	public ColumnSchema[] getColumnSchemas(Connection aConn, String aSchemaName, String aTableName) throws SQLException
	{
		if(aSchemaName == null)
			aSchemaName = getSchemaName(aConn) ;
		String sql = XString.msgFmt(sSQL_GetColumnSchemas , aSchemaName , aTableName) ;
		GetColumnSchemas action = new GetColumnSchemas() ;
		DBHelper.executeQuery(aConn, sql, action) ;
		return action.mColSchemaList.toArray(new ColumnSchema[0]) ;
	}
	
	@Override
	public IndexSchema[] getIndexSchemas(Connection aConn, String aSchemaName, String aTableName) throws SQLException
	{
		try(Statement stm = aConn.createStatement())
		{
			String sql = XString.msgFmt(sSQL_GetIndexCols , aSchemaName , aTableName) ;
			GetIndexSchemas action = new GetIndexSchemas() ;
			DBHelper.executeQuery(stm, sql, action);
			return action.getIndexSchemas() ;
		}
	}
	
	@Override
	public ConstraintSchema getPrimaryKey(Connection aConn, String aSchemaName, String aTableName) throws SQLException
	{
		try(PreparedStatement pstm = aConn.prepareStatement(sSQL_GetPrimaryKey))
		{
			pstm.setString(1, XString.isEmpty(aSchemaName)? getSchemaName(aConn) : aSchemaName) ;
			pstm.setString(2, aTableName);
			try(ResultSet rs = pstm.executeQuery())
			{
				TDengineConstraintSchema cst = null ;
				List<String> colNames = null ;
				while(rs.next())
				{
					if(cst == null)
					{
						cst = new TDengineConstraintSchema() ;
						cst.setName(rs.getString("CONSTRAINT_NAME")) ;
						cst.setOwner(aTableName) ;
						colNames = XC.arrayList() ;
					}
					colNames.add(rs.getString("COLUMN_NAME")) ;
				}
				if(cst != null)
					cst.setColumnNames(colNames.toArray(JCommon.sEmptyStringArray)) ;
				return cst ;
			}
		}
	}

	@Override
	public void createTables(Statement aStm, TableSchema... aTblSchemas) throws SQLException
	{
		if(XC.isEmpty(aTblSchemas))
			return ;
		
		//创建表
		List<String> sqlList = new ArrayList<>() ;
		try
		{
			for(TableSchema schema : aTblSchemas)
			{
				String sql = getCreateTableSql(schema) ;
				sqlList.add(sql) ;
				aStm.addBatch(sql) ;
			}
			aStm.executeBatch() ;
		}
		catch(SQLException e)
		{
			throw new SQLException(XString.splice("SQL语句：" , XString.toString("\n", sqlList)), e) ;
		}
		
		//外键
		for(TableSchema tblSchema : aTblSchemas)
		{
			List<ConstraintSchema> fkList = tblSchema.getForeignKeyConstraintSchema() ;
			if(XC.isNotEmpty(fkList))
			{
				for(ConstraintSchema fkCons : fkList)
				{
					//先判断表是否存在
					throw new IllegalStateException("尚未实现") ;
				}
			}
		}
	}

	@Override
	public void createTable(Statement aStm, TableSchema aTblSchema) throws SQLException
	{
		String sql = getCreateTableSql(aTblSchema) ;
		//创建表
		try
		{
			aStm.execute(sql) ;
		}
		catch(SQLException e)
		{
			throw new SQLException(XString.splice("SQL语句：" , sql), e) ;
		}
		
		//外键
		List<ConstraintSchema> fkList = aTblSchema.getForeignKeyConstraintSchema() ;
		if(XC.isNotEmpty(fkList))
		{
			for(ConstraintSchema fkCons : fkList)
			{
				//先判断表是否存在
				throw new IllegalStateException("尚未实现") ;
			}
		}
	}
	
	protected String getCreateTableSql(TableSchema aTblSchema)
	{
		StringBuilder createTblSqlBld = new StringBuilder("CREATE STABLE `")
			.append(aTblSchema.getFullName()).append("` (") ;
		final First first = new First() ;
		boolean haveTag = false ;
		for(ColumnSchema colSchema :aTblSchema.getColumnSchemas())
		{
			if(((TDengineColumnSchema)colSchema).isTag())
			{
				haveTag = true ;
				continue ;
			}
			first.checkAndNotFirstDo(()->createTblSqlBld.append(" , ")) ;
			createTblSqlBld.append(colSchema.getSqlText()) ;
		}
		if(haveTag)
		{
			createTblSqlBld.append(") TAGS (") ;
			first.reset() ;
			for(ColumnSchema colSchema :aTblSchema.getColumnSchemas())
			{
				if(((TDengineColumnSchema)colSchema).isTag())
				{
					first.checkAndNotFirstDo(()->createTblSqlBld.append(" , ")) ;
					createTblSqlBld.append(colSchema.getSqlText()) ;
				}
			}
		}
		createTblSqlBld.append(")") ;
		return createTblSqlBld.toString() ;
	}
	
	@Override
	public void alterTableName(Connection aConn, String aSchemaName, String aTableName, String aNewTableName)
			throws SQLException
	{
		try(Statement stm = aConn.createStatement())
		{
			JSqlBuilder sqlBld = JSqlBuilder.one("ALTER TABLE ")
				.checkAppend(XString.isNotEmpty(aSchemaName) , "${F0}.", aSchemaName)
				.checkAppend(true , "`${F1}` RENAME `${F2}`", aTableName , aNewTableName)
				;
			stm.execute(sqlBld.toString()) ;
		}
	}

	@Override
	public ConstraintSchema createConstraint_PrimaryKey(String aName, String aTableName, String aColumnName)
	{
		return null;
	}
	
	@Override
	public UpdateOrInsertKit
			createUpdateKit(String aTableName, String[] aColumnNames, String[] aColumnTypes, int... aPKColIndexes)
	{
		return new UpdateOrInsertKit(buildUpdateSql(aTableName, aColumnNames, aPKColIndexes)
				, aPKColIndexes
				, aColumnTypes);
	}
	
	@Override
	public String buildUpdateSql(String aTableName , String[] aColumnNames , int... aPKColIndexes )
	{
		StringBuilder strBld = new StringBuilder() ;
		strBld.append("UPDATE ").append(aTableName).append(" SET ") ;
		First first = new First() ;
		for(int i=0 ; i<aColumnNames.length ; i++)
		{
			first.checkAndNotFirstDo(()->strBld.append(" , ")) ;
			strBld.append(checkSqlField(aColumnNames[i])).append(" = ?") ;
		}
		strBld.append(" WHERE ") ;
		for(int i=0 ; i<aPKColIndexes.length ; i++)
		{
			if(i>0)
				strBld.append(" AND ") ;
			strBld.append(checkSqlField(aColumnNames[aPKColIndexes[i]])).append(" = ?") ;
		}
		return strBld.toString() ;
	}
	
	@Override
	public UpdateOrInsertKit
			createUpdateKit(String aTableName, String[] aColumnNames, int[] aColumnTypes, int... aPKColIndexes)
	{
		return new UpdateOrInsertKit(buildUpdateSql(aTableName, aColumnNames, aPKColIndexes)
				, aColumnTypes);
	}

	@Override
	public UpdateOrInsertKit createInsertKit(String aTableFullName, String[] aColumnNames, int[] aColumnTypes)
	{
		return new UpdateOrInsertKit(buildInsertSql(aTableFullName, aColumnNames) , aColumnTypes) ;
	}
	
	@Override
	public <X extends SQLException> void queryPage(Connection aConn ,
			String aSql,
			int aPageSize,
			int aPage,
			EConsumer<ResultSetMetaData, X> aResultMetaConsumer,
			EConsumer<ResultSet, X> aResultSetConsumer,
			Wrapper<JSONObject> aResultMeta,
			boolean aCareTotalAmount ,
			Object... aParamVals) throws SQLException
	{
		aSql = aSql.trim() ;
		Assert.isTrue(aSql.substring(0, 7).toUpperCase().equals("SELECT ") , "此分页查询方法要求SQL语句必需以“SELECT ”开头");
		boolean queryHasMore = aResultMeta != null && !aCareTotalAmount ;
		StringBuilder sqlBld = new StringBuilder(aSql) ;
		if(aResultMeta != null && aCareTotalAmount)
			sqlBld.insert(7, "SQL_CALC_FOUND_ROWS ") ;
		sqlBld.append(" LIMIT ").append(aPage*aPageSize).append(" , ")
				.append(queryHasMore?aPageSize+1:aPageSize)		// 如果不关心总数，就多查1条，以确定是否hasMore
				.toString() ;
		String sql = sqlBld.toString() ;
		try(PreparedStatement pstm = aConn.prepareStatement(sql))
		{
			if(XC.isNotEmpty(aParamVals))
			{
				if(aParamVals.length == 1 && aParamVals[0] == null && !sql.contains("?"))
				{
					//do nothing
				}
				else
				{
					for(int i=1 ; i<=aParamVals.length ; i++)
					{
						pstm.setObject(i, aParamVals[i-1]) ;
					}
				}
			}
			try(ResultSet rs = pstm.executeQuery())
			{
				if(aResultMeta != null)
				{
					if(aResultMeta.isNull())
						aResultMeta.set(new JSONObject()) ;
					aResultMeta.get().put("pageSize", aPageSize)
							.put("pageIndex", aPage) ;
				}
				
				if(aResultMetaConsumer != null)
					aResultMetaConsumer.accept(rs.getMetaData()) ;
				
				for(int i=0 ; i<aPageSize && rs.next() ; i++)
				{
					aResultSetConsumer.accept(rs) ;
				}
				if(queryHasMore)
				{
					aResultMeta.get().put("hasMore" , rs.next()) ;
				}
			}
			if(aResultMeta != null && aCareTotalAmount)
			{
				try(ResultSet rs = pstm.executeQuery("SELECT FOUND_ROWS()"))
				{
					if(rs.next())
					{
						long totalAmount = rs.getLong(1) ;
						aResultMeta.get().put("totalAmount", totalAmount)
							.put("pageAmount" , Math.ceil(totalAmount*1d/aPageSize)) ;
					}
				}
			}
		}
	}
	
	@Override
	public List<String> getAllSchemaNames(Connection aConn) throws SQLException
	{
		List<String> nameList = XC.arrayList() ;
		DBHelper.executeQuery(aConn, "SELECT schema_name FROM information_schema.schemata" , (rs)->{
			nameList.add(rs.getString(1)) ;
		});
		return nameList ;
	}
	
	@Override
	public void createDatabase(Connection aConn, String aSchemaName) throws SQLException
	{
		DBHelper.execute(aConn , XString.msgFmt("CREATE DATABASE IF NOT EXISTS {} DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci" 
				, aSchemaName)) ;
	}
	
	@Override
	public void grantSchemaPrivileges(Connection aConn , String aSchemaName, String aUser) throws SQLException
	{
		List<String> sqlList = XC.arrayList() ;
		sqlList.add(XString.msgFmt("GRANT Alter ON {}.* TO '{}'@'%'" , aSchemaName , aUser)) ;
		sqlList.add(XString.msgFmt("GRANT Create ON {}.* TO '{}'@'%'" , aSchemaName , aUser));
		sqlList.add(XString.msgFmt("GRANT Create view ON {}.* TO '{}'@'%'" , aSchemaName , aUser));
		sqlList.add(XString.msgFmt("GRANT Delete ON {}.* TO '{}'@'%'" , aSchemaName , aUser)) ;
		sqlList.add(XString.msgFmt("GRANT Drop ON {}.* TO '{}'@'%'")) ;
		sqlList.add(XString.msgFmt("GRANT Grant option ON {}.* TO '{}'@'%'" , aSchemaName , aUser)) ;
		sqlList.add(XString.msgFmt("GRANT Index ON {}.* TO '{}'@'%'" , aSchemaName , aUser)) ;
		sqlList.add(XString.msgFmt("GRANT Insert ON {}.* TO '{}'@'%'" , aSchemaName , aUser)) ;
		sqlList.add(XString.msgFmt("GRANT References ON {}.* TO '{}'@'%'" , aSchemaName , aUser)) ;
		sqlList.add(XString.msgFmt("GRANT Select ON {}.* TO '{}'@'%'" , aSchemaName , aUser)) ;
		sqlList.add(XString.msgFmt("GRANT Show view ON {}.* TO '{}'@'%'" , aSchemaName , aUser)) ;
		sqlList.add(XString.msgFmt("GRANT Trigger ON {}.* TO '{}'@'%'" , aSchemaName , aUser)) ;
		sqlList.add(XString.msgFmt("GRANT Update ON {}.* TO '{}'@'%'" , aSchemaName , aUser)) ;
		sqlList.add(XString.msgFmt("GRANT Alter routine ON {}.* TO '{}'@'%'" , aSchemaName , aUser)) ;
		sqlList.add(XString.msgFmt("GRANT Create routine ON {}.* TO '{}'@'%'" , aSchemaName , aUser)) ;
		sqlList.add(XString.msgFmt("GRANT Create temporary tables ON {}.* TO '{}'@'%'" , aSchemaName , aUser)) ;
		sqlList.add(XString.msgFmt("GRANT Execute ON {}.* TO '{}'@'%'" , aSchemaName , aUser)) ;
		sqlList.add(XString.msgFmt("GRANT Lock tables ON {}.* TO '{}'@'%'" , aSchemaName , aUser)) ;
		sqlList.add("FLUSH PRIVILEGES") ; 

		try(Statement stm = aConn.createStatement())
		{
			for(String sql : sqlList)
				stm.addBatch(sql) ;
			stm.executeBatch() ;
		}
	}
	
	@Override
	public String escape(String aStr , char...aChs)
	{
		if(aStr == null || aStr.isEmpty())
			return aStr ;
		char[] chs = aStr.toCharArray() ;
		StringBuilder strBld = null ;
		for(int i=0 ; i<chs.length ; i++)
		{
			if(XC.contains(aChs, chs[i]))
			{
				if(strBld == null)
				{
					strBld = new StringBuilder() ;
					if(i>0)
						strBld.append(chs, 0, i) ;
				}
				strBld.append("\\") ;
			}
			if(strBld != null)
				strBld.append(chs[i]) ;
		}
		return strBld == null?aStr:strBld.toString();
	}
	
	public Collection<String> getTopicNames(Connection aConn) throws SQLException
	{
		List<String> topicNames = XC.arrayList() ;
		DBHelper.executeQuery(aConn, "SHOW TOPICS" , rs->{
			topicNames.add(rs.getString(1)) ;
		}) ;
		return topicNames ;
	}
}
