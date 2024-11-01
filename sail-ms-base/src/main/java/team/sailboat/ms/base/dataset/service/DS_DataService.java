package team.sailboat.ms.base.dataset.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import team.sailboat.base.data.DataEngine;
import team.sailboat.base.data.IDataEngine;
import team.sailboat.base.data.IRDBDataProvider;
import team.sailboat.base.dataset.InParam;
import team.sailboat.base.dataset.OutParam;
import team.sailboat.base.def.WorkEnv;
import team.sailboat.base.ds.DataSource;
import team.sailboat.base.logic.INode;
import team.sailboat.base.sql.ISqlBloodEngine;
import team.sailboat.base.sql.model.BTable;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dtool.DBType;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.jfilter.AviatorExpBuilder;
import team.sailboat.commons.fan.jfilter.AviatorExpression;
import team.sailboat.commons.fan.jfilter.JFilterParser;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.ms.base.dataset.tool.SqlParams;
import team.sailboat.ms.base.service.Common_DataSourceService;

@Service
public class DS_DataService
{
	
	@Autowired
	Common_DataSourceService mDsService ;
	
	IDataEngine mDataEngine ;
	
	final JFilterParser<AviatorExpression> mFilterParser = new JFilterParser<>(AviatorExpBuilder::new) ;
	
	public DS_DataService()
	{
	}
	
	@PostConstruct
	void _init() throws SQLException
	{
		mDataEngine = new DataEngine((dsId , workEnv)->{
			try
			{
				return mDsService.getDataSource_passwd(dsId, workEnv) ;
			}
			catch(Exception e)
			{
				WrapException.wrapThrow(e) ;
				// dead code
				return null ;
			}
		}) ;
	}
	
	public IDataEngine getDataEngine()
	{
		return mDataEngine;
	}
	
	/**
	 * 获取数据源上表的预览数据
	 * @param aDataSourceId
	 * @param aEnv
	 * @param aTableName
	 * @return
	 * @throws Exception
	 */
	public JSONObject getPreviewData(String aDataSourceId , WorkEnv aEnv , String aTableName) throws Exception
	{
		DataSource ds = mDsService.getDataSource_passwd(aDataSourceId , aEnv) ;
		Assert.notNull(ds, "不存在id为%s的数据源！", aDataSourceId) ;
		return mDataEngine.getPreviewData(ds , aEnv , aTableName) ;
	}
	
	public AviatorExpression toExpression(INode<?> aFilterNode)
	{
		return mFilterParser.parseFilter(INode.buildJFilter(aFilterNode)) ;
	}
	
	/**
	 * 
	 * @param aSql
	 * @param aInParamMap
	 * @return
	 * @throws Exception 
	 */
	public List<OutParam> parseOutParamsForSql(String aDataSourceId , WorkEnv aEnv , String aSql , List<InParam> aInParams) throws Exception
	{
		DataSource ds = mDsService.getDataSource_passwd(aDataSourceId , aEnv) ;
		Assert.isTrue(ds.getType().isRDB() , "指定的数据源[%s]不是关系数据库！" , aDataSourceId) ;
		IRDBDataProvider dp = (IRDBDataProvider) mDataEngine.getDataProvider(ds.getType()) ;
		JSONObject jo = dp.getData(ds, aEnv, aSql, aInParams, true, 1) ;
		JSONObject colsJo = jo.optJSONObject("columns") ;
		LinkedHashMap<String , OutParam> outParamMap = XC.linkedHashMap() ;
		colsJo.forEach((key , value)->outParamMap.put(key, new OutParam(key , ((JSONObject)value).optString("dataType")))) ;
		JSONObject metaJo = jo.optJSONObject("meta") ;
		metaJo.forEach((key , value)->{
			JSONObject colJo = (JSONObject)value ;
			OutParam param = outParamMap.get(key) ;
			param.setDescription(colJo.optString("comment")) ;
			param.setExpression(colJo.optString("columnName")) ;
		});
		return new ArrayList<>(outParamMap.values()) ;
	}
	
	public SqlParams parseSqlParams(DBType aDBType , String aSql)
	{
		SqlParams sqlParams = new SqlParams(XString.extractParamNames(aSql)) ;
		Set<String> exprSqlSegs = sqlParams.getExprSqlSegs() ;
		String sql = aSql ;
		if(XC.isNotEmpty(exprSqlSegs))
		{
			for(String exprSqlSeg : exprSqlSegs)
			{
				sql = sql.replace("${"+exprSqlSeg+"}" , "") ;
			}
		}
		
		ISqlBloodEngine engine = ISqlBloodEngine.of(aDBType, "default") ;
		List<BTable> tableList = engine.parse(sql) ;
		BTable tbl = tableList.get(0) ;
		Collection<String> outParams = tbl.getColumnNames() ;
		sqlParams.setOutParamNames(outParams) ;
		return sqlParams ;
	}
}
