package team.sailboat.commons.fan.dtool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.struct.Wrapper;
import team.sailboat.commons.fan.text.XString;

public abstract class DBTool implements IDBTool 
{
	protected abstract String getSQL_IsTableExists() ;
	
	protected abstract String getSQL_IsTableExists_Owner() ;
	
	protected abstract String getSQL_GetTableNames() ;
	
	protected abstract String getSQL_GetFirst() ;
	
	protected abstract String getSQL_GetCurrentDateTime() ;	
	
	protected String getSQL_DropTable()
	{
		return "DROP TABLE %s CASCADE" ;
	}
	
	protected String getSQL_TruncateTable()
	{
		return "TRUNCATE TABLE %s" ;
	}
	
	@Override
	public void createIndex(Connection aConn, IndexSchema aIndexSchema) throws SQLException
	{
		throw new IllegalStateException(getClass().getSimpleName() + "不支持创建索引！") ;
 	}
	
	@Override
	public Date getDBCurrentDateTime(Statement aStm) throws SQLException
	{
		Wrapper<Date> wrapper = new Wrapper<>() ;
		DBHelper.executeQuery(aStm, getSQL_GetCurrentDateTime() , (rs)->{
			Object date = rs.getObject(1) ;
			if(date instanceof Date)
				wrapper.set((Date)date) ;
			else
				throw new IllegalStateException("未预料到的类型："+date.getClass().getName()) ;
			return false ;
		} , 1) ;
		return wrapper.get() ;
	}
	
	@Override
	public Date getDBCurrentDateTime(Connection aConn) throws SQLException
	{
		try(Statement stm = aConn.createStatement())
		{
			return getDBCurrentDateTime(stm) ;
		}
	}
	
	@Override
	public boolean isTableExists(Statement aStm, String aTableName, String aOwner) throws SQLException
	{
		if(XString.isEmpty(aOwner))
			aOwner = getSchemaName(aStm.getConnection()) ;
		String sql = String.format(getSQL_IsTableExists_Owner() , aOwner , aTableName) ;
		try(ResultSet rs = aStm.executeQuery(sql))
		{
			Assert.isTrue(rs.next() , "SQL语句：%s 没有查询出结果" , sql) ;
			return rs.getInt(1)>0 ;
		}
	}
	
	/**
	 * 取得用户表
	 * @param aConn
	 * @param aSchemaName
	 * @return
	 * @throws SQLException 
	 */
	@Override
	public String[] getTableNames(Connection aConn, String aSchemaName) throws SQLException
	{
		String sql = String.format(getSQL_GetTableNames() , aSchemaName==null?getSchemaName(aConn):aSchemaName) ;
		final List<String> tableNameList = new ArrayList<>() ;
		DBHelper.executeQuery(aConn, sql, (ResultSet aT)->tableNameList.add(aT.getString(1))) ;
		return tableNameList.toArray(JCommon.sEmptyStringArray) ;
	}
	
	@Override
	public boolean isTableEmpty(Statement aStm, String aOwner, String aTableName) throws SQLException
	{
		String sql = String.format(getSQL_GetFirst() , DBHelper.getTableFullName(aOwner, aTableName)) ;
		try(ResultSet rs = aStm.executeQuery(sql))
		{
			return !rs.next() ;
		}
	}
	
	@Override
	public String getSchemaName(Connection aConn) throws SQLException
	{
		return aConn.getMetaData().getUserName() ;
	}
	
	@Override
	public void createTables(Connection aConn, TableSchema... aTblSchemas) throws SQLException
	{
		try(Statement stm = aConn.createStatement())
		{
			createTables(stm, aTblSchemas); 
		}
	}
	
	@Override
	public void dropTables(Connection aConn , String... aTableFullNames) throws SQLException
	{
		try(Statement stm = aConn.createStatement())
		{
			dropTables(stm, aTableFullNames) ;
			aConn.commit();
		}
	}
	
	@Override
	public void dropTables(Statement aStm , String... aTableFullNames) throws SQLException
	{
		for(int i=0 ; i<aTableFullNames.length ; i++)
		{
			aStm.addBatch(String.format(getSQL_DropTable() , aTableFullNames[i])) ;
		}
		aStm.executeBatch() ;
	}
	
	@Override
	public int[] dropTableIndexes(Connection aConn, String aOwner, String aTableName, String... aIndexNames) throws SQLException
	{
		if(XC.isEmpty(aIndexNames))
			return new int[0] ;
		List<String> sqlList = XC.arrayList() ;
		for(String indexName : aIndexNames)
		{
			if("PRIMARY".equals(indexName))
			{
				sqlList.add(XString.msgFmt("ALTER TABLE {}{} DROP PRIMARY KEY" , XString.isEmpty(aOwner)?"":aOwner+"."
						, aTableName)) ;
			}
			else
			{
				sqlList.add(XString.msgFmt("ALTER TABLE {}{} DROP INDEX {}" , XString.isEmpty(aOwner)?"":aOwner+"."
						, aTableName , indexName)) ;
			}
		}
		try(Statement stm = aConn.createStatement())
		{
			for(String sql : sqlList)
			{
				stm.addBatch(sql);
			}
			return stm.executeBatch() ;
		}
	}
	
	@Override
	public void dropDatabase(Connection aConn, String aDatabaseName) throws SQLException
	{
		try(Statement stm = aConn.createStatement())
		{
			stm.execute("DROP DATABASE " + aDatabaseName) ;
		}
	}
	
	@Override
	public void truncateTables(Statement aStm , String... aTableFullNames) throws SQLException
	{
		for(int i=0 ; i<aTableFullNames.length ; i++)
		{
			aStm.addBatch(String.format(getSQL_TruncateTable() , aTableFullNames[i])) ;
		}
		aStm.executeBatch() ;
	}
	
	@Override
	public void renameTable(Statement aStm, String aOldName, String aNewName) throws SQLException
	{
		aStm.execute(XString.msgFmt("ALTER TABLE {} RENAME TO {}" , aOldName , aNewName)) ;
	}
	
	@Override
	public void renameTable(Connection aConn, String aOldName, String aNewName) throws SQLException
	{
		try(Statement stm = aConn.createStatement())
		{
			renameTable(stm , aOldName, aNewName) ;
			aConn.commit();
		}
	}
	
	@Override
	public UpdateOrInsertKit createUpdateOrInsertKit(String aTableName, String[] aColumnNames, int[] aColumnTypes
			, int... aPKColIndexes)
	{
		return new UpdateOrInsertKit(buildUpdateOrInsertKitSql(aTableName, aColumnNames , aPKColIndexes) , aColumnTypes) ;
	}
	
	@Override
	public UpdateOrInsertKit createUpdateOrInsertKit(String aTableName,
			String[] aColumnNames,
			String[] aColumnTypes,
			int... aPKColIndexes)
	{
		return new UpdateOrInsertKit(buildUpdateOrInsertKitSql(aTableName, aColumnNames , aPKColIndexes) , aColumnTypes) ;
	}
	
	
	@Override
	public UpdateOrInsertKit createInsertKit(String aTableFullName, String[] aColumnNames, String[] aColumnTypes)
	{
		return new UpdateOrInsertKit(buildInsertSql(aTableFullName, aColumnNames) , aColumnTypes) ;
	}
	
	@Override
	public UpdateOrInsertKit createInsertOrIgnoreKit(String aTableName, String[] aColumnNames, String[] aColumnTypes, int... aPKColIndexes)
	{
		return new UpdateOrInsertKit(buildInsertOrIgnoreSql(aTableName, aColumnNames, aPKColIndexes), aColumnTypes) ;
	}
}
