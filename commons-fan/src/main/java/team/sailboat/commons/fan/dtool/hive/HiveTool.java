package team.sailboat.commons.fan.dtool.hive;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.PooledConnection;

import team.sailboat.commons.fan.collection.XC;
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

public class HiveTool extends DBTool implements HiveConst
{
	static final String sSQL_IsTableExists = "SELECT COUNT(*) FROM  information_schema.tables WHERE TABLE_NAME='%s'" ;
	static final String sSQL_IsTableExists_Onwer = "SELECT COUNT(*) FROM  information_schema.tables WHERE TABLE_SCHEMA='%1$s' AND TABLE_NAME='%2$s'" ;
	static final String sSQL_GetTableNames = "SELECT * FROM  information_schema.tables where TABLE_SCHEMA='%s'" ;
	static final String sSQL_GetFirst = "SELECT * FROM %s LIMIT 1" ; 
	static final String sSQL_GetTableDetails = "SELECT TABLE_NAME , TABLE_COMMENT , ENGINE , TABLE_COLLATION FROM information_schema.tables where TABLE_SCHEMA='%s'" ;
	static final String sSQL_GetTableDetail = "DESC FORMATTED %s" ;
	static final String sSQL_GetTablesComment = "SELECT TABLE_NAME , TABLE_COMMENT FROM information_schema.tables WHERE TABLE_SCHEMA='%1$s' AND TABLE_NAME IN (%2$s)" ;
	static final String sSQL_GetColumnSchemas = "SELECT * FROM information_schema.columns WHERE TABLE_SCHEMA='%1$s' AND TABLE_NAME='%2$s'" ;
	static final String sSQL_GetIndexCols = "SELECT * FROM information_schema.STATISTICS where TABLE_SCHEMA='%1$s' and TABLE_NAME='%2$s' ORDER BY SEQ_IN_INDEX ASC" ;
	
	/**
	 * 数据库的字段类型名 到标准类型名的映射
	 */
	static Map<String, String> sDataTypeMap = XC.hashMap(
			sDataType_BIGINT , XClassUtil.sCSN_Long
			,sDataType_INT , XClassUtil.sCSN_Integer
			,sDataType_FLOAT , XClassUtil.sCSN_Float
			,sDataType_DOUBLE , XClassUtil.sCSN_Double
			,sDataType_STRING , XClassUtil.sCSN_String
			,sDataType_TIMESTAMP , XClassUtil.sCSN_DateTime
			,sDataType_BOOLEAN , XClassUtil.sCSN_Bool) ;
	
	
	@Override
	public String getCSN(String aRawTypeName)
	{
		return aRawTypeName == null?null:sDataTypeMap.get(aRawTypeName.toUpperCase());
	}
	
	@Override
	public String getSchemaName(Connection aConn) throws SQLException
	{
		return aConn.getCatalog() ;
	}
	
	@Override
	public String getCreateTableSql(Connection aConn, String aDBName, String aTableName) throws SQLException
	{
		try(Statement stm = aConn.createStatement())
		{
			ResultSet rs = stm.executeQuery("SHOW CREATE TABLE "+(XString.isEmpty(aDBName)?"":aDBName+".")+"`"
					+aTableName+"`") ;
			// 一个SQL语句一行被拆分成了结果集中的一行，所以一条建表语句对应多条记录
			StringBuilder sqlBld = new StringBuilder() ;
			while(rs.next())
			{
				if(sqlBld.length() > 0)
					sqlBld.append("\n") ;
				sqlBld.append(rs.getString(1)) ;
			}
			return sqlBld.toString() ;
		}
	}
	
	@Override
	public String getDBSchemaName(ResultSetMetaData aRsmd, int aIndex) throws SQLException
	{
		// Hive不支持从ResultSetMetaData中获取Catalog或者Schema
		return null ;
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
		return DBType.Hive ;
	}
	
	protected String getSQL_DropTable()
	{
		return "DROP TABLE IF EXISTS %s" ;
	}
	
	@Override
	public void dropTables(Statement aStm , String... aTableFullNames) throws SQLException
	{
		for(int i=0 ; i<aTableFullNames.length ; i++)
		{
			aStm.execute(String.format(getSQL_DropTable() , aTableFullNames[i])) ;
		}
	}
	
	@Override
	public TableSchemaBuilder builder_tableSchema()
	{
		return null ;
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
		StringBuilder strBld = new StringBuilder() ;
		if(aColumnNames.length == aPKColIndexes.length)
			strBld.append("INSERT IGNORE INTO ") ;
		else
			strBld.append("INSERT INTO ") ; 
		strBld.append(aTableName).append("(")
				.append(XString.toString(" , ", aColumnNames))
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
				strBld.append(aColumnNames[i]).append("=VALUES(").append(aColumnNames[i]).append(")") ;
			}
		}
		return strBld.toString() ;
	}
	
	@Override
	public TableSchema[] getTableSchemas(Connection aConn, String aSchemaName) throws SQLException
	{
		throw new IllegalStateException("未实现") ;
	}
	
	@Override
	public TableSchema getTableSchema(Connection aConn, String aSchemaName, String aTableName) throws SQLException
	{
		if(XString.isNotEmpty(aSchemaName))
			aSchemaName = aSchemaName.trim().toLowerCase() ;
		Connection t_conn = aConn ;
		if(t_conn instanceof PooledConnection)
			t_conn = ((PooledConnection)aConn).getConnection() ;
		String oldSchema = t_conn.getSchema() ;
		boolean changedSchema = false ;
		if(XString.isNotEmpty(aSchemaName) && JCommon.unequals(oldSchema , aSchemaName))
		{
			t_conn.setSchema(aSchemaName) ;
			changedSchema = true ;
		}
		try
		{
			String sql = String.format(sSQL_GetTableDetail, XString.isNotEmpty(aSchemaName)?aSchemaName+".`"+aTableName+"`"
					: "`"+aTableName+"`") ;
			final Wrapper<TableSchema> wrapper = new Wrapper<>() ;
			query(aConn, sql, (rs)->{
				TableSchema schema = new TableSchema() ;
				int flag = 0 ;
				bp_1656:while(rs.next())
				{
					String col0 = rs.getString(1) ;
					switch(flag)
					{
					case 0:
						if("# col_name".equals(col0))
							// 列名开始
							flag = 1 ;
						break ;
					case 1:
						// 列名
						if(XString.isEmpty(col0))
						{
							// 列结束
							flag = 2 ;
						}
						else
							schema.addColumnSchema(new HiveColumnSchema(col0 , rs.getString(2) , rs.getString(3))) ;
						break ;
					case 2:
						if("# Detailed Table Infomation".equals(col0))
							flag = 3 ;
						break ;
					case 3:
						if("Database:".equals(col0))
							schema.setOwner(rs.getString(2)) ;
						else if("Table Parameters:".equals(col0))
							flag = 4;
						break ;
					case 4:
						if("comment".equals(rs.getString(2)))
							schema.setComment(rs.getString(3)) ;
						else if("# Storage Infomation".equals(col0))
							flag = 5;
						break ;
					case 5:
					default:
						break bp_1656;
					}
				}
				wrapper.set(schema) ;
				
			} , 1000) ;
			if(!wrapper.isNull())
			{
				wrapper.get().setName(aTableName) ;
			}
			return wrapper.get() ;
		}
		finally
		{
			if(changedSchema)
				t_conn.setSchema(oldSchema) ;
		}
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
	public HiveColumnSchema[] getColumnSchemas(Connection aConn, String aSchemaName, String aTableName) throws SQLException
	{
		if(XString.isNotEmpty(aSchemaName))
			aSchemaName = aSchemaName.trim().toLowerCase() ;
		Connection t_conn = aConn ;
		if(t_conn instanceof PooledConnection)
			t_conn = ((PooledConnection)aConn).getConnection() ;
		String oldSchema = t_conn.getSchema() ;
		boolean changedSchema = false ;
		if(XString.isNotEmpty(aSchemaName) && JCommon.unequals(oldSchema , aSchemaName))
		{
			t_conn.setSchema(aSchemaName) ;
			changedSchema = true ;
		}
		try
		{
			String sql = String.format(sSQL_GetTableDetail, XString.isNotEmpty(aSchemaName)?aSchemaName+".`"+aTableName+"`"
					: "`"+aTableName+"`") ;
			List<HiveColumnSchema> colSchemaList = XC.arrayList() ;
			query(aConn, sql, (rs)->{
				int flag = 0 ;
				bp_1656:while(rs.next())
				{
					String col0 = rs.getString(1) ;
					switch(flag)
					{
					case 0:
						if("# col_name".equals(col0))
							// 列名开始
							flag = 1 ;
						break ;
					case 1:
						// 列名
						if(XString.isEmpty(col0))
						{
							// 列结束
							flag = 2 ;
						}
						else
							colSchemaList.add(new HiveColumnSchema(col0 , rs.getString(2) , rs.getString(3))) ;
						break ;
					case 2:
						if("# Detailed Table Infomation".equals(col0))
							flag = 3 ;
						break ;
					case 3:
						if("Table Parameters:".equals(col0))
							flag = 4;
						break ;
					case 4:
						if("# Storage Infomation".equals(col0))
							flag = 5;
						break ;
					case 5:
					default:
						break bp_1656;
					}
				}
				
			} , 1000) ;
			return colSchemaList.toArray(new HiveColumnSchema[0]) ;
		}
		finally
		{
			if(changedSchema)
				t_conn.setSchema(oldSchema) ;
		}
	}
	
	@Override
	public IndexSchema[] getIndexSchemas(Connection aConn, String aSchemaName, String aTableName) throws SQLException
	{
		throw new IllegalStateException("未实现") ;
	}
	
	@Override
	public ConstraintSchema getPrimaryKey(Connection aConn, String aSchemaName, String aTableName) throws SQLException
	{
		throw new IllegalStateException("未实现!") ;
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
		throw new IllegalStateException("未实现") ;
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
		return null;
	}
	
	@Override
	public UpdateOrInsertKit
			createUpdateKit(String aTableName, String[] aColumnNames, String[] aColumnTypes, int... aPKColIndexes)
	{
		return new UpdateOrInsertKit(buildUpdateSql(aTableName, aColumnNames, aPKColIndexes), aColumnTypes);
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
	public String buildInsertOrIgnoreSql(String aTableName, String[] aColumnNames, int[] aPKColIndexes)
	{
		throw new IllegalStateException("未实现") ;
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
		String sql = new StringBuilder(aSql)
				.append(" LIMIT ").append(aPage*aPageSize).append(" , ").append(aPageSize+1).toString() ;
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
				if(aResultMeta != null)
				{
					aResultMeta.get().put("hasMore" , rs.next()) ;
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
		DBHelper.execute(aConn, "CREATE DATABASE IF NOT EXISTS " + aSchemaName) ;
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
	
	@Override
	public boolean isRSMDSupportColumnSourceTable()
	{
		return false ;
	}
}
