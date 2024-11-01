package team.sailboat.base.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.aviator.AviatorEvaluator;

import team.sailboat.base.dataset.DatasetDesc_Sql;
import team.sailboat.base.dataset.IDataset;
import team.sailboat.base.dataset.InParam;
import team.sailboat.base.dataset.OutParam;
import team.sailboat.base.dataset.SqlInParam;
import team.sailboat.base.def.DataSourceType;
import team.sailboat.base.def.WorkEnv;
import team.sailboat.base.ds.DSHelper_JDBC;
import team.sailboat.base.ds.DataSource;
import team.sailboat.base.sql.ISqlBloodEngine;
import team.sailboat.base.sql.model.BName;
import team.sailboat.base.sql.model.BTable;
import team.sailboat.commons.fan.collection.HashMultiMap;
import team.sailboat.commons.fan.collection.IMultiMap;
import team.sailboat.commons.fan.collection.SizeIter;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dtool.ColumnSchema;
import team.sailboat.commons.fan.dtool.IDBTool;
import team.sailboat.commons.fan.dtool.dm.DMTool;
import team.sailboat.commons.fan.dtool.hive.HiveTool;
import team.sailboat.commons.fan.dtool.mysql.MySQLTool;
import team.sailboat.commons.fan.dtool.pg.PgTool;
import team.sailboat.commons.fan.dtool.taos.TDengineTool;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.jquery.JQuery;
import team.sailboat.commons.fan.jquery.RDB_JQuery;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.struct.Wrapper;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.fan.time.XTime;

public class RDBDataProvider implements IRDBDataProvider
{
	
	static final int sPreviewLineLimit = 500 ;
	
	static final int sResultLineLimit = 10000 ;
	
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	DataSourceType mDsType ;
	
	IDBTool mDBTool ;
	
	Function<String, String> mWrapStringFunc ;
	
	public RDBDataProvider(DataSourceType aDsType)
	{
		mDsType = aDsType ;
		_init();
	}
	
	void _init()
	{
		if(mDBTool == null)
		{
			switch(mDsType)
			{
			case MySql:
				mDBTool = new MySQLTool(true) ;
				break ;
			case PostgreSQL:
				mDBTool = new PgTool() ;
			case MySql5:
				mDBTool = new MySQLTool(false) ;
				break ;
			case Hive:
				mDBTool = new HiveTool() ;
				break ;
			case DM:
				mDBTool = new DMTool() ;
				break ;
			case TDengine:
				mDBTool = new TDengineTool() ;
				break ;
			default:
				throw new IllegalStateException(String.format("未支持的数据源类型：%s！" , mDsType.name())) ;
			}
			_initWrapStringFunc();
		}
	}
	
	void _initWrapStringFunc()
	{
		switch(mDBTool.getDBType())
		{
		case MySQL:
		case MySQL5:
		case Hive:
		case TDengine:
			mWrapStringFunc = (str)->str != null?'\''+mDBTool.escape(str, '\'')+'\'':null ;
			break ;
		case PostgreSQL:
			mWrapStringFunc = (str)->{
				if(str != null)
				{
					String newStr = mDBTool.escape(str, '\'') ;
					return newStr != str?("E'"+newStr+'\''):('\''+str+'\'') ;
				}
				return null ;
			} ;
			break ;
		case DM:
			mWrapStringFunc = (str)->str != null?'"'+mDBTool.escape(str, '\'')+'"':null ;
			break ;
		default:
			throw new IllegalStateException("未支持的数据库类型："+mDBTool.getDBType()) ;
		}
	}
	
	public DataSourceType getDataSourceType()
	{
		return mDsType ;
	}
	
	@Override
	public JSONObject getData(DataSource aDs, WorkEnv aEnv, String aSql, List<InParam> aInParams
			, boolean aContainsMeta , int aLimitAmount) throws Exception
	{
		Assert.notEmpty(aSql , "查询SQL不能为空！") ;
		javax.sql.DataSource ds = DSHelper_JDBC.getDataSource(aDs, aEnv) ;
		String sql = XC.isEmpty(aInParams)?aSql:injectParamValues(aSql, aInParams) ;
		return (JSONObject)newJQueryCareTableMeta(ds , (rsmd , i)->{
				try
				{
					return IDBTool.convertTypeToCSN(rsmd.getColumnType(i)) ;
				}
				catch(SQLException e)
				{
					WrapException.wrapThrow(e) ;
					return null ;			// dead code
				}
			} , sql).queryCustom(aLimitAmount) ;
	}
	
	protected String injectParamValues(String aSql , Map<String, Object> aCtxMap , Collection<SqlInParam> aSqlInParams)
	{
		Map<String, Object> paramMap = XC.hashMap() ; 
		for(SqlInParam sqlParam : aSqlInParams)
		{
			if(sqlParam.isSqlSeg())
			{
				Object val = AviatorEvaluator.compile(sqlParam.getValueExpr()).execute(aCtxMap) ;
				paramMap.put(sqlParam.getWholeExpr() , val) ;
			}
			else
			{
				Object val = null ;
				if(sqlParam.isParamHolder())
				{
					val = aCtxMap.get(sqlParam.getWholeExpr()) ;
					Assert.notNull(val , "没有为SQL中的参数[%s]指定值！" , sqlParam) ;
				}
				else
				{
					val = AviatorEvaluator.compile(sqlParam.getValueExpr()).execute(aCtxMap) ;
				}
				if(val instanceof String)
				{
					val = mWrapStringFunc.apply((String)val) ;
				}
				else if(val instanceof Date)
				{
					val = "'"+XTime.format$yyyyMMddHHmmssSSS((Date)val)+"'" ;
				}
				paramMap.put(sqlParam.getWholeExpr() , val) ;
			}
		}
		return XString.format(aSql, paramMap) ;
	}
	
	protected String injectParamValues(String aSql , List<InParam> aInParamList)
	{
		String sql = aSql ;
		Map<String ,Object> paramMap = XC.hashMap() ;	
		for(InParam inParam : aInParamList)
		{
			String paramName = inParam.getName() ;
			String value = inParam.getExample() ;
			if(value == null)
				continue ;
			value = XString.format(value, RunParams.getParamMap()) ;
			switch(inParam.getDataType())
			{
			case XClassUtil.sCSN_String:
				paramMap.put(paramName, mWrapStringFunc.apply(value)) ;
				break ;
			case XClassUtil.sCSN_Double:
				paramMap.put(paramName, XString.isEmpty(value)?null:Double.valueOf(value)) ;
				break ;
			case XClassUtil.sCSN_Long:
				paramMap.put(paramName, XString.isEmpty(value)?null:Long.valueOf(value)) ;
				break ;
			case XClassUtil.sCSN_Integer:
				paramMap.put(paramName, XString.isEmpty(value)?null:Integer.valueOf(value)) ;
				break ;
			case XClassUtil.sCSN_Float:
				paramMap.put(paramName, XString.isEmpty(value)?null:Float.valueOf(value)) ;
				break ;
			case XClassUtil.sCSN_Bool:
				paramMap.put(paramName, XString.isEmpty(value)?null:Boolean.valueOf(value)) ;
				break ;
			}
		}
		sql = XString.format(aSql, paramMap) ;
		// 检查sql里面是不是还有参数
		LinkedHashSet<String> paramNames = XString.extractParamNames(sql) ;
		if(!paramNames.isEmpty())
		{
			throw new IllegalStateException("SQL中的参数没有设置："+XString.toString(",", paramNames)) ;
		}
		return sql ;
	}
	
	@Override
	public void consume(DataSource aDs, IDataset aDataset, Map<String, Object> aParamMap, Consumer<Object[]> aConsumer)
			throws Exception
	{
		javax.sql.DataSource ds = DSHelper_JDBC.getDataSource(aDs, aDataset.getWorkEnv()) ;
		DatasetDesc_Sql dsDesc = (DatasetDesc_Sql)aDataset.getDatasetDescriptor() ;
		String sql = dsDesc.getRealQuerySql() ;
		if(dsDesc.isSqlHaveParams())
		{
			sql = injectParamValues(sql , aParamMap , dsDesc.getSqlInParamMap().values()) ;
		}
		mLogger.info("执行SQL查询：{}" , sql) ;
		List<OutParam> outParams = dsDesc.getOutParams() ;
		final int fieldAmount = outParams.size() ;
		try(Connection conn = ds.getConnection()
				; Statement stm = conn.createStatement())
		{
			ResultSet rs = stm.executeQuery(sql) ;
			Class<?>[] types = new Class[fieldAmount] ;
			for(int i=0 ; i<fieldAmount ; i++)
				types[i] = XClassUtil.getClassOfCSN(outParams.get(i).getDataType()) ;
			while(rs.next())
			{
				Object[] cells = new Object[fieldAmount] ;
				for(int i=0 ; i<fieldAmount ; i++)
				{
					OutParam outParam = outParams.get(i) ;
					cells[i] = XClassUtil.typeAdapt(rs.getObject(outParam.getName()) , types[i]) ;
				}
				aConsumer.accept(cells);
			}
		}
	}

	@Override
	public JSONObject getData(DataSource aDs , IDataset aDataset, Map<String, Object> aCtxMap) throws Exception
	{
		javax.sql.DataSource ds = DSHelper_JDBC.getDataSource(aDs, aDataset.getWorkEnv()) ;
		DatasetDesc_Sql dsDesc = (DatasetDesc_Sql)aDataset.getDatasetDescriptor() ;
//		Map<String , Object> ctxMap = aParamMap == null? null: CS.hashMap(aParamMap) ;
//		// 根据InParam中定义的类型，对aParamMap进行处理，将单引号进行转义，对数值、Boolean进行类型转换
//		List<InParam> inParamList = dsDesc.getInParams() ;
//		int inParamCount = XCollections.count(inParamList) ;
//		if(inParamCount > 0)
//		{
//			for(InParam inParam : inParamList)
//			{
//				Object value = ctxMap.get(inParam.getName()) ;
//				if(value != null)
//				{
//					// 看看是什么类型，把它转过去
//					ctxMap.put(inParam.getName() , XClassUtil.typeAdapt(value, inParam.getDataType())) ;
//				}
//				else
//				{
//					if(XString.isNotEmpty(inParam.getDefaultValue()))
//					{
//						ctxMap.put(inParam.getName() , XClassUtil.typeAdapt(inParam.getDefaultValue() , inParam.getDataType())) ;
//					}
//					else
//					{
//						Assert.isNotTrue(inParam.isRequired(), "必填参数[%s]没有指定，且没有缺省值！" , inParam.getName()) ;
//					}
//				}
//			}
//		}
//		// 处理前置表达式
//		ParamList paramList = dsDesc.getParamList() ;
//		if(paramList != null && paramList.isNotEmpty())
//		{
//			List<Param> params = paramList.getParams() ;
//			if(paramList.hasAviator() && ctxMap == null)
//			{
//				ctxMap = CS.hashMap() ;
//			}
//			for(Param param : params)
//			{
//				if(param instanceof Param_Aviator)
//				{
//					Param_Aviator param_1 = (Param_Aviator)param ;
//					Expression expr = AviatorEvaluator.compile(param_1.getExpr() , true) ;
//					Object val = expr.execute(ctxMap) ;
//					if(val != null)
//						ctxMap.put(param_1.getName() , val) ;
//				}
//				else if(param instanceof Param_InvokeApi)
//				{
//					Param_InvokeApi param_1 = (Param_InvokeApi)param ;
//					IRestClient client = null ;
//					switch(param_1.getClientType())
//					{
//					case Workspace:
//						client = (IRestClient)ctxMap.get("_ds_client") ;
//						break ;
//					case Gateway:
//						client = (IRestClient)ctxMap.get("_gw_client") ;
//						break ;
//					default:
//						throw new IllegalArgumentException("未支持的ClientType="+param_1.getClientType().name()) ;
//					}
//					Assert.notNull(client , "没有取得HttpClient!" ) ;
//				}
//			}
//		}

		String sql = dsDesc.getRealQuerySql() ;
		if(dsDesc.isSqlHaveParams())
		{
			sql = injectParamValues(sql , aCtxMap , dsDesc.getSqlInParamMap().values()) ;
		}
		mLogger.info("执行SQL查询：{}" , sql) ;
		JSONObject resultJo = (JSONObject)newJQuery(ds, dsDesc, sql).queryCustom(sResultLineLimit) ;
		return resultJo ;
	}
	
	protected JQuery newJQuery(javax.sql.DataSource aDs , DatasetDesc_Sql aDatasetDesc , String aBaseSql, Object... aArgs)
	{
		return newJQuery(aDs, (rsmd , i)->{
			try
			{
				OutParam param = aDatasetDesc.getOutParamByName(rsmd.getColumnLabel(i)) ;
				return param == null?null:param.getDataType() ;
			}
			catch(SQLException e)
			{
				WrapException.wrapThrow(e) ;
				return null ;			// dead code
			}
		}, aBaseSql, aArgs) ;
	}
	
	protected  JQuery newJQuery(javax.sql.DataSource aDs , String aBaseSql, Object... aArgs)
	{
		return newJQuery(aDs, (rsmd , i)->{
			try
			{
				return IDBTool.convertTypeToCSN(rsmd.getColumnType(i)) ;
			}
			catch(SQLException e)
			{
				WrapException.wrapThrow(e) ;
				return null ;			// dead code
			}
		} , aBaseSql, aArgs) ;
	}
	
	protected  JQuery newJQueryCareTableMeta(javax.sql.DataSource aDs , BiFunction<ResultSetMetaData, Integer , String> aDataTypeFunc 
			, String aBaseSql, Object... aArgs) throws SQLException
	{
		// 键是columnLabel，值是类型
		JSONObject columnsJo = new JSONObject() ;
		Wrapper<JSONObject> resultWrapper = new Wrapper<>() ;
		JSONObject metaJo = new JSONObject() ;
		IMultiMap<String, BName> colSourceTableMap_0 = null ;
		if(!mDBTool.isRSMDSupportColumnSourceTable())
		{
			ISqlBloodEngine engine = ISqlBloodEngine.of(mDBTool.getDBType(), mDBTool.getSchemaName(aDs.getConnection())) ;
			BTable tbl = engine.parse(null , aBaseSql).get(0) ;
			colSourceTableMap_0 = tbl.getColumnsSourceTables() ;
		}
		IMultiMap<String, BName> colSourceTableMap = colSourceTableMap_0 ;
		return new RDB_JQuery(aDs).oneJa(aBaseSql, (Object[])aArgs)
				.careResultSetMetadata((rsmd)->{
					int len = rsmd.getColumnCount() ;
					Set<BName> tableFullNameSet = XC.hashSet() ;
					IMultiMap<String, String> nameLabelMap = new HashMultiMap<>() ;
					for(int i=1 ; i<=len ; i++)
					{
						String name = rsmd.getColumnLabel(i) ;
						int j = name.indexOf('.') ; 
						name = j == -1?name:name.substring(j+1) ;
						String dataType = aDataTypeFunc.apply(rsmd, i) ;
						columnsJo.put(name , new JSONObject().put("dataType" , dataType)
								.put("index", i-1)) ;
						BName tableName = null ;
						if(colSourceTableMap != null)
						{
							SizeIter<BName> tableNameIt = colSourceTableMap.get(name) ;
							if(tableNameIt != null && tableNameIt.size() == 1)
							{
								tableName = XC.getFirst(tableNameIt) ;
							}
						}
						else
						{
							String schemaName = mDBTool.getDBSchemaName(rsmd, i) ;
//							Assert.notEmpty(schemaName , "查询不到列[%s]所属数据库！" , name) ;
							if(schemaName == null)
							{
								// 有可能查不到的，比如：函数调用
							}
							else
								tableName = new BName(schemaName, rsmd.getTableName(i)) ;
						}
						if(tableName != null)
						{
							tableFullNameSet.add(tableName) ;
							nameLabelMap.put(tableName.getName()+"."+rsmd.getColumnName(i) , name) ;
						}
						metaJo.put(name, new JSONObject().put("dbName" , tableName==null?null:tableName.getPrefix())
								.put("tableName", tableName==null?null:tableName.getLocalName())
								.put("columnLabel", rsmd.getColumnLabel(i))
								.put("columnName", rsmd.getColumnName(i))
								.put("columnTypeName", rsmd.getColumnTypeName(i))) ;
					}
					try(Connection conn = aDs.getConnection())
					{
						for(BName tableName : tableFullNameSet)
						{
							ColumnSchema[] colSchemas = mDBTool.getColumnSchemas(conn
									, tableName.getPrefix() 
									, tableName.getLocalName()) ;
							for(ColumnSchema colSchema : colSchemas)
							{
								String key = tableName.getName()+"."+colSchema.getColumnName() ;
								SizeIter<String> nameIt = nameLabelMap.get(key) ;
								if(nameIt != null)
								{
									for(String name : nameIt)
									{
										metaJo.optJSONObject(name).put("comment" , colSchema.getComment()) ;
									}
								}
							}
							
						}
					}
				})
				.resultFactory((ja)->{
					if(resultWrapper.isNull())
						resultWrapper.set(new JSONObject()) ;
					return resultWrapper.get().put("data", ja)
							.put("columns" , columnsJo)
							.put("meta", metaJo) ;
				});
	}
	
	protected  JQuery newJQuery(javax.sql.DataSource aDs , BiFunction<ResultSetMetaData, Integer , String> aDataTypeFunc 
			, String aBaseSql, Object... aArgs)
	{
		// 键是columnLabel，值是类型
		JSONObject columnsJo = new JSONObject() ;
		Wrapper<JSONObject> resultWrapper = new Wrapper<>() ;
		return new RDB_JQuery(aDs).oneJa(aBaseSql, (Object[])aArgs)
				.careResultSetMetadata((rsmd)->{
					int len = rsmd.getColumnCount() ;
					for(int i=1 ; i<=len ; i++)
					{
						String name = rsmd.getColumnLabel(i) ;
						int j = name.indexOf('.') ; 
						name = j == -1?name:name.substring(j+1) ;
						String dataType = aDataTypeFunc.apply(rsmd, i) ;
						columnsJo.put(name , new JSONObject().put("dataType", dataType)
								.put("index", i-1)) ;
					}
				})
				.resultFactory((ja)->{
					if(resultWrapper.isNull())
						resultWrapper.set(new JSONObject()) ;
					return resultWrapper.get().put("data", ja)
							.put("columns" , columnsJo) ;
				});
	}


	@Override
	public JSONObject getPreviewData(DataSource aDataSource, WorkEnv aEnv, String aTableName) throws Exception
	{
		Assert.isNotTrue(XString.containsAny(aTableName, ' ', '\n' , '\t') , "表名%s不合法！" , aTableName) ;
		javax.sql.DataSource ds = DSHelper_JDBC.getDataSource(aDataSource, aEnv) ;
		return (JSONObject)newJQuery(ds, "SELECT * FROM "+aTableName +" LIMIT "+sPreviewLineLimit).queryCustom() ;
	}

}
