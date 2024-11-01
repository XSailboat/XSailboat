package team.sailboat.commons.ms.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.sql.DataSource;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dtool.ColumnInfo;
import team.sailboat.commons.fan.dtool.ColumnSchema;
import team.sailboat.commons.fan.dtool.DBHelper;
import team.sailboat.commons.fan.dtool.IDBTool;
import team.sailboat.commons.fan.dtool.RS2JSONObject;
import team.sailboat.commons.fan.dtool.TableSchema;
import team.sailboat.commons.fan.infc.ESupplier;
import team.sailboat.commons.fan.infc.IterateOpCode;
import team.sailboat.commons.fan.infc.IteratorPredicate;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONException;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.struct.Wrapper;
import team.sailboat.commons.fan.text.XString;

public class DBKit
{
	
	public static JSONArray queryAll(DataSource aDS , String aSql , Object...aParamVals) throws SQLException
	{
		return queryAll_3(aDS, aSql, null , null , null , null , aParamVals) ;
	}
	
	public static JSONArray queryAll_2(DataSource aDS , String aSql 
			, List<ColumnInfo> aColInfos 
			, Object...aParamVals) throws SQLException
	{
		return queryAll_3(aDS, aSql, aColInfos , null, null , null , aParamVals) ;
	}
	
	public static JSONArray queryAll_3(DataSource aDS , String aSql 
			, List<ColumnInfo> aColInfos
			, IteratorPredicate<ResultSet> aPred , Consumer<JSONObject> aObjHandler , Map<String , String> aColNameCvtMap
			, Object...aParamVals) throws SQLException
	{
		try(Connection conn = aDS.getConnection())
		{
			IDBTool dbTool = DBHelper.getDBTool(conn) ;
			JSONArray result = new JSONArray() ;
			
			if(XString.count(aSql, '?', 0) < aParamVals.length && XC.isNotEmpty(aParamVals))
			{
				Collection<Object> list = null ;
				for(int i=0 ; i<aParamVals.length ; i++)
				{
					if(aParamVals[i] == null)
					{
						list = XC.extract(aParamVals, (obj)->obj!=null) ;
						break ;
					}
				}
				if(list != null)
 					aParamVals = list.toArray() ;
			}
			dbTool.query(conn, aSql, (rs)->{
				if(aColInfos != null)
				{
					dbTool.getColumnInfos(rs.getMetaData() , aColInfos) ;
				}
				RS2JSONObject cvt = new RS2JSONObject(rs.getMetaData() , aColNameCvtMap) ;
				while(rs.next())
				{
					if(aPred != null)
					{
						switch(aPred.visit(rs))
						{
						case IterateOpCode.sContinue:
						{
							JSONObject jobj = cvt.apply(rs) ;
							if(aObjHandler != null)
								aObjHandler.accept(jobj) ;
							result.put(jobj) ;
							break ;
						}
						case IterateOpCode.sBreak:
							break ;
						case IterateOpCode.sInterrupted:
							return ;
						default:
							throw new IllegalStateException("不合法的迭代行为代码") ;
						}
					}
					else
					{
						JSONObject jobj = cvt.apply(rs) ;
						if(aObjHandler != null)
							aObjHandler.accept(jobj) ;
						result.put(jobj) ;
					}
				}
			}, 1000 , aParamVals);
			return result ;
		}
	}
	
	public static JSONArray queryAll_1(DataSource aDS , String aSql , int aSize , Map<String , String> aColumnNameMap , Object...aParamVals) throws SQLException
	{
		try(Connection conn = aDS.getConnection())
		{
			IDBTool dbTool = DBHelper.getDBTool(conn) ;
			JSONArray result = new JSONArray() ;
			final int size = aSize<=0?Integer.MAX_VALUE:aSize ;
			dbTool.query(conn, aSql, (rs)->{
				if(aColumnNameMap != null)
				{
					ResultSetMetaData rsmd = rs.getMetaData() ;
					int len = rsmd.getColumnCount() ;
					for(int i=1 ; i<=len ; i++)
						aColumnNameMap.put(rsmd.getTableName(i)+"."+rsmd.getColumnName(i) , rsmd.getColumnLabel(i)) ;
				}
				RS2JSONObject cvt = new RS2JSONObject(rs.getMetaData()) ;
				while(rs.next())
				{
					result.put(cvt.apply(rs)) ;
					if(result.size() >= size)
						break ;
				}
			}, size , aParamVals);
			return result ;
		}
	}
	
	public static JSONObject queryPage(DataSource aDS , String aSql , int aPageSize , int aPage , Object...aParamVals) throws SQLException
	{
		return queryPageAndHandle(aDS, aSql, aPageSize, aPage, null, aParamVals) ;
	}
	
	public static JSONObject queryPageAndHandle(DataSource aDS , String aSql , int aPageSize , int aPage , Consumer<JSONObject> aHandler , Object...aParamVals) throws SQLException
	{
		try(Connection conn = aDS.getConnection())
		{
			return queryPageAndHandle(conn, aSql, aPageSize, aPage, aHandler, aParamVals) ;
		}
	}
	
	public static JSONObject queryPageAndHandle(ESupplier<Connection , SQLException> aConnSupplier , String aSql , int aPageSize , int aPage , Consumer<JSONObject> aHandler , Object...aParamVals) throws SQLException
	{
		try(Connection conn = aConnSupplier.get())
		{
			return queryPageAndHandle(conn, aSql, aPageSize, aPage, aHandler, aParamVals) ;
		}
	}
	
	public static JSONObject queryPageAndHandle(Connection aConn , String aSql , int aPageSize , int aPage , Consumer<JSONObject> aHandler , Object...aParamVals) throws SQLException
	{
		IDBTool dbTool = DBHelper.getDBTool(aConn) ;
		Wrapper<JSONObject> resultWrapper = new Wrapper<JSONObject>() ;
		Wrapper<RS2JSONObject> cvtWrapper = new Wrapper<>() ;
		Wrapper<JSONObject> metadataJobjWrapper = new Wrapper<>() ;
		JSONArray data = new JSONArray() ;
		dbTool.queryPage(aConn, aSql, aPageSize, aPage, (rs)->{
			if(cvtWrapper.isNull())
			{
				cvtWrapper.set(new RS2JSONObject(rs.getMetaData())) ;
				metadataJobjWrapper.set(toJSONObject(rs.getMetaData())) ;
			}
			JSONObject jobj = cvtWrapper.get().apply(rs) ;
			if(aHandler != null)
				aHandler.accept(jobj) ;
			data.put(jobj) ;
		}, resultWrapper , aParamVals);
		
		return resultWrapper.get().put("rows", data).put("metadata" , metadataJobjWrapper.get()) ;
	}
	
	public static Map<String , String> getColumnAliasNames(DataSource aDS , String aDBTableName) throws SQLException
	{
		try(Connection conn = aDS.getConnection())
		{
			IDBTool dbTool = DBHelper.getDBTool(conn) ;
			String schema = null ;
			String tableName = aDBTableName ;
			int k = aDBTableName.indexOf('.') ;
			if(k != -1)
			{
				schema = aDBTableName.substring(0, k) ;
				tableName = aDBTableName.substring(k+1) ;
			}
			TableSchema tblSchema = dbTool.getTableSchema(conn, schema , tableName) ;
			Assert.notNull(tblSchema , "找不到数据库表的定义："+aDBTableName) ;
			List<ColumnSchema> cols = tblSchema.getColumnSchemas() ;
			Map<String , String> aliasNamesMap = new HashMap<String, String>() ; 
			if(XC.isNotEmpty(cols))
			{
				for(ColumnSchema col : cols)
				{
					String aliasName = col.getComment() ;
					if(XString.isEmpty(aliasName))
						continue ;
					int i = XString.indexOf(aliasName , ',' , ' ') ;
					aliasNamesMap.put(col.getColumnName(), i != -1?aliasName.substring(0, i):aliasName) ;
				}
			}
			return aliasNamesMap ;
		}
	}
	
	public static Date getLatestDate(DataSource aDS , String aTableName , String aField , String aCondition) throws SQLException
	{
		try(Connection conn = aDS.getConnection())
		{
			Wrapper<Date> dateWrapper = new Wrapper<Date>() ;
			DBHelper.executeQuery(conn , String.format("SELECT %1$s FROM %2$s %3$s ORDER BY %1$s DESC LIMIT 1" , aField
					, aTableName , XString.isEmpty(aCondition)?"":(" WHERE "+aCondition)), (rs)->{
						dateWrapper.set(rs.getTimestamp(1)) ;
					});
			return dateWrapper.get() ;
		}
	}
	
	public static JSONObject toJSONObject(ResultSetMetaData aRsmd) throws JSONException, SQLException
	{
		JSONObject jobj = new JSONObject() ;
		final int colCount = aRsmd.getColumnCount() ;
		for(int i=1 ; i<=colCount  ; i++)
		{
			String colLabel = aRsmd.getColumnLabel(i) ;
			String colName = aRsmd.getColumnName(i) ;
			jobj.put(XString.isEmpty(colLabel)?colName:colLabel , new JSONObject()
					.put("columnName" , colName)
					.put("columnLabel" , colLabel)
					.put("tableName" , aRsmd.getTableName(i))
					.put("schemaName", aRsmd.getSchemaName(i))
					.put("dataType" , aRsmd.getColumnTypeName(i))
					.put("catalogName" , aRsmd.getCatalogName(i))) ;
		}
		return jobj ;
	}
	
	/**
	 * 在字符串两端加上“%”，将空格段替换为“%”
	 * @param aPattern
	 * @return
	 */
	public static String asLike(String aPattern)
	{
		if(aPattern == null)
			return null ;
		aPattern = aPattern.trim() ;
		if(aPattern.isEmpty())
			return null ;
		return "%"+aPattern.replaceAll(" +", "%")+"%" ;
	}
}
