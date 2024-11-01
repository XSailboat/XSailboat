package team.sailboat.commons.fan.dtool.oracle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dtool.ColumnSchema;
import team.sailboat.commons.fan.dtool.ConstraintSchema;
import team.sailboat.commons.fan.dtool.DBHelper;
import team.sailboat.commons.fan.dtool.DBTool;
import team.sailboat.commons.fan.dtool.DBType;
import team.sailboat.commons.fan.dtool.IndexSchema;
import team.sailboat.commons.fan.dtool.TableSchema;
import team.sailboat.commons.fan.dtool.TableSchemaBuilder;
import team.sailboat.commons.fan.dtool.UpdateOrInsertKit;
import team.sailboat.commons.fan.infc.EConsumer;
import team.sailboat.commons.fan.jquery.JSqlBuilder;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.First;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.struct.Wrapper;
import team.sailboat.commons.fan.text.XString;

public class OracleTool extends DBTool implements OracleConst
{
	static final String sSQL_GetTableNames = "SELECT table_name FROM all_tables WHERE owner = '%s'" ;
	static final String sSQL_GetColumnSchemas = "SELECT * FROM all_tab_columns WHERE table_name='%1$s' AND owner='%2$s'" ;
	static final String sSQL_GetIndexNames = "SELECT index_name , uniqueness from all_indexes where owner='%1$s' AND table_name='%2$s'" ;
	static final String sSQL_GetIndexCols = "SELECT * FROM USER_IND_COLUMNS WHERE INDEX_NAME in (%s)" ;
	static final String sSQL_GetTableDetails = "SELECT t1.table_name  , t1.tablespace_name , t2.comments FROM all_tables t1 , all_tab_comments t2 where t1.owner='%1$s' AND t1.owner=t2.owner AND t1.table_name=t2.table_name" ;
	static final String sSQL_GetTableDetail = sSQL_GetTableDetails + " AND t1.table_name='%2$s'" ;
	static final String sSQL_GetTablesComment = "SELECT table_name , comments FROM all_tab_comments WHERE owner='%1$s' AND table_name IN (%2$s)" ;
	static final String sSQL_IsTableExists_Onwer = "SELECT COUNT(*) FROM DBA_TABLES WHERE OWNER='%1$s' AND TABLE_NAME='%2$s'" ;
	static final String sSQL_IsTableExists = "SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME='%s'" ;
	static final String sSQL_GetFirst = "SELECT * FROM %s WHERE ROWNUM<2" ; 
	
	static final String sSQL_NoLogging = "ALTER TABLE %s NOLOGGING" ;
	
	/**
	 * 数据库的字段类型名 到标准类型名的映射
	 */
	static Map<String, String> sDataTypeMap = XC.hashMap(
			sDataType_INTEGER , XClassUtil.sCSN_Integer
			,sDataType_VARCHAR2 , XClassUtil.sCSN_String
			,sDataType_NVARCHAR2 , XClassUtil.sCSN_String
			,sDataType_NUMBER , XClassUtil.sCSN_Double
			,sDataType_FLOAT , XClassUtil.sCSN_Float
			,sDataType_DATE , XClassUtil.sCSN_DateTime
			);
	
	
	@Override
	public String getCSN(String aRawTypeName)
	{
		return aRawTypeName == null?null:sDataTypeMap.get(aRawTypeName.toUpperCase());
	}
	
	@Override
	public DBType getDBType()
	{
		return DBType.Oracle ;
	}
	
	@Override
	public String getDBSchemaName(ResultSetMetaData aRsmd, int aIndex) throws SQLException
	{
		return aRsmd.getSchemaName(aIndex) ;
	}
	
	@Override
	public TableSchemaBuilder builder_tableSchema()
	{
		return new OracleTableSchemaBuilder(getDBType()) ;
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
		return "SELECT sysdate FROM dual" ;
	}
	
	@Override
	public TableSchema[] getTableSchemas(Connection aConn, String aSchemaName) throws SQLException
	{
		if(aSchemaName == null)
			aSchemaName = aConn.getMetaData().getUserName() ;
		String sql = String.format(sSQL_GetTableDetails, aSchemaName) ;
		final List<TableSchema> tblSchemaList = new ArrayList<>() ;
		final String schemaName = aSchemaName ; 
		DBHelper.executeQuery(aConn, sql, (ResultSet rs)->tblSchemaList.add(new OracleTableSchema(schemaName , rs.getString("TABLE_NAME")
				, rs.getString("COMMENTS")
				, rs.getString("TABLESPACE_NAME") ))) ;
		for(TableSchema tblSchema : tblSchemaList)
		{
			tblSchema.setColumnSchemas(getColumnSchemas(aConn, aSchemaName, tblSchema.getName())) ;
			tblSchema.setIndexSchemas(getIndexSchemas(aConn, aSchemaName, tblSchema.getName()));
		}
		return tblSchemaList.toArray(new TableSchema[0]) ;
	}
	
	@Override
	public TableSchema getTableSchema(Connection aConn, String aSchemaName, String aTableName) throws SQLException
	{
		if(aSchemaName == null)
			aSchemaName = aConn.getMetaData().getUserName() ;
		String sql = String.format(sSQL_GetTableDetail, aSchemaName , aTableName) ;
		final Wrapper<TableSchema> wrapper = new Wrapper<>() ;
		final String schemaName = aSchemaName ;
		DBHelper.executeQuery(aConn, sql, (ResultSet rs)->wrapper.set(new OracleTableSchema(schemaName , rs.getString("TABLE_NAME")
				, rs.getString("COMMENTS")
				, rs.getString("TABLESPACE_NAME")))) ;
		if(!wrapper.isNull())
		{
			TableSchema tblSchema = wrapper.get() ;
			tblSchema.setColumnSchemas(getColumnSchemas(aConn, aSchemaName, tblSchema.getName())) ;
			tblSchema.setIndexSchemas(getIndexSchemas(aConn, aSchemaName, tblSchema.getName()));
		}
		return wrapper.get() ;
	}
	
	@Override
	public Map<String, String> getTablesComment(Connection aConn, String aSchemaName, String... aTableNames)
			throws SQLException
	{
		if(XC.isEmpty(aTableNames))
			return Collections.emptyMap() ;
		if(XString.isEmpty(aSchemaName))
			aSchemaName = aConn.getCatalog() ;
		
		String sql = String.format(sSQL_GetTablesComment, aSchemaName , XString.toString(",", "'" ,aTableNames)) ;
		Map<String, String> map = new HashMap<>() ;
		DBHelper.executeQuery(aConn, sql, (ResultSet rs)->map.put(rs.getString(1), rs.getString(2))) ;
		return map ;
	}
	
	@Override
	public ColumnSchema[] getColumnSchemas(Connection aConn, String aSchemaName, String aTableName) throws SQLException
	{
		if(aSchemaName == null)
			aSchemaName = getSchemaName(aConn) ;
		String sql = String.format(sSQL_GetColumnSchemas , aTableName , aSchemaName) ;
		GetColumnSchemas action = new GetColumnSchemas() ;
		DBHelper.executeQuery(aConn, sql, action) ;
		return action.mColSchemaList.toArray(new ColumnSchema[0]) ;
	}
	
	@Override
	public ConstraintSchema getPrimaryKey(Connection aConn, String aSchemaName, String aTableName) throws SQLException
	{
		throw new IllegalStateException("未实现!") ;
	}
	
	@Override
	public IndexSchema[] getIndexSchemas(Connection aConn, String aSchemaName, String aTableName) throws SQLException
	{
		try(Statement stm = aConn.createStatement())
		{
			final List<Object[]> indexList = getIndexNames(stm, aSchemaName, aTableName) ;
			if(XC.isNotEmpty(indexList))
			{
				StringBuilder strBld = new StringBuilder() ;
				boolean first = true ;
				for(Object[] params : indexList)
				{
					if(first)
						first = false ;
					else
						strBld.append(",") ;
					strBld.append('\'').append(params[0])
							.append('\'') ;
				}
				String sql = String.format(sSQL_GetIndexCols , strBld.toString()) ;
				GetIndexSchemas action = new GetIndexSchemas(indexList) ;
				DBHelper.executeQuery(stm, sql, action);
				return action.getIndexSchemas() ;
			}
		}
		return null;
	}
	
	List<Object[]> getIndexNames(Statement aStm , String aSchemaName , String aTableName) throws SQLException
	{
		String sql = String.format(sSQL_GetIndexNames, aSchemaName , aTableName) ;
		final List<Object[]> indexList = new ArrayList<>() ;
		DBHelper.executeQuery(aStm, sql, (ResultSet rs)->indexList.add(new Object[]{
				rs.getString(1) ,
				rs.getString(2) 
		}));
		return indexList ;
	}
	
	private String getCreateTableSql(TableSchema aTblSchema)
	{
		StringBuilder createTblSqlBld = new StringBuilder("CREATE TABLE ") ;
		createTblSqlBld.append(aTblSchema.getFullName()).append("(") ;
		final First first = new First() ;
		for(ColumnSchema colSchema :aTblSchema.getColumnSchemas())
		{
			first.checkAndNotFirstDo(()->createTblSqlBld.append(" , ")) ;
			createTblSqlBld.append(getColumnLine(colSchema, (OracleConstraintSchema)aTblSchema.getConstraintOnlyFor(colSchema.getColumnName()))) ;
		}
		
		//处理主键、且是单列约束
		List<ConstraintSchema> pkList = aTblSchema.getPrimaryKeyConstraintSchema(true) ;
		if(XC.isNotEmpty(pkList))
		{
			for(ConstraintSchema cons : pkList)
			{
				first.checkAndNotFirstDo(()->createTblSqlBld.append(" , ")) ;
				createTblSqlBld.append(cons.getSqlText()) ;
			}
		}
		//处理多参数约束
		List<ConstraintSchema> consList = aTblSchema.getMultiColsConstraintSchema() ;
		if(!consList.isEmpty())
		{
			for(ConstraintSchema cons : consList)
			{
				first.checkAndNotFirstDo(()->createTblSqlBld.append(" , ")) ;
				createTblSqlBld.append(cons.getSqlText()) ;
			}
		}
		createTblSqlBld.append(")") ;
		if(aTblSchema != null)
		{
			OracleTableSchema oracleTblSchema = (OracleTableSchema)aTblSchema ;
			if(oracleTblSchema.getTableSpace() != null)
				createTblSqlBld.append(" tablespace ").append(oracleTblSchema.getTableSpace()) ;
		}
		return createTblSqlBld.toString() ;
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
		
		//创建索引
		List<IndexSchema> indexSchemaList = aTblSchema.getIndexSchemas() ;
		if(XC.isNotEmpty(indexSchemaList))
		{
			for(IndexSchema indexSchema : indexSchemaList)
				aStm.execute(getCreateIndexLine(aTblSchema.getOwner() , indexSchema)) ;
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
	
	@Override
	public String getCreateTableSql(Connection aConn, String aDBName, String aTableName) throws SQLException
	{
		try(Statement stm = aConn.createStatement())
		{
			String sql = XString.msgFmt("SELECT DBMS_METADATA.GET_DDL('TABLE','{}','{}') FROM DUAL"
					, aTableName , XString.isEmpty(aDBName)?getSchemaName(aConn):aDBName) ;
			ResultSet rs = stm.executeQuery(sql) ;
			if(rs.next())
			{
				// 第1列是建表语句
				return rs.getString(1) ;
			}
			else
				return null ;
		}
	}
	
	
	@Override
	public void createTables(Statement aStm, TableSchema... aTblSchemas) throws SQLException
	{
		for(TableSchema tblSchema : aTblSchemas)
		{
			String sql = getCreateTableSql(tblSchema) ;
			//创建表
			try
			{
				aStm.execute(sql) ;
			}
			catch(SQLException e)
			{
				throw new SQLException(XString.splice("SQL语句：" , sql), e) ;
			}
			
			List<String> sqlList = ((OracleTableSchema)tblSchema).getSqls_trigger() ;
			if(XC.isNotEmpty(sqlList))
			{
				for(String sql_0 : sqlList)
					aStm.execute(sql_0) ;
			}
			
			//创建索引
			List<IndexSchema> indexSchemaList = tblSchema.getIndexSchemas() ;
			if(XC.isNotEmpty(indexSchemaList))
			{
				for(IndexSchema indexSchema : indexSchemaList)
				{
					String createIndexSql = getCreateIndexLine(tblSchema.getOwner() , indexSchema) ;
					try
					{
						aStm.execute(createIndexSql) ;
					}
					catch(SQLException e)
					{
						throw new SQLException("SQL语句："+createIndexSql , e) ;
					}
				}
			}
		}
		
		//处理外键
		for(TableSchema tblSchema : aTblSchemas)
		{
			List<ConstraintSchema> fkList = tblSchema.getForeignKeyConstraintSchema() ;
			if(XC.isNotEmpty(fkList))
			{
				for(ConstraintSchema fkCons : fkList)
				{
					//先判断表时候存在
					throw new IllegalStateException("尚未实现") ;
				}
			}
		}
	}
	
	@Override
	public void alterTableName(Connection aConn, String aSchemaName, String aTableName, String aNewTableName)
			throws SQLException
	{
		try(Statement stm = aConn.createStatement())
		{
			JSqlBuilder sqlBld = JSqlBuilder.one("ALTER TABLE ")
				.checkAppend(XString.isNotEmpty(aSchemaName) , "${F0}.", aSchemaName)
				.checkAppend(true , "`${F1}` RENAME TO `${F2}`", aTableName , aNewTableName)
				;
			stm.execute(sqlBld.toString()) ;
		}
	}
	
	@Override
	public ConstraintSchema createConstraint_PrimaryKey(String aName, String aTableName, String aColumnName)
	{
		OracleConstraintSchema constraint = new OracleConstraintSchema(aName , OracleConst.sConstraintType_P) ;
		constraint.setOwner(aTableName) ;
		constraint.setColumnNames(aColumnName);
		return constraint ;
	}
	
//	@Override
//	protected String processUpdateOrInsertKitSQL(String aTableName , String[] aPKColNames
//			, String[] aColumnNames)
//	{
//		StringBuilder strBld = new StringBuilder() ;
//		strBld.append("MERGE INTO ").append(aTableName).append(" t1 USING (SELECT ")
//				.append(XString.toString(" , ", "? AS " , "" , aPKColNames))
//				.append(" , ").append(XString.toString(" , ", "? AS " , "" , aColumnNames)) ;
//		strBld.append(" FROM DUAL) t2 ON (") ;
//		First first = new First() ;
//		for(String pkColName : aPKColNames)
//		{
//			first.checkAndNotFirstDo(()->strBld.append("AND"));
//			strBld.append("t1.").append(pkColName).append(" = t2.").append(pkColName) ;
//		}
//		strBld.append(") WHEN MATCHED THEN UPDATE SET ") ;
//		first.reset();
//		for(String colName : aColumnNames)
//		{
//			first.checkAndNotFirstDo(()->strBld.append(" , "));
//			strBld.append("t1.").append(colName).append(" = t2.").append(colName) ;
//		}
//		return strBld.append(" WHEN NOT MATCHED THEN INSERT (")
//				.append(XString.toString(" , ", aPKColNames))
//				.append(XString.toString(" , ", aColumnNames))
//				.append(") VALUES(")
//				.append(XString.toString(" , ", "t2." , "" , aPKColNames))
//				.append(" , ")
//				.append(XString.toString(" , ", "t2." , "" , aColumnNames))
//				.append(")")
//				.toString() ;
//	}
	
	@Override
	public String buildUpdateOrInsertKitSql(String aTableName
			, String[] aColumnNames , int[] aPKColIndexes)
	{
		StringBuilder strBld = new StringBuilder() ;
		strBld.append("MERGE INTO ").append(aTableName).append(" t1 USING (SELECT ")
				.append(XString.toString(" , ", "? AS " , "" , aColumnNames))
				.append(" FROM DUAL) t2 ON (") ;
		First first = new First() ;
		for(int pkColIndex : aPKColIndexes)
		{
			String pkColName = aColumnNames[pkColIndex] ;
			first.checkAndNotFirstDo(()->strBld.append("AND"));
			strBld.append("t1.").append(pkColName).append(" = t2.").append(pkColName) ;
		}
		strBld.append(") WHEN MATCHED THEN UPDATE SET ") ;
		first.reset();
		for(int i=0 ;i<aColumnNames.length ; i++)
		{
			if(XC.contains(aPKColIndexes, i))
				continue ;
			first.checkAndNotFirstDo(()->strBld.append(" , "));
			strBld.append("t1.").append(aColumnNames[i]).append(" = t2.").append(aColumnNames[i]) ;
		}
		return strBld.append(" WHEN NOT MATCHED THEN INSERT ( ")
				.append(XString.toString(" , ", aColumnNames))
				.append(") VALUES( ")
				.append(XString.toString(" , ", "t2." , "" , aColumnNames))
				.append(")")
				.toString() ;
	}
	
	@Override
	public String buildInsertSql(String aTableFullName , String[] aColumnNames)
	{
		StringBuilder strBld = new StringBuilder() ;
		strBld.append("INSERT /*APPEND*/ INTO ").append(aTableFullName).append(" ( ") ;
		First first = new First() ;
		for(int i=0 ; i<aColumnNames.length ; i++)
		{
			first.checkAndNotFirstDo(()->strBld.append(" , ")) ;
			strBld.append(aColumnNames[i]) ;
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
	public String buildInsertOrIgnoreSql(String aTableName, String[] aColumnNames, int[] aPKColIndexes)
	{
		throw new IllegalStateException("未实现！") ;
	}
	
	@Override
	public UpdateOrInsertKit createInsertKit(String aTableFullName, String[] aColumnNames, int[] aColumnTypes)
	{
		OracleUpdateOrInsertKit kit = new OracleUpdateOrInsertKit(buildInsertSql(aTableFullName, aColumnNames) , aColumnTypes) ;
		kit.setFirstSqlOfTransaction(String.format(sSQL_NoLogging, aTableFullName)) ;
		return kit ;
	}
	
	@Override
	public UpdateOrInsertKit
			createUpdateKit(String aTableName, String[] aColumnNames, String[] aColumnTypes, int... aPKColIndexes)
	{
		OracleUpdateOrInsertKit kit = new OracleUpdateOrInsertKit(buildUpdateSql(aTableName, aColumnNames, aPKColIndexes) , aColumnTypes);
		kit.setFirstSqlOfTransaction(String.format(sSQL_NoLogging, aTableName)) ;
		return kit ;
	}
	
	public String buildUpdateSql(String aTableName , String[] aColumnNames , int... aPKColIndexes )
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
		for(int i=0 ; i<aPKColIndexes.length ; i++)
		{
			if(i>0)
				strBld.append(" AND ") ;
			strBld.append(aColumnNames[aPKColIndexes[i]]).append(" = ?") ;
		}
		return strBld.toString() ;
	}
	
	@Override
	public UpdateOrInsertKit
			createUpdateKit(String aTableName, String[] aColumnNames, int[] aColumnTypes, int... aPKColIndexes)
	{
		OracleUpdateOrInsertKit kit = new OracleUpdateOrInsertKit(buildUpdateSql(aTableName, aColumnNames, aPKColIndexes) , aColumnTypes);
		kit.setFirstSqlOfTransaction(String.format(sSQL_NoLogging, aTableName)) ;
		return kit ;
	}
	
	static String getCreateIndexLine(String aSchemaName , IndexSchema aIndexSchema)
	{
		StringBuilder strBld = new StringBuilder() ;
		strBld.append("CREATE") ;
		if(aIndexSchema.isUnique())
			strBld.append(" UNIQUE") ;
		strBld.append(" INDEX ").append(aIndexSchema.getName())
			.append(" ON ")
			.append(DBHelper.getTableFullName(aSchemaName, aIndexSchema.getTableName()))
			.append("(") ;
		final First first = new First() ;
		for(Entry<String , Boolean> entry : aIndexSchema.getColumnMap().entrySet())
		{
			first.checkAndNotFirstDo(()->strBld.append(" , "));
			strBld.append(entry.getKey()) ;
			if(entry.getValue() != null)
			{
				strBld.append(" ").append(entry.getValue()?"ASC" : "DESC") ;
			}
		}
		strBld.append(')') ;
		return strBld.toString() ;
	}
	
	/**
	 * 创建表格(CREATE TABLE)时用的列定义行
	 * @param aColSchema
	 * @param aConsSchema
	 * @return
	 */
	static String getColumnLine(ColumnSchema aColSchema , OracleConstraintSchema aConsSchema)
	{
		StringBuilder colBld = new StringBuilder(aColSchema.getColumnName()) ;
		
		if(sOneParamDataTypeSet.contains(aColSchema.getDataType()))
		{
			colBld.append(String.format(" %1$s(%2$d)", sDataType_VARCHAR2, aColSchema.getDataLength())) ;
		}
		else if(sTwoParamsDataTypeSet.contains(aColSchema.getColumnName()))
		{
			colBld.append(String.format(" %1$s(%2$d , %3$d)", sDataType_NUMBER 
					, aColSchema.getDataLength() , aColSchema.getDataPrecision())) ;
		}
		else
		{
			colBld.append(' ').append(aColSchema.getDataType()) ;
		}
		
		if(aConsSchema != null && aConsSchema.isEnabled())
		{
			if(sConstraintType_C.equals(aConsSchema.getType()))
			{
				Matcher matcher = sPtn_CC_NotNull.matcher(aConsSchema.getType()) ;
				if(matcher.matches())
				{
					colBld.append(String.format(" CONSTRAINT %s NOT NULL", aConsSchema.getName())) ;
				}
				else
					colBld.append(String.format(" CONSTRAINT %1$s CHECK (%2$s)", aConsSchema.getName()
							, aConsSchema.getCondition())) ;
			}
			else if(sConstraintType_U.equals(aConsSchema.getType()))
			{
				colBld.append(String.format(" CONSTRAINT %s UNIQUE", aConsSchema.getName())) ;
			}
			else if(sConstraintType_P.equals(aConsSchema.getType()))
			{
				colBld.append(String.format(" CONSTRAINT %s PRIMARY KEY", aConsSchema.getName())) ;
			}
		}
		
		return colBld.toString() ;
	}
	
	static class GetIndexSchemas implements EConsumer<ResultSet, SQLException>
	{
		static final String sNonUnique = "NONUNIQUE" ;
		static final String sAsc = "ASC" ;
		
		static final String sCol_TableName = "TABLE_NAME" ;
		static final String sCol_ColumnName = "COLUMN_NAME" ;
		static final String sCol_Descend = "DESCEND" ;
		static final String sCol_IndexName = "INDEX_NAME" ;
		
		Map<String , IndexSchema> mIndexMap = new LinkedHashMap<>() ;

		public GetIndexSchemas(List<Object[]> aIndexes)
		{
			 for(Object[] param : aIndexes)
			 {
				 IndexSchema indexSchema = new IndexSchema((String)param[0]) ;
				 indexSchema.setUnique(!sNonUnique.equalsIgnoreCase((String)param[1])) ;
				 mIndexMap.put(indexSchema.getName(), indexSchema) ;
			 }
		}
		
		public IndexSchema[] getIndexSchemas()
		{
			return mIndexMap.values().toArray(new IndexSchema[0]) ;
		}
		
		@Override
		public void accept(ResultSet aT) throws SQLException
		{
			String indexName = aT.getString(sCol_IndexName) ;
			IndexSchema indexSchema = mIndexMap.get(indexName) ;
			Assert.notNull(indexSchema, "在查询ALL_INDEXES表时，查到索引%s，而在查询ALL_IND_COLUMNS时却查不到" 
					, indexName) ;
			indexSchema.setTableName(aT.getString(sCol_TableName));
			indexSchema.addColumn(aT.getString(sCol_ColumnName) , sAsc.equalsIgnoreCase(aT.getString(sCol_Descend))) ;
		}
		
	}
	
	@Override
	public <X extends SQLException> void queryPage(Connection aConn,
			String aSql,
			int aPageSize,
			int aPage,
			EConsumer<ResultSetMetaData, X> aResultMetaConsumer,
			EConsumer<ResultSet, X> aResultSetConsumer,
			Wrapper<JSONObject> aResultMeta,
			boolean aCareTotalAmount,
			Object... aParamVals) throws SQLException
	{
		throw new UnsupportedOperationException("未实现针对Oracle数据库的分页查询方法") ;
	}
	
	@Override
	public List<String> getAllSchemaNames(Connection aConn) throws SQLException
	{
		throw new IllegalStateException("尚未实现") ;
	}
	
	@Override
	public void createDatabase(Connection aConn, String aSchemaName) throws SQLException
	{
		throw new IllegalStateException("尚未实现") ;
	}
	
	@Override
	public void grantSchemaPrivileges(Connection aConn, String aSchemaName, String aUser) throws SQLException
	{
		throw new IllegalStateException("尚未实现") ;
	}
	
	/**
	 * SQL最后需要加 ESCAPE '\'
	 */
	@Override
	public String escape(String aStr , char...aChs)
	{
		if(aStr == null || aStr.isEmpty())
			return aStr ;
		char[] chs = aStr.toCharArray() ;
		StringBuilder strBld = new StringBuilder() ;
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
				if(chs[i] == '\'')
					strBld.append('\'') ;
				else
					strBld.append('\\') ;
					
			}
			if(strBld != null)
				strBld.append(chs[i]) ;
		}
		return strBld == null?aStr:strBld.toString();
	}
}
