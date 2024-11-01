package team.sailboat.commons.fan.dtool.pg;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.First;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.struct.Bool;
import team.sailboat.commons.fan.struct.Wrapper;
import team.sailboat.commons.fan.text.XString;

public class PgTool extends DBTool implements PgConst
{
//	static final String sSQL_IsTableExists = "SELECT COUNT(*) FROM  information_schema.tables WHERE tablename='%s'" ;
	static final String sSQL_IsTableExists_Onwer = "SELECT COUNT(*) FROM pg_catalog.pg_tables WHERE schemaname='%1$s' AND tablename='%2$s'" ;
	static final String sSQL_GetTableNames = "SELECT * FROM  information_schema.tables where TABLE_SCHEMA='%s'" ;
	static final String sSQL_GetFirst = "SELECT * FROM %s LIMIT 1" ; 
	static final String sSQL_GetTableDetails = "SELECT TABLE_NAME , TABLE_COMMENT , ENGINE , TABLE_COLLATION FROM information_schema.tables where TABLE_SCHEMA='%s'" ;
	static final String sSQL_GetTableDetail = "SELECT relname AS TABLE_NAME , CAST(obj_description(relfilenode , 'pg_class') AS VARCHAR) AS TABLE_COMMENT , NULL AS ENGINE , NULL AS TABLE_COLLATION FROM pg_catalog.pg_class where relkind = 'r' AND relname !='%1$s' AND relname='%2$s'" ;
	static final String sSQL_GetIndexCols = "SELECT"
			+ "    n.nspname as schema_name,"
			+ "    t.relname as table_name,"
			+ "    i.relname as index_name,"
			+ "    a.attname as column_name"
			+ " FROM"
			+ "    pg_catalog.pg_class t,"
			+ "    pg_catalog.pg_class i,"
			+ "    pg_catalog.pg_index ix,"
			+ "    pg_catalog.pg_attribute a,"
			+ "    pg_catalog.pg_namespace n"
			+ " WHERE"
			+ "    t.oid = ix.indrelid"
			+ "    AND i.oid = ix.indexrelid"
			+ "    AND a.attrelid = t.oid"
			+ "    AND a.attnum = ANY(ix.indkey)"
			+ "    AND t.relkind = 'r'"
			+ "    AND n.nspname = '%1$s'"
			+ "    AND t.relname = '%2$s'"
			+ " ORDER BY"
			+ "    n.nspname,"
			+ "    t.relname,"
			+ "    i.relname,"
			+ "    array_position(ix.indkey , a.attnum)"
			;
	
	static final String sSQL_GetPrimaryKey = "select * from information_schema.table_constraints tc , information_schema.key_column_usage kcu "
			+ " WHERE tc.constraint_type ='PRIMARY KEY'"
			+ "	AND tc.constraint_catalog = kcu.constraint_catalog"
			+ "	AND tc.constraint_schema = kcu.constraint_schema"
			+ "	AND tc.constraint_name  = kcu.constraint_name"
			+ "	AND kcu.table_schema = ?"
			+ "	AND kcu.table_name = ?"
			+ " order by kcu.ordinal_position ASC" ;
	
	DBType mDBType ;
	
	/**
	 * 数据库的字段类型名 到标准类型名的映射
	 */
	static Map<String, String> sDataTypeMap = XC.hashMap(
			sDataType_BIGINT , XClassUtil.sCSN_Long
			,sDataType_TEXT , XClassUtil.sCSN_String
			,sDataType_TIMESTAMP , XClassUtil.sCSN_DateTime
			,sDataType_INTEGER , XClassUtil.sCSN_Integer) ;
	
	
	
	
	public PgTool()
	{
		mDBType = DBType.PostgreSQL ;
	}
	
	@Override
	protected String getSQL_DropTable()
	{
		return "DROP TABLE IF EXISTS %s" ;
	}
	
	@Override
	public void createIndex(Connection aConn, IndexSchema aIndexSchema) throws SQLException
	{
		StringBuilder strBld = new StringBuilder() ;
		for(Entry<String , Boolean> entry : aIndexSchema.getColumnMap().entrySet())
		{
			if(strBld.length() > 0)
				strBld.append(" , ") ;
			String colName = entry.getKey() ;
			strBld.append(sKeyWords.contains(colName)?("\""+colName+"\""):colName) ;
			if(Boolean.FALSE.equals(entry.getValue()))
				strBld.append(" DESC") ;
		}
		
		String sql = XString.msgFmt("CREATE {} INDEX {} USING BTREE ON {} ({})"
				, aIndexSchema.isUnique()?"UNIQUE":"" 
				, aIndexSchema.getName()
				, aIndexSchema.getTableName()
				, strBld.toString());
		
		DBHelper.execute(aConn , true , sql) ;
	}
	
	@Override
	public String getCSN(String aRawTypeName)
	{
		return aRawTypeName == null?null:sDataTypeMap.get(aRawTypeName.toUpperCase());
	}
	
	@Override
	public String getSchemaName(Connection aConn) throws SQLException
	{
		return aConn.getSchema() ;
	}
	
	@Override
	public String getDBSchemaName(ResultSetMetaData aRsmd, int aIndex) throws SQLException
	{
		return aRsmd.getSchemaName(aIndex) ;
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
		return new PgTableSchemaBuilder() ;
	}
	
	@Override
	protected String getSQL_IsTableExists()
	{
		throw new IllegalStateException("未实现！") ;
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
		StringBuilder strBld = new StringBuilder("INSERT INTO ")
				.append(aTableName).append(" (")
				.append(XString.toString(" , ", aColumnNames))
				.append(") VALUES(") ;
		for(int i=0 ; i<aColumnNames.length ; i++)
		{
			if(i>0)
				strBld.append(" , ") ;
			strBld.append("?") ;
		}
		strBld.append(") ON CONFLICT(") ;
		for(int i=0 ; i<aPKColIndexes.length ; i++)
		{
			if(i > 0)
				strBld.append(" , ") ;
			strBld.append(aColumnNames[aPKColIndexes[i]]) ;
		}
		strBld.append(")") ;
		if(aColumnNames.length == aPKColIndexes.length)
			strBld.append(" DO NOTHING") ;
		else
		{
			strBld.append(" DO UPDATE SET ") ;
			boolean first = true ;
			for(int i=0 ; i<aColumnNames.length ; i++)
			{
				if(!XC.contains(aPKColIndexes , i))
				{
					if(first)
						first = false ;
					else
						strBld.append(" , ") ;
					strBld.append(aColumnNames[i]).append("= EXCLUDED.").append(aColumnNames[i]) ;
				}
			}
		}
		return strBld.toString() ;
	}
	
	@Override
	public String buildInsertOrIgnoreSql(String aTableName, String[] aColumnNames, int[] aPKColIndexes)
	{
		StringBuilder strBld = new StringBuilder("INSERT INTO ")
				.append(aTableName).append(" (")
				.append(XString.toString(" , ", aColumnNames))
				.append(") VALUES(") ;
		for(int i=0 ; i<aColumnNames.length ; i++)
		{
			if(i>0)
				strBld.append(" , ") ;
			strBld.append("?") ;
		}
		strBld.append(") ON CONFLICT(") ;
		for(int i=0 ; i<aPKColIndexes.length ; i++)
		{
			if(i > 0)
				strBld.append(" , ") ;
			strBld.append(aColumnNames[aPKColIndexes[i]]) ;
		}
		strBld.append(") DO NOTHING") ;
		return strBld.toString() ;
	}
	
	@Override
	public TableSchema[] getTableSchemas(Connection aConn, String aSchemaName) throws SQLException
	{
		if(aSchemaName == null)
			aSchemaName = aConn.getMetaData().getUserName() ;
		String sql = String.format(sSQL_GetTableDetails, aSchemaName) ;
		final List<TableSchema> tblSchemaList = new ArrayList<>() ;
		final String schemaName = aSchemaName ; 
		DBHelper.executeQuery(aConn, sql, (ResultSet rs)->tblSchemaList.add(new PgTableSchema(schemaName , rs.getString("TABLE_NAME")
				, rs.getString("TABLE_COMMENT")
				, rs.getString("TABLE_COLLATION")))) ;
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
		if(XString.isEmpty(aSchemaName))
			aSchemaName = aConn.getSchema() ;
		String sql = String.format(sSQL_GetTableDetail, aSchemaName , aTableName) ;
		final Wrapper<TableSchema> wrapper = new Wrapper<>() ;
		final String schemaName = aSchemaName ;
		DBHelper.executeQuery(aConn, sql, (ResultSet rs)->wrapper.set(new PgTableSchema(schemaName , rs.getString("TABLE_NAME")
				, rs.getString("TABLE_COMMENT")
				, rs.getString("TABLE_COLLATION")))) ;
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
		String sqlFmt = "SELECT tbl.table_name , d.description AS table_comment FROM information_schema.tables AS tbl"
				+ " JOIN pg_catalog.pg_class c ON c.relname = tbl.table_name"
				+ " LEFT JOIN pg_description d ON c.oid = d.objoid and d.objsubid = 0"
				+ " LEFT JOIN pg_namespace n on n.oid = c.relnamespace AND n.nspname = tbl.table_schema"
				+ " WHERE c.relkind = 'r'  AND tbl.table_schema = '{}'  AND tbl.table_type = 'BASE TABLE' and n.nspname = tbl.table_schema"
				+ "{} ORDER BY tbl.table_name" ;
		
		if(XString.isEmpty(aSchemaName))
			aSchemaName = aConn.getSchema() ;
		String tableFilterSql = "" ;
		if(XC.isNotEmpty(aTableNames))
		{
			tableFilterSql = " AND tbl.table_name IN (" + XString.toString(",", "'" ,aTableNames)+")" ;
		}
		String sql = XString.msgFmt(sqlFmt , aSchemaName , tableFilterSql) ;
		LinkedHashMap<String, String> map = XC.linkedHashMap() ;
		DBHelper.executeQuery(aConn, sql, (ResultSet rs)->map.put(rs.getString(1), rs.getString(2))) ;
		return map ;
	}
	
	@Override
	public JSONArray getTablesInfos(Connection aConn , String aDBName) throws SQLException
	{
		final String sqlFmt = "SELECT table_name , c.oid AS object_id , r.rolname AS owner , n.spcname as tablespace"
				+ " , reltuples AS estimate_row_count , relispartition AS partitions"
				+ " , CAST(obj_description(relfilenode , 'pg_class') AS VARCHAR) as table_comment"
				+ " , relpartbound AS partition_by , reloptions AS extra_options"
				+ " FROM information_schema.tables AS tbl"
				+ " JOIN pg_catalog.pg_class c ON c.relname = tbl.table_name"
				+ " LEFT JOIN pg_roles r ON c.relowner  = r.oid"
				+ " LEFT JOIN pg_tablespace n ON c.reltablespace = n.oid"
				+ " WHERE c.relkind = 'r'"		// 只选择表  
				+ "   AND tbl.table_schema = ?"
				+ "   AND tbl.table_type = 'BASE TABLE'" // 只选择基础表  
				+ " ORDER BY tbl.table_name" ;
		
		JSONArray ja = new JSONArray() ;
		Bool hasDefaultSpace = new Bool(false) ;
		DBHelper.executeQuery(aConn , sqlFmt, rs->{
			String tablespace = rs.getString(4) ;
			if(tablespace == null)
				hasDefaultSpace.set(true) ;
			Array partitions = rs.getArray(6) ;
			ja.put(new JSONObject().put("tableName", rs.getString(1))
					.put("objectId" , rs.getString(2))
					.put("owner" , rs.getString(3))
					.put("tablespace", tablespace)
					.put("estimateRowCount" , rs.getLong(5))
					.put("partitions" , new JSONArray(partitions.getArray()))
					.put("tableComment", rs.getString(7))
					.put("partitionBy" , rs.getString(8))
					.put("extraOptions" , rs.getString(9))
					) ;
			return true ;
		} , 1000 , aDBName) ;
		if(hasDefaultSpace.get())
		{
			Wrapper<String> wrapper = new Wrapper<String>() ;
			DBHelper.executeQuery(aConn , "select t.spcname from pg_database d left join pg_tablespace t on d.dattablespace = t.oid where datname = ?", rs->{
				wrapper.set(rs.getString(1)) ;
				return false ;
			} , 1 , aConn.getSchema()) ;
			ja.forEachJSONObject(jo->{
				if(jo.optString("tablespace" , null) == null)
					jo.put("tablespace" , wrapper.get()) ;
			}) ;
		}
		return ja ;
	}
	
	
	@Override
	public ColumnSchema[] getColumnSchemas(Connection aConn, String aSchemaName, String aTableName) throws SQLException
	{
		final String sqlFmt = "SELECT a.attname AS column_name , format_type(a.atttypid,a.atttypmod) AS data_type , col_description(a.attrelid,a.attnum) AS column_comment"
				+ " FROM pg_class AS c , pg_attribute AS a , pg_namespace n"
				+ " WHERE a.attrelid = c.oid AND a.attnum > 0 and n.oid = c.relnamespace AND a.atttypid != 0 "
				+ "    AND n.nspname = ? AND c.relname = ?";
		
		if(aSchemaName == null)
			aSchemaName = getSchemaName(aConn) ;
		GetColumnSchemas action = new GetColumnSchemas() ;
		DBHelper.executeQuery(aConn, sqlFmt, action
				, 1000 , aSchemaName , aTableName) ;
		return action.mColSchemaList.toArray(new ColumnSchema[0]) ;
	}
	
	@Override
	public ConstraintSchema getPrimaryKey(Connection aConn, String aSchemaName, String aTableName) throws SQLException
	{
		try(PreparedStatement pstm = aConn.prepareStatement(sSQL_GetPrimaryKey))
		{
			pstm.setString(1, XString.isEmpty(aSchemaName)? aConn.getSchema() : aSchemaName) ;
			pstm.setString(2, aTableName);
			try(ResultSet rs = pstm.executeQuery())
			{
				PgConstraintSchema cst = null ;
				List<String> colNames = null ;
				while(rs.next())
				{
					if(cst == null)
					{
						cst = new PgConstraintSchema() ;
						cst.setName(rs.getString("constraint_name")) ;
						cst.setOwner(aTableName) ;
						colNames = XC.arrayList() ;
					}
					colNames.add(rs.getString("column_name")) ;
				}
				if(cst != null)
					cst.setColumnNames(colNames.toArray(JCommon.sEmptyStringArray)) ;
				return cst ;
			}
		}
	}
	
	@Override
	public IndexSchema[] getIndexSchemas(Connection aConn, String aSchemaName, String aTableName) throws SQLException
	{
		try(Statement stm = aConn.createStatement())
		{
			String sql = String.format(sSQL_GetIndexCols , XString.isEmpty(aSchemaName)?getSchemaName(aConn):aSchemaName , aTableName) ;
			GetIndexSchemas action = new GetIndexSchemas() ;
			DBHelper.executeQuery(stm, sql, action);
			return action.getIndexSchemas() ;
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
				for(String sql : getCreateTableSql(schema))
				{
					aStm.addBatch(sql) ;
				}
			}
			aStm.executeBatch() ;
			aStm.getConnection().commit() ;
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
	public String getCreateTableSql(Connection aConn, String aDBName, String aTableName) throws SQLException
	{
		/**
		 * PG库没有提供直接获取建表语句的SQL
		 */
		return XString.toString(";\n", getCreateTableSql(getTableSchema(aConn, aDBName, aTableName))) ;
	}

	@Override
	public void createTable(Statement aStm, TableSchema aTblSchema) throws SQLException
	{
		List<String> sqlList = getCreateTableSql(aTblSchema) ;
		//创建表
		try
		{
			for(String sql : sqlList)
				aStm.addBatch(sql) ;
			aStm.executeBatch() ;
			aStm.getConnection().commit() ;
		}
		catch(SQLException e)
		{
			throw new SQLException(XString.splice("SQL语句：" , XString.toString(" ;\n", sqlList)), e) ;
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
	
	protected List<String> getCreateTableSql(TableSchema aTblSchema)
	{
		List<String> sqlList = XC.arrayList() ;
		StringBuilder createTblSqlBld = new StringBuilder("CREATE TABLE \"")
			.append(aTblSchema.getFullName()).append("\" (") ;
		final First first = new First() ;
		for(ColumnSchema colSchema :aTblSchema.getColumnSchemas())
		{
			first.checkAndNotFirstDo(()->createTblSqlBld.append(" , ")) ;
			createTblSqlBld.append(colSchema.getSqlText()) ;
		}
		
		//处理主键、且是单列约束
		List<ConstraintSchema> pkList = aTblSchema.getPrimaryKeyConstraintSchema(true) ;
		if(XC.isNotEmpty(pkList))
		{
			for(ConstraintSchema cons : pkList)
			{
				createTblSqlBld.append(" , ").append(cons.getSqlText()) ;
			}
		}
		//处理多参数约束
		List<ConstraintSchema> consList = aTblSchema.getMultiColsConstraintSchema() ;
		if(!consList.isEmpty())
		{
			for(ConstraintSchema cons : consList)
			{
				createTblSqlBld.append(" , ").append(cons.getSqlText()) ;
			}
		}
		
		createTblSqlBld.append(")") ;
		sqlList.add(createTblSqlBld.toString()) ;
		// 注释
		String tblFullName = aTblSchema.getFullName() ;
		for(ColumnSchema colSchema :aTblSchema.getColumnSchemas())
		{
			if(XString.isNotEmpty(colSchema.getComment()))
			{
				sqlList.add(XString.msgFmt("COMMENT ON COLUMN {}.{} IS '{}'" 
						, tblFullName , colSchema.getColumnName() , colSchema.getComment())) ;
			}
		}
		if(XString.isNotEmpty(aTblSchema.getComment()))
		{
			sqlList.add(XString.msgFmt("COMMENT ON TABLE {} IS '{}'" 
					, tblFullName , aTblSchema.getComment())) ;
		}
		
		//索引
		List<IndexSchema> indexList = aTblSchema.getIndexSchemas() ;
		if(XC.isNotEmpty(indexList))
		{
			for(IndexSchema indexSchema : indexList)
			{
				if(XString.isNotEmpty(indexSchema.getFeatureSqlSeg()))
				{
					sqlList.add(XString.msgFmt("CREATE INDEX {} ON {} {}"
							, indexSchema.getName() 
							, indexSchema.getTableName() 
							, indexSchema.getFeatureSqlSeg())) ;
				}
				else
				{
					sqlList.add(XString.msgFmt("CREATE {}INDEX {} ON {} ({})"
							, indexSchema.isUnique()?"UNIQUE ":""
							, indexSchema.getName() 
							, indexSchema.getTableName() 
							, XString.toString(",", indexSchema.getColumnNames()))) ;
				}
			}
		}
		
		if(aTblSchema != null)
		{
			PgTableSchema tblSchema = (PgTableSchema)aTblSchema ;
			if(XString.isNotEmpty(tblSchema.getCharacterSet()))
				createTblSqlBld.append(" CHARACTER SET = ").append(tblSchema.getCharacterSet()) ;
			if(XString.isNotEmpty(tblSchema.getTableCollation()))
				createTblSqlBld.append(" COLLATE = ").append(tblSchema.getTableCollation()) ;
			if(XString.isNotEmpty(tblSchema.getRowFormat()))
				createTblSqlBld.append(" ROW_FORMAT=").append(tblSchema.getRowFormat()) ;
		}
		return sqlList ;
	}
	
	@Override
	public void alterTableName(Connection aConn, String aSchemaName, String aTableName, String aNewTableName)
			throws SQLException
	{
		try(Statement stm = aConn.createStatement())
		{
			JSqlBuilder sqlBld = JSqlBuilder.one("ALTER TABLE ")
				.checkAppend(XString.isNotEmpty(aSchemaName) , "${F0}.", aSchemaName)
				.checkAppend(true , "${F1} RENAME TO ${F2}", aTableName , aNewTableName)
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
		return new UpdateOrInsertKit(buildUpdateSql(aTableName, aColumnNames, aPKColIndexes) , aPKColIndexes, aColumnTypes);
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
		return new UpdateOrInsertKit(buildUpdateSql(aTableName, aColumnNames, aPKColIndexes), aColumnTypes);
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
		
		final int offset = aPage*aPageSize ;
		String sql = new StringBuilder(aSql).append(" OFFSET ").append(offset).toString() ;
		
		try(PreparedStatement pstm = aConn.prepareStatement(sql , ResultSet.TYPE_SCROLL_INSENSITIVE
				, ResultSet.CONCUR_READ_ONLY))
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
				if(aResultMeta != null)
				{
					if(aCareTotalAmount)
					{
						rs.last() ;
						int totalAmount = rs.getRow()+offset ;
						aResultMeta.get().put("totalAmount", totalAmount)
							.put("pageAmount" , Math.ceil(totalAmount*1d/aPageSize)) ;
					}
					else
					{
						aResultMeta.get().put("hasMore" , rs.next()) ;
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
	
	
	/**
	 * 
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
