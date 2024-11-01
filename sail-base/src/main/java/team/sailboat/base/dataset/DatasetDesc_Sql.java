package team.sailboat.base.dataset;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.First;
import team.sailboat.commons.fan.text.XString;

/**
 * 数据源描述信息
 *
 * @author yyl
 * @since 2021年12月14日
 */
@Schema(name="DatasetDesc_Sql" , description="基于SQL的数据集描述")
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(value = Include.NON_NULL)
public class DatasetDesc_Sql extends DatasetDescriptor
{
	@Schema(description = "SQL条件")
	String conditionSql ;
	
	@Schema(description = "查询SQL，如果是单表简单查询，则不用设置")
	String querySql ;
	
	@JsonIgnore
	String buildSql ;
	
	@Schema(description = "数据源表，查询SQL为单表简单定义的时候所基于的数据表")
	String sourceTable ;
	
	/**
	 * 只是单表简单情形下有用
	 * 键是数据库的源字段，值是参数条件
	 * 输入参数的原始字段名和参数名的映射关系，只有单表简单定义的时候才可能有内容
	 */
	@Schema(description = "输入的参数条件")
	Map<String, SqlParamCondition> inParamCndMap ;
	
	/**
	 * 键是数据库的源字段，值是输出参数名
	 */
	@JsonIgnore
	Map<String, String> outFieldParamMap ;
	
	@Schema(description = "排序信息")
	List<ColumnOrdering> columnOrderings ;
	
	@Schema(hidden = true , description = "SQL中是否包含参数。键是完整参数表达式，值是值表达式")
	@JsonIgnore
	@Setter(value = AccessLevel.NONE)
	Map<String , SqlInParam> sqlInParamMap ;
	
	public DatasetDesc_Sql()
	{
		super(DatasetSource.Sql) ;
	}
	
	@JsonIgnore
	public boolean isSqlHaveParams()
	{
		return getSqlInParamMap().size() > 0 ;
	}
	
	public Map<String, SqlInParam> getSqlInParamMap()
	{
		if(sqlInParamMap == null)
		{
			Collection<String> sqlParamNames = XString.extractParamNames(getRealQuerySql()) ;
			if(XC.isNotEmpty(sqlParamNames))
			{
				sqlInParamMap = XC.linkedHashMap() ;
				for(String sqlParamName : sqlParamNames)
				{
					SqlInParam p = new SqlInParam(sqlParamName) ;
					sqlInParamMap.put(p.getWholeExpr(), p) ;
				}
			}
			else
				sqlInParamMap = Collections.emptyMap() ;
		}
		return sqlInParamMap ;
	}
	
	@Schema(description = "查询SQL，如果是单表简单查询，则不用设置")
	public String getRealQuerySql()
	{
		if(XString.isEmpty(querySql))
		{
			if(buildSql == null)
				buildQuerySql(); 
			return buildSql ;
		}
		else
			return querySql ;
	}
	
	void buildQuerySql()
	{
		if(XC.isNotEmpty(outParams) && XString.isNotEmpty(sourceTable))
		{
			StringBuilder sqlBld = new StringBuilder("SELECT") ;
			First first = new First() ;
			for(OutParam param : outParams)
			{
				if(!first.checkDo())
					sqlBld.append(',') ;
				String expression = param.getExpression() ;
				Assert.notEmpty(expression , "单表查询的时候，返回参数的表达式不能为空！") ;
				sqlBld.append(' ').append(expression).append(" AS ").append(param.getName()) ;
			}
			sqlBld.append(" FROM ").append(sourceTable) ;
			boolean haveInParams = XC.isNotEmpty(inParams) ;
			boolean haveCndSql = XString.isNotBlank(conditionSql) ;
			if(haveInParams || haveCndSql)
			{
				sqlBld.append(" WHERE ") ;
				first.reset();
				if(haveCndSql)
				{
					sqlBld.append("(").append(conditionSql).append(")") ;
					first.checkDo() ;
				}
				if(haveInParams)
				{
					for(SqlParamCondition sp : inParamCndMap.values())
					{
						if(sp.getOper() == null)
							continue ;
						if(!first.checkDo())
							sqlBld.append(" AND ") ;
						sqlBld.append(sp.getSource())
							.append(' ').append(sp.getOper().getOperator()).append(' ')
							.append("${").append(sp.getValue()).append('}') ;
					}
				}
			}
			if(XC.isNotEmpty(columnOrderings))
			{
				sqlBld.append(" ORDER BY") ;
				boolean first_0 = true ;
				for(ColumnOrdering colOrder : columnOrderings)
				{
					if(first_0)
						first_0 = false ;
					else
						sqlBld.append(" , ") ;
					sqlBld.append(' ').append(colOrder.getName()).append(' ').append(colOrder.getOrdering().name) ;
				}
			}
			buildSql = sqlBld.toString() ;
		}
	}
	
	public void setSourceTable(String aSourceTable)
	{
		sourceTable = aSourceTable;
		buildSql = null ;
	}
	
	/**
	 * 是不是单表简单查询
	 * @return
	 */
	@Schema(description = "是否是简单表格定义的方式" , accessMode = AccessMode.READ_ONLY)
	@JsonProperty(access = Access.READ_ONLY)
	public boolean isSimpleTableSql()
	{
		return XString.isEmpty(querySql) && XString.isNotEmpty(sourceTable) ;
	}
	
	@Override
	public void setInParams(List<InParam> aInParams)
	{
		super.setInParams(aInParams);
		buildSql = null ;
	}
	
	@Override
	public void setOutParams(List<OutParam> aOutParams)
	{
		super.setOutParams(aOutParams);
		outFieldParamMap = null ;
		buildSql = null ;
	}
	
	
	/**
	 * 输出参数的原始字段名和参数名的映射关系，只有单表简单定义的时候才可能有内容
	 * @return
	 */
	@JsonIgnore
	@Schema(hidden = true)
	public Map<String, String> getOutFieldParamMap()
	{
		if(outFieldParamMap == null && outParams != null)
		{
			Map<String, String> map = new HashMap<String, String>() ;
			for(OutParam outParam : outParams)
			{
				if(outParam.isTableField())
					map.put(outParam.getExpression(), outParam.getName())  ;
			}
			outFieldParamMap = map ;
		}
		return outFieldParamMap;
	}
	
//	@Override
//	public JSONObject setTo(JSONObject aJSONObj)
//	{
//		return super.setTo(aJSONObj)
//				.put("querySql", mQuerySql)
//				.put("sourceTable" , mSourceTable)
//				.put("outFieldParamMap", mOutFieldParamMap)
//				.put("inParamCndMap", mInParamCndMap)
//				.put("columnOrderings", new JSONArray(mColumnOrderings))
//				.put("isSimpleTableSql" , isSimpleTableSql())
//				;
//	}
	
	@Override
	public DatasetDescriptor clone()
	{
		return initClone(new DatasetDesc_Sql()) ;
	}
	
	@Override
	protected DatasetDescriptor initClone(DatasetDescriptor aClone)
	{
		DatasetDesc_Sql clone = (DatasetDesc_Sql) super.initClone(aClone) ;
		clone.querySql = querySql ;
		clone.sourceTable = sourceTable ;
		clone.outFieldParamMap = outFieldParamMap != null?new HashMap<>(outFieldParamMap):null ;
		clone.inParamCndMap = inParamCndMap != null?new HashMap<>(inParamCndMap):null ;
		clone.columnOrderings = XC.deepCloneArrayList(columnOrderings) ;
		clone.conditionSql = conditionSql ;
		return clone ;
	}
	
//	public static DatasetDesc_Sql build(JSONObject aJo)
//	{
//		DatasetDesc_Sql dataDesc = new DatasetDesc_Sql() ;
//		DatasetDescriptor.build(dataDesc, aJo);
//		dataDesc.mQuerySql = aJo.optString("querySql") ;
//		dataDesc.mSourceTable = aJo.optString("sourceTable") ;
//		JSONObject jo = aJo.optJSONObject("outFieldParamMap") ;
//		if(jo != null)
//			dataDesc.mOutFieldParamMap = jo.toStringMap() ;
//		jo = aJo.optJSONObject("inParamCndMap") ;
//		if(jo != null)
//		{
//			Map<String, SqlParamCondition> map = CS.hashMap() ; 
//			jo.forEach((key , value)->{
//				map.put(key, SqlParamCondition.build((JSONObject) value)) ;
//			});
//			dataDesc.mInParamCndMap = map ;
//		}
//		JSONArray ja = aJo.optJSONArray("columnOrderings") ;
//		if(ja != null)
//		{
//			List<ColumnOrdering> orderList = CS.arrayList() ;
//			ja.forEach((obj)->orderList.add(new ColumnOrdering(((JSONObject)obj).optString("name") 
//					, SQLOrdering.valueOf(((JSONObject)obj).optString("ordering") )))) ;
//			dataDesc.mColumnOrderings = orderList ;
//		}
//		return dataDesc ;
//	}
}
