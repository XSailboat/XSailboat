package team.sailboat.commons.ms.db;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import javax.sql.DataSource;

import team.sailboat.commons.fan.collection.AutoCleanHashMap;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dtool.ColumnInfo;
import team.sailboat.commons.fan.dtool.IDBTool;
import team.sailboat.commons.fan.dtool.SqlParams;
import team.sailboat.commons.fan.jfilter.JFilterParser;
import team.sailboat.commons.fan.jfilter.SqlFilterBuilder;
import team.sailboat.commons.fan.jquery.JQuery;
import team.sailboat.commons.fan.jquery.JQueryJa;
import team.sailboat.commons.fan.jquery.JQueryJo;
import team.sailboat.commons.fan.jquery.RDB_JQuery;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONException;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.struct.Wrapper;
import team.sailboat.commons.fan.struct.XInt;
import team.sailboat.commons.fan.text.XString;

public abstract class DBHelperBase
{
	protected final JFilterParser<SqlParams> mFilterParser = new JFilterParser<>(SqlFilterBuilder::new);
	
	AutoCleanHashMap<String, Object> mCache = null ;
	
	protected RDB_JQuery mDBQuery ;

	protected JSONObject queryPage(DataSource aDataSource, String aTableName, JSONObject aJFilter, int aPageSize,
			int aPage, LinkedHashMap<String, Boolean> aSorts , Object...aArgs) throws SQLException {
		return queryPage(aDataSource, aTableName, aJFilter, aPageSize, aPage, null, aSorts , aArgs);
	}

	protected JSONObject queryPage(DataSource aDataSource, String aTableName, JSONObject aJFilter, int aPageSize,
			int aPage, Consumer<JSONObject> aHandler , LinkedHashMap<String, Boolean> aSorts)
			throws SQLException {
		SqlParams cnd = null;
		if (aJFilter != null && !aJFilter.isEmpty())
			cnd = mFilterParser.parseFilter(aJFilter);

		StringBuilder sqlBld = new StringBuilder("SELECT * FROM ").append(aTableName);
		if (cnd != null && XString.isNotEmpty(cnd.getSql()))
			sqlBld.append(" WHERE " + cnd.getSql());

		if (XC.isNotEmpty(aSorts)) {
			sqlBld.append(" ORDER BY");
			for (Entry<String, Boolean> sort : aSorts.entrySet())
				sqlBld.append(' ').append(sort.getKey())
						.append(Boolean.FALSE.equals(sort.getValue()) ? " DESC" : " ASC");
		}

		return DBKit.queryPageAndHandle(aDataSource, sqlBld.toString(), aPageSize, aPage, aHandler 
				, cnd != null ?cnd.getParamValues():JCommon.sEmptyObjectArray);
	}

	protected JSONArray getFieldValues(DataSource aDataSource, String aTableName, String aFieldName)
			throws SQLException {
		JSONArray jarray = DBKit.queryAll(aDataSource,
				String.format("SELECT %1$s FROM %2$s GROUP BY %1$s ORDER BY %1$s ASC ", aFieldName, aTableName));
		final int len = jarray.size() ;
		JSONArray result = new JSONArray();
		if (len > 0) {
			for (int i = 0; i < len; i++) {
				result.put(jarray.optJSONObject(i).optString(aFieldName));
			}
		}
		return result;
	}
	
	protected JSONArray queryAll(DataSource aDataSource ,  String aTableName , JSONObject aJFilter 
			, @SuppressWarnings("unchecked") Entry<String, Boolean>... aSorts) throws SQLException
	{
		SqlParams cnd = null;
		if (aJFilter != null && !aJFilter.isEmpty())
			cnd = mFilterParser.parseFilter(aJFilter);

		StringBuilder sqlBld = new StringBuilder("SELECT * FROM ").append(aTableName);
		if (cnd != null && XString.isNotEmpty(cnd.getSql()))
			sqlBld.append(" WHERE " + cnd.getSql());

		if (XC.isNotEmpty(aSorts)) {
			sqlBld.append(" ORDER BY");
			for (Entry<String, Boolean> sort : aSorts)
				sqlBld.append(' ').append(sort.getKey())
						.append(Boolean.FALSE.equals(sort.getValue()) ? " DESC" : " ASC");
		}

		return DBKit.queryAll(aDataSource , sqlBld.toString() 
				, cnd != null?cnd.getParamValues():JCommon.sEmptyObjectArray) ;
	}
	
	protected JSONObject query_JSONArray(DataSource aDS , String aSql , Object...aParams) throws JSONException, SQLException
	{
		List<ColumnInfo> colInfoList = XC.arrayList() ;
		JSONArray jarray = DBKit.queryAll_2(aDS , aSql , colInfoList , aParams) ;
		JSONObject result = new JSONObject().put("data", jarray)
				.put("meta" , parseColumnAliasNames(null , colInfoList))
				.put("columns" , ColumnInfo.toColumns(colInfoList)) ;
		
		return result ;
	}
	
	protected JSONObject parseColumnAliasNames(String aDBTableName , List<ColumnInfo> aColInfoList)
	{
		JSONObject metaJObj = new JSONObject() ;
		//要提取那几个属性，采取排除法
		for(ColumnInfo colInfo : aColInfoList)
		{
			String tableName = JCommon.defaultIfEmpty(colInfo.getTableName() , aDBTableName) ;
			if(XString.isEmpty(tableName))
				continue ;
			metaJObj.put(colInfo.getColumnLabel() , new JSONObject()
					.put("aliasName" , getAliasNameOfDBTableColumn(tableName , colInfo.getColumnName()))
					.put("index" , colInfo.getIndex())
					.put("commonDataType" , IDBTool.convertTypeToCSN(colInfo.getSqlDataType()))) ;
		}
		return metaJObj ;
	}
	
	public abstract DataSource getDataSource() ;
	
	public RDB_JQuery getJQueryBuilder()
	{
		if(mDBQuery == null)
			mDBQuery = new RDB_JQuery(getDataSource()) ;
		return mDBQuery ;
	}
	
	Map<String, Object> getLongCache()
	{
		if(mCache == null)
		{
			 mCache = AutoCleanHashMap.withExpired_Created(10) ;
		}
		return mCache ;
	}
	
	@SuppressWarnings("unchecked")
	public String getAliasNameOfDBTableColumn(String aDBTableName , String aColumnName)
	{
		String key = "ColAlias_SourceDB_"+aDBTableName ;
		getLongCache() ;
		Object obj = mCache.get(key) ;
		try
		{
			if(obj == null)
			{
				obj = DBKit.getColumnAliasNames(getDataSource() , aDBTableName) ;
				mCache.put(key , obj);
			}
		}
		catch (SQLException e)
		{
			throw new IllegalStateException(e) ;
		}
		return obj == null?null:((Map<String , String>)obj).get(aColumnName) ;
	}
	
	/**
	 * 
	 * @param aTable			表名，也可以是SQL查询语句
	 * @param aField			字段名
	 * @param aPattern			SQL匹配条件，例如“%办公室%”
	 * @param aHeadN			前面多少条
	 * @param aAddiFields		在结果中要呈现的额外字段，可以没有
	 * @return
	 * @throws SQLException
	 */
	public JSONArray getFieldValues(String aTable , String aField , String aPattern  , int aHeadN , String...aAddiFields) throws SQLException
	{
		String addiFieldsStr = XC.isNotEmpty(aAddiFields)?XString.toString(" , ", aAddiFields):null ;
		JQuery jquery = getJQueryBuilder().oneJo("SELECT ").append(aField)
				.append(addiFieldsStr != null , " , " + addiFieldsStr)
				.append(" FROM ") ;
		boolean isVirTbl = aTable == null || aTable.indexOf(' ') != -1 ;
		if(isVirTbl)
			jquery.append("(").append(aTable).append(")") ;
		else
			jquery.append(aTable) ;
		jquery.append(" tfv WHERE ")
			.append(aField).append(" IS NOT NULL") ;
		if(XString.isNotEmpty(aPattern))
			jquery.append(" AND ").append(aField).append(true , " LIKE ?" , aPattern) ;
		JSONArray resultJa = jquery.append(" GROUP BY ").append(aField).append(addiFieldsStr != null , " , "+addiFieldsStr)
				.append(" LIMIT 0,").append(Integer.toString(Math.max(5, Math.min(aHeadN , 500))))
				.query() ;
		if(resultJa.isNotEmpty() && XC.isEmpty(aAddiFields))
		{
			JSONArray ja = new JSONArray() ;
			resultJa.forEach((obj)->ja.putIfNotNull(((JSONObject)obj).optString(aField))) ;
			resultJa = ja ;
		}
		return resultJa ;
		
	}
	
	protected  JQueryJo newJQuery_page_jobj(String aBaseSql , boolean aCareTotalAmount , Object... aArgs)
	{
		return (JQueryJo) standardizedPageSet(getJQueryBuilder().oneJo(aBaseSql , (Object[])aArgs) , aCareTotalAmount) ;
	}
	
	protected  JQueryJa newJQuery_page_jarray(String aBaseSql , boolean aCareTotalAmount , Object... aArgs)
	{
		return (JQueryJa) standardizedPageSet(getJQueryBuilder().oneJa(aBaseSql , (Object[])aArgs) , aCareTotalAmount) ;
	}
	
	protected  JQueryJa newJQuery_jarray(String aBaseSql , Object... aArgs)
	{
		return (JQueryJa) standardizedSet(getJQueryBuilder().oneJa(aBaseSql , (Object[])aArgs)) ;
	}
	
	protected  JQueryJo newJQuery_jobj(String aBaseSql , Object... aArgs)
	{
		return (JQueryJo) standardizedSet(getJQueryBuilder().oneJo(aBaseSql , (Object[])aArgs)) ;
	}
	
	protected JQuery standardizedPageSet(JQuery aJq , boolean aCareTotalAmount)
	{
		List<ColumnInfo> colInfoList = XC.arrayList() ;
		Wrapper<JSONObject> resultWrapper = new Wrapper<>() ;
		return aJq.careResultSetMetadata((rsmd)->aJq.getColumnInfos(rsmd, colInfoList))
				.carePageQueryMeta((jobj)->{
					resultWrapper.set(jobj) ;
				} , aCareTotalAmount)
				.resultFactory((ja)->{
					if(resultWrapper.isNull())
						resultWrapper.set(new JSONObject()) ;
					JSONObject metaJo = parseColumnAliasNames(null , colInfoList) ;
					return resultWrapper.get().put("data", ja)
							.put("meta" , metaJo)
							.put("columns" , ColumnInfo.toColumns(colInfoList));
				});
	}
	
	protected JQuery standardizedSet(JQuery aJq)
	{
		return standardizedPageSet(aJq, false) ;
	}
	
	protected JQuery standardizedSet(JQuery aJq , boolean aColumnNameHumpFormat)
	{
		Wrapper<JSONObject> resultWrapper = new Wrapper<>() ;
		List<ColumnInfo> colInfoList = XC.arrayList() ;
		return aJq.careResultSetMetadata(rsmd->aJq.getColumnInfos(rsmd, colInfoList))
				.resultFactory((ja , ctx)->{
					if(resultWrapper.isNull())
						resultWrapper.set(new JSONObject()) ;
					JSONObject metaJo = parseColumnAliasNames(null , colInfoList) ;
					XInt index = new XInt(0) ;
					metaJo.forEach((key , val)->{
						((JSONObject)val).put("index" , index.getAndIncrement()) ;
					});
					if(aColumnNameHumpFormat)
						JQueryJa.makeColumnNameHumpFormat(metaJo) ;
					return resultWrapper.get().put("data", ja)
							.put("meta" , metaJo)
							.put("columns" , ColumnInfo.toColumns(colInfoList))
							.put("hasMore" , ctx != null?ctx.getHasMore():null , false);
				});
	}
	
	
}
